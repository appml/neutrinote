package com.appmindlab.nano;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by saelim on 3/21/2017.
 */

@TargetApi(21)
public class BackupJobService extends JobService {
    // Data source
    private DataSource mDatasource;

    // File system
    private String mDirPath = "", mSubDirPath = "";

    // Preferences
    private SharedPreferences mSharedPreferences;
    private String mLocalRepoPath;
    private Uri mBackupUri;
    private boolean mLowSpaceMode = false;
    private boolean mFileNameAsTitle;
    private int mMaxDeletedCopiesAge;

    // Notification
    protected NotificationManager mNotifyManager;
    protected NotificationCompat.Builder mBuilder;
    protected NotificationCompat.BigTextStyle mBigTextStyle = new NotificationCompat.BigTextStyle();
    protected PendingIntent mIntent;

    @Override
    public boolean onStartJob(JobParameters params) {
        final JobParameters info = params;

        // Get preferences
        loadPref();

        // Sanity check
        if (DisplayDBEntry.display_dbentry != null)
            return false;

        // Run outside of the UI thread
        Thread t = new Thread() {
            public void run() {
                // Basics
                String status;

                // Preference editor
                SharedPreferences.Editor editor = mSharedPreferences.edit();

                // Misc
                Intent newIntent;

                // Open the database
                mDatasource = new DataSource();
                mDatasource.open();

                // Setup notification
                mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Utils.makeNotificationChannel(mNotifyManager, Const.BACKUP_CHANNEL_ID, Const.BACKUP_CHANNEL_NAME, Const.BACKUP_CHANNEL_DESC, Const.BACKUP_CHANNEL_LEVEL);
                mBuilder = new NotificationCompat.Builder(getApplicationContext(), Const.BACKUP_CHANNEL_ID);

                newIntent = new Intent(getApplicationContext(), MainActivity.class);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mIntent = PendingIntent.getActivity(getApplicationContext(), 0, newIntent, 0);

                try {
                    ///////////////////////
                    // 1. Backup app data
                    ///////////////////////
                    backupAppData(getApplicationContext());

                    ////////////////////
                    // 2. Backup files
                    ////////////////////

                    // Purge old backups
                    purgeBackups();

                    // Time-stamped folder
                    SimpleDateFormat sdf = new SimpleDateFormat(Const.DIRPATH_DATE_FORMAT, Locale.getDefault());
                    mSubDirPath = sdf.format(new Date());
                    status = backupFiles(getApplicationContext(), true);

                    // Merged folder
                    mSubDirPath = Const.INCREMENTAL_BACKUP_PATH;
                    status = backupFiles(getApplicationContext(), true);

                    // Save the log status
                    editor.putString(Const.AUTO_BACKUP_LOG, status);
                    editor.apply();

                    // Update notification
                    mBigTextStyle.bigText(status);
                    mBuilder.setStyle(mBigTextStyle);
                    mBuilder.setContentText(status).setProgress(0, 0, false);

                    // Removes the progress bar
                    mNotifyManager.notify(0, mBuilder.build());

                } catch (Exception e) {
                    e.printStackTrace();
                    mBuilder.setContentText(getApplicationContext().getResources().getString(R.string.error_backup));
                }

                // Clean up
                mDatasource.close();

                // Inform job manager
                jobFinished(info, false);
            }
        };
        t.start();

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Clean up when stopped unexpectedly
        if (mDatasource != null)
            mDatasource.close();

        return false;
    }

    // Backup app data
    protected void backupAppData(Context context) {
        List<DBEntry> results;
        DBEntry entry;
        String content = "", app_data_file, app_settings_file;
        int count;

        // Set up file names
        app_data_file = Utils.makeFileName(context, Const.APP_DATA_FILE);
        app_settings_file = Utils.makeFileName(context, Const.APP_SETTINGS_FILE);

        // 1. Backup app data
        results = mDatasource.getAllActiveContentlessRecords("title", "ASC");
        count = results.size();

        // Fill up the metadata string
        for (int i = 0; i < count; i++) {
            entry = results.get(i);
            content = content + entry.getTitle() + Const.SUBDELIMITER + entry.getStar() + Const.SUBDELIMITER + entry.getPos() + Const.SUBDELIMITER + entry.getMetadata() + Const.SUBDELIMITER + entry.getAccessed().getTime() + Const.SUBDELIMITER + entry.getLatitude() + Const.SUBDELIMITER + entry.getLongitude();
            content = content + Const.SUBDELIMITER + entry.getCreated().getTime() + Const.SUBDELIMITER + entry.getModified().getTime();
            content = content + Const.SUBDELIMITER + Const.SUBDELIMITER + Const.DELIMITER;
        }

        // Save to metadata file
        results = mDatasource.getRecordByTitle(app_data_file);
        if (results.size() == 1) {
            entry = results.get(0);
            mDatasource.updateRecord(entry.getId(), app_data_file, content, 0, null, true, app_data_file);
        } else if (results.size() == 0) {
            mDatasource.createRecord(app_data_file, content, 0, null, true);
        }

        // 2. Backup settings
        Map<String, ?> prefs = new TreeMap<>(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getAll());
        String key, value;
        content = "";
        for (Map.Entry<String, ?> pref : prefs.entrySet()) {
            key = pref.getKey();
            value = pref.getValue().toString();

            // Skip log
            if ((key.equals(Const.AUTO_BACKUP_LOG)) || (key.equals(Const.SYNC_LOG)) || (!key.startsWith(Const.PACKAGE)))
                continue;

            // Skip local repository path (should only be saved via UI)
            if (key.equals(Const.PREF_LOCAL_REPO_PATH))
                continue;

            if (Arrays.asList(Const.ALL_PREFS).contains(key))
                content += key + Const.SETTINGS_DELIMITER + value + Const.DELIMITER;
        }

        // Save to settings file
        results = mDatasource.getRecordByTitle(app_settings_file);
        if (results.size() == 1) {
            entry = results.get(0);
            mDatasource.updateRecord(entry.getId(), app_settings_file, content, 0, null, true, app_settings_file);
        } else if (results.size() == 0) {
            mDatasource.createRecord(app_settings_file, content, 0, null, true);
        }

        return;
    }

    // Backup files
    protected String backupFiles(Context context, boolean notifyProgress) {
        String status;
        int count, incr = 0;
        DocumentFile dir, dest_dir, attachment_dir, font_dir, log_dir;
        File trash_dir;

        dir = DocumentFile.fromTreeUri(getApplicationContext(), mBackupUri);

        if (mSubDirPath.equals(Const.INCREMENTAL_BACKUP_PATH)) {
            try {
                // Handle merged folder
                dest_dir = Utils.getSAFSubDir(getApplicationContext(), dir, Const.INCREMENTAL_BACKUP_PATH);
            }
            catch (Exception e) {
                e.printStackTrace();
                status = context.getResources().getString(R.string.error_backup);
                return status;
            }
        }
        else {
            // Handle timestamped folder
            dest_dir = dir.createDirectory(mSubDirPath);
        }

        List<Long> results = mDatasource.getAllActiveRecordsIDs("title", "ASC");
        count = results.size();

        if (notifyProgress) {
            mBuilder.setContentTitle(context.getResources().getString(R.string.status_auto_backup)).setContentText(context.getResources().getString(R.string.status_auto_backup_in_progress)).setSmallIcon(R.drawable.ic_archive_vector).setColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.colorPrimary));
            mBuilder.setProgress(100, incr, false);
            mNotifyManager.notify(0, mBuilder.setContentIntent(mIntent).build());
        }

        for (int i = 0; i < count; i++) {
            exportSAFFile(dest_dir, results.get(i));

            // Update notification
            if (notifyProgress) {
                incr = (int) ((i / (float) count) * 100);
                mBuilder.setProgress(100, incr, false);
                mNotifyManager.notify(0, mBuilder.build());
            }
        }

        // Backup attachments
        attachment_dir = Utils.getSAFSubDir(getApplicationContext(), dest_dir, Const.ATTACHMENT_PATH);
        Utils.exportToSAFFolder(getApplicationContext(), new File(mLocalRepoPath + "/" + Const.ATTACHMENT_PATH), attachment_dir, true);

        // Backup fonts
        font_dir = Utils.getSAFSubDir(getApplicationContext(), dest_dir, Const.CUSTOM_FONTS_PATH);
        Utils.exportToSAFFolder(getApplicationContext(), new File(mLocalRepoPath + "/" + Const.CUSTOM_FONTS_PATH), font_dir, true);

        // Backup multitype file
        if (Utils.fileExists(getApplicationContext(), mLocalRepoPath, Const.MULTI_TYPE))
            Utils.exportToSAFFile(getApplicationContext(), mLocalRepoPath + "/", Const.MULTI_TYPE, dest_dir);

        // Backup sync log
        // Note: comment out to delegate to mirror to handle
        /*
        if (Utils.fileExists(getApplicationContext(), mLocalRepoPath, Const.SYNC_LOG_FILE)) {
            Utils.exportToSAFFile(getApplicationContext(), mLocalRepoPath + "/", Const.SYNC_LOG_FILE, dest_dir);

            // Move log folder to backup
            // Note: no need to delete source copy as that's managed by purging; destination copy can be manually removed by user
            log_dir = Utils.getSAFSubDir(getApplicationContext(), dest_dir, Const.LOG_PATH);
            Utils.moveToSAFFolder(getApplicationContext(), mBackupUri, new File(mLocalRepoPath + "/" + Const.LOG_PATH), log_dir, false, false);
        }

        // Backup import errors by moving import error folder to backup
        // Note: need to remove source copy as its content will keep growing; destination copy can be manually removed by user
        Utils.moveToSAFFolder(getApplicationContext(), mBackupUri, new File(mLocalRepoPath + "/" + Const.IMPORT_ERROR_PATH), dest_dir, true, false);
        */

        // When the loop is finished, updates the notification
        Date now = new Date();
        status = context.getResources().getString(R.string.status_auto_backup_completed) + " " + Utils.getSystemDateFormat(context, Locale.getDefault()).format(now) + Utils.getSystemTimeFormat(context, Locale.getDefault()).format(now);

        // Purge deleted copies
        purgeDeletedCopies();

        return status;
    }

    // Export a file to SAF
    protected void exportSAFFile(DocumentFile dir, Long id) {
        try {
            String title, content;
            DBEntry entry;

            // Get content
            List<DBEntry> results = mDatasource.getRecordById(id);

            if (results.size() > 0) {
                entry = results.get(0);
                title = entry.getTitle();
                content = entry.getContent();
                Utils.writeSAFFile(getApplicationContext(), dir, title, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Purge old backups
    protected void purgeBackups() {
        // Get a list of files
        DocumentFile dir = DocumentFile.fromTreeUri(this, mBackupUri);
        DocumentFile[] files = dir.listFiles();

        // Sort by modified date (descending)
        Arrays.sort(files, new Comparator<DocumentFile>() {
            public int compare(DocumentFile f1, DocumentFile f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });

        // Keep only a specified number of copies
        int i = 0;
        for (DocumentFile file : files) {
            if ((file.isDirectory()) && (!Arrays.asList(Const.RESERVED_FOLDER_NAMES).contains(file.getName()))) {
                i++;
                if (i >= Const.MAX_BACKUP_COUNT)
                    file.delete();
            }
        }
    }

    // Purge old deleted copies
    protected void purgeDeletedCopies() {
        // Get a list of files
        DocumentFile dir = DocumentFile.fromTreeUri(getApplicationContext(), mBackupUri);
        DocumentFile trash_dir = Utils.getSAFSubDir(getApplicationContext(), dir, Const.TRASH_PATH);
        DocumentFile[] files = trash_dir.listFiles();

        // Sort by modified date (descending)
        Arrays.sort(files, new Comparator<DocumentFile>() {
            public int compare(DocumentFile f1, DocumentFile f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1*mMaxDeletedCopiesAge);

        for (DocumentFile file : files) {
            if (!file.isDirectory()) {
                if ((mMaxDeletedCopiesAge > 0) && (cal.getTime().after(new Date(file.lastModified()))))
                    file.delete();
            }
        }
    }

    // Load preferences
    protected void loadPref() {
        try {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mLocalRepoPath = mSharedPreferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
            mBackupUri = Uri.parse(mSharedPreferences.getString(Const.PREF_BACKUP_URI, ""));
            mLowSpaceMode = mSharedPreferences.getBoolean(Const.PREF_LOW_SPACE_MODE, false);
            mMaxDeletedCopiesAge = Integer.parseInt(mSharedPreferences.getString(Const.PREF_MAX_DELETED_COPIES_AGE, Const.MAX_DELETED_COPIES_AGE));
            mFileNameAsTitle = Utils.fileNameAsTitle(getApplicationContext());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}


