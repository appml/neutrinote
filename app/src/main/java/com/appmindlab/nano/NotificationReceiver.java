package com.appmindlab.nano;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import java.util.ArrayList;

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
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            // Append to scrapbook
            doAppendScrapbook(context, intent);
        }
    }

    // Append to scrapbook file
    private void doAppendScrapbook(Context context, Intent intent) {
        Bundle bundle = RemoteInput.getResultsFromIntent(intent);
        String scrapbook_file = Utils.makeFileName(context, Const.SCRAPBOOK_TITLE);

        if (bundle != null) {
            String input = bundle.getCharSequence(Const.SCRAPBOOK_NOTIFICATION_KEY).toString();

            ArrayList<DBEntry> results = mDatasource.getRecordByTitle(scrapbook_file);
            if (results.size() == 1) {
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
        }

        // Reset notification
        resetScrapbookNotification(context, scrapbook_file);
    }

    // Reset scrapbook notification
    private void resetScrapbookNotification(Context context, String title) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Const.SCRAPBOOK_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_mode_edit_vector)
                .setOngoing(true)
                .setContentTitle(title);

        // Remote input
        RemoteInput remote_input = new RemoteInput.Builder(Const.SCRAPBOOK_NOTIFICATION_KEY)
                .setLabel(context.getResources().getString(R.string.hint_content))
                .build();

        // Pending intent
        Intent paste_intent = new Intent(context, NotificationReceiver.class);
        paste_intent.setAction(Const.ACTION_UPDATE_SCRAPBOOK);
        PendingIntent paste_pending_intent = PendingIntent.getBroadcast(context,
                Const.REQUEST_CODE_SCRAPBOOK_PASTE,
                paste_intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Paste button
        NotificationCompat.Action reply_action = new NotificationCompat.Action.Builder(
                android.R.drawable.sym_action_chat, context.getResources().getString(R.string.scrapbook_paste), paste_pending_intent)
                .addRemoteInput(remote_input)
                .setAllowGeneratedReplies(false)
                .build();

        builder.addAction(reply_action);

        // Create notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(Const.SCRAPBOOK_NOTIFICATION_ID, builder.build());
    }
}

