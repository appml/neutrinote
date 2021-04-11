package com.appmindlab.nano;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Created by saelim on 8/5/2015.
 */
public class BackupScheduleReceiver extends BroadcastReceiver {
    private AlarmManager mService;

    @Override
    public void onReceive(Context context, Intent intent) {
        mService = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, BackupServiceReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 30);    // Start 30 seconds after boot completed

        long repeat_time;

        // Retrieve backup frequency from preference
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean incremental_backup = mySharedPreferences.getBoolean(Const.PREF_INCREMENTAL_BACKUP, false);

        // Empty path implies no backup is used
        if (!incremental_backup) {
            pending.cancel();
            mService.cancel(pending);
        }
        else {
            repeat_time = AlarmManager.INTERVAL_HOUR * Const.AUTO_BACKUP_FREQ;
            mService.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), repeat_time, pending);
        }
    }
}