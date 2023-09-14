package com.appmindlab.nano;

import android.util.Log;
import android.view.ScaleGestureDetector;

/**
 * Created by saelim on 9/13/2023.
 */

public class ContentScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private float mScaleFactor;

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mScaleFactor *= detector.getScaleFactor();

        // testing
        Log.d(Const.TAG, "nano -- onScale, mScaleFactor: " + mScaleFactor);

        // Sanity check
        // mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

        // Perform text expansion
        DisplayDBEntry.display_dbentry.doTextExpansion();
    }
}

