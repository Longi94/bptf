package com.tlongdev.bktf.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;

public class DatabaseProvider extends ContentProvider {

    /**
     * URI matcher result codes
     */
    public static final int PRICE_LIST = 100;
    public static final int ITEM_SCHEMA = 101;

    /**
     * The URI Matcher used by this content provider
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /**
     * The database helper.
     */
    private DatabaseHelper mOpenHelper;

    /**
     * Builds an URI matcher to match the given URIs
     *
     * @return the URI matcher
     */
    private static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DatabaseContract.PATH_PRICE_LIST, PRICE_LIST);
        matcher.addURI(authority, DatabaseContract.PATH_ITEM_SCHEMA, ITEM_SCHEMA);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                tableName = PriceEntry.TABLE_NAME;
                break;
            case ITEM_SCHEMA:
                tableName = ItemSchemaEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor = mOpenHelper.getReadableDatabase().query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (retCursor != null)
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_PRICE_LIST;
            case ITEM_SCHEMA:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_ITEM_SCHEMA;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        long _id;
        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                _id = db.insert(PriceEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PriceEntry.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case ITEM_SCHEMA:
                _id = db.insert(ItemSchemaEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ItemSchemaEntry.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                rowsDeleted = db.delete(PriceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_SCHEMA:
                rowsDeleted = db.delete(ItemSchemaEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                rowsUpdated = db.update(PriceEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ITEM_SCHEMA:
                rowsUpdated = db.update(ItemSchemaEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                tableName = PriceEntry.TABLE_NAME;
                break;
            case ITEM_SCHEMA:
                tableName = ItemSchemaEntry.TABLE_NAME;
                break;
            default:
                return super.bulkInsert(uri, values);
        }

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnCount;
    }
}
