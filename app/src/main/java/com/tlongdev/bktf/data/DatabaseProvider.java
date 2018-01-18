/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.tlongdev.bktf.data.DatabaseContract.UserBackpackEntry;

public class DatabaseProvider extends ContentProvider {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = DatabaseProvider.class.getSimpleName();

    /**
     * URI matcher result codes
     */
    private static final int BACKPACK = 104;
    private static final int BACKPACK_GUEST = 105;

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
        matcher.addURI(authority, DatabaseContract.PATH_BACKPACK, BACKPACK);
        matcher.addURI(authority, DatabaseContract.PATH_BACKPACK + "/guest", BACKPACK_GUEST);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        String tableName;
        switch (sUriMatcher.match(uri)) {
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

        if (getContext() != null && retCursor != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
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
        if (getContext() != null && (selection == null || rowsDeleted != 0)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case BACKPACK:
                rowsUpdated = db.update(UserBackpackEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BACKPACK_GUEST:
                rowsUpdated = db.update(UserBackpackEntry.TABLE_NAME_GUEST, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null && rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String tableName;
        switch (sUriMatcher.match(uri)) {
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
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return returnCount;
    }
}
