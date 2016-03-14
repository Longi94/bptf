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
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class LoadAllPricesInteractor extends AsyncTask<Void, Void, Cursor> {

    @Inject @Named("readable") SQLiteDatabase mDatabase;

    private Context mContext;
    private Callback mCallback;

    public LoadAllPricesInteractor(Context context, BptfApplication application, Callback callback) {
        mContext = context;
        mCallback = callback;
        application.getInteractorComponent().inject(this);
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        Cursor cursor = mDatabase.rawQuery("SELECT " +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_DEFINDEX + "," +
                        DatabaseContract.ItemSchemaEntry.TABLE_NAME + "." + DatabaseContract.ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_ITEM_QUALITY + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_ITEM_TRADABLE + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_ITEM_CRAFTABLE + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_PRICE_INDEX + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_CURRENCY + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_PRICE + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_PRICE_HIGH + "," +
                        Utility.getRawPriceQueryString(mContext) + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_DIFFERENCE + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_AUSTRALIUM +
                        " FROM " + DatabaseContract.PriceEntry.TABLE_NAME +
                        " LEFT JOIN " + DatabaseContract.ItemSchemaEntry.TABLE_NAME +
                        " ON " + DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_DEFINDEX + " = " + DatabaseContract.ItemSchemaEntry.TABLE_NAME + "." + DatabaseContract.ItemSchemaEntry.COLUMN_DEFINDEX +
                        " ORDER BY " + DatabaseContract.PriceEntry.COLUMN_LAST_UPDATE + " DESC",
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
            mCallback.onFinish(cursor);
        }
    }

    @Override
    protected void onCancelled(Cursor cursor) {
        if (mCallback != null) {
            mCallback.onFail();
        }
    }

    public interface Callback {
        void onFinish(Cursor prices);

        void onFail();
    }
}
