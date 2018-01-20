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
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.flatbuffers.prices.Prices;
import com.tlongdev.bktf.flatbuffers.prices.Price;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.util.Utility;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Vector;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Task for fetching all data for prices database and updating it in the background.
 */
public class TlongdevPriceListInteractor extends AsyncTask<Void, Integer, Integer> {

    private static final String LOG_TAG = TlongdevPriceListInteractor.class.getSimpleName();

    @Inject
    SharedPreferences mPrefs;
    @Inject
    SharedPreferences.Editor mEditor;
    @Inject
    Context mContext;

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
        return run();
    }

    public Integer run() {

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

            Uri uri = Uri.parse(mContext.getString(R.string.main_host) + "/fbs/prices").buildUpon()
                    .appendQueryParameter("since", String.valueOf(latestUpdate))
                    .build();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(uri.toString())
                    .build();

            Response response = client.newCall(request).execute();

            if (response.body() != null) {
                return parseFlatBuffer(response.body().byteStream());
            } else if (response.code() >= 500) {
                errorMessage = "Server error: " + response.code();
            } else if (response.code() >= 400) {
                errorMessage = "Client error: " + response.code();
            }
            return -1;

        } catch (IOException e) {
            //There was a network error
            errorMessage = mContext.getString(R.string.error_network);
            e.printStackTrace();
        }

        return -1;
    }

    private Integer parseFlatBuffer(InputStream inputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        Prices pricesBuf = Prices.getRootAsPrices(buffer);

        Vector<ContentValues> cVVector = new Vector<>();

        for (int i = 0; i < pricesBuf.pricesLength(); i++) {
            Price priceBuf = pricesBuf.prices(i);
            ContentValues values = buildContentValues(priceBuf);
            cVVector.add(values);
        }

        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            //Insert all the data into the database
            rowsInserted = mContext.getContentResolver()
                    .bulkInsert(PriceEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows into prices table");
        }

        return 0;
    }

    private ContentValues buildContentValues(Price priceBuf) throws IOException {
        ContentValues values = new ContentValues();

        int defindex = priceBuf.defindex();
        int quality = (int) priceBuf.quality();
        boolean tradable = priceBuf.tradable();
        boolean craftable = priceBuf.craftable();
        double value = priceBuf.price();
        double high = priceBuf.priceMax();
        double raw = priceBuf.raw();

        Item item = new Item();
        item.setDefindex(defindex);
        defindex = item.getFixedDefindex();
        values.put(PriceEntry.COLUMN_DEFINDEX, defindex);

        values.put(PriceEntry.COLUMN_ITEM_QUALITY, quality);
        values.put(PriceEntry.COLUMN_ITEM_TRADABLE, tradable);
        values.put(PriceEntry.COLUMN_ITEM_CRAFTABLE, craftable);
        values.put(PriceEntry.COLUMN_PRICE_INDEX, priceBuf.priceIndex());
        values.put(PriceEntry.COLUMN_AUSTRALIUM, priceBuf.australium());
        values.put(PriceEntry.COLUMN_CURRENCY, priceBuf.currency());
        values.put(PriceEntry.COLUMN_PRICE, value);
        values.put(PriceEntry.COLUMN_PRICE_HIGH, high);
        values.put(PriceEntry.COLUMN_LAST_UPDATE, priceBuf.updateTs());
        values.put(PriceEntry.COLUMN_DIFFERENCE, (double) priceBuf.difference());
        values.put(PriceEntry.COLUMN_WEAPON_WEAR, 0);

        if (quality == Quality.UNIQUE && tradable && craftable) {
            if (defindex == 143) { //buds
                Utility.putDouble(mEditor, mContext.getString(R.string.pref_buds_raw), raw);
                mEditor.apply();
            } else if (defindex == 5002) { //metal
                if (high > value) {
                    //If the metal has a high price, save the average as raw.
                    Utility.putDouble(mEditor, mContext.getString(R.string.pref_metal_raw_usd), ((value + high) / 2));
                } else {
                    //save as raw price
                    Utility.putDouble(mEditor, mContext.getString(R.string.pref_metal_raw_usd), value);
                }
                mEditor.apply();
            } else if (defindex == 5021) { //key
                Utility.putDouble(mEditor, mContext.getString(R.string.pref_key_raw), raw);
                mEditor.apply();
            }
        }

        return values;
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

    public int getRowsInserted() {
        return rowsInserted;
    }

    public long getSinceParam() {
        return latestUpdate;
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