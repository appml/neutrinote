package com.appmindlab.nano;

/**
 * Created by saelim on 7/10/2015.
 */
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = SuggestionProvider.class.getName();
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String sel, String[] selArgs, String sortOrder) {

        // Retrieves a custom suggestion cursor and returns it
        Cursor cursor = super.query(uri, projection, sel, selArgs, sortOrder);

        Uri icon_uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + DBApplication.getAppContext().getPackageName() + "/drawable/ic_accessed_vector");
        MatrixCursor cursor_new = new MatrixCursor(cursor.getColumnNames());

        // Replace the icon
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            cursor_new.addRow(new Object[]{
                    cursor.getInt(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_FORMAT)),
                    icon_uri,
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY)),
                    cursor.getInt(cursor.getColumnIndex("_id"))
            });

            cursor.moveToNext();
        }

        return cursor_new;
    }

}
