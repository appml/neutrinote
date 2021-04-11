package com.appmindlab.nano;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by saelim on 9/19/2016.
 */
public class MainStatusGestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        // Scroll to top
        try {
            MainActivity.main_activity.scrollToTop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        try {
            // Go to the list relevant to the status message
            MainActivity.main_activity.doLongTapStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        try {
            // Go to the default filter
            MainActivity.main_activity.doDoubleTapStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > Const.MAIN_STATUS_SWIPE_H_MAX_OFF_PATH){
                return false;
            }
            // right to left swipe
            if (e1.getX() - e2.getX() > Const.MAIN_STATUS_SWIPE_H_MIN_DISTANCE
                    && Math.abs(velocityX) > Const.MAIN_STATUS_SWIPE_H_THRESHOLD_VELOCITY) {
                // Go to the next filter
                MainActivity.main_activity.doSwipeStatus(true);
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > Const.MAIN_STATUS_SWIPE_H_MIN_DISTANCE
                    && Math.abs(velocityX) > Const.MAIN_STATUS_SWIPE_H_THRESHOLD_VELOCITY) {
                // Go to the previous filter
                MainActivity.main_activity.doSwipeStatus(false);
            }
            // bottom to top swipe
            else if (e1.getY() - e2.getY() > Const.MAIN_STATUS_SWIPE_H_MIN_DISTANCE
                    && Math.abs(velocityY) > Const.MAIN_STATUS_SWIPE_H_THRESHOLD_VELOCITY) {
                // Do nothing
            }
            // top to bottom swipe
            else if (e2.getY() - e1.getY() > Const.MAIN_STATUS_SWIPE_H_MIN_DISTANCE
                    && Math.abs(velocityY) > Const.MAIN_STATUS_SWIPE_H_THRESHOLD_VELOCITY) {
                // Do nothing
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event)  {
        return true;
    }
}
