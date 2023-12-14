package com.appmindlab.nano;

/**
 * Created by saelim on 6/24/2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataSource {

    // Database fields
    private SQLiteDatabase mDatabase;
    private DBHelper mDbHelper;
    private String mFilter = Const.HIDE_PATTERN;
    private String mOrderByPrefix = Const.NULL_SYM;

    private String[] allColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_TITLE,
            DBHelper.COLUMN_CONTENT,

            DBHelper.COLUMN_STAR,
            DBHelper.COLUMN_DELETED,

            DBHelper.COLUMN_CREATED,
            DBHelper.COLUMN_MODIFIED,
            DBHelper.COLUMN_ACCESSED,

            DBHelper.COLUMN_METADATA,
            DBHelper.COLUMN_POS,
            DBHelper.COLUMN_PASSCODE,
            DBHelper.COLUMN_LATITUDE,
            DBHelper.COLUMN_LONGITUDE,

            "LENGTH(" + DBHelper.COLUMN_CONTENT + ")"
    };

    // Without content column
    private String[] contentlessColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_TITLE,

            DBHelper.COLUMN_STAR,
            DBHelper.COLUMN_DELETED,

            DBHelper.COLUMN_CREATED,
            DBHelper.COLUMN_MODIFIED,
            DBHelper.COLUMN_ACCESSED,

            DBHelper.COLUMN_METADATA,
            DBHelper.COLUMN_POS,
            DBHelper.COLUMN_PASSCODE,
            DBHelper.COLUMN_LATITUDE,
            DBHelper.COLUMN_LONGITUDE,

            "LENGTH(" + DBHelper.COLUMN_CONTENT + ")"
    };

    // Columns with content preview
    private String[] previewColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_TITLE,
            "substr(" + DBHelper.COLUMN_CONTENT + ", -" + Const.PREVIEW_LEN + ")",

            DBHelper.COLUMN_STAR,
            DBHelper.COLUMN_DELETED,

            DBHelper.COLUMN_CREATED,
            DBHelper.COLUMN_MODIFIED,
            DBHelper.COLUMN_ACCESSED,

            DBHelper.COLUMN_METADATA,
            DBHelper.COLUMN_POS,
            DBHelper.COLUMN_PASSCODE,
            DBHelper.COLUMN_LATITUDE,
            DBHelper.COLUMN_LONGITUDE,

            "LENGTH(" + DBHelper.COLUMN_CONTENT + ")"
    };

    // ID and title
    private String[] minimalColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_TITLE
    };

    // ID, title, and modified
    private String[] basicColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_TITLE,
            DBHelper.COLUMN_MODIFIED,
            "LENGTH(" + DBHelper.COLUMN_CONTENT + ")"
    };

    // ID, title, modified, and metadata
    private String[] simpleColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_TITLE,
            DBHelper.COLUMN_MODIFIED,
            DBHelper.COLUMN_METADATA,
            "LENGTH(" + DBHelper.COLUMN_CONTENT + ")"
    };

    public DataSource() {
        mDbHelper = DBApplication.getDBHelper();
    }

    public void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    protected SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    // Count the number of records, must be called after open()
    public long count() {
        return DatabaseUtils.queryNumEntries(mDatabase, DBHelper.TABLE);
    }

    public boolean isOpen() {
        return (mDatabase.isOpen());
    }

    public void close() {
        if ((MainActivity.main_activity == null) && (DisplayDBEntry.display_dbentry == null))
            mDbHelper.close();
    }

    public boolean isNull() {
        return (mDbHelper==null);
    }

    // Return database size
    public long getSize() {
        String path = mDatabase.getPath();
        return new File(path).length();
    }

    // Get filter pattern
    protected String getFilter() {
        return mFilter;
    }

    // Set filter pattern
    protected void setFilter(String filter) {
        mFilter = filter;
    }

    // Get order by prefix
    protected String getOrderByPrefix() {
        if (mOrderByPrefix.equals(Const.NULL_SYM))
            return mOrderByPrefix;

        return mOrderByPrefix + ",";
    }

    // Set order by prefix
    protected void setOrderByPrefix(String orderByPrefix) { mOrderByPrefix = orderByPrefix; }

    // Get columns
    protected String[] getColumns(String mode) {
        String content_filter;

        // Determine filter for content column
        if (mode.equals(Const.PREVIEW_AT_START))
            content_filter =  "CASE WHEN LENGTH(" + DBHelper.COLUMN_CONTENT + ") > " + Const.CURSOR_SAFE_CONTENT_LEN + " THEN SUBSTR(" + DBHelper.COLUMN_CONTENT + ", 0, " + Const.SOFT_PREVIEW_LEN + ") ELSE " + DBHelper.COLUMN_CONTENT + " END";

        else if (mode.equals(Const.PREVIEW_AT_END))
            content_filter =  "CASE WHEN LENGTH(" + DBHelper.COLUMN_CONTENT + ") > " + Const.CURSOR_SAFE_CONTENT_LEN + " THEN SUBSTR(" + DBHelper.COLUMN_CONTENT + ", -" + Const.SOFT_PREVIEW_LEN + ") ELSE " + DBHelper.COLUMN_CONTENT + " END";

        else if (mode.equals(Const.PREVIEW_LAZY))
            content_filter =  "SUBSTR(" + DBHelper.COLUMN_CONTENT + ", -" + Const.SOFT_PREVIEW_LEN + ")";

        else if (mode.equals(Const.PREVIEW_OFF))
            content_filter = "''";

        else
            content_filter = DBHelper.COLUMN_CONTENT;

        String[] columns = {
                DBHelper.COLUMN_ID,
                DBHelper.COLUMN_TITLE,

                content_filter,

                DBHelper.COLUMN_STAR,
                DBHelper.COLUMN_DELETED,

                DBHelper.COLUMN_CREATED,
                DBHelper.COLUMN_MODIFIED,
                DBHelper.COLUMN_ACCESSED,

                DBHelper.COLUMN_METADATA,
                DBHelper.COLUMN_POS,
                DBHelper.COLUMN_PASSCODE,
                DBHelper.COLUMN_LATITUDE,
                DBHelper.COLUMN_LONGITUDE,

                "LENGTH(" + DBHelper.COLUMN_CONTENT + ")"
        };

        return columns;
    }

    // Get search columns
    protected String[] getSearchColumns(String mode) {
        String content_filter;

        // Determine filter for content column
        if (mode.equals(Const.PREVIEW_OFF))
            content_filter = "''";

        else if (mode.equals(Const.PREVIEW_LAZY))
            content_filter =  "SUBSTR(" + DBHelper.COLUMN_CONTENT + ", -" + Const.SOFT_PREVIEW_LEN + ")";

        else
            content_filter = DBHelper.COLUMN_CONTENT;

        String[] columns = {
                DBHelper.COLUMN_ID,
                DBHelper.COLUMN_TITLE,

                content_filter,

                DBHelper.COLUMN_STAR,
                DBHelper.COLUMN_DELETED,

                DBHelper.COLUMN_CREATED,
                DBHelper.COLUMN_MODIFIED,
                DBHelper.COLUMN_ACCESSED,

                DBHelper.COLUMN_METADATA,
                DBHelper.COLUMN_POS,
                DBHelper.COLUMN_PASSCODE,
                DBHelper.COLUMN_LATITUDE,
                DBHelper.COLUMN_LONGITUDE,

                "LENGTH(" + DBHelper.COLUMN_CONTENT + ")"
        };

        return columns;
    }

    public synchronized DBEntry createRecord(String title, String content, int star, Date modified, boolean sync) {
        ContentValues values = new ContentValues();
        Date now = new Date();

        values.put(DBHelper.COLUMN_TITLE, title);
        values.put(DBHelper.COLUMN_CONTENT, content);
        values.put(DBHelper.COLUMN_STAR, star);
        values.put(DBHelper.COLUMN_DELETED, 0);

        values.put(DBHelper.COLUMN_CREATED, now.getTime());

        if (modified == null)
            values.put(DBHelper.COLUMN_MODIFIED, now.getTime());
        else
            values.put(DBHelper.COLUMN_MODIFIED, modified.getTime());

        values.put(DBHelper.COLUMN_ACCESSED, now.getTime());

        long insertId = mDatabase.insertWithOnConflict(DBHelper.TABLE, null, values, Const.CONFLICT_POLICY);

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                allColumns, DBHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);

        cursor.moveToFirst();
        DBEntry entry = cursorToRecord(cursor);

        cursor.close();

        // Handle sync
        if (sync) {
            Utils.writeLocalRepoFile(DBApplication.getAppContext(), title, content);
        }

        return entry;
    }

    // Update record (title, content, and star)
    public synchronized long updateRecord(long id, String title, String content, int star, Date modified, boolean sync, String prevTitle) {
        ContentValues values = new ContentValues();
        Date now = new Date();
        long result;
        DBEntry entry;

        // Sanity check (use delete record instead emptying)
        if ((content == null) && (content.length() == 0)) return -1L;

        values.put(DBHelper.COLUMN_TITLE, title);
        values.put(DBHelper.COLUMN_CONTENT, content);
        values.put(DBHelper.COLUMN_STAR, star);
        values.put(DBHelper.COLUMN_DELETED, 0);

        if (modified == null)
            values.put(DBHelper.COLUMN_MODIFIED, now.getTime());
        else
            values.put(DBHelper.COLUMN_MODIFIED, modified.getTime());

        values.put(DBHelper.COLUMN_ACCESSED, now.getTime());

        result = mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);

        if (result > 0) {

            // Handle sync
            if (sync) {
                Utils.writeLocalRepoFile(DBApplication.getAppContext(), title, content);

                // Handle rename
                if (!title.toLowerCase(Locale.getDefault()).equals(prevTitle.toLowerCase(Locale.getDefault())))
                    Utils.deleteLocalRepoFile(DBApplication.getAppContext(), prevTitle);
            }

            return id;
        }
        else {
            // Otherwise create a new record anyway
            entry = createRecord(title, content, star, modified, sync);
            if (title != null)
                return entry.getId();
        }

        return -1L;
    }

    // Update record (all fields except content)
    public synchronized long updateRecord(long id, int star, long pos, String metadata, long accessed, long created, long modified, double latitude, double longitude) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_STAR, star);
        values.put(DBHelper.COLUMN_POS, pos);
        values.put(DBHelper.COLUMN_METADATA, metadata);

        values.put(DBHelper.COLUMN_ACCESSED, accessed);
        values.put(DBHelper.COLUMN_CREATED, created);
        values.put(DBHelper.COLUMN_MODIFIED, modified);

        values.put(DBHelper.COLUMN_LATITUDE, latitude);
        values.put(DBHelper.COLUMN_LONGITUDE, longitude);

        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update record (star and metadata only)
    public synchronized long updateRecord(long id, int star, String metadata) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_STAR, star);
        values.put(DBHelper.COLUMN_METADATA, metadata);

        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update content
    public synchronized long updateRecordContent(long id, String content, Date modified) {
        ContentValues values = new ContentValues();
        Date now = new Date();

        // Sanity check (use delete record instead emptying)
        if ((content == null) && (content.length() == 0)) return -1L;

        values.put(DBHelper.COLUMN_CONTENT, content);

        if (modified == null)
            values.put(DBHelper.COLUMN_MODIFIED, now.getTime());
        else
            values.put(DBHelper.COLUMN_MODIFIED, modified.getTime());

        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update the access time
    public synchronized long updateRecordAccessTime(long id) {
        ContentValues values = new ContentValues();
        Date now = new Date();

        values.put(DBHelper.COLUMN_ACCESSED, now.getTime());
        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update the access time
    public synchronized long updateRecordAccessTime(long id, long timestamp) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_ACCESSED, timestamp);
        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update the creation time
    public synchronized long updateRecordCreatedTime(long id) {
        ContentValues values = new ContentValues();
        Date now = new Date();

        values.put(DBHelper.COLUMN_CREATED, now.getTime());
        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[]{Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update the creation time
    public synchronized long updateRecordCreatedTime(long id, long timestamp) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_CREATED, timestamp);
        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update the modified time
    public synchronized long updateRecordModifiedTime(long id) {
        ContentValues values = new ContentValues();
        Date now = new Date();

        values.put(DBHelper.COLUMN_MODIFIED, now.getTime());
        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[]{Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update the modified time
    public synchronized long updateRecordModifiedTime(long id, long timestamp) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_MODIFIED, timestamp);
        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update distance
    public synchronized long updateRecordDistance(long id, double distance) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_DISTANCE, distance);
        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Toggle record star status
    public int toggleRecordStarStatus(long id) {
        ArrayList<DBEntry> results = getRecordById(id);

        if (results.size() == 1) {
            if (results.get(0).getStar() == 1) {
                updateRecordStarStatus(id, 0);
                return 0;
            }

            else {
                updateRecordStarStatus(id, 1);
                return 1;
            }
        }
        return 0;
    }

    // Update the metadata
    public synchronized long updateRecordMetadata(long id, String metadata) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_METADATA, metadata);
        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update record star status
    public synchronized long updateRecordStarStatus(long id, int star) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_STAR, star);
        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update record pos
    public synchronized long updateRecordPos(long id, long pos) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_POS, pos);
        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update the passcode
    public synchronized long updateRecordPasscode(long id, String passcode) {
        ContentValues values = new ContentValues();

        // Base64 encode the code
        try {
            passcode = Base64.encodeToString(passcode.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        values.put(DBHelper.COLUMN_PASSCODE, passcode);

        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Update the coordinates
    public synchronized long updateRecordCoordinates(long id, double latitude, double longitude) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_LATITUDE, latitude);
        values.put(DBHelper.COLUMN_LONGITUDE, longitude);

        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[] {Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Mark a record as deleted by ID
    public synchronized void markRecordDeletedById(long id, int deleted) {
        // Just mark the record as deleted
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_DELETED, deleted);

        mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " = ?",
                new String[]{Long.toString(id)},
                Const.CONFLICT_POLICY);
    }

    // Delete a record
    public synchronized void deleteRecord(DBEntry entry) {
        long id = entry.getId();
        mDatabase.delete(DBHelper.TABLE,
                DBHelper.COLUMN_ID + " = " + id,
                null);
    }

    // Delete a record by ID
    public synchronized void deleteRecordById(long id) {
        mDatabase.delete(DBHelper.TABLE,
                DBHelper.COLUMN_ID + " = " + id,
                null);
    }

    // Delete all records
    public synchronized void deleteAllRecords() {
        mDatabase.delete(DBHelper.TABLE,
                null,
                null);
    }

    // Clear the coordinates for all records
    public long clearAllCoordinates() {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_LATITUDE, -1);
        values.put(DBHelper.COLUMN_LONGITUDE, -1);

        return mDatabase.updateWithOnConflict(DBHelper.TABLE,
                values,
                DBHelper.COLUMN_ID + " > ?",
                new String[] {Long.toString(0)},
                Const.CONFLICT_POLICY);
    }

    // Get all records
    protected ArrayList<DBEntry> getAllRecords(String orderBy, String orderDirection) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();
        DBEntry entry;

        Cursor cursor;

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        cursor = mDatabase.query(DBHelper.TABLE,
                allColumns,
                Const.EXCLUDE_LARGE_FILES,
                null,
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                entry = cursorToRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get all basic records
    protected ArrayList<DBEntry> getAllBasicRecords(String orderBy, String orderDirection) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();
        DBEntry entry;

        Cursor cursor;

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        cursor = mDatabase.query(DBHelper.TABLE,
                basicColumns,
                Const.EXCLUDE_LARGE_FILES,
                null,
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                entry = cursorToBasicRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get all simple records
    protected ArrayList<DBEntry> getAllSimpleRecords(String orderBy, String orderDirection) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();
        DBEntry entry;

        Cursor cursor;

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        cursor = mDatabase.query(DBHelper.TABLE,
                simpleColumns,
                Const.EXCLUDE_LARGE_FILES,
                null,
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                entry = cursorToSimpleRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get cursor for all records
    protected Cursor getAllRecordsCursor(String orderBy, String orderDirection) {
        DBEntry entry;

        Cursor cursor;

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        cursor = mDatabase.query(DBHelper.TABLE,
                allColumns,
                Const.EXCLUDE_LARGE_FILES,
                null,
                null,
                null,
                orderBy + " " + orderDirection);

        cursor.moveToFirst();
        return cursor;
    }

    // Get all active records regardless of hidden filter
    public ArrayList<DBEntry> getAllActiveRecords(String orderBy, String orderDirection) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                allColumns,
                DBHelper.COLUMN_DELETED + " = 0" + " AND " + Const.EXCLUDE_LARGE_FILES,
                null,
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get all active records without content regardless of hidden filter
    public ArrayList<DBEntry> getAllActiveContentlessRecords(String orderBy, String orderDirection) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                contentlessColumns,
                DBHelper.COLUMN_DELETED + " = 0" + " AND " + Const.EXCLUDE_LARGE_FILES,
                null,
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToContentlessRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get all active starred records
    public ArrayList<DBEntry> getAllActiveStarredRecords(String orderBy, String orderDirection) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                allColumns,
                DBHelper.COLUMN_STAR + " = 1 AND " + DBHelper.COLUMN_DELETED + " = 0" + " AND " + Const.EXCLUDE_LARGE_FILES,
                null,
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get all active records by last modified date
    public Cursor getAllActiveRecordsByLastModifiedCursor(String orderBy, String orderDirection, long filter, String op, String mode) {
        String[] columns = getColumns(mode);

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                columns,
                "( " + DBHelper.COLUMN_MODIFIED + op + filter + ") AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                new String[]{getFilter()},
                null,
                null,
                orderBy + " " + orderDirection);

        cursor.moveToFirst();
        return cursor;
    }

    // Get all active records by last accessed date
    public Cursor getAllActiveRecordsByLastAccessedCursor(String orderBy, String orderDirection, long filter, String op, String mode) {
        String[] columns = getColumns(mode);

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                columns,
                "( " + DBHelper.COLUMN_ACCESSED + op + filter + ") AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                new String[]{getFilter()},
                null,
                null,
                orderBy + " " + orderDirection);

        cursor.moveToFirst();
        return cursor;
    }

    // Get all active records modified near by
    public Cursor getAllActiveRecordsModifiedNearbyCursor(String orderBy, String orderDirection, double distance, String mode) {
        String[] columns = getColumns(mode);

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                columns,
                "(" + DBHelper.COLUMN_DISTANCE + " < " + distance + ") AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                new String[]{getFilter()},
                null,
                null,
                orderBy + " " + orderDirection);

        cursor.moveToFirst();
        return cursor;
    }

    // Get all active records by metadata match
    public Cursor getAllActiveRecordsByMetadataCursor(String criteria, String orderBy, String orderDirection, String mode) {
        Cursor cursor;
        String[] columns = getColumns(mode);

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        // Clean up
        criteria = criteria.trim();

        if (criteria.length() > 0)
            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    "(" + DBHelper.COLUMN_METADATA + " LIKE ?) AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{"%" + criteria + "%", getFilter()},
                    null,
                    null,
                    orderBy + " " + orderDirection);
        else
            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    "(coalesce(" + DBHelper.COLUMN_METADATA + ", '') = '') AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{getFilter()},
                    null,
                    null,
                    orderBy + " " + orderDirection);

        cursor.moveToFirst();
        return cursor;
    }

    // Get all active records by metadata match
    public Cursor getAllActiveRecordsByMetadataRegCursor(String criteria, String orderBy, String orderDirection, String mode) {
        Cursor cursor;
        String[] columns = getColumns(mode);

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        // Clean up
        criteria = criteria.trim();

        if (criteria.length() > 0)
            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    "(" + DBHelper.COLUMN_METADATA + " GLOB ?) AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{criteria, getFilter()},
                    null,
                    null,
                    orderBy + " " + orderDirection);

        else
            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    "(coalesce(" + DBHelper.COLUMN_METADATA + ", '') = '') AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{getFilter()},
                    null,
                    null,
                    orderBy + " " + orderDirection);

        cursor.moveToFirst();
        return cursor;
    }


    // Get all deleted records
    public ArrayList<DBEntry> getAllDeletedRecords(String orderBy, String orderDirection) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                basicColumns,
                DBHelper.COLUMN_DELETED + " = ?",
                new String[]{Integer.toString(1)},
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToBasicRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get cursor for all active records
    public Cursor getAllActiveRecordsCursor(String orderBy, String orderDirection, String mode) {
        String[] columns = getColumns(mode);

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                columns,
                "(" + DBHelper.COLUMN_DELETED + " = ?) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                new String[]{Integer.toString(0), getFilter()},
                null,
                null,
                orderBy + " " + orderDirection);

        cursor.moveToFirst();
        return cursor;
    }

    // Get record by id
    public ArrayList<DBEntry> getRecordById(long id) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                allColumns,
                DBHelper.COLUMN_ID + " = ?",
                new String[]{Long.toString(id)},
                null,
                null,
                null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get record by title
    public ArrayList<DBEntry> getRecordByTitle(String title) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                allColumns,
                DBHelper.COLUMN_TITLE + " = ? COLLATE NOCASE",
                new String[]{title},
                null,
                null,
                null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get record by title
    public ArrayList<DBEntry> getRecordByTitle(String title, String orderBy, String orderDirection) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                allColumns,
                DBHelper.COLUMN_TITLE + " = ? COLLATE NOCASE",
                new String[]{title},
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get basic record by title
    public ArrayList<DBEntry> getBasicRecordByTitle(String title) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                basicColumns,
                DBHelper.COLUMN_TITLE + " = ? COLLATE NOCASE",
                new String[]{title},
                null,
                null,
                null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToBasicRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get simple record by title
    public ArrayList<DBEntry> getSimpleRecordByTitle(String title, String orderBy, String orderDirection) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                simpleColumns,
                DBHelper.COLUMN_TITLE + " = ? COLLATE NOCASE",
                new String[]{title},
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToSimpleRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get content less record by title
    public ArrayList<DBEntry> getContentlessRecordByTitle(String title) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                contentlessColumns,
                DBHelper.COLUMN_TITLE + " = ? COLLATE NOCASE",
                new String[]{title},
                null,
                null,
                null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToContentlessRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get title for the next new note
    public String getNextNewNoteTitle(Context context) {
        String title = Utils.makeNextFileName(context, Utils.getNewNoteTitleTemplate(context));
        int count, next = 0;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                contentlessColumns,
                DBHelper.COLUMN_TITLE + " like ? COLLATE NOCASE AND " + DBHelper.COLUMN_DELETED + " = 0",
                new String[]{title},
                null,
                null,
                DBHelper.COLUMN_CREATED + " DESC");

        try {
            count = cursor.getCount();

            if (count > 0) {
                cursor.moveToFirst();
                DBEntry entry = cursorToContentlessRecord(cursor);
                String temp = entry.getTitle();
                next = Integer.parseInt(temp.substring(temp.indexOf("(")+1, temp.indexOf(")")));
            }

            title = title.replace("%", Integer.toString(Math.max(count, next)+1));
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return title;
    }

    // Get record with content preview by title
    public ArrayList<DBEntry> getRecordPreviewByTitle(String title) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                previewColumns,
                DBHelper.COLUMN_TITLE + " = ? COLLATE NOCASE",
                new String[]{title},
                null,
                null,
                null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Search records
    protected Cursor searchRecordsCursor(String criteria, String orderBy, String orderDirection, String mode) {
        Cursor cursor;
        String[] columns = getColumns(mode);

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        if (criteria.startsWith(Const.TITLEONLY)) {
            criteria = Utils.cleanCriteria(criteria);

            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    "(" + DBHelper.COLUMN_TITLE + " LIKE ?) AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{"%" + criteria + "%", getFilter()},
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.TITLEREGONLY)) {
            criteria = Utils.cleanCriteria(criteria);

            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    "(" + DBHelper.COLUMN_TITLE + " GLOB ?) AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{criteria, getFilter()},
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.METADATAONLY)) {
            criteria = Utils.cleanCriteria(criteria);

            if (criteria.length() > 0)
                cursor = mDatabase.query(DBHelper.TABLE,
                        columns,
                        "(" + DBHelper.COLUMN_METADATA + " LIKE ?) AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                        new String[]{"%" + criteria + "%", getFilter()},
                        null,
                        null,
                        orderBy + " " + orderDirection);

            else
                cursor = mDatabase.query(DBHelper.TABLE,
                        columns,
                        "(coalesce(" + DBHelper.COLUMN_METADATA + ", '') = '') AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                        new String[]{getFilter()},
                        null,
                        null,
                        orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.METADATAREGONLY)) {
            criteria = Utils.cleanCriteria(criteria);

            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    "(" + DBHelper.COLUMN_METADATA + " GLOB ?) AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{criteria, getFilter()},
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.SIMILARQUERY)) {
            criteria = Utils.cleanCriteria(criteria);

            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    "(" + DBHelper.COLUMN_METADATA + " LIKE ?) AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{"%" + criteria + "%", getFilter()},
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.ANDQUERY)) {
            criteria = Utils.cleanCriteria(criteria);

            String[] parts = criteria.split(",");
            String temp1 = "", temp2 = "", temp3 = "", criteria_temp;
            String qry;

            // Build template
            for (int i=0; i < parts.length; i++) {

                if (i == parts.length-1) {
                    temp1 = temp1 + DBHelper.COLUMN_CONTENT + " LIKE ? ";
                    temp2 = temp2 + DBHelper.COLUMN_TITLE + " LIKE ? ";
                    temp3 = temp3 + DBHelper.COLUMN_METADATA + " LIKE ? ";
                }

                else {
                    temp1 = temp1 + DBHelper.COLUMN_CONTENT + " LIKE ? AND ";
                    temp2 = temp2 + DBHelper.COLUMN_TITLE + " LIKE ? AND ";
                    temp3 = temp3 + DBHelper.COLUMN_METADATA + " LIKE ? AND ";
                }
            }

            // Query template
            qry = "((" + temp1 + ") OR (" + temp2 + ") OR (" + temp3 + ")) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES;

            // Criteria
            criteria_temp = criteria + "," + criteria + "," + criteria + "," + getFilter();
            parts = criteria_temp.split(",");

            // Build criteria
            for (int i=0; i < parts.length; i++) {
                parts[i] = '%' + parts[i].trim() + '%';
            }

            cursor = mDatabase.query(DBHelper.TABLE,
                    getSearchColumns(mode),
                    qry,
                    parts,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.ORQUERY)) {
            criteria = Utils.cleanCriteria(criteria);

            String[] parts = criteria.split(",");
            String temp1 = "", temp2 = "", temp3 = "", criteria_temp;
            String qry;

            // Build template
            for (int i=0; i < parts.length; i++) {

                if (i == parts.length-1) {
                    temp1 = temp1 + DBHelper.COLUMN_CONTENT + " LIKE ? ";
                    temp2 = temp2 + DBHelper.COLUMN_TITLE + " LIKE ? ";
                    temp3 = temp3 + DBHelper.COLUMN_METADATA + " LIKE ? ";
                }

                else {
                    temp1 = temp1 + DBHelper.COLUMN_CONTENT + " LIKE ? OR ";
                    temp2 = temp2 + DBHelper.COLUMN_TITLE + " LIKE ? OR ";
                    temp3 = temp3 + DBHelper.COLUMN_METADATA + " LIKE ? OR ";
                }
            }

            // Query template
            qry = "((" + temp1 + ") OR (" + temp2 + ") OR (" + temp3 + ")) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES;

            // Criteria
            criteria_temp = criteria + "," + criteria + "," + criteria + "," + getFilter();
            parts = criteria_temp.split(",");

            // Build criteria
            for (int i=0; i < parts.length; i++) {
                parts[i] = '%' + parts[i].trim() + '%';
            }

            cursor = mDatabase.query(DBHelper.TABLE,
                    getSearchColumns(mode),
                    qry,
                    parts,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.RELATEDQUERY)) {
            criteria = Utils.cleanCriteria(criteria);
            String[] parts = criteria.replace(" ", ",").split(",");
            String qry = "", criteria_temp;

            // Build template
            for (int i=0; i < parts.length; i++) {

                if (i == parts.length-1) {
                    qry = qry + DBHelper.COLUMN_METADATA + " LIKE ? ";
                }

                else {
                    qry = qry + DBHelper.COLUMN_METADATA + " LIKE ? OR ";
                }
            }

            qry = "(" + qry + ") AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES;
            criteria_temp = criteria.replace(" ", ",") + "," + getFilter();
            parts = criteria_temp.split(",");

            // Build criteria
            for (int i=0; i < parts.length; i++) {
                parts[i] = parts[i].trim() + '%';
            }

            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    qry,
                    parts,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.TAGALLQUERY)) {
            criteria = Utils.cleanCriteria(criteria);
            String[] parts = criteria.split(",");;
            String qry = "", criteria_temp;

            // Build template
            for (int i=0; i < parts.length; i++) {

                if (i == parts.length-1) {
                    qry = qry + DBHelper.COLUMN_METADATA + " LIKE ? ";
                }

                else {
                    qry = qry + DBHelper.COLUMN_METADATA + " LIKE ? AND ";
                }
            }

            qry = "(" + qry + ") AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES;
            criteria_temp = criteria + "," + getFilter();
            parts = criteria_temp.split(",");

            // Build criteria
            for (int i=0; i < parts.length; i++) {
                parts[i] = '%' + parts[i].trim() + '%';
            }

            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    qry,
                    parts,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.TAGANYQUERY)) {
            criteria = Utils.cleanCriteria(criteria);
            String[] parts = criteria.split(",");
            String qry = "", criteria_temp;

            // Build template
            for (int i=0; i < parts.length; i++) {

                if (i == parts.length-1) {
                    qry = qry + DBHelper.COLUMN_METADATA + " LIKE ? ";
                }

                else {
                    qry = qry + DBHelper.COLUMN_METADATA + " LIKE ? OR ";
                }
            }

            qry = "(" + qry + ") AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES;
            criteria_temp = criteria + "," + getFilter();
            parts = criteria_temp.split(",");

            // Build criteria
            for (int i=0; i < parts.length; i++) {
                parts[i] = '%' + parts[i].trim() + '%';
            }

            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    qry,
                    parts,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.ANDGQUERY)) {
            criteria = Utils.cleanCriteria(criteria);

            String[] parts = criteria.split(",");
            String temp1 = "", temp2 = "", temp3 = "", criteria_temp;
            String qry;

            // Build template
            for (int i=0; i < parts.length; i++) {

                if (i == parts.length-1) {
                    temp1 = temp1 + DBHelper.COLUMN_CONTENT + " GLOB ? ";
                    temp2 = temp2 + DBHelper.COLUMN_TITLE + " GLOB ? ";
                    temp3 = temp3 + DBHelper.COLUMN_METADATA + " GLOB ? ";
                }

                else {
                    temp1 = temp1 + DBHelper.COLUMN_CONTENT + " GLOB ? AND ";
                    temp2 = temp2 + DBHelper.COLUMN_TITLE + " GLOB ? AND ";
                    temp3 = temp3 + DBHelper.COLUMN_METADATA + " GLOB ? AND ";
                }
            }

            // Query template
            qry = "((" + temp1 + ") OR (" + temp2 + ") OR (" + temp3 + ")) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES;

            // Criteria
            criteria_temp = criteria + "," + criteria + "," + criteria + "," + getFilter();
            parts = criteria_temp.split(",");

            // Build criteria (except hidden filter)
            for (int i=0; i < parts.length-1; i++) {
                parts[i] = '*' + parts[i].trim() + '*';
            }

            cursor = mDatabase.query(DBHelper.TABLE,
                    getSearchColumns(mode),
                    qry,
                    parts,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.ORGQUERY)) {
            criteria = Utils.cleanCriteria(criteria);

            String[] parts = criteria.split(",");
            String temp1 = "", temp2 = "", temp3 = "", criteria_temp;
            String qry;

            // Build template
            for (int i=0; i < parts.length; i++) {

                if (i == parts.length-1) {
                    temp1 = temp1 + DBHelper.COLUMN_CONTENT + " GLOB ? ";
                    temp2 = temp2 + DBHelper.COLUMN_TITLE + " GLOB ? ";
                    temp3 = temp3 + DBHelper.COLUMN_METADATA + " GLOB ? ";
                }

                else {
                    temp1 = temp1 + DBHelper.COLUMN_CONTENT + " GLOB ? OR ";
                    temp2 = temp2 + DBHelper.COLUMN_TITLE + " GLOB ? OR ";
                    temp3 = temp3 + DBHelper.COLUMN_METADATA + " GLOB ? OR ";
                }
            }

            // Query template
            qry = "((" + temp1 + ") OR (" + temp2 + ") OR (" + temp3 + ")) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES;

            // Criteria
            criteria_temp = criteria + "," + criteria + "," + criteria + "," + getFilter();
            parts = criteria_temp.split(",");

            // Build criteria (except hidden filter)
            for (int i=0; i < parts.length-1; i++) {
                parts[i] = '*' + parts[i].trim() + '*';
            }

            cursor = mDatabase.query(DBHelper.TABLE,
                    getSearchColumns(mode),
                    qry,
                    parts,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.JOINQUERY)) {
            criteria = Utils.cleanCriteria(criteria);

            String[] parts = criteria.split(",");
            String temp1 = "", temp2 = "", temp3 = "", criteria_temp;
            String qry;

            // Build template
            for (int i=0; i < parts.length; i++) {

                if (i == parts.length-1) {
                    temp1 = temp1 + DBHelper.COLUMN_CONTENT + " LIKE ? ";
                    temp2 = temp2 + DBHelper.COLUMN_TITLE + " LIKE ? ";
                    temp3 = temp3 + DBHelper.COLUMN_METADATA + " LIKE ? ";
                }

                else {
                    temp1 = temp1 + DBHelper.COLUMN_CONTENT + " LIKE ? OR ";
                    temp2 = temp2 + DBHelper.COLUMN_TITLE + " LIKE ? OR ";
                    temp3 = temp3 + DBHelper.COLUMN_METADATA + " LIKE ? OR ";
                }
            }

            // Query template
            qry = "(((" + temp1 + ") OR (" + temp2 + ")) AND (" + temp3 + ")) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES;

            // Criteria
            criteria_temp = criteria + "," + criteria + "," + criteria + "," + getFilter();
            parts = criteria_temp.split(",");

            // Build criteria
            for (int i=0; i < parts.length; i++) {
                parts[i] = '%' + parts[i].trim() + '%';
            }

            cursor = mDatabase.query(DBHelper.TABLE,
                    getSearchColumns(mode),
                    qry,
                    parts,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.INQUERY)) {
            criteria = Utils.cleanCriteria(criteria);

            String[] parts = criteria.split(",");
            String temp = "";
            String qry;

            // Build template
            if (parts.length == 1) {  // Only search the title
                qry = "(" + DBHelper.COLUMN_TITLE + " LIKE ?)";
                parts[0] = "%" + parts[0].trim() + "%";
            }

            else {
                for (int i=1; i < parts.length; i++) {  // The first must match title
                    if (i == parts.length-1) {
                        temp = temp + DBHelper.COLUMN_CONTENT + " LIKE ? ";
                    }
                    else {
                        temp = temp + DBHelper.COLUMN_CONTENT + " LIKE ? OR ";
                    }
                }

                // Query template
                qry = "(" + DBHelper.COLUMN_TITLE + " LIKE ?) AND (" + temp + ")";

                // Build criteria
                parts[0] = "%" + parts[0].trim() + "%";
                for (int i=1; i < parts.length; i++) {
                    parts[i] = '%' + parts[i].trim() + '%';
                }
            }

            cursor = mDatabase.query(DBHelper.TABLE,
                    getSearchColumns(mode),
                    qry,
                    parts,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.startsWith(Const.SCOPEQUERY)) {
            criteria = Utils.cleanCriteria(criteria);

            String[] parts = criteria.split(",");
            String temp = "";
            String qry;

            // Build template
            if (parts.length == 1) {  // Only search the metadata
                qry = "(" + DBHelper.COLUMN_METADATA + " LIKE ?)";
                parts[0] = "%" + parts[0].trim() + "%";
            }

            else {
                for (int i=1; i < parts.length; i++) {  // The first must match metadata
                    if (i == parts.length-1) {
                        temp = temp + DBHelper.COLUMN_CONTENT + " LIKE ? ";
                    }
                    else {
                        temp = temp + DBHelper.COLUMN_CONTENT + " LIKE ? OR ";
                    }
                }

                // Query template
                qry = "(" + DBHelper.COLUMN_METADATA + " LIKE ?) AND (" + temp + ")";

                // Build criteria
                parts[0] = "%" + parts[0].trim() + "%";
                for (int i=1; i < parts.length; i++) {
                    parts[i] = '%' + parts[i].trim() + '%';
                }
            }

            cursor = mDatabase.query(DBHelper.TABLE,
                    getSearchColumns(mode),
                    qry,
                    parts,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.equals(Const.STARRED_SYM)) {
            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    "(" + DBHelper.COLUMN_STAR + " = 1) AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{getFilter()},
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.equals(Const.NUM_SYM)) {
            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    DBHelper.COLUMN_TITLE + " GLOB '[0-9]*' AND (" + DBHelper.COLUMN_DELETED + " = 0)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    null,
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if ((criteria.length() == 1) && (Character.isLetter(criteria.charAt(0)))) {
            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    DBHelper.COLUMN_TITLE + " LIKE ? AND (" + DBHelper.COLUMN_DELETED + " = 0)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{criteria + "%"},
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else if (criteria.equals((Const.ALL_SYM))) {
            cursor = mDatabase.query(DBHelper.TABLE,
                    columns,
                    "(" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{getFilter()},
                    null,
                    null,
                    orderBy + " " + orderDirection);
        }
        else {
            cursor = mDatabase.query(DBHelper.TABLE,
                    getSearchColumns(mode),
                    "(" + DBHelper.COLUMN_TITLE + " LIKE ? OR " + DBHelper.COLUMN_CONTENT + " GLOB ? OR " + DBHelper.COLUMN_CONTENT + " LIKE ? COLLATE NOCASE OR " + DBHelper.COLUMN_METADATA + " LIKE ?) AND (" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ? )" + " AND " + Const.EXCLUDE_LARGE_FILES,
                    new String[]{"%" + criteria + "%", criteria, "%" + criteria + "%", "%" + criteria + "%", getFilter()},

                    null,
                    null,
                    orderBy + " " + orderDirection);
        }

        cursor.moveToFirst();
        return cursor;
    }

    // Get titles
    public String[] getAllActiveRecordsTitles(String orderBy, String orderDirection) {
        List<String> results = new ArrayList<String>();
        String[] titles;
        String title;

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                new String[] {DBHelper.COLUMN_TITLE},
                "(" + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " NOT LIKE ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                new String[] {getFilter()},
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                title = cursor.getString(0).trim();

                if (title.length() > 0) {
                    if (!(results.contains(title)))
                        results.add(title);
                }
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        // Sort the list
        Collections.sort(results);
        titles = new String[results.size()];
        results.toArray(titles);
        return titles;
    }

    // Get IDs
    // Note: include hidden files
    public ArrayList<Long> getAllActiveRecordsIDs(String orderBy, String orderDirection) {
        ArrayList<Long> results = new ArrayList<>();
        long id;

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                new String[] {DBHelper.COLUMN_ID},
                "(" + DBHelper.COLUMN_DELETED + " = 0) AND " + Const.EXCLUDE_LARGE_FILES,
                null,
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                id = cursor.getLong(0);

                if (!(results.contains(id)))
                    results.add(id);

                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get IDs based on last modified time
    // Note: include hidden files
    public ArrayList<Long> getAllActiveRecordsIDsByLastModified(String orderBy, String orderDirection, long filter, String op) {
        ArrayList<Long> results = new ArrayList<>();
        long id;

        // Apply order by prefix
        orderBy = getOrderByPrefix() + orderBy;

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                new String[] {DBHelper.COLUMN_ID},
                "( " + DBHelper.COLUMN_MODIFIED + op + filter + ") AND (" + DBHelper.COLUMN_DELETED + " = 0) AND " + Const.EXCLUDE_LARGE_FILES,
                null,
                null,
                null,
                orderBy + " " + orderDirection);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                id = cursor.getLong(0);

                if (!(results.contains(id)))
                    results.add(id);

                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get working set of a specific size
    public ArrayList<DBEntry> getWorkingSet(int limit) {
        ArrayList<DBEntry> results = new ArrayList<DBEntry>();

        Cursor cursor = mDatabase.query(DBHelper.TABLE,
                minimalColumns,
                "( " + DBHelper.COLUMN_DELETED + " = 0) AND (" + DBHelper.COLUMN_TITLE + " GLOB ?)" + " AND " + Const.EXCLUDE_LARGE_FILES,
                new String[]{"[^" + getFilter() + "]*" },
                null,
                null,
                DBHelper.COLUMN_ACCESSED + " " + Const.SORT_DESC,
                String.valueOf(limit));

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBEntry entry = cursorToMinimalRecord(cursor);
                results.add(entry);
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        return results;
    }

    // Get unique metadata
    public String[] getUniqueMetadata() {
        List<String> results = new ArrayList<String>();
        String[] items;
        String[] tags;
        String metadata;

        Cursor cursor = mDatabase.query(true,
                DBHelper.TABLE,
                new String[] {DBHelper.COLUMN_METADATA},
                DBHelper.COLUMN_METADATA + " IS NOT NULL",
                null,
                null,
                null,
                null,
                null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                metadata = cursor.getString(0).trim();

                if (metadata.length() > 0) {
                    if (!(results.contains(metadata)))
                        results.add(metadata);

                    // Add each piece
                    items = metadata.split(" ");
                    for (int i=0; i < items.length; i++) {
                        if (!(results.contains(items[i])))
                            results.add(items[i].trim());
                    }
                }
                cursor.moveToNext();
            }
        }
        finally {
            // Make sure to close the cursor
            cursor.close();
        }

        // Sort the list
        Collections.sort(results);
        tags = new String[results.size()];
        results.toArray(tags);
        return tags;
    }

    // Get cursor into record
    protected DBEntry cursorToRecord(Cursor cursor) {
        DBEntry entry = new DBEntry();
        String content, metadata, passcode;

        entry.setId(cursor.getLong(0));

        entry.setTitle(cursor.getString(1));
        content = cursor.getString(2);
        entry.setContent(content);

        entry.setStar(cursor.getInt(3));
        entry.setDeleted(cursor.getInt(4));

        entry.setCreated(new Date(cursor.getLong(5)));
        entry.setModified(new Date(cursor.getLong(6)));
        entry.setAccessed(new Date(cursor.getLong(7)));

        metadata = cursor.getString(8);
        if (metadata == null)
            metadata = "";
        entry.setMetadata(metadata);

        entry.setPos(cursor.getLong(9));

        passcode = cursor.getString(10);
        // Base64 decode
        if ((passcode != null) && (passcode.length() > 0)) {
            byte[] temp = Base64.decode(passcode, Base64.DEFAULT);
            try {
                passcode = new String(temp, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        entry.setPasscode(passcode);

        entry.setLatitude(cursor.getFloat(11));
        entry.setLongitude(cursor.getFloat(12));

        entry.setSize(cursor.getLong(13));

        return entry;
    }

    // Get cursor into contentless record
    protected DBEntry cursorToContentlessRecord(Cursor cursor) {
        DBEntry entry = new DBEntry();
        String metadata, passcode;

        entry.setId(cursor.getLong(0));

        entry.setTitle(cursor.getString(1));

        entry.setStar(cursor.getInt(2));
        entry.setDeleted(cursor.getInt(3));

        entry.setCreated(new Date(cursor.getLong(4)));
        entry.setModified(new Date(cursor.getLong(5)));
        entry.setAccessed(new Date(cursor.getLong(6)));

        metadata = cursor.getString(7);
        if (metadata == null)
            metadata = "";
        entry.setMetadata(metadata);

        entry.setPos(cursor.getLong(8));

        passcode = cursor.getString(9);
        // Base64 decode
        if ((passcode != null) && (passcode.length() > 0)) {
            byte[] temp = Base64.decode(passcode, Base64.DEFAULT);
            try {
                passcode = new String(temp, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        entry.setPasscode(passcode);

        entry.setLatitude(cursor.getFloat(10));
        entry.setLongitude(cursor.getFloat(11));

        entry.setSize(cursor.getLong(12));

        return entry;
    }

    // Get cursor into minimal record
    protected DBEntry cursorToMinimalRecord(Cursor cursor) {
        DBEntry entry = new DBEntry();

        entry.setId(cursor.getLong(0));
        entry.setTitle(cursor.getString(1));

        return entry;
    }

    // Get cursor into basic record
    protected DBEntry cursorToBasicRecord(Cursor cursor) {
        DBEntry entry = new DBEntry();

        entry.setId(cursor.getLong(0));
        entry.setTitle(cursor.getString(1));
        entry.setModified(new Date(cursor.getLong(2)));

        entry.setSize(cursor.getLong(3));

        return entry;
    }

    // Get cursor into simple record
    protected DBEntry cursorToSimpleRecord(Cursor cursor) {
        DBEntry entry = new DBEntry();
        String metadata;

        entry.setId(cursor.getLong(0));
        entry.setTitle(cursor.getString(1));
        entry.setModified(new Date(cursor.getLong(2)));

        metadata = cursor.getString(3);
        if (metadata == null)
            metadata = "";
        entry.setMetadata(metadata);

        entry.setSize(cursor.getLong(4));

        return entry;
    }
}

