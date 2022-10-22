package com.appmindlab.nano;

/**
 * Created by saelim on 8/5/2015.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class BackupService extends Service {
    private final IBinder mBinder = new BackupServiceBinder();

    // Data source
    private DataSource mDatasource;

    // File system
    private String mDirPath="", mSubDirPath="", mFullPath="";
    private File mSDcard;

    // Preferences
    private SharedPreferences mSharedPreferences;
    private String mLocalRepoPath;
    private boolean mLowSpaceMode = false;
    private boolean mIncrementalBackup, mFileNameAsTitle;
    private int mMaxDeletedCopiesAge;

    // Notification
    protected NotificationManager mNotifyManager;
    protected NotificationCompat.Builder mBuilder;
    protected NotificationCompat.BigTextStyle mBigTextStyle = new NotificationCompat.BigTextStyle();
    protected PendingIntent mIntent;

    // Settings
    protected int mMaxBackupCount;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Intent cur_intent = intent;

        // Get preferences
        loadPref();

        // Sanity check
        if ((DisplayDBEntry.display_dbentry != null) && (mSharedPreferences.getString(Const.AUTO_BACKUP_LOG, "").length() > 0))
            return Service.START_NOT_STICKY;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)  // Handled by BackupWorker instead
            return Service.START_NOT_STICKY;

        // Run outside of the UI thread
        Thread t = new Thread(){
            public void run(){
                // Basics
                String status = Const.NULL_SYM;

                // Preference editor
                SharedPreferences.Editor editor = mSharedPreferences.edit();

                // Misc
                Intent newIntent;

                // Get backup type
                String action = cur_intent.getAction();

                // Open the database
                mDatasource = new DataSource();
                mDatasource.open();

                // Setup notification
                mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Utils.makeNotificationChannel(mNotifyManager, Const.BACKUP_CHANNEL_ID, Const.BACKUP_CHANNEL_NAME, Const.BACKUP_CHANNEL_DESC, Const.BACKUP_CHANNEL_LEVEL);
                mBuilder = new NotificationCompat.Builder(getApplicationContext(), Const.BACKUP_CHANNEL_ID);

                newIntent = new Intent(getApplicationContext(), MainActivity.class);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mIntent = PendingIntent.getActivity(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_IMMUTABLE);

                try {
                    ///////////////////////
                    // 1. Backup app data
                    ///////////////////////
                    backupAppData(getApplicationContext());

                    ////////////////////
                    // 2. Backup files
                    ////////////////////
                    if (mMaxBackupCount > 0) {
                        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) || ((action != null) && (action.equals(Const.ACTION_FULL_BACKUP)))) {
                            SimpleDateFormat sdf = new SimpleDateFormat(Const.DIRPATH_DATE_FORMAT, Locale.getDefault());
                            mSubDirPath = sdf.format(new Date());
                            status = backupFiles(getApplicationContext(), mSubDirPath, true, true);
                        } else {
                            status = backupFiles(getApplicationContext(), Const.INCREMENTAL_BACKUP_PATH, false, true);
                        }
                    }

                    // Purge deleted copies
                    purgeDeletedCopies();

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
            }
        };
        t.start();

        return Service.START_NOT_STICKY;
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
    protected String backupFiles(Context context, String path, boolean fullBackup, boolean notifyProgress) {
        String status;
        List<Long> ids;
        int count, incr = 0;

        // Determine sd card state
        Utils.getSDState();

        // Determine the path
        if (Utils.mExternalStorageWriteable) {
            SimpleDateFormat sdf = new SimpleDateFormat(Const.DIRPATH_DATE_FORMAT, Locale.getDefault());
            mSubDirPath = sdf.format(new Date());

            // Compute export path
            String export_path;

            if (mLowSpaceMode)
                export_path = Utils.getAppPathRemovableStorage(this);
            else
                export_path = Utils.getParentPath(mLocalRepoPath);

            mDirPath = export_path + "/" + Const.EXPORT_PATH;

            // Make directory if not available yet
            File dir = new File(mDirPath);
            if (!dir.isDirectory())
                dir.mkdir();

            // Purge old directories to make room
            purgeBackups(dir);

            // Make subdirectory if not available yet
            if (dir.isDirectory()) {
                mFullPath = mDirPath + "/" + path;
                dir = new File(mFullPath);
                if (!dir.isDirectory())
                    dir.mkdir();

                if (dir.isDirectory()) {
                    ids = mDatasource.getAllActiveRecordsIDs(DBHelper.COLUMN_MODIFIED, Const.SORT_DESC);
                    count = ids.size();

                    if (notifyProgress) {
                        mBuilder.setContentTitle(context.getResources().getString(R.string.status_auto_backup)).setContentText(context.getResources().getString(R.string.status_auto_backup_in_progress)).setSmallIcon(R.drawable.ic_archive_vector).setColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.colorPrimary));
                        mBuilder.setProgress(100, incr, false);
                        mNotifyManager.notify(0, mBuilder.setContentIntent(mIntent).build());
                    }

                    for (int i = 0; i < count; i++) {
                        exportSDFile(ids.get(i), fullBackup);

                        // Update notification
                        if (notifyProgress) {
                            incr = (int) ((i / (float) count) * 100);
                            mBuilder.setProgress(100, incr, false);
                            mNotifyManager.notify(0, mBuilder.build());
                        }
                    }

                    // Backup attachments
                    Utils.copyFolder(context, new File(mLocalRepoPath + "/" + Const.ATTACHMENT_PATH), new File(mFullPath + "/" + Const.ATTACHMENT_PATH), fullBackup);

                    // Backup fonts
                    Utils.copyFolder(context, new File(mLocalRepoPath + "/" + Const.CUSTOM_FONTS_PATH), new File(mFullPath + "/" + Const.CUSTOM_FONTS_PATH), fullBackup);

                    // Backup multitype file
                    if (Utils.fileExists(context, mLocalRepoPath, Const.MULTI_TYPE))
                        Utils.copyFile(context, mLocalRepoPath + "/", Const.MULTI_TYPE, mFullPath + "/");
                }

                // When the loop is finished, updates the notification
                Date now = new Date();
                int num_files = Utils.getFileCountFromDirectory(context, dir, mFullPath);
                status = context.getResources().getString(R.string.status_auto_backup_completed) + " " + Utils.getSystemDateFormat(context, Locale.getDefault()).format(now) + Utils.getSystemTimeFormat(context, Locale.getDefault()).format(now);
                status += " (" + mFullPath + ")";
                status += ": " + num_files + "+";
            } else {
                status = mFullPath + context.getResources().getString(R.string.error_create_path);
            }
        } else {
            status = context.getResources().getString(R.string.error_no_writable_external_storage);
        }

        return status;
    }

    // Export a file to SD card
    protected void exportSDFile(Long id, boolean overwrite) {
        try {
            String title, content,  file_name;
            DBEntry entry;

            // Get content
            List<DBEntry> results = mDatasource.getRecordById(id);

            if (results.size() > 0) {
                entry = results.get(0);
                title = entry.getTitle();
                content = entry.getContent();

                if (mFileNameAsTitle)
                    file_name = title;
                else
                    file_name = title + ".txt";

                if (!overwrite) {
                    // Skip if the version on disk is newer
                    File temp = new File(mFullPath + "/" + file_name);
                    if (entry.getModified().getTime() < temp.lastModified()) {
                        return;
                    }
                }

                FileOutputStream file = new FileOutputStream(mFullPath + "/" + file_name);
                file.write(content.getBytes());
                file.flush();
                file.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Purge old backups
    protected void purgeBackups(File directory) {
        // Get a list of files
        File[] files = directory.listFiles();

        // Sort by modified date (descending)
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });

        // Keep only a specified number of copies
        int i = 0;

        for (File file : files) {
            if ((file.isDirectory()) && (!Arrays.asList(Const.RESERVED_FOLDER_NAMES).contains(file.getName()))) {
                i++;
                if (i >= mMaxBackupCount)
                    Utils.deleteDirectories(file);
            }
        }
    }

    // Purge old deleted copies
    protected void purgeDeletedCopies() {
        mDirPath = mLocalRepoPath + "/" + Const.TRASH_PATH;
        File dir = new File(mDirPath);
        if (!dir.isDirectory())
            return;

        // Sanity check
        if (mMaxDeletedCopiesAge < 0)
            return;

        // Get a list of files
        File[] files = dir.listFiles();

        // Sort by modified date (descending)
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1*mMaxDeletedCopiesAge);

        for (File file : files) {
            if (!file.isDirectory()) {
                if ((mMaxDeletedCopiesAge > 0) && (cal.getTime().after(new Date(file.lastModified()))))
                    file.delete();
            }
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public class BackupServiceBinder extends Binder {
        BackupService getService() {
            return BackupService.this;
        }
    }

    // Load preferences
    protected void loadPref() {
        try {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mIncrementalBackup = mSharedPreferences.getBoolean(Const.PREF_INCREMENTAL_BACKUP, false);
            mLocalRepoPath = mSharedPreferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
            mMaxBackupCount = Integer.parseInt(mSharedPreferences.getString(Const.PREF_MAX_BACKUP_COUNT, String.valueOf(Const.MAX_BACKUP_COUNT)));
            mLowSpaceMode = mSharedPreferences.getBoolean(Const.PREF_LOW_SPACE_MODE, false);
            mMaxDeletedCopiesAge = Integer.parseInt(mSharedPreferences.getString(Const.PREF_MAX_DELETED_COPIES_AGE, Const.MAX_DELETED_COPIES_AGE));
            mFileNameAsTitle = Utils.fileNameAsTitle(getApplicationContext());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

