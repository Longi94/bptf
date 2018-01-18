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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.dao.PriceDao;
import com.tlongdev.bktf.data.entity.Price;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.network.TlongdevInterface;
import com.tlongdev.bktf.util.Utility;

import java.io.IOException;
import java.io.InputStream;
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
    TlongdevInterface mTlongdevInterface;
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

            Uri uri = Uri.parse(mContext.getString(R.string.main_host) + "/bptf/legacy/prices").buildUpon()
                    .appendQueryParameter("since", String.valueOf(latestUpdate))
                    .build();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(uri.toString())
                    .build();

            Response response = client.newCall(request).execute();

            if (response.body() != null) {
                return parseJson(response.body().byteStream());
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

    private int parseJson(InputStream inputStream) throws IOException {
        //Create a parser from the input stream for fast parsing and low impact on memory
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(inputStream);

        List<Price> prices = new LinkedList<>();
        int retVal = 0;

        //Not a JSON if it doesn't start with START OBJECT
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            return -1;
        }

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String name = parser.getCurrentName();
            parser.nextToken();

            switch (name) {
                case "success":
                    if (parser.getIntValue() == 0) {
                        retVal = 1;
                    }
                    break;
                case "message":
                    errorMessage = parser.getText();
                    break;
                case "prices":

                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        Price price = buildPrice(parser);
                        prices.add(price);
                    }

                    if (prices.size() > 0) {
                        mPriceDao.insert(prices);
                        Log.v(LOG_TAG, "inserted " + prices.size() + " rows into prices table");
                    }
                    break;
            }
        }

        parser.close();

        return retVal;
    }

    private Price buildPrice(JsonParser parser) throws IOException {
        Price price = new Price();

        int defindex = 0;
        int quality = 0;
        int tradable = 0;
        int craftable = 0;
        double value = 0;
        Double high = null;
        double raw = 0;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            parser.nextToken();
            switch (parser.getCurrentName()) {
                case "defindex":
                    Item item = new Item();
                    item.setDefindex(parser.getIntValue());
                    defindex = item.getFixedDefindex();
                    price.setDefindex(defindex);
                    break;
                case "quality":
                    quality = parser.getIntValue();
                    price.setQuality(quality);
                    break;
                case "tradable":
                    tradable = parser.getIntValue();
                    price.setTradable(tradable == 1);
                    break;
                case "craftable":
                    craftable = parser.getIntValue();
                    price.setCraftable(craftable == 1);
                    break;
                case "price_index":
                    price.setPriceIndex(parser.getIntValue());
                    break;
                case "australium":
                    price.setAustralium(parser.getIntValue() == 1);
                    break;
                case "currency":
                    price.setCurrency(parser.getText());
                    break;
                case "value":
                    value = parser.getDoubleValue();
                    price.setValue(value);
                    break;
                case "value_high":
                    high = parser.getDoubleValue();
                    price.setHighValue(high);
                    break;
                case "value_raw":
                    raw = parser.getDoubleValue();
                    break;
                case "last_update":
                    price.setLastUpdate(parser.getLongValue());
                    break;
                case "difference":
                    price.setDifference(parser.getDoubleValue());
                    break;
            }
        }

        price.setWeaponWear(0);

        if (quality == Quality.UNIQUE && tradable == 1 && craftable == 1) {
            if (defindex == 143) { //buds
                Utility.putDouble(mEditor, mContext.getString(R.string.pref_buds_raw), raw);
                mEditor.apply();
            } else if (defindex == 5002) { //metal

                double highPrice = high == null ? 0 : high;

                if (highPrice > value) {
                    //If the metal has a high price, save the average as raw.
                    Utility.putDouble(mEditor, mContext.getString(R.string.pref_metal_raw_usd), ((value + highPrice) / 2));
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