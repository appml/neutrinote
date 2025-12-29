package com.appmindlab.nano;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saelim on 8/5/2015.
 */
public class WidgetService extends RemoteViewsService {
    private DataSource mDatasource;
    private SharedPreferences mSharedPreferences;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    private class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private List<WidgetItem> mWidgetItems = new ArrayList<WidgetItem>();
        private Context mContext;
        private int mAppWidgetId;

        public StackRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            Log.d(Const.TAG, "nano - WidgetService: onCreate() ");

            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            update();
        }

        @Override
        public void onDestroy() {
            Log.d(Const.TAG, "nano - WidgetService: onDestroy() ");
            mDatasource.close();
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        public long getItemId(int position) {
            return mWidgetItems.get(position).id;
        }

        public boolean hasStableIds() {
            return true;
        }

        public void onDataSetChanged() {
            update();
        }

        public RemoteViews getViewAt(int position) {
            if (position < 0 || position > mWidgetItems.size()) {
                return null;
            }

            RemoteViews rv;
            rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);

            WidgetItem entry = mWidgetItems.get(position);
            rv.setTextViewText(R.id.widget_item_title, entry.title);
            rv.setTextViewText(R.id.widget_item_content, entry.content);

            // Next, we set an intent so that clicking on this view will result
            // in launching of the app with appropriate record
            Bundle extras = new Bundle();
            extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            extras.putLong(Const.EXTRA_ID, entry.id);

            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            fillInIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            rv.setOnClickFillInIntent(R.id.widget_item_title, fillInIntent);
            rv.setOnClickFillInIntent(R.id.widget_item_content, fillInIntent);
            return rv;
        }

        private void update() {
            try {
                DBEntry entry;
                WidgetItem item;
                String title, content;
                String preview_mode = mSharedPreferences.getString(Const.PREF_PREVIEW_MODE, Const.PREVIEW_AT_END);
                String order_by = mSharedPreferences.getString(Const.PREF_WIDGET_ORDER_BY, Const.SORT_BY_TITLE);
                String order_direction = mSharedPreferences.getString(Const.PREF_WIDGET_ORDER_BY_DIRECTION, Const.SORT_ASC);

                Log.d(Const.TAG, "nano - WidgetService: update() ");

                // Make sure the database is open
                resumeDatabase();

                mWidgetItems.clear();

                // Starred items by default
                List<DBEntry> results = mDatasource.getAllActiveStarredRecords(order_by, order_direction);

                // Fall back to default
                if ((results == null) || (results.isEmpty())) {
                    results = mDatasource.getAllActiveRecords(order_by, order_direction);
                }

                for (int i = 0; i < results.size(); i++) {
                    entry = results.get(i);
                    title = entry.getTitle();
                    content = entry.getContent();

                    // Skip the system files
                    if (title.equals(Utils.makeFileName(getApplicationContext(), Const.APP_DATA_FILE)) || title.equals(Utils.makeFileName(getApplicationContext(), Const.APP_SETTINGS_FILE)))
                        continue;

                    // Extract content
                    if (content.length() > Const.WIDGET_LEN) {
                        if (preview_mode.equals(Const.PREVIEW_AT_START)) {
                            content = content.substring(0, Const.WIDGET_LEN);
                            content = Utils.subStringWordBoundary(content, 0, Const.WIDGET_LEN-1);
                        }
                        else {
                            content = content.substring(content.length() - Const.WIDGET_LEN);
                            content = Utils.subStringWordBoundary(content, 1, Const.WIDGET_LEN);
                        }
                    }
                    else {
                        content = Utils.subStringWordBoundary(content, 0, content.length());
                    }

                    item =  new WidgetItem(entry.getId(), entry.getTitle(), content);
                    mWidgetItems.add(item);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                mDatasource.close();
            }
        }

        public int getCount() {
            return mWidgetItems.size();
        }

        // Setup database
        private void setupDatabase() {
            // Get all entries from the database
            mDatasource = new DataSource();
            mDatasource.open();
        }

        // Resume database
        private void resumeDatabase() {
            if ((mDatasource == null) || (!mDatasource.isOpen())) {
                setupDatabase();
            }
        }
    }
}
