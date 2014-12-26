package com.tlongdev.bktf.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.tlongdev.bktf.data.PriceListContract.PriceEntry;
import com.tlongdev.bktf.data.PriceListDbHelper;

public class TestProvider extends AndroidTestCase{

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(PriceListDbHelper.DATABASE_NAME);
    }

    public void testInsertReadDb() {

        ContentValues testValues = TestDb.createValues();

        Uri insertUri = mContext.getContentResolver().insert(PriceEntry.CONTENT_URI, testValues);
        long rowId = ContentUris.parseId(insertUri);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                PriceEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                PriceEntry.buildPriceListUri(rowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                PriceEntry.buildPriceListUriWithName(TestDb.TEST_NAME),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);// Now see if we can successfully query if we include the row id

        cursor = mContext.getContentResolver().query(
                PriceEntry.buildPriceListUriWithNameSpecific(TestDb.TEST_NAME,
                        TestDb.TEST_QUALITY, TestDb.TEST_TRADABLE, TestDb.TEST_CRAFTABLE, TestDb.TEST_PRICE_INDEX),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        cursor = mContext.getContentResolver().query(
                PriceEntry.buildPriceListSearchUri("rush"),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(PriceEntry.CONTENT_URI);
        assertEquals(PriceEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                PriceEntry.buildPriceListUriWithName(TestDb.TEST_NAME));
        assertEquals(PriceEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                PriceEntry.buildPriceListUriWithNameSpecific(TestDb.TEST_NAME,
                        TestDb.TEST_QUALITY, TestDb.TEST_TRADABLE, TestDb.TEST_CRAFTABLE, TestDb.TEST_PRICE_INDEX));
        assertEquals(PriceEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(PriceEntry.buildPriceListUri(1L));
        assertEquals(PriceEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(PriceEntry.buildPriceListSearchUri(TestDb.TEST_NAME));
        assertEquals(PriceEntry.CONTENT_TYPE, type);
    }
}
