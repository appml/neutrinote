package com.appmindlab.nano;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by saelim on 8/19/2015.
 */
public class ModuleStatReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        if (Const.ACTION_UPDATE_SYNC_LOG.equals(intent.getAction())) {
            String sync_log = intent.getStringExtra(Intent.EXTRA_TEXT);

            // Save the status in persistent storage since Minutes may not be running
            editor.putString(Const.SYNC_LOG, sync_log);
            editor.commit();
        }
    }
}
