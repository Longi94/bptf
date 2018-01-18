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
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 11.
 */
public class LoadCalculatorItemsInteractor extends AsyncTask<Void, Void, Void> {

    @Inject @Named("readable")
    SupportSQLiteDatabase mDatabase;
    @Inject Context mContext;

    private final List<Item> mItems = new LinkedList<>();
    private final List<Integer> mCount = new LinkedList<>();
    private double mTotalValue;

    private final Callback mCallback;

    public LoadCalculatorItemsInteractor(BptfApplication application, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected Void doInBackground(Void... params) {

        String sql = "SELECT " +
                "calculator.defindex," +
                "item_schema.item_name," +
                "calculator.quality," +
                "calculator.tradable," +
                "calculator.craftable," +
                "calculator.price_index," +
                "calculator.count," +
                "pricelist.currency," +
                "pricelist.price," +
                "pricelist.max," +
                Utility.getRawPriceQueryString(mContext) + " price_raw," +
                "pricelist.difference," +
                "calculator.australium" +
                " FROM calculator" +
                " LEFT JOIN pricelist" +
                " ON calculator.defindex = pricelist.defindex" +
                " AND calculator.tradable = pricelist.tradable" +
                " AND calculator.craftable = pricelist.craftable" +
                " AND calculator.price_index = pricelist.price_index" +
                " AND calculator.quality = pricelist.quality" +
                " AND calculator.australium = pricelist.australium" +
                " LEFT JOIN item_schema" +
                " ON calculator.defindex = item_schema.defindex" +
                " ORDER BY item_name ASC";

        Cursor cursor = mDatabase.query(sql, null);

        if (cursor != null) {
            mTotalValue = 0;

            while (cursor.moveToNext()) {
                Item item = new Item();
                item.setDefindex(cursor.getInt(cursor.getColumnIndex("defindex")));
                item.setName(cursor.getString(cursor.getColumnIndex("item_name")));
                item.setQuality(cursor.getInt(cursor.getColumnIndex("quality")));
                item.setTradable(cursor.getInt(cursor.getColumnIndex("tradable")) == 1);
                item.setCraftable(cursor.getInt(cursor.getColumnIndex("craftable")) == 1);
                item.setAustralium(cursor.getInt(cursor.getColumnIndex("australium")) == 1);
                item.setPriceIndex(cursor.getInt(cursor.getColumnIndex("price_index")));

                int count = cursor.getInt(cursor.getColumnIndex("count"));
                if (cursor.getString(cursor.getColumnIndex("currency")) != null) {
                    Price price = new Price();
                    price.setValue(cursor.getDouble(cursor.getColumnIndex("price")));
                    price.setHighValue(cursor.getDouble(cursor.getColumnIndex("max")));
                    price.setRawValue(cursor.getDouble(cursor.getColumnIndex("price_raw")));
                    price.setDifference(cursor.getDouble(cursor.getColumnIndex("difference")));
                    price.setCurrency(cursor.getString(cursor.getColumnIndex("currency")));
                    item.setPrice(price);

                    mTotalValue += item.getPrice().getRawValue() * count;
                }

                mItems.add(item);
                mCount.add(count);
            }
            cursor.close();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mCallback != null) {
            mCallback.onLoadCalculatorItemsFinished(mItems, mCount, mTotalValue);
        }
    }

    public interface Callback {
        void onLoadCalculatorItemsFinished(List<Item> items, List<Integer> count, double totalValue);
    }
}
