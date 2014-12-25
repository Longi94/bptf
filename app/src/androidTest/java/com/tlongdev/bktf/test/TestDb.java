package com.tlongdev.bktf.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.tlongdev.bktf.data.PriceListContract.PriceEntry;
import com.tlongdev.bktf.data.PriceListDbHelper;

import java.util.Map;
import java.util.Set;

public class TestDb extends AndroidTestCase{

    public static final String LOG_TAG = TestDb.class.getSimpleName();
    static String TEST_NAME = "Brush";
    static int TEST_QUALITY = 6;
    static int TEST_TRADABLE = 1;
    static int TEST_CRAFTABLE = 1;
    static int TEST_PRICE_INDEX = 1;

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(PriceListDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new PriceListDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        PriceListDbHelper dbHelper = new PriceListDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createValues();

        long rowId = db.insert(PriceEntry.TABLE_NAME, null, testValues);

        assertTrue(rowId != -1);
        Log.d(LOG_TAG, "New row id: " + rowId);

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                PriceEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, testValues);
    }

    static ContentValues createValues() {
        ContentValues values = new ContentValues();

        values.put(PriceEntry.COLUMN_ITEM_NAME, TEST_NAME);
        values.put(PriceEntry.COLUMN_ITEM_QUALITY, TEST_QUALITY);
        values.put(PriceEntry.COLUMN_ITEM_TRADABLE, TEST_TRADABLE);
        values.put(PriceEntry.COLUMN_ITEM_CRAFTABLE, TEST_CRAFTABLE);
        values.put(PriceEntry.COLUMN_PRICE_INDEX, TEST_PRICE_INDEX);
        values.put(PriceEntry.COLUMN_ITEM_PRICE_CURRENCY, "Metal");
        values.put(PriceEntry.COLUMN_ITEM_PRICE, 1.33);
        values.put(PriceEntry.COLUMN_ITEM_PRICE_MAX, 1.66);
        values.put(PriceEntry.COLUMN_ITEM_PRICE_RAW, 1.46);
        values.put(PriceEntry.COLUMN_LAST_UPDATE, 123);
        values.put(PriceEntry.COLUMN_DIFFERENCE, 0.11);

        return values;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
