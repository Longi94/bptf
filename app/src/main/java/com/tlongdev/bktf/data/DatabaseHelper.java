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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper ourInstance;

    public static DatabaseHelper getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new DatabaseHelper(context);
        }
        return ourInstance;
    }

    private static final int DATABASE_VERSION = 9;
    public static final String DATABASE_NAME = "bptf.db";

    private final Context mContext;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // TODO: 2017-12-02 remove

        if (mContext.databaseList().length > 1) {
            mContext.deleteDatabase("pricelist.db");
            mContext.deleteDatabase("items.db");
            mContext.deleteDatabase("backpack.db");
        }

        final String SQL_CREATE_BACKPACK_TABLE =
                "CREATE TABLE " + UserBackpackEntry.TABLE_NAME + " (" +
                        UserBackpackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        UserBackpackEntry.COLUMN_POSITION + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_UNIQUE_ID + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_ORIGINAL_ID + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_DEFINDEX + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_LEVEL + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_ORIGIN + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_QUALITY + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_CUSTOM_NAME + " TEXT, " +
                        UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION + " TEXT, " +
                        UserBackpackEntry.COLUMN_EQUIPPED + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_ITEM_INDEX + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_PAINT + " INTEGER, " +
                        UserBackpackEntry.COLUMN_CRAFT_NUMBER + " INTEGER, " +
                        UserBackpackEntry.COLUMN_CREATOR_NAME + " TEXT, " +
                        UserBackpackEntry.COLUMN_GIFTER_NAME + " TEXT, " +
                        UserBackpackEntry.COLUMN_CONTAINED_ITEM + " TEXT, " +
                        UserBackpackEntry.COLUMN_AUSTRALIUM + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_DECORATED_WEAPON_WEAR + " INTEGER, " +


                        " UNIQUE (" + UserBackpackEntry.COLUMN_POSITION + ", " +
                        UserBackpackEntry.COLUMN_UNIQUE_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_GUEST_BACKPACK_TABLE =
                "CREATE TABLE " + UserBackpackEntry.TABLE_NAME_GUEST + " (" +
                        UserBackpackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        UserBackpackEntry.COLUMN_POSITION + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_UNIQUE_ID + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_ORIGINAL_ID + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_DEFINDEX + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_LEVEL + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_ORIGIN + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_QUALITY + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_CUSTOM_NAME + " TEXT, " +
                        UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION + " TEXT, " +
                        UserBackpackEntry.COLUMN_EQUIPPED + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_ITEM_INDEX + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_PAINT + " INTEGER, " +
                        UserBackpackEntry.COLUMN_CRAFT_NUMBER + " INTEGER, " +
                        UserBackpackEntry.COLUMN_CREATOR_NAME + " TEXT, " +
                        UserBackpackEntry.COLUMN_GIFTER_NAME + " TEXT, " +
                        UserBackpackEntry.COLUMN_CONTAINED_ITEM + " TEXT, " +
                        UserBackpackEntry.COLUMN_AUSTRALIUM + " INTEGER NOT NULL, " +
                        UserBackpackEntry.COLUMN_DECORATED_WEAPON_WEAR + " INTEGER, " +


                        " UNIQUE (" + UserBackpackEntry.COLUMN_POSITION + ", " +
                        UserBackpackEntry.COLUMN_UNIQUE_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_BACKPACK_TABLE);
        db.execSQL(SQL_CREATE_GUEST_BACKPACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + UserBackpackEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserBackpackEntry.TABLE_NAME_GUEST);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        editor.remove(mContext.getString(R.string.pref_last_item_schema_update));
        editor.remove(mContext.getString(R.string.pref_last_price_list_update));

        editor.apply();

        if (oldVersion < 7) {
            mContext.deleteDatabase("pricelist.db");
            mContext.deleteDatabase("items.db");
            mContext.deleteDatabase("backpack.db");
        }

        onCreate(db);
    }
}
