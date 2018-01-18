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

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 22.
 */
public class LoadSearchItemsInteractor extends AsyncTask<Void, Void, Cursor> {

    @Inject @Named("readable")
    SupportSQLiteDatabase mDatabase;

    private final String mQuery;
    private final boolean mFilter;
    private final int mFilterQuality;
    private final boolean mFilterTradable;
    private final boolean mFilterCraftable;
    private final boolean mFilterAustralium;
    private final Callback mCallback;

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
                "pricelist.defindex," +
                "item_schema.item_name," +
                "pricelist.quality," +
                "pricelist.tradable," +
                "pricelist.craftable," +
                "pricelist.price_index," +
                "pricelist.currency," +
                "pricelist.price," +
                "pricelist.max," +
                "pricelist.australium" +
                " FROM pricelist" +
                " LEFT JOIN item_schema" +
                " ON pricelist.defindex = item_schema.defindex" +
                " WHERE ";

        String query = mQuery != null && mQuery.length() > 0 ? "%" + mQuery + "%" : "ASDASD"; //stupid
        String[] selectionArgs;

        if (mFilter) {
            sql += "item_schema.item_name LIKE ? AND " +
                    "quality = ? AND " +
                    "tradable = ? AND " +
                    "craftable = ? AND " +
                    "australium = ?";
            selectionArgs = new String[]{query, String.valueOf(mFilterQuality),
                    mFilterTradable ? "1" : "0", mFilterCraftable ? "1" : "0",
                    mFilterAustralium ? "1" : "0"};
        } else {
            sql += "item_schema.item_name LIKE ? AND " +
                    "NOT(quality = ? AND " +
                    "price_index != ?)";
            selectionArgs = new String[]{query, "5", "0"};
        }

        Cursor cursor = mDatabase.query(sql, selectionArgs);

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
