package com.appmindlab.nano;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DBEntryProvider extends ContentProvider {

    public static final Uri ENTRY_CONTENT_URI = Uri.parse("content://" + Const.PACKAGE + "/titles");

    private DBHelper mDBHelper;

    // URI Requests
    private static final int TITLES = 1;

    private static UriMatcher mUriMatcher = null;

    private static final String DATABASE_NAME = "app_db";
    private static final int DATABASE_VERSION = 2;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(Const.PACKAGE, DBHelper.COLUMN_TITLE, TITLES);
    }

    @Override
    public boolean onCreate() {
        try {
            Log.e(Const.TAG, "nano - DBEntryProvider: onCreate()");

            mDBHelper = DBHelper.getInstance(getContext());;
        }
        catch (Exception e) {
            Log.e(Const.TAG, "nano - DBEntryProvider: onCreate() error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        Cursor cursor = null;
        String order_by = sortOrder;

        int uri_type = mUriMatcher.match(uri);
        if (uri_type == TITLES) {
            builder.setTables(DBHelper.TABLE);
            builder.appendWhere(DBHelper.COLUMN_TITLE + " = " + uri.getPathSegments().get(1));

            if (TextUtils.isEmpty(sortOrder))
                order_by = Const.SORT_ASC;
        }

        cursor = builder.query(db, projection, selection, selectionArgs, null, null, order_by);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}

