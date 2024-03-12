package com.appmindlab.nano;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by saelim on 6/24/2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper mInstance;

    public static final String TABLE = "records";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";

    public static final String COLUMN_STAR = "star";
    public static final String COLUMN_DELETED = "deleted";

    public static final String COLUMN_CREATED = "created";
    public static final String COLUMN_MODIFIED = "modified";
    public static final String COLUMN_ACCESSED = "accessed";

    public static final String COLUMN_METADATA = "metadata";
    public static final String COLUMN_POS = "pos";
    public static final String COLUMN_PASSCODE = "passcode";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_DISTANCE = "distance";

    // Indices
    public static final String INDEX_TITLE = COLUMN_TITLE + "_idx";
    public static final String INDEX_METADATA = COLUMN_METADATA + "_idx";
    public static final String INDEX_STAR = COLUMN_STAR + "_idx";
    public static final String INDEX_DELETED = COLUMN_DELETED + "_idx";

    private static final String DATABASE_NAME = "app_db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_CONTENT + " text not null, "

            + COLUMN_STAR + " integer default 0, "
            + COLUMN_DELETED + " integer default 0, "

            + COLUMN_CREATED + " date, "
            + COLUMN_MODIFIED + " date default (datetime('now','localtime')), "
            + COLUMN_ACCESSED + " date, "

            + COLUMN_METADATA + " text, "
            + COLUMN_POS + " integer default -1, "
            + COLUMN_PASSCODE + " text, "
            + COLUMN_LATITUDE + " double, "
            + COLUMN_LONGITUDE + " double, "
            + COLUMN_DISTANCE + " double)";

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBHelper(context.getApplicationContext());
        }

        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d(Const.TAG, "nano - Initialize database");

        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to " + newVersion);

        // db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        // onCreate(db);

        if (oldVersion == 1) {
            db.execSQL("CREATE INDEX " + INDEX_TITLE + " ON " + TABLE + " (" + COLUMN_TITLE + ")");
            db.execSQL("CREATE INDEX " + INDEX_METADATA + " ON " + TABLE + " (" + COLUMN_METADATA + ")");
            db.execSQL("CREATE INDEX " + INDEX_STAR + " ON " + TABLE + " (" + COLUMN_STAR + ")");
            db.execSQL("CREATE INDEX " + INDEX_DELETED + " ON " + TABLE + " (" + COLUMN_DELETED + ")");
        }
    }
}