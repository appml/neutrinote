package com.appmindlab.nano;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MirrorWorker extends Worker {
    // Data source
    private DataSource mDatasource;

    // Worker related
    private WorkerParameters mWorkerParameters;

    // SAF
    private DocumentFile dir, dest_dir, attachment_dir, font_dir, log_dir, exported_md_dir, exported_html_dir;

    // Preferences
    private SharedPreferences mSharedPreferences;
    private String mLocalRepoPath;
    private Uri mBackupUri, mMirrorUri;
    private int mMaxSyncLogFileSize = Const.MAX_SYNC_LOG_FILE_SIZE * Const.ONE_KB;
    private int mMaxSyncLogFileAge = Const.MAX_SYNC_LOG_FILE_AGE;
    private boolean mFileNameAsTitle;

    // Last mirror time
    private long mLastMirrored = 0;

    // Notification
    protected NotificationManager mNotifyManager;
    protected NotificationCompat.Builder mBuilder;
    protected NotificationCompat.BigTextStyle mBigTextStyle = new NotificationCompat.BigTextStyle();
    protected PendingIntent mIntent;

    public MirrorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mWorkerParameters = workerParams;
        mNotifyManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(Const.TAG, "nano - MirrorWorker loading preference...");

        // Get preferences
        loadPref();

        Log.d(Const.TAG, "nano - MirrorWorker sanity checking...");

        // Sanity check
        if (DisplayDBEntry.display_dbentry != null)
            return null;

        // Basics
        String status = "";
        int count;

        // Preference editor
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        // Misc
        Intent newIntent;

        Log.d(Const.TAG, "nano - MirrorWorker started [ last mirrored time: " + mLastMirrored + " ]");

        // Open the database
        mDatasource = new DataSource();
        mDatasource.open();

        // Setup notification
        setForegroundAsync(createForegroundInfo(Const.MIRROR_CHANNEL_DESC));
        mNotifyManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getApplicationContext(), Const.MIRROR_CHANNEL_ID);
        newIntent = new Intent(getApplicationContext(), MainActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        mIntent = PendingIntent.getActivity(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_IMMUTABLE);

        try {

            ///////////////////////
            // TO MIRROR (NOTES) //
            ///////////////////////

            Log.d(Const.TAG, "nano - MirrorWorker: To Mirror ");

            // Retrieve records modified after last mirror
            List<Long> results = mDatasource.getAllActiveRecordsIDsByLastModified(Const.SORT_BY_TITLE, Const.SORT_ASC, mLastMirrored, ">");
            count = results.size();

            dir = DocumentFile.fromTreeUri(getApplicationContext(), mBackupUri);
            dest_dir = Utils.getSAFSubDir(getApplicationContext(), dir, Const.MIRROR_PATH);
            mMirrorUri = dest_dir.getUri();

            for (int i = 0; i < count; i++) {
                exportSAFFile(dest_dir, results.get(i));
            }

            /////////////////////////
            // FROM MIRROR (NOTES) //
            /////////////////////////

            Log.d(Const.TAG, "nano - MirrorWorker: From Mirror ");

            String file_name;
            for (DocumentFile file : dest_dir.listFiles()) {
                // Sanity check
                if (file.isDirectory())  continue;

                file_name = file.getName();
                if (file_name == null)   continue;

                if (Arrays.asList(Const.RESERVED_FOLDER_NAMES).contains(file_name)) {
                    // Notes with reserved folder names need to be removed
                    file.delete();
                    continue;
                }

                if (file_name.endsWith(")")) {
                    // Notes with duplicate names need to be removed
                    file.delete();
                    continue;
                }

                importSAFFile(file, false);
            }

            // Do below if not an instant operation
            if (!mWorkerParameters.getTags().contains(Const.MIRROR_INSTANT_WORK_TAG)) {
                ////////////////////////
                // TO MIRROR (OTHERS) //
                ////////////////////////

                Log.d(Const.TAG, "nano - MirrorWorker: 'attachments' to mirror ");

                // Backup attachments
                attachment_dir = Utils.getSAFSubDir(getApplicationContext(), dest_dir, Const.ATTACHMENT_PATH);
                Utils.exportToSAFFolderByLastModified(getApplicationContext(), new File(mLocalRepoPath + "/" + Const.ATTACHMENT_PATH), attachment_dir, mLastMirrored, false);

                Log.d(Const.TAG, "nano - MirrorWorker: 'fonts' to mirror ");

                // Backup fonts
                font_dir = Utils.getSAFSubDir(getApplicationContext(), dest_dir, Const.CUSTOM_FONTS_PATH);
                Utils.exportToSAFFolderByLastModified(getApplicationContext(), new File(mLocalRepoPath + "/" + Const.CUSTOM_FONTS_PATH), font_dir, mLastMirrored,false);

                // Backup multitype file
                if (Utils.fileExists(getApplicationContext(), mLocalRepoPath, Const.MULTI_TYPE))
                    Utils.exportToSAFFile(getApplicationContext(), mLocalRepoPath + "/", Const.MULTI_TYPE, dest_dir);

                // Backup sync log
                if (Utils.fileExists(getApplicationContext(), mLocalRepoPath, Const.SYNC_LOG_FILE)) {
                    Utils.exportToSAFFile(getApplicationContext(), mLocalRepoPath + "/", Const.SYNC_LOG_FILE, dest_dir);

                    // Move log folder to backup
                    // Note: no need to delete source copy as that's managed by purging; destination copy can be manually removed by user
                    log_dir = Utils.getSAFSubDir(getApplicationContext(), dest_dir, Const.LOG_PATH);
                    Utils.moveToSAFFolder(getApplicationContext(), mMirrorUri, new File(mLocalRepoPath + "/" + Const.LOG_PATH), log_dir, false, false, false);
                }

                // Backup import errors by moving import error folder to backup
                // Note: need to remove source copy as its content will keep growing; destination copy can be manually removed by user
                Utils.moveToSAFFolder(getApplicationContext(), mMirrorUri, new File(mLocalRepoPath + "/" + Const.IMPORT_ERROR_PATH), dest_dir,true, false, false);

                //////////////////////////
                // FROM MIRROR (OTHERS) //
                //////////////////////////

                Log.d(Const.TAG, "nano - MirrorWorker: 'attachments' from mirror ");

                // Restore attachments
                attachment_dir = dest_dir.findFile(Const.ATTACHMENT_PATH);
                Utils.importFromSAFFolder(getApplicationContext(), attachment_dir, mLocalRepoPath + "/" + Const.ATTACHMENT_PATH, false);

                Log.d(Const.TAG, "nano - MirrorWorker: 'fonts' from mirror ");

                // Restore fonts
                font_dir = dest_dir.findFile(Const.CUSTOM_FONTS_PATH);
                Utils.importFromSAFFolder(getApplicationContext(), font_dir, mLocalRepoPath + "/" + Const.CUSTOM_FONTS_PATH, false);
            }

            Log.d(Const.TAG, "nano - MirrorWorker: purge from local repo notes removed from mirror");

            // Purge from local repo notes removed from mirror
            // Basically purge any notes with modification already mirrored and were present at last mirroring but are now missing from the mirror
            List<DBEntry> items = mDatasource.getAllActiveRecordsTitlesByLastModified(Const.SORT_BY_TITLE, Const.SORT_ASC, mLastMirrored, "<");
            DBEntry entry;

            for (int i = 0; i < items.size(); i++) {
                entry = items.get(i);
                if (dest_dir.findFile(Utils.getFileNameFromTitle(getApplicationContext(), entry.getTitle())) == null) {
                    mDatasource.deleteRecordById(entry.getId());  // Purge right away to prevent unexpected restore
                }
            }

            Log.d(Const.TAG, "nano - Mirror worker: Finishing Up");

            // Update status
            Date now = new Date();
            status += Utils.getSystemDateFormat(getApplicationContext(), Locale.getDefault()).format(now) + Utils.getSystemTimeFormat(getApplicationContext(), Locale.getDefault()).format(now);

            // Save the log status
            if (!mWorkerParameters.getTags().contains(Const.MIRROR_INSTANT_WORK_TAG)) {
                mLastMirrored = now.getTime();
                editor.putString(Const.AUTO_MIRROR_LOG, status);
                editor.putLong(Const.MIRROR_TIMESTAMP, mLastMirrored);
                editor.apply();
            }

            // Update notification
            mBigTextStyle.bigText(getApplicationContext().getResources().getString(R.string.message_auto_mirror_log) + status);
            mBuilder.setStyle(mBigTextStyle);
            mBuilder.setContentText(status).setProgress(0, 0, false);

            // Removes the progress bar
            mNotifyManager.notify(Const.MIRROR_NOTIFICATION_ID, mBuilder.build());
            mNotifyManager.cancel(Const.MIRROR_NOTIFICATION_ID);

            // Update widget
            Intent intent = new Intent(Const.ACTION_UPDATE_WIDGET);
            getApplicationContext().sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
            mNotifyManager.cancel(Const.MIRROR_NOTIFICATION_ID);
        } finally {
            Log.d(Const.TAG, "nano - Mirror worker finished");
        }

        // Clean up
        mDatasource.close();

        return Result.success();
    }

    @Override
    public void onStopped() {
        // Clean up when stopped unexpectedly
        if (mDatasource != null)
            mDatasource.close();
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(@NonNull String progress) {
        Context context = getApplicationContext();
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        Notification notification = new NotificationCompat.Builder(context, Const.MIRROR_CHANNEL_ID)
                .setContentTitle(Const.MIRROR_CHANNEL_NAME)
                .setTicker(Const.MIRROR_CHANNEL_NAME)
                .setSmallIcon(R.drawable.ic_archive_vector)
                .setOngoing(true)
                .build();

        return new ForegroundInfo(Const.MIRROR_NOTIFICATION_ID, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        // Create a Notification channel
        Utils.makeNotificationChannel(mNotifyManager, Const.MIRROR_CHANNEL_ID, Const.MIRROR_CHANNEL_NAME, Const.MIRROR_CHANNEL_DESC, Const.MIRROR_CHANNEL_LEVEL);
    }

    // Export a file to SAF
    protected void exportSAFFile(DocumentFile dir, Long id) {
        try {
            String title, content;
            Date lastModified;
            DBEntry entry;

            // Get content
            List<DBEntry> results = mDatasource.getRecordById(id);

            if (results.size() > 0) {
                entry = results.get(0);
                title = entry.getTitle();

                // Sanity check
                if (Arrays.asList(Const.RESERVED_FOLDER_NAMES).contains(title)) {
                    // Notes with reserved folder names need to be removed
                    mDatasource.markRecordDeletedById(entry.getId(), 1);
                    return;
                }

                if ((Utils.fileNameAsTitle(getApplicationContext())) && (title.endsWith(")"))) {
                    // Notes with duplicate names need to be removed
                    mDatasource.markRecordDeletedById(entry.getId(), 1);
                    return;
                }

                content = entry.getContent();
                lastModified = entry.getModified();

                Utils.writeSAFFile(getApplicationContext(), dir, title, content, lastModified);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Import a file from SAF
    protected void importSAFFile(DocumentFile file, boolean overwrite) {
        try {
            String title, content, line;
            StringBuilder buf = new StringBuilder();
            Calendar cal;
            Date modified;
            DBEntry entry;
            BufferedReader reader;
            FileInputStream in;

            title = Utils.getTitleFromDocumentFileName(getApplicationContext(), file);

            // Sanity check
            if ((title == null) || (title.length() == 0))  return;

            List<DBEntry> records = mDatasource.getRecordByTitle(title);

            // Get data from the file
            content = Utils.readFromSAFFile(getApplicationContext(), file);
            modified = new Date(file.lastModified());

            if (records.size() > 0) {
                // Update existing
                entry = records.get(0);

                if (!overwrite) {
                    Log.d(Const.TAG, "nano - importSAFFile: checking " + title + " ...");

                    if ((entry.getModified().after(modified)) || (entry.getModified().equals(modified)))  return;
                }

                mDatasource.updateRecord(entry.getId(), entry.getTitle(), content, entry.getStar(), modified, true, entry.getTitle());

                // Update status
                if ((MainActivity.main_activity != null) && (!Utils.isHiddenFile(entry.getTitle())))
                    MainActivity.main_activity.addStatus(entry.getTitle() + getApplicationContext().getResources().getString(R.string.status_updated_remotely));

                if ((DisplayDBEntry.display_dbentry != null) && (!Utils.isHiddenFile(entry.getTitle())))
                    DisplayDBEntry.display_dbentry.addStatus(entry.getTitle() + getApplicationContext().getResources().getString(R.string.status_updated_remotely));

                // Append to sync history
                Utils.appendSyncLogFile(getApplicationContext(), mLocalRepoPath, title, Utils.getRevisionSummaryStr(getApplicationContext(), entry.getContent(), content), mMaxSyncLogFileSize, mMaxSyncLogFileAge);
            } else {
                // Create new
                mDatasource.createRecord(title, content, 0, modified, true);
            }

            Log.d(Const.TAG, "nano - importSAFFile: " + title + " processed.");
        } catch (Exception e) {
            Log.i(Const.TAG, "importSAFFile: failed");
            e.printStackTrace();
        }
    }

    // Load preferences
    protected void loadPref() {
        try {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mLocalRepoPath = mSharedPreferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
            mBackupUri = Uri.parse(mSharedPreferences.getString(Const.PREF_BACKUP_URI, ""));
            mFileNameAsTitle = Utils.fileNameAsTitle(getApplicationContext());

            // Hacks
            mMaxSyncLogFileSize = Integer.valueOf(mSharedPreferences.getString(Const.PREF_MAX_SYNC_LOG_FILE_SIZE, String.valueOf(Const.MAX_SYNC_LOG_FILE_SIZE))) * Const.ONE_KB;
            mMaxSyncLogFileAge = Integer.valueOf(mSharedPreferences.getString(Const.PREF_MAX_SYNC_LOG_FILE_AGE, String.valueOf(Const.MAX_SYNC_LOG_FILE_AGE)));

            // Last mirrored time
            mLastMirrored = mSharedPreferences.getLong(Const.MIRROR_TIMESTAMP, 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

