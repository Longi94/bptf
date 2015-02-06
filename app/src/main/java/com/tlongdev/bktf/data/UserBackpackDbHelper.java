package com.tlongdev.bktf.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tlongdev.bktf.data.UserBackpackContract.UserBackpackEntry;

public class UserBackpackDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "backpack.db";

    public UserBackpackDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PRICE_LIST_TABLE =
                "CREATE TABLE " + UserBackpackEntry.TABLE_NAME + " (" +
                        UserBackpackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        UserBackpackEntry.COLUMN_UNIQUE_ID + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_ORIGINAL_ID + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_DEFINDEX + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_LEVEL + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_QUANTITY + " INTEGER, " +
                        UserBackpackEntry.COLUMN_ORIGIN + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_INVENTORY_TOKEN + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_QUALITY + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_CUSTOM_NAME + " TEXT, " +
                        UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION + " TEXT, " +
                        UserBackpackEntry.COLUMN_EQUIPPED + " INTEGER NOT NULL " +


                        " UNIQUE (" + UserBackpackEntry.COLUMN_INVENTORY_TOKEN + ", " +
                        UserBackpackEntry.COLUMN_UNIQUE_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_PRICE_LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PriceListContract.PriceEntry.TABLE_NAME);
        onCreate(db);
    }
}
