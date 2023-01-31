package com.appmindlab.nano;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * Created by saelim on 9/15/16.
 */
public class EditStatusGestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDown(@NonNull MotionEvent event) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent event) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
        // Show clipboard and general statistics
        try {
            DisplayDBEntry.display_dbentry.showStat();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent event) {
        // Show contextual details
        try {
            DisplayDBEntry.display_dbentry.showDetails();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent event) {
        return true;
    }

    @Override
    public boolean onFling (@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > Const.EDIT_STATUS_SWIPE_H_MAX_OFF_PATH){
                return false;
            }

            // right to left swipe
            if (e1.getX() - e2.getX() > Const.EDIT_STATUS_SWIPE_H_MIN_DISTANCE
                    && Math.abs(velocityX) > Const.EDIT_STATUS_SWIPE_H_THRESHOLD_VELOCITY) {
                // Go to anchor
                DisplayDBEntry.display_dbentry.handleEditStatusLeftSwipe();
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > Const.EDIT_STATUS_SWIPE_H_MIN_DISTANCE
                    && Math.abs(velocityX) > Const.EDIT_STATUS_SWIPE_H_THRESHOLD_VELOCITY) {
                // Go to last markdown scroll position
                DisplayDBEntry.display_dbentry.doGotoMarkdownViewPos();
            }
            // bottom to top swipe
            else if (e1.getY() - e2.getY() > Const.EDIT_STATUS_SWIPE_V_MIN_DISTANCE
                    && Math.abs(velocityY) > Const.EDIT_STATUS_SWIPE_V_THRESHOLD_VELOCITY) {
                // Go to previous match
                if (DisplayDBEntry.display_dbentry.hasHits())
                    DisplayDBEntry.display_dbentry.doGotoMatch(-1, true);

                // Show working set only if not in active search
                else
                    DisplayDBEntry.display_dbentry.handleWorkingSet();
            }
            // top to bottom swipe
            else if (e2.getY() - e1.getY() > Const.EDIT_STATUS_SWIPE_V_MIN_DISTANCE
                    && Math.abs(velocityY) > Const.EDIT_STATUS_SWIPE_V_THRESHOLD_VELOCITY) {
                // Go to next match
                if (DisplayDBEntry.display_dbentry.hasHits())
                    DisplayDBEntry.display_dbentry.doGotoMatch(1, true);

                // Show in note navigation only if not in Markdown view nor active search
                else
                    DisplayDBEntry.display_dbentry.handleInNoteNavigation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(@NonNull MotionEvent event)  {
        try {
            // Set up anchor
            DisplayDBEntry.display_dbentry.setupAnchor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
