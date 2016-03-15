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

    private Context mContext;

    private List<Item> mItems = new LinkedList<>();
    private List<Integer> mCount = new LinkedList<>();
    private double mTotalValue;

    private Callback mCallback;

    public LoadCalculatorItemsInteractor(Context context, BptfApplication application, Callback callback) {
        mContext = context;
        mCallback = callback;
        application.getInteractorComponent().inject(this);
    }

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
                Item item = new Item(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_DEFINDEX)),
                        cursor.getString(cursor.getColumnIndex(ItemSchemaEntry.COLUMN_ITEM_NAME)),
                        cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_ITEM_QUALITY)),
                        cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_ITEM_TRADABLE)) == 1,
                        cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_ITEM_CRAFTABLE)) == 1,
                        cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_AUSTRALIUM)) == 1,
                        cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_PRICE_INDEX)),
                        null
                );

                int count = cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_COUNT));
                if (cursor.getString(cursor.getColumnIndex(PriceEntry.COLUMN_CURRENCY)) != null) {
                    item.setPrice(new Price(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE)),
                            cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)),
                            cursor.getDouble(cursor.getColumnIndex("price_raw")),
                            0,
                            cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_DIFFERENCE)),
                            cursor.getString(cursor.getColumnIndex(PriceEntry.COLUMN_CURRENCY))));

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
