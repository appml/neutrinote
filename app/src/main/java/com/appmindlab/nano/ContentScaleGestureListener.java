package com.appmindlab.nano;

import android.util.Log;
import android.view.ScaleGestureDetector;

/**
 * Created by saelim on 9/13/2023.
 */

public class ContentScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private float mScaleFactor = 1;
    private int mContentPos = -1;

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        // Remember content position
        mContentPos = DisplayDBEntry.display_dbentry.getContentPos();

        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor *= detector.getScaleFactor();

        // Sanity check
        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

        // testing
        Log.d(Const.TAG, "nano -- onScale, mScaleFactor: " + mScaleFactor);

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        // testing
        Log.d(Const.TAG, "nano -- onScaleEnd, mScaleFactor: " + mScaleFactor);

        // Scale font size
        DisplayDBEntry.display_dbentry.scaleFontSize(mScaleFactor, mContentPos);

        // Reset
        mScaleFactor = 1;
        mContentPos = -1;
    }
}
