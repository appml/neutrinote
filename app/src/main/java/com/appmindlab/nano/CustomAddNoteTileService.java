package com.appmindlab.nano;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

/**
 * Created by saelim on 8/9/17.
 */

@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
public class CustomAddNoteTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();

        if (!isLocked()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            intent.setAction(Const.ACTION_VIEW_ENTRY);
            intent.putExtra(Const.EXTRA_ID, -1);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivityAndCollapse(intent);
        }
    }
}
