package com.appmindlab.nano;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Created by saelim on 12/11/2015.
 */
public class BackupAgent extends BackupAgentHelper {
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper pref_backup_helper = new SharedPreferencesBackupHelper(this, Const.BACKUP_PREF_KEY);

        addHelper(Const.BACKUP_PREF, pref_backup_helper);
        super.onCreate();
    }
}
