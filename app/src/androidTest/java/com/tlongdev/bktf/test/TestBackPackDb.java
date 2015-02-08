package com.tlongdev.bktf.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.tlongdev.bktf.data.UserBackpackContract;
import com.tlongdev.bktf.data.UserBackpackDbHelper;

import java.util.Map;
import java.util.Set;

public class TestBackPackDb extends AndroidTestCase {

    public static final String LOG_TAG = TestBackPackDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(UserBackpackDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new UserBackpackDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        UserBackpackDbHelper dbHelper = new UserBackpackDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createValues();

        long rowId = db.insert(UserBackpackContract.UserBackpackEntry.TABLE_NAME, null, testValues);

        assertTrue(rowId != -1);
        Log.d(LOG_TAG, "New row id: " + rowId);

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                UserBackpackContract.UserBackpackEntry.TABLE_NAME,  // Table to Query
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

        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_UNIQUE_ID, 1234);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_ORIGINAL_ID, 1234);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_DEFINDEX, 1);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_LEVEL, 10);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_QUANTITY, 1);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_ORIGIN, 1);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE, 0);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT, 0);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_INVENTORY_TOKEN, 16);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_QUALITY, 5);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_CUSTOM_NAME, "asdasd");
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION, "asdasdasdasd");
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_EQUIPPED, 1);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_PRICE_INDEX, 0);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_PAINT, 550);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_CRAFT_NUMBER, 0);
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_CREATOR_NAME, "repp");
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_GIFTER_NAME, "reppppp");
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_CONTAINED_ITEM, "this is a string");
        values.put(UserBackpackContract.UserBackpackEntry.COLUMN_AUSTRALIUM, 0);
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
