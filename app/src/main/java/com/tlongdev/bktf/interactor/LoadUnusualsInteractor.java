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
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.util.Utility;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public class LoadUnusualsInteractor extends AsyncTask<Void, Void, Void> {

    @Inject @Named("readable") SQLiteDatabase mDatabase;
    @Inject Context mContext;

    private Callback mCallback;
    private String mFilter;
    private int mDefindex;
    private int mIndex;

    private List<Item> mItems = new LinkedList<>();

    public LoadUnusualsInteractor(BptfApplication application, int defindex, int index, String filter,
                                  Callback callback) {
        application.getInteractorComponent().inject(this);
        mFilter = filter;
        mCallback = callback;
        mDefindex = defindex;
        mIndex = index;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected Void doInBackground(Void... params) {

        int index;
        String selection = PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + " = ?";
        String nameColumn;
        String joinOn;

        //If defindex is -1, user is browsing by effects
        if (mDefindex != -1) {
            index = mDefindex;
            nameColumn = UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME;
            joinOn = UnusualSchemaEntry.TABLE_NAME + " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + " = " + UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_ID;
            selection = selection + " AND " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = ? AND " +
                    UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME + " LIKE ?";
        } else {
            index = mIndex;
            nameColumn = ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME;
            joinOn = ItemSchemaEntry.TABLE_NAME + " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX;
            selection = selection + " AND " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                    ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ?";
        }

        String sql = "SELECT " +
                PriceEntry.TABLE_NAME + "." + PriceEntry._ID + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                PriceEntry.COLUMN_PRICE_INDEX + "," +
                PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.COLUMN_PRICE_HIGH + "," +
                PriceEntry.COLUMN_LAST_UPDATE + "," +
                PriceEntry.COLUMN_DIFFERENCE + "," +
                nameColumn + " name" +
                " FROM " + PriceEntry.TABLE_NAME +
                " LEFT JOIN " + joinOn +
                " WHERE " + selection +
                " ORDER BY " + Utility.getRawPriceQueryString(mContext) + " DESC";

        String[] selectionArgs = {"5", String.valueOf(index), "%" + mFilter + "%"};

        Cursor cursor = mDatabase.rawQuery(sql, selectionArgs);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Price price = new Price();
                price.setValue(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE)));
                price.setHighValue(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)));
                price.setCurrency(cursor.getString(cursor.getColumnIndex(PriceEntry.COLUMN_CURRENCY)));
                price.setDifference(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_DIFFERENCE)));
                price.setLastUpdate(cursor.getLong(cursor.getColumnIndex(PriceEntry.COLUMN_LAST_UPDATE)));

                Item item = new Item();
                item.setName(cursor.getString(cursor.getColumnIndex("name")));
                item.setDefindex(cursor.getInt(cursor.getColumnIndex(PriceEntry.COLUMN_DEFINDEX)));
                item.setPriceIndex(cursor.getInt(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE_INDEX)));
                item.setQuality(Quality.UNUSUAL);
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
            mCallback.onUnusualsLoadFinished(mItems);
        }
    }

    public interface Callback {
        void onUnusualsLoadFinished(List<Item> items);

        void onUnusualsLoadFailed();
    }
}
