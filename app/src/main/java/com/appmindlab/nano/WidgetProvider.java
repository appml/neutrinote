package com.appmindlab.nano;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by saelim on 8/5/2015.
 */
public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Const.TAG, "nano - WidgetProvider: onReceive() ");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds;

        appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

        if (appWidgetIds.length > 0) {
            if (intent.getAction().equals(Const.ACTION_UPDATE_WIDGET)) {
                for (int i=0; i < appWidgetIds.length; ++i) {
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i], R.id.stack_view);
                }
            }

            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {
			/*
			 * Here we setup the intent which points to the StackViewService
			 * which will provide the views for this collection.
			 */
            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			/*
			 * When intents are compared, the extras are ignored, so we need to
			 * embed the extras into the data so that the extras will not be
			 * ignored.
			 */
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            rv.setRemoteAdapter(R.id.stack_view, intent);

			/*
			 * The empty view is displayed when the collection has no items. It
			 * should be a sibling of the collection view.
			 */
            rv.setEmptyView(R.id.stack_view, R.id.empty_view);

            /*
             * Click empty view to refresh
             */
            intent = new Intent(Const.ACTION_UPDATE_WIDGET);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            rv.setOnClickPendingIntent(R.id.empty_view, pendingIntent);

			/*
			 * Here we setup the a pending intent template. Individuals items of
			 * a collection cannot setup their own pending intents, instead, the
			 * collection as a whole can setup a pending intent template, and
			 * the individual items can set a fillInIntent to create unique
			 * before on an item to item basis.
			 */
            Intent viewEntryIntent = new Intent(context, MainActivity.class);
            viewEntryIntent.setAction(Const.ACTION_VIEW_ENTRY);
            viewEntryIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetIds[i]);

            viewEntryIntent.setData(Uri.parse("widgetid" + appWidgetIds[i]));
            PendingIntent viewEntryPendingIntent = PendingIntent.getActivity(
                    context, 0, viewEntryIntent, PendingIntent.FLAG_MUTABLE);
            rv.setPendingIntentTemplate(R.id.stack_view, viewEntryPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }
}

