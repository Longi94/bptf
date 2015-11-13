package com.tlongdev.bktf.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.tlongdev.bktf.data.DatabaseContract.*;

public class DatabaseProvider extends ContentProvider {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = DatabaseProvider.class.getSimpleName();

    /**
     * URI matcher result codes
     */
    public static final int RAW_QUERY = 99;
    public static final int PRICE_LIST = 100;
    public static final int ITEM_SCHEMA = 101;
    public static final int ORIGIN_NAMES = 102;
    public static final int UNUSUAL_SCHEMA = 103;
    public static final int BACKPACK = 104;
    public static final int BACKPACK_GUEST = 105;

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
        matcher.addURI(authority, DatabaseContract.PATH_RAW_QUERY, RAW_QUERY);
        matcher.addURI(authority, DatabaseContract.PATH_PRICE_LIST, PRICE_LIST);
        matcher.addURI(authority, DatabaseContract.PATH_ITEM_SCHEMA, ITEM_SCHEMA);
        matcher.addURI(authority, DatabaseContract.PATH_UNUSUAL_SCHEMA, UNUSUAL_SCHEMA);
        matcher.addURI(authority, DatabaseContract.PATH_ORIGIN_NAMES, ORIGIN_NAMES);
        matcher.addURI(authority, DatabaseContract.PATH_BACKPACK, BACKPACK);
        matcher.addURI(authority, DatabaseContract.PATH_BACKPACK + "/guest", BACKPACK_GUEST);

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
            case RAW_QUERY:
                retCursor = mOpenHelper.getReadableDatabase().rawQuery(selection, selectionArgs);
                if (retCursor != null)
                    retCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return retCursor;
            case PRICE_LIST:
                tableName = PriceEntry.TABLE_NAME;
                break;
            case ITEM_SCHEMA:
                tableName = ItemSchemaEntry.TABLE_NAME;
                break;
            case UNUSUAL_SCHEMA:
                tableName = UnusualSchemaEntry.TABLE_NAME;
                break;
            case ORIGIN_NAMES:
                tableName = OriginEntry.TABLE_NAME;
                break;
            case BACKPACK:
                tableName = UserBackpackEntry.TABLE_NAME;
                break;
            case BACKPACK_GUEST:
                tableName = UserBackpackEntry.TABLE_NAME_GUEST;
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
            case UNUSUAL_SCHEMA:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_UNUSUAL_SCHEMA;
            case ORIGIN_NAMES:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_ORIGIN_NAMES;
            case BACKPACK:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_BACKPACK;
            case BACKPACK_GUEST:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_BACKPACK;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        long _id;
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                tableName = PriceEntry.TABLE_NAME;
                returnUri = PriceEntry.CONTENT_URI;
                break;
            case ITEM_SCHEMA:
                tableName = ItemSchemaEntry.TABLE_NAME;
                returnUri = ItemSchemaEntry.CONTENT_URI;
                break;
            case UNUSUAL_SCHEMA:
                tableName = UnusualSchemaEntry.TABLE_NAME;
                returnUri = UnusualSchemaEntry.CONTENT_URI;
                break;
            case ORIGIN_NAMES:
                tableName = OriginEntry.TABLE_NAME;
                returnUri = OriginEntry.CONTENT_URI;
                break;
            case BACKPACK:
                tableName = UserBackpackEntry.TABLE_NAME;
                returnUri = UserBackpackEntry.CONTENT_URI;
                break;
            case BACKPACK_GUEST:
                tableName = UserBackpackEntry.TABLE_NAME_GUEST;
                returnUri = UserBackpackEntry.CONTENT_URI_GUEST;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        _id = db.insert(tableName, null, values);
        if (_id > 0)
            return returnUri;
        else
            throw new android.database.SQLException("Failed to insert row into " + uri);
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
            case UNUSUAL_SCHEMA:
                rowsDeleted = db.delete(UnusualSchemaEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ORIGIN_NAMES:
                rowsDeleted = db.delete(OriginEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BACKPACK:
                rowsDeleted = db.delete(UserBackpackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BACKPACK_GUEST:
                rowsDeleted = db.delete(UserBackpackEntry.TABLE_NAME_GUEST, selection, selectionArgs);
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
            case UNUSUAL_SCHEMA:
                rowsUpdated = db.update(UnusualSchemaEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ORIGIN_NAMES:
                rowsUpdated = db.update(OriginEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BACKPACK:
                rowsUpdated = db.update(UserBackpackEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BACKPACK_GUEST:
                rowsUpdated = db.update(UserBackpackEntry.TABLE_NAME_GUEST, values, selection, selectionArgs);
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
            case UNUSUAL_SCHEMA:
                tableName = UnusualSchemaEntry.TABLE_NAME;
                break;
            case ORIGIN_NAMES:
                tableName = OriginEntry.TABLE_NAME;
                break;
            case BACKPACK:
                tableName = UserBackpackEntry.TABLE_NAME;
                break;
            case BACKPACK_GUEST:
                tableName = UserBackpackEntry.TABLE_NAME_GUEST;
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
