package com.tlongdev.bktf.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tlongdev.bktf.data.PriceListContract.PriceEntry;


public class PriceListDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "pricelist.db";

    public PriceListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PRICE_LIST_TABLE =
                "CREATE TABLE " + PriceEntry.TABLE_NAME + " (" +
                PriceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                PriceEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                PriceEntry.COLUMN_ITEM_QUALITY + " INTEGER NOT NULL, " +
                PriceEntry.COLUMN_ITEM_TRADABLE + " INTEGER NOT NULL, " +
                PriceEntry.COLUMN_ITEM_CRAFTABLE + " INTEGER NOT NULL, " +
                PriceEntry.COLUMN_PRICE_INDEX + " INTEGER NOT NULL, " +
                PriceEntry.COLUMN_ITEM_PRICE_CURRENCY + " TEXT NOT NULL, " +
                PriceEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL, " +
                PriceEntry.COLUMN_ITEM_PRICE_MAX + " REAL, " +
                PriceEntry.COLUMN_ITEM_PRICE_RAW + " REAL NOT NULL, " +
                PriceEntry.COLUMN_LAST_UPDATE + " INTEGER NOT NULL, " +
                PriceEntry.COLUMN_DIFFERENCE + " REAL NOT NULL, " +

                " UNIQUE (" + PriceEntry.COLUMN_ITEM_NAME + ", " +
                        PriceEntry.COLUMN_ITEM_QUALITY + ", " +
                        PriceEntry.COLUMN_ITEM_TRADABLE + ", " +
                        PriceEntry.COLUMN_ITEM_CRAFTABLE + ", " +
                        PriceEntry.COLUMN_PRICE_INDEX + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_PRICE_LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PriceEntry.TABLE_NAME);
        onCreate(db);
    }
}
