package com.appmindlab.nano;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by saelim on 7/15/2016.
 */
public class AddNoteWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Get all ids
        ComponentName thisWidget = new ComponentName(context, AddNoteWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.add_note_widget_layout);

            // Create a pending intent
            Intent viewEntryIntent = new Intent(context, MainActivity.class);
            viewEntryIntent.setAction(Const.ACTION_VIEW_ENTRY);
            viewEntryIntent.putExtra(Const.EXTRA_ID, -1);
            viewEntryIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent viewEntryPendingIntent = PendingIntent.getActivity(context, 0, viewEntryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.add_new_widget_imageview, viewEntryPendingIntent);
            appWidgetManager.updateAppWidget(widgetId, rv);
        }
    }
}

