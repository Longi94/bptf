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
import com.tlongdev.bktf.data.DatabaseContract.CalculatorEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
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

    @Inject @Named("readable") SQLiteDatabase mDatabase;
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
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_DEFINDEX + "," +
                ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_QUALITY + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_TRADABLE + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_CRAFTABLE + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_PRICE_INDEX + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_COUNT + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                Utility.getRawPriceQueryString(mContext) + " price_raw," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DIFFERENCE + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_AUSTRALIUM +
                " FROM " + CalculatorEntry.TABLE_NAME +
                " LEFT JOIN " + PriceEntry.TABLE_NAME +
                " ON " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_DEFINDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX +
                " AND " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_TRADABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE +
                " AND " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_CRAFTABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE +
                " AND " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_PRICE_INDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX +
                " AND " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_QUALITY + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY +
                " AND " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_AUSTRALIUM + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                " ON " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                " ORDER BY " + ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC";

        Cursor cursor = mDatabase.rawQuery(sql, null);

        if (cursor != null) {
            mTotalValue = 0;

            while (cursor.moveToNext()) {
                Item item = new Item();
                item.setDefindex(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_DEFINDEX)));
                item.setName(cursor.getString(cursor.getColumnIndex(ItemSchemaEntry.COLUMN_ITEM_NAME)));
                item.setQuality(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_ITEM_QUALITY)));
                item.setTradable(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_ITEM_TRADABLE)) == 1);
                item.setCraftable(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_ITEM_CRAFTABLE)) == 1);
                item.setAustralium(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_AUSTRALIUM)) == 1);
                item.setPriceIndex(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_PRICE_INDEX)));

                int count = cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_COUNT));
                if (cursor.getString(cursor.getColumnIndex(PriceEntry.COLUMN_CURRENCY)) != null) {
                    Price price = new Price();
                    price.setValue(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE)));
                    price.setHighValue(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)));
                    price.setRawValue(cursor.getDouble(cursor.getColumnIndex("price_raw")));
                    price.setDifference(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_DIFFERENCE)));
                    price.setCurrency(cursor.getString(cursor.getColumnIndex(PriceEntry.COLUMN_CURRENCY)));
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
