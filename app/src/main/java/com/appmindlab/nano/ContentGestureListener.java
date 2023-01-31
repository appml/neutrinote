package com.appmindlab.nano;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * Created by saelim on 10/27/2017.
 */

public class ContentGestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent event) {
        try {
            DisplayDBEntry.display_dbentry.setToolBarVisible(false);
            DisplayDBEntry.display_dbentry.showHideToolBar(false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent event) {
        try {
            DisplayDBEntry.display_dbentry.setToolBarVisible(true);
            DisplayDBEntry.display_dbentry.showHideToolBar(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
