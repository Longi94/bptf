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
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.fragment.UnusualPresenter;
import com.tlongdev.bktf.util.Utility;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class LoadUnusualEffectsInteractor extends AsyncTask<Void, Void, Void> {

    @Inject @Named("readable")
    SupportSQLiteDatabase mDatabase;
    @Inject SharedPreferences mPrefs;
    @Inject Context mContext;

    @UnusualPresenter.UnusualOrder
    private final int mOrderBy;
    private final Callback mCallback;
    private final String mFilter;

    private final List<Item> mItems = new LinkedList<>();

    public LoadUnusualEffectsInteractor(BptfApplication application, String filter,
                                        @UnusualPresenter.UnusualOrder int orderBy, Callback callback) {
        application.getInteractorComponent().inject(this);
        mFilter = filter;
        mOrderBy = orderBy;
        mCallback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {

        String sql = "SELECT pricelist.price_index," +
                " unusual_schema.name," +
                " AVG(" + Utility.getRawPriceQueryString(mContext) + ") avg_price " +
                " FROM pricelist" +
                " LEFT JOIN unusual_schema" +
                " ON pricelist.price_index = unusual_schema._id " +
                " WHERE unusual_schema.name LIKE ? AND " +
                "pricelist.quality = ? AND " +
                "price_index != ? " +
                " GROUP BY price_index";

        String[] selectionArgs = {"%" + mFilter + "%", "5", "0"};

        switch (mOrderBy) {
            case UnusualPresenter.ORDER_BY_NAME:
                sql += " ORDER BY unusual_schema.name ASC";
                break;
            case UnusualPresenter.ORDER_BY_PRICE:
                sql += " ORDER BY avg_price DESC";
                break;
        }

        Cursor cursor = mDatabase.query(sql, selectionArgs);

        double rawKeyPrice = Utility.getDouble(mPrefs, mContext.getString(R.string.pref_key_raw), 1);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Price price = new Price();
                price.setValue(cursor.getDouble(cursor.getColumnIndex("avg_price")) / rawKeyPrice);

                Item item = new Item();
                item.setPriceIndex(cursor.getInt(cursor.getColumnIndex("price_index")));
                item.setName(cursor.getString(cursor.getColumnIndex("name")));
                item.setPrice(price);

                mItems.add(item);
            }
            cursor.close();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mCallback != null) {
            mCallback.onUnusualEffectsLoadFinished(mItems);
        }
    }

    public interface Callback {
        void onUnusualEffectsLoadFinished(List<Item> items);
    }
}
