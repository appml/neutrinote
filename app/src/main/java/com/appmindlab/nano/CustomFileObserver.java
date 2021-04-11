package com.appmindlab.nano;

/**
 * Created by saelim on 7/27/2015.
 */

import android.content.SharedPreferences;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;


/**
 * Created by saelim on 7/20/2015.
 */
public class CustomFileObserver extends FileObserver {
    protected String mAbsolutePath;
    protected SharedPreferences mSharedPreferences;

    public CustomFileObserver(String path) {
        super(path, FileObserver.ALL_EVENTS);
        mAbsolutePath = path;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.main_activity);
    }

    @Override
    public void onEvent(int event, String path) {
        if (path == null) {
            return;
        }

        // Verify file extension
        if (Utils.stripExtension("txt", path).length() == 0) {
            return;
        }

        try {
            // A new file or subdirectory was created under the monitored directory
            if ((FileObserver.CREATE & event) != 0) {
                Log.i(Const.TAG, "create " + path);
                MainActivity.main_activity.doImportLocalRepoFile(new File(mAbsolutePath + "/" + path));
            }

            // Someone has a file or directory open for writing, and closed it
            if ((FileObserver.CLOSE_WRITE & event) != 0) {
                Log.i(Const.TAG, "close_write " + path);
                MainActivity.main_activity.doImportLocalRepoFile(new File(mAbsolutePath + "/" + path));
            }

            // A file was deleted from the monitored directory
            if ((FileObserver.DELETE & event) != 0) {
                Log.i(Const.TAG, "delete " + path);
                if (mSharedPreferences.getBoolean(Const.PREF_LAZY_UPDATE, false))
                    MainActivity.main_activity.doImportLocalRepo();
            }

            // The monitored file or directory was deleted, monitoring effectively stops
            if ((FileObserver.DELETE_SELF & event) != 0) {
                Log.i(Const.TAG, "delete_self " + path);
                if (mSharedPreferences.getBoolean(Const.PREF_LAZY_UPDATE, false))
                    MainActivity.main_activity.doImportLocalRepo();
            }

            // A file or subdirectory was moved from the monitored directory
            if ((FileObserver.MOVED_FROM & event) != 0) {
                Log.i(Const.TAG, "moved_from " + path);
                if (mSharedPreferences.getBoolean(Const.PREF_LAZY_UPDATE, false))
                    MainActivity.main_activity.doImportLocalRepo();
            }

            // A file or subdirectory was moved to the monitored directory
            if ((FileObserver.MOVED_TO & event) != 0) {
                Log.i(Const.TAG, "moved_to " + path);
                if (mSharedPreferences.getBoolean(Const.PREF_LAZY_UPDATE, false))
                    MainActivity.main_activity.doImportLocalRepo();
            }

            // The monitored file or directory was moved; monitoring continues
            if ((FileObserver.MOVE_SELF & event) != 0) {
                Log.i(Const.TAG, "move_self " + path);
                if (mSharedPreferences.getBoolean(Const.PREF_LAZY_UPDATE, false))
                    MainActivity.main_activity.doImportLocalRepo();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}


