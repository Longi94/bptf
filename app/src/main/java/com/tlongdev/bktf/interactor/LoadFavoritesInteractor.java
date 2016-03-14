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
import com.tlongdev.bktf.data.DatabaseContract.FavoritesEntry;
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
 * @since 2016. 03. 12.
 */
public class LoadFavoritesInteractor extends AsyncTask<Void, Void, Void> {

    @Inject @Named("readable") SQLiteDatabase mDatabase;

    private Callback mCallback;
    private Context mContext;

    private List<Item> mItems = new LinkedList<>();

    public LoadFavoritesInteractor(Context context, BptfApplication application, Callback callback) {
        application.getInteractorComponent().inject(this);
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {

        String sql = "SELECT " +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + "," +
                ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_QUALITY + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_TRADABLE + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_CRAFTABLE + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_PRICE_INDEX + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                Utility.getRawPriceQueryString(mContext) + " price_raw," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DIFFERENCE + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_AUSTRALIUM +
                " FROM " + FavoritesEntry.TABLE_NAME +
                " LEFT JOIN " + PriceEntry.TABLE_NAME +
                " ON " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_TRADABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_CRAFTABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_PRICE_INDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_QUALITY + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_AUSTRALIUM + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                " ON " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                " ORDER BY " + ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC";

        Cursor cursor = mDatabase.rawQuery(sql, null);

        if (cursor != null) {
            while (cursor.moveToFirst()) {
                Item item = new Item(cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COLUMN_DEFINDEX)),
                        cursor.getString(cursor.getColumnIndex(ItemSchemaEntry.COLUMN_ITEM_NAME)),
                        cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COLUMN_ITEM_QUALITY)),
                        cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COLUMN_ITEM_TRADABLE)) == 1,
                        cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COLUMN_ITEM_CRAFTABLE)) == 1,
                        cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COLUMN_AUSTRALIUM)) == 1,
                        cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COLUMN_PRICE_INDEX)),
                        null
                );

                if (cursor.getString(cursor.getColumnIndex(PriceEntry.COLUMN_CURRENCY)) != null) {
                    item.setPrice(new Price(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE)),
                            cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)),
                            cursor.getDouble(cursor.getColumnIndex("price_raw")),
                            0,
                            cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_DIFFERENCE)),
                            cursor.getString(cursor.getColumnIndex(PriceEntry.COLUMN_CURRENCY))));
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
