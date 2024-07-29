package com.appmindlab.nano;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {
    // SQLite related
    private DataSource mDatasource;

    // Settings related
    private SharedPreferences mSharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_UPDATE_SCRAPBOOK.equals(intent.getAction())) {
            // Setup database
            if ((mDatasource == null) || (!mDatasource.isOpen())) {
                mDatasource = new DataSource();
                mDatasource.open();
            }

            // Setup preferences
            mSharedPreferences = context.getSharedPreferences(Const.PACKAGE, Context.MODE_PRIVATE);

            // Append to scrapbook
            doAppendScrapbook(context, intent);
        }
    }

    // Append to scrapbook file
    private void doAppendScrapbook(Context context, Intent intent) {
        String scrapbook_file = Utils.makeFileName(context, Const.SCRAPBOOK_TITLE);
        ArrayList<DBEntry> results = mDatasource.getRecordByTitle(scrapbook_file);

        if (results.size() == 1) {
            Bundle bundle = RemoteInput.getResultsFromIntent(intent);
            if (bundle != null) {
                String input = bundle.getCharSequence(Const.SCRAPBOOK_NOTIFICATION_KEY).toString();

                // Evaluate built-in variables
                boolean eval_built_in_variables = mSharedPreferences.getBoolean(Const.PREF_EVAL_BUILT_IN_VARIALBES, false);
                if (eval_built_in_variables) {
                    String custom_date_format = mSharedPreferences.getString(Const.PREF_CUSTOM_DATE_FORMAT, "");
                    String custom_time_format = mSharedPreferences.getString(Const.PREF_CUSTOM_TIME_FORMAT, "");

                    input = Utils.evalGlobalVariables(context, input, custom_date_format, custom_time_format, false);
                }

                DBEntry entry = results.get(0);

                StringBuilder sb = new StringBuilder();
                sb.append(entry.getContent());
                sb.append("\r\n\r\n");
                sb.append(input);

                // Sanity check
                if (sb.length() < entry.getSize()) {
                    Toast.makeText(DBApplication.getAppContext(), DBApplication.getAppContext().getResources().getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
                    return;
                }

                mDatasource.updateRecord(entry.getId(), entry.getTitle(), sb.toString(), entry.getStar(), null, true, entry.getTitle());

                boolean store_location = mSharedPreferences.getBoolean(Const.PREF_LOCATION_AWARE, false);
                Location location = null;
                if (store_location)
                    location = Utils.getLocation(DBApplication.getAppContext());

                if ((store_location) && (location != null))
                    mDatasource.updateRecordCoordinates(entry.getId(), location.getLatitude(), location.getLongitude());

                Toast.makeText(context, scrapbook_file + context.getResources().getString(R.string.status_scrapbook_updated), Toast.LENGTH_LONG).show();
            }

            // Reset notification
            resetScrapbookNotification(context, intent, scrapbook_file, results.get(0));
        }
        else {
            // Cancel notification
            cancelScrapbookNotification(context, intent);
        }
    }

    // Reset scrapbook notification
    private void resetScrapbookNotification(Context context, Intent intent, String title, DBEntry entry) {
        int notification_id = intent.getIntExtra(Const.EXTRA_SCRAPBOOK_NOTIFICATION_ID, 0);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        NotificationCompat.Action paste_action, goto_action;

        RemoteInput remote_input;

        Intent paste_intent, goto_intent;
        PendingIntent paste_pending_intent, goto_pending_intent;

        // Preview
        PrettyTime pretty_time = new PrettyTime(Locale.getDefault());
        Date timestamp = entry.getModified();

        // Clear existing notification
        manager.cancel(notification_id);

        // Create new notification
        builder = new NotificationCompat.Builder(context, Const.SCRAPBOOK_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_mode_edit_vector)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText(Utils.readableFileSize(entry.getSize()) + " / " + pretty_time.format(timestamp));

        // Remote input
        remote_input = new RemoteInput.Builder(Const.SCRAPBOOK_NOTIFICATION_KEY)
                .setLabel(context.getResources().getString(R.string.hint_content))
                .build();

        // Paste button
        paste_intent = new Intent(context, NotificationReceiver.class);
        paste_intent.setAction(Const.ACTION_UPDATE_SCRAPBOOK);
        paste_pending_intent = PendingIntent.getBroadcast(context,
                Const.REQUEST_CODE_SCRAPBOOK_PASTE,
                paste_intent,
                PendingIntent.FLAG_MUTABLE);

        paste_action = new NotificationCompat.Action.Builder(
                android.R.drawable.sym_action_chat, context.getResources().getString(R.string.scrapbook_write), paste_pending_intent)
                .addRemoteInput(remote_input)
                .setAllowGeneratedReplies(false)
                .build();

        builder.addAction(paste_action);

        // Goto button
        goto_intent = new Intent(context, MainActivity.class);
        goto_intent.setAction(Const.ACTION_VIEW_ENTRY);
        goto_intent.putExtra(Const.EXTRA_ID, entry.getId());
        goto_pending_intent = PendingIntent.getActivity(context,
                0,
                goto_intent,
                PendingIntent.FLAG_MUTABLE);

        goto_action = new NotificationCompat.Action.Builder(
                android.R.drawable.sym_action_chat, context.getResources().getString(R.string.scrapbook_goto), goto_pending_intent)
                .build();

        builder.addAction(goto_action);

        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Utils.makeNotificationChannel(manager, Const.SCRAPBOOK_CHANNEL_ID, Const.SCRAPBOOK_CHANNEL_NAME, Const.SCRAPBOOK_CHANNEL_DESC, Const.SCRAPBOOK_CHANNEL_LEVEL);
        manager.notify(Const.SCRAPBOOK_NOTIFICATION_ID, builder.build());
    }

    // Cancel scrapbook notification
    private void cancelScrapbookNotification(Context context, Intent intent) {
        int notification_id = intent.getIntExtra(Const.EXTRA_SCRAPBOOK_NOTIFICATION_ID, 0);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Clear existing notification
        manager.cancel(notification_id);
    }
}
