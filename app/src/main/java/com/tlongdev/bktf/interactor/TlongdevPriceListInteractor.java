/**
 * Copyright 2016 Long Tran
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.interactor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.dao.PriceDao;
import com.tlongdev.bktf.data.entity.Price;
import com.tlongdev.bktf.flatbuffers.prices.Prices;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.util.Utility;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

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
    @Inject
    PriceDao mPriceDao;

    //Whether it's an update or full database download
    private boolean updateDatabase;

    //Whether it was a user initiated update
    private boolean manualSync;

    //Error message to be displayed to the user
    private String errorMessage;

    //The listener that will be notified when the fetching finishes
    private Callback mCallback;

    //the variable that contains the birth time of the youngest price
    private long latestUpdate = 0;

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
                Price price = mPriceDao.getNewestPrice();

                if (price != null) {
                    latestUpdate = price.getLastUpdate();
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

        List<Price> prices = new LinkedList<>();

        for (int i = 0; i < pricesBuf.pricesLength(); i++) {
            com.tlongdev.bktf.flatbuffers.prices.Price priceBuf = pricesBuf.prices(i);
            Price price = readBuf(priceBuf);
            prices.add(price);
        }

        if (prices.size() > 0) {
            mPriceDao.insert(prices);
            Log.v(LOG_TAG, "inserted " + prices.size() + " rows into prices table");
        }

        return 0;
    }

    private Price readBuf(com.tlongdev.bktf.flatbuffers.prices.Price priceBuf) {
        Price price = new Price();

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
        price.setDefindex(defindex);

        price.setDefindex(defindex);
        price.setQuality(quality);
        price.setTradable(tradable);
        price.setCraftable(craftable);
        price.setPriceIndex(priceBuf.priceIndex());
        price.setAustralium(priceBuf.australium());
        price.setCurrency(priceBuf.currency());
        price.setValue(value);
        price.setHighValue(high);
        price.setLastUpdate(priceBuf.updateTs());
        price.setDifference((double) priceBuf.difference());
        price.setWeaponWear(0);

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

        return price;
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