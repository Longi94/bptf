package com.tlongdev.bktf.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.tlongdev.bktf.data.UserBackpackContract.UserBackpackEntry;
import com.tlongdev.bktf.data.UserBackpackDbHelper;

public class TestBackpackProvider extends AndroidTestCase{

    public static final String LOG_TAG = TestBackpackProvider.class.getSimpleName();

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(UserBackpackDbHelper.DATABASE_NAME);
    }

    public void testInsertReadDb() {

        ContentValues testValues = TestBackPackDb.createValues();

        Uri insertUri = mContext.getContentResolver().insert(UserBackpackEntry.CONTENT_URI, testValues);
        long rowId = ContentUris.parseId(insertUri);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                UserBackpackEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                UserBackpackEntry.buildBackPackUri(rowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(UserBackpackEntry.CONTENT_URI);
        assertEquals(UserBackpackEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                UserBackpackEntry.buildBackPackUri(0));
        assertEquals(UserBackpackEntry.CONTENT_TYPE, type);
    }
}
