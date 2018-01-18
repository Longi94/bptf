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
import com.tlongdev.bktf.data.DatabaseContract.CalculatorEntry;
import com.tlongdev.bktf.data.DatabaseContract.FavoritesEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 12.
 */
public class LoadFavoritesInteractor extends AsyncTask<Void, Void, Void> {

    @Inject @Named("readable")
    SupportSQLiteDatabase mDatabase;
    @Inject Context mContext;

    private final Callback mCallback;

    private final List<Item> mItems = new LinkedList<>();

    public LoadFavoritesInteractor(BptfApplication application, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected Void doInBackground(Void... params) {

        String sql = "SELECT " +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + "," +
                "item_schema.item_name," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_QUALITY + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_TRADABLE + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_CRAFTABLE + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_PRICE_INDEX + "," +
                "pricelist.currency," +
                "pricelist.price," +
                "pricelist.max," +
                Utility.getRawPriceQueryString(mContext) + " price_raw," +
                "pricelist.difference," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_AUSTRALIUM +
                " FROM " + FavoritesEntry.TABLE_NAME +
                " LEFT JOIN pricelist" +
                " ON " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + " = pricelist.defindex" +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_TRADABLE + " = pricelist.tradable" +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_CRAFTABLE + " = pricelist.craftable" +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_PRICE_INDEX + " = pricelist.price_index" +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_QUALITY + " = pricelist.quality" +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_AUSTRALIUM + " = pricelist.australium" +
                " LEFT JOIN item_schema" +
                " ON " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + " = item_schema.defindex" +
                " ORDER BY item_name ASC";

        Cursor cursor = mDatabase.query(sql, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Item item = new Item();
                item.setDefindex(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_DEFINDEX)));
                item.setName(cursor.getString(cursor.getColumnIndex("item_name")));
                item.setQuality(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_ITEM_QUALITY)));
                item.setTradable(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_ITEM_TRADABLE)) == 1);
                item.setCraftable(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_ITEM_CRAFTABLE)) == 1);
                item.setAustralium(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_AUSTRALIUM)) == 1);
                item.setPriceIndex(cursor.getInt(cursor.getColumnIndex(CalculatorEntry.COLUMN_PRICE_INDEX)));

                if (cursor.getString(cursor.getColumnIndex("currency")) != null) {
                    Price price = new Price();
                    price.setValue(cursor.getDouble(cursor.getColumnIndex("price")));
                    price.setHighValue(cursor.getDouble(cursor.getColumnIndex("max")));
                    price.setRawValue(cursor.getDouble(cursor.getColumnIndex("price_raw")));
                    price.setDifference(cursor.getDouble(cursor.getColumnIndex("difference")));
                    price.setCurrency(cursor.getString(cursor.getColumnIndex("currency")));
                    item.setPrice(price);
                }

                mItems.add(item);
            }
            cursor.close();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mCallback != null) {
            mCallback.onLoadFavoritesFinished(mItems);
        }
    }

    public interface Callback {
        void onLoadFavoritesFinished(List<Item> items);
    }
}
