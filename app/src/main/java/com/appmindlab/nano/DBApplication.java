package com.appmindlab.nano;

/**
 * Created by saelim on 6/30/2015.
 */

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Date;

public class DBApplication extends Application {
    private static Context mContext;

    // Default uncaught exception handler
    private Thread.UncaughtExceptionHandler mUEH;

    // Last app launch time
    private Date mLastLaunchTime;

    // Custom uncaught exception handler
    private Thread.UncaughtExceptionHandler mCustomUEH =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable thrown) {
                    // Determine acceptable last launch time
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MINUTE, -1 * Const.MIN_RELAUNCH_INTERVAL);
                    Date launch_time = cal.getTime();

                    if (mLastLaunchTime.before(launch_time)) {
                        PendingIntent activity = PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class), PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                        // Auto restart activity
                        AlarmManager alarm_manager;
                        alarm_manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        alarm_manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, Const.AUTO_RELAUNCH_DELAY, activity);

                        mLastLaunchTime = launch_time;
                    }

                    // Exit
                    System.exit(Const.ERROR_UNEXPECTED);

                    // Pass exception on to OS (important)
                    mUEH.uncaughtException(thread, thrown);
                }
            };

    public static Context getAppContext() {
        return mContext;
    }
    public static DBHelper getDBHelper() {
        return DBHelper.getInstance(mContext);
    }

    public void onCreate(){
        super.onCreate();
        mContext = getApplicationContext();

        // Setup last launch time
        Calendar cal = Calendar.getInstance();
        mLastLaunchTime = cal.getTime();

        // Setup uncaught exception handler
        mUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mCustomUEH);    // Comment out this line for debugging
    }
}
