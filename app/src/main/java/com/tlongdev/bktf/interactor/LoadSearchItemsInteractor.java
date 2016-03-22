/**
 * Copyright 2016 Long Tran
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

package com.tlongdev.bktf.interactor;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 22.
 */
public class LoadSearchItemsInteractor extends AsyncTask<Void, Void, Cursor> {

    @Inject ContentResolver mContentResolver;

    private String mQuery;
    private boolean mFilter;
    private int mFilterQuality;
    private boolean mFilterTradable;
    private boolean mFilterCraftable;
    private boolean mFilterAustralium;
    private Callback mCallback;

    public LoadSearchItemsInteractor(BptfApplication application, String query, boolean filter,
                                     int filterQuality, boolean filterTradable, boolean filterCraftable,
                                     boolean filterAustralium, Callback callback) {
        application.getInteractorComponent().inject(this);
        mQuery = query;
        mFilter = filter;
        mFilterQuality = filterQuality;
        mFilterTradable = filterTradable;
        mFilterCraftable = filterCraftable;
        mFilterAustralium = filterAustralium;
        mCallback = callback;
    }

    @Override
    protected Cursor doInBackground(Void... params) {

        String sql = "SELECT " +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                " FROM " + PriceEntry.TABLE_NAME +
                " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                " WHERE ";

        String query = mQuery != null && mQuery.length() > 0 ? "%" + mQuery + "%" : "ASDASD"; //stupid
        String[] selectionArgs;

        if (mFilter) {
            sql += ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ? AND " +
                    PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                    PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                    PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                    PriceEntry.COLUMN_AUSTRALIUM + " = ?";
            selectionArgs = new String[]{query, String.valueOf(mFilterQuality),
                    mFilterTradable ? "1" : "0", mFilterCraftable ? "1" : "0",
                    mFilterAustralium ? "1" : "0"};
        } else {
            sql += ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ? AND " +
                    "NOT(" + PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                    PriceEntry.COLUMN_PRICE_INDEX + " != ?)";
            selectionArgs = new String[]{query, "5", "0"};
        }

        Cursor cursor = mContentResolver.query(DatabaseContract.RAW_QUERY_URI,
                null, sql, selectionArgs, null
        );

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                return cursor;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        if (mCallback != null) {
            mCallback.onSearchItemsLoaded(cursor);
        }
    }

    public interface Callback {
        void onSearchItemsLoaded(Cursor items);
    }
}
