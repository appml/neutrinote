package com.appmindlab.nano;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/**
 * Created by saelim on 8/7/17.
 */

@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
public class CustomSyncTileService extends TileService {
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    private void updateTile() {
        boolean is_active = getServiceStatus();

        if (is_active) {
            // Animate the tile icon
            // Note: better implement with AnimatedVectorDrawable, which is not supported now (https://medium.com/google-developers/quick-settings-tiles-e3c22daf93a8)
            // Otherwise, one can create icon, call icon's loadDrawable() and animate the drawable by calling start()
            new CountDownTimer(Const.SYNC_TILE_REFRESH_PERIOD, Const.SYNC_TILE_REFRESH_DELAY) {
                Tile tile = getQsTile();
                boolean tick = false;

                @Override
                public void onTick(long millisUntilFinished) {
                    tick = !tick;
                    if (tick)
                        tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_cached_vector));
                    else
                        tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_sync_vector));

                    tile.updateTile();
                }

                @Override
                public void onFinish() {
                    tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_sync_vector));
                    tile.setState(Tile.STATE_ACTIVE);
                    tile.updateTile();
                }
            }.start();
        }
        else {
            // Handled by onFinished already
        }
    }

    // Track number of taps
    private boolean getServiceStatus() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSharedPreferencesEditor = mSharedPreferences.edit();

        boolean is_active = !mSharedPreferences.getBoolean(Const.TILE_SERVICE_STATE, false);

        mSharedPreferencesEditor.putBoolean(Const.TILE_SERVICE_STATE, is_active);
        mSharedPreferencesEditor.apply();

        return is_active;
    }

    @Override
    public void onClick() {
        super.onClick();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String path = mSharedPreferences.getString(Const.PREF_LOCAL_REPO_PATH, "");

        // Update tile UI
        updateTile();

        // Send a sync request
        Utils.sendSyncRequest(getApplicationContext(), path);

        // Update tile UI
        updateTile();
    }
}
