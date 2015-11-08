package com.tlongdev.bktf.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tlongdev.bktf.data.DatabaseContract.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "pricelist.db";

    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
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
                        ItemSchemaEntry.COLUMN_DESCRIPTION + " TEXT, " +
                        ItemSchemaEntry.COLUMN_TYPE_NAME + " INTEGER NOT NULL, " +
                        ItemSchemaEntry.COLUMN_PROPER_NAME + " INTEGER NOT NULL, " +

                        " UNIQUE (" + PriceEntry.COLUMN_DEFINDEX + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_UNUSUAL_SCHEMA_TABLE =
                "CREATE TABLE " + UnusualSchemaEntry.TABLE_NAME + " (" +
                        UnusualSchemaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        UnusualSchemaEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                        UnusualSchemaEntry.COLUMN_NAME + " TEXT NOT NULL, " +

                        " UNIQUE (" + UnusualSchemaEntry.COLUMN_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_ORIGIN_NAMES_TABLE =
                "CREATE TABLE " + OriginEntry.TABLE_NAME + " (" +
                        OriginEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        OriginEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                        OriginEntry.COLUMN_NAME + " TEXT NOT NULL, " +

                        " UNIQUE (" + OriginEntry.COLUMN_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_PRICE_LIST_TABLE);
        db.execSQL(SQL_CREATE_ITEM_SCHEMA_TABLE);
        db.execSQL(SQL_CREATE_UNUSUAL_SCHEMA_TABLE);
        db.execSQL(SQL_CREATE_ORIGIN_NAMES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PriceEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ItemSchemaEntry.TABLE_NAME);

        if (newVersion >= 7 && oldVersion < 7) {
            mContext.deleteDatabase("pricelist.db");
            mContext.deleteDatabase("items.db");
            mContext.deleteDatabase("backpack.db");
        }

        onCreate(db);
    }
}
