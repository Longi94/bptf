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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class LoadAllPricesInteractor extends AsyncTask<Void, Void, Cursor> {

    @Inject @Named("readable") SQLiteDatabase mDatabase;
    @Inject Context mContext;

    private final Callback mCallback;

    public LoadAllPricesInteractor(BptfApplication application, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        Cursor cursor = mDatabase.rawQuery("SELECT " +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                        ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                        Utility.getRawPriceQueryString(mContext) + " raw_price," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DIFFERENCE + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                        " FROM " + PriceEntry.TABLE_NAME +
                        " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                        " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                        " ORDER BY " + PriceEntry.COLUMN_LAST_UPDATE + " DESC",
                null
        );

        //Raw query is lazy, it won't actually query until we actually ask for the data.
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
            mCallback.onLoadPricesFinished(cursor);
        }
    }

    @Override
    protected void onCancelled(Cursor cursor) {
        if (mCallback != null) {
            mCallback.onLoadPricesFailed();
        }
    }

    public interface Callback {
        void onLoadPricesFinished(Cursor prices);

        void onLoadPricesFailed();
    }
}
