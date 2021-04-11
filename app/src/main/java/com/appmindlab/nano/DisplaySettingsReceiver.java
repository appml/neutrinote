package com.appmindlab.nano;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by saelim on 8/6/2015.
 */
public class DisplaySettingsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (Const.ACTION_CHANGE_DISPLAY_SETTINGS.equals(intent.getAction())) {
            float light_level = intent.getFloatExtra(Const.EXTRA_LIGHT_LEVEL, 0);
            int lightLevelThreshold = intent.getIntExtra(Const.EXTRA_LIGHT_LEVEL_THRESHOLD, Const.LIGHT_LEVEL_THRESHOLD_DIRECT_SUNLIGHT);
            String theme;

            if (light_level >= lightLevelThreshold)
                theme = "day";
            else
                theme = "night";

            editor.putString(Const.PREF_THEME, theme);
            editor.commit();

            // Remember the light level
            Utils.setLightLevel(light_level);
        }
    }

}

