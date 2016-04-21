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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.network.TlongdevInterface;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevPrice;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevPricesPayload;
import com.tlongdev.bktf.util.Utility;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

import retrofit2.Response;

/**
 * Task for fetching all data for prices database and updating it in the background.
 */
public class TlongdevPriceListInteractor extends AsyncTask<Void, Integer, Integer> {

    private static final String LOG_TAG = TlongdevPriceListInteractor.class.getSimpleName();

    @Inject SharedPreferences mPrefs;
    @Inject SharedPreferences.Editor mEditor;
    @Inject TlongdevInterface mTlongdevInterface;
    @Inject Context mContext;

    //Whether it's an update or full database download
    private boolean updateDatabase;

    //Whether it was a user initiated update
    private boolean manualSync;

    //Error message to be displayed to the user
    private String errorMessage;

    //The listener that will be notified when the fetching finishes
    private Callback mCallback;

    //the variable that contains the birth time of the youngest price
    private int latestUpdate = 0;

    private int rowsInserted = 0;

    public TlongdevPriceListInteractor(BptfApplication application,
                                       boolean updateDatabase, boolean manualSync,
                                       Callback callback) {
        application.getInteractorComponent().inject(this);
        this.updateDatabase = updateDatabase;
        this.manualSync = manualSync;
        this.mCallback = callback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer doInBackground(Void... params) {

        if (System.currentTimeMillis() - mPrefs.getLong(mContext
                .getString(R.string.pref_last_price_list_update), 0) < 3600000L
                && !manualSync) {
            //This task ran less than an hour ago and wasn't a manual sync, nothing to do.
            return 0;
        }

        try {
            //Get the youngest price from the database. If it's an update only prices newer than this
            //will be updated to speed up the update and reduce data usage.
            if (updateDatabase) {
                String[] columns = {PriceEntry.COLUMN_LAST_UPDATE};
                Cursor cursor = mContext.getContentResolver().query(
                        PriceEntry.CONTENT_URI,
                        columns,
                        null,
                        null,
                        PriceEntry.COLUMN_LAST_UPDATE + " DESC LIMIT 1"
                );
                if (cursor != null) {
                    if (cursor.moveToFirst())
                        latestUpdate = cursor.getInt(0);
                    cursor.close();
                }
            }

            Response<TlongdevPricesPayload> response = mTlongdevInterface.getPrices(latestUpdate).execute();

            if (response.body() != null) {
                TlongdevPricesPayload payload = response.body();
                if (payload.getSuccess() == 1) {
                    return insertPrices(payload.getPrices());
                } else {
                    errorMessage = payload.getMessage();
                }
            } else if (response.raw().code() >= 500) {
                errorMessage = "Server error: " + response.raw().code();
            } else if (response.raw().code() >= 400) {
                errorMessage = "Client error: " + response.raw().code();
            }
            return -1;

        } catch (IOException e) {
            //There was a network error
            errorMessage = mContext.getString(R.string.error_network);
            Crashlytics.logException(e);
        }

        return -1;
    }

    private Integer insertPrices(List<TlongdevPrice> prices) {
        //Iterator that will iterate through the mItems
        Vector<ContentValues> cVVector = new Vector<>();

        for (TlongdevPrice price : prices) {
            cVVector.add(buildContentValues(price));

            if (price.getQuality() == 6 && price.getTradable() == 1 && price.getCraftable() == 1) {
                if (price.getDefindex() == 143) { //buds
                    Utility.putDouble(mEditor, mContext.getString(R.string.pref_buds_raw), price.getValueRaw());
                    mEditor.apply();
                } else if (price.getDefindex() == 5002) { //metal

                    double highPrice = price.getValueHigh() == null ? 0 : price.getValueHigh();

                    if (highPrice > price.getValue()) {
                        //If the metal has a high price, save the average as raw.
                        Utility.putDouble(mEditor, mContext.getString(R.string.pref_metal_raw_usd), ((price.getValue() + highPrice) / 2));
                    } else {
                        //save as raw price
                        Utility.putDouble(mEditor, mContext.getString(R.string.pref_metal_raw_usd), price.getValue());
                    }
                    mEditor.apply();
                } else if (price.getDefindex() == 5021) { //key
                    Utility.putDouble(mEditor, mContext.getString(R.string.pref_key_raw), price.getValueRaw());
                    mEditor.apply();
                }
            }
        }

        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            //Insert all the data into the database
            rowsInserted = mContext.getContentResolver()
                    .bulkInsert(PriceEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows into prices table");
        }

        return prices.size();
    }

    private ContentValues buildContentValues(TlongdevPrice price) {
        //Fix the defindex for pricing
        Item item = new Item();
        item.setDefindex(price.getDefindex());

        //The DV that will contain all the data
        ContentValues itemValues = new ContentValues();

        //Put all the data into the content values
        itemValues.put(PriceEntry.COLUMN_DEFINDEX, item.getFixedDefindex());
        itemValues.put(PriceEntry.COLUMN_ITEM_QUALITY, price.getQuality());
        itemValues.put(PriceEntry.COLUMN_ITEM_TRADABLE, price.getTradable());
        itemValues.put(PriceEntry.COLUMN_ITEM_CRAFTABLE, price.getCraftable());
        itemValues.put(PriceEntry.COLUMN_PRICE_INDEX, price.getPriceIndex());
        itemValues.put(PriceEntry.COLUMN_AUSTRALIUM, price.getAustralium());
        itemValues.put(PriceEntry.COLUMN_CURRENCY, price.getCurrency());
        itemValues.put(PriceEntry.COLUMN_PRICE, price.getValue());
        itemValues.put(PriceEntry.COLUMN_PRICE_HIGH, price.getValueHigh());
        itemValues.put(PriceEntry.COLUMN_LAST_UPDATE, price.getLastUpdate());
        itemValues.put(PriceEntry.COLUMN_DIFFERENCE, price.getDifference());
        itemValues.put(PriceEntry.COLUMN_WEAPON_WEAR, 0);

        return itemValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(Integer integer) {
        if (mCallback != null) {
            if (integer >= 0) {
                //Notify the listener that the update finished
                mCallback.onPriceListFinished(rowsInserted, latestUpdate);
            } else {
                mCallback.onPriceListFailed(errorMessage);
            }
        }
    }

    /**
     * Listener interface
     */
    public interface Callback {
        /**
         * Notify the listener, that the fetching has stopped.
         */
        void onPriceListFinished(int newItems, long sinceParam);

        void onPriceListFailed(String errorMessage);
    }
}