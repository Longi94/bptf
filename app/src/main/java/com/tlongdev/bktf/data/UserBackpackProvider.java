package com.tlongdev.bktf.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class UserBackpackProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private UserBackpackDbHelper mOpenHelper;

    public static final int BACKPACK = 100;
    public static final int BACKPACK_GUEST = 101;

    private static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = UserBackpackContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, UserBackpackContract.PATH_BACKPACK, BACKPACK);
        matcher.addURI(authority, UserBackpackContract.PATH_BACKPACK + "/guest", BACKPACK_GUEST);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new UserBackpackDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case BACKPACK:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        UserBackpackContract.UserBackpackEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case BACKPACK_GUEST:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        UserBackpackContract.UserBackpackEntry.TABLE_NAME_GUEST,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (retCursor != null)
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        return UserBackpackContract.UserBackpackEntry.CONTENT_TYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        long _id;

        switch (sUriMatcher.match(uri)) {
            case BACKPACK:
                _id = db.insert(UserBackpackContract.UserBackpackEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = UserBackpackContract.UserBackpackEntry.CONTENT_URI;
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case BACKPACK_GUEST:
                _id = db.insert(UserBackpackContract.UserBackpackEntry.TABLE_NAME_GUEST, null, values);
                if (_id > 0)
                    returnUri = UserBackpackContract.UserBackpackEntry.CONTENT_URI;
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return returnUri;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case BACKPACK:
                rowsDeleted = db.delete(
                        UserBackpackContract.UserBackpackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BACKPACK_GUEST:
                rowsDeleted = db.delete(
                        UserBackpackContract.UserBackpackEntry.TABLE_NAME_GUEST, selection, selectionArgs);
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
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case BACKPACK:
                rowsUpdated = db.update(UserBackpackContract.UserBackpackEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case BACKPACK_GUEST:
                rowsUpdated = db.update(UserBackpackContract.UserBackpackEntry.TABLE_NAME_GUEST, values, selection,
                        selectionArgs);
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
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int returnCount;

        switch (sUriMatcher.match(uri)) {
            case BACKPACK:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(UserBackpackContract.UserBackpackEntry.TABLE_NAME, null, value);
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
            case BACKPACK_GUEST:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(UserBackpackContract.UserBackpackEntry.TABLE_NAME_GUEST, null, value);
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
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
}
