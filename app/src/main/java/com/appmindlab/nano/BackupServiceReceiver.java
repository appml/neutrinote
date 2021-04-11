package com.appmindlab.nano;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by saelim on 8/5/2015.
 */

public class BackupServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            // No longer work under Android 8
            // https://developer.android.com/about/versions/oreo/android-8.0-changes.html#back-all
            Intent service = new Intent(context, BackupService.class);
            service.setAction(intent.getAction());
            service.putExtra(Const.EXTRA_MAX_BACKUP_COUNT, intent.getIntExtra(Const.EXTRA_MAX_BACKUP_COUNT, Const.MAX_BACKUP_COUNT));
            service.putExtra(Const.EXTRA_MAX_BACKUP_AGE, intent.getIntExtra(Const.EXTRA_MAX_BACKUP_AGE, Const.MAX_BACKUP_AGE));
            context.startService(service);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
