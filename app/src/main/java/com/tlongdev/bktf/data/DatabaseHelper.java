package com.tlongdev.bktf.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "pricelist.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PRICE_LIST_TABLE =
                "CREATE TABLE " + PriceEntry.TABLE_NAME + " (" +
                        PriceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        PriceEntry.COLUMN_DEFINDEX + " INTEGER NOT NULL, " +
                        PriceEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                        PriceEntry.COLUMN_ITEM_QUALITY + " INTEGER NOT NULL, " +
                        PriceEntry.COLUMN_ITEM_TRADABLE + " INTEGER NOT NULL, " +
                        PriceEntry.COLUMN_ITEM_CRAFTABLE + " INTEGER NOT NULL, " +
                        PriceEntry.COLUMN_PRICE_INDEX + " INTEGER NOT NULL, " +
                        PriceEntry.COLUMN_AUSTRALIUM + " INTEGER NOT NULL, " +
                        PriceEntry.COLUMN_CURRENCY + " TEXT NOT NULL, " +
                        PriceEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                        PriceEntry.COLUMN_PRICE_HIGH + " REAL, " +
                        PriceEntry.COLUMN_LAST_UPDATE + " INTEGER NOT NULL, " +
                        PriceEntry.COLUMN_DIFFERENCE + " REAL NOT NULL, " +
                        PriceEntry.COLUMN_WEAPON_WEAR + " INTEGER NOT NULL, " +

                        " UNIQUE (" + PriceEntry.COLUMN_DEFINDEX + ", " +
                        PriceEntry.COLUMN_ITEM_QUALITY + ", " +
                        PriceEntry.COLUMN_ITEM_TRADABLE + ", " +
                        PriceEntry.COLUMN_ITEM_CRAFTABLE + ", " +
                        PriceEntry.COLUMN_PRICE_INDEX + ", " +
                        PriceEntry.COLUMN_AUSTRALIUM + ", " +
                        PriceEntry.COLUMN_WEAPON_WEAR + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_ITEM_SCHEMA_TABLE =
                "CREATE TABLE " + ItemSchemaEntry.TABLE_NAME + " (" +
                        ItemSchemaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        ItemSchemaEntry.COLUMN_DEFINDEX + " INTEGER NOT NULL, " +
                        ItemSchemaEntry.COLUMN_ITEM_NAME + " INTEGER NOT NULL, " +
                        ItemSchemaEntry.COLUMN_TYPE_NAME + " INTEGER NOT NULL, " +
                        ItemSchemaEntry.COLUMN_PROPER_NAME + " INTEGER NOT NULL, " +

                        " UNIQUE (" + PriceEntry.COLUMN_DEFINDEX + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_PRICE_LIST_TABLE);
        db.execSQL(SQL_CREATE_ITEM_SCHEMA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PriceEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ItemSchemaEntry.TABLE_NAME);
        onCreate(db);
    }
}
