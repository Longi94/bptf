/**
 * Copyright 2015 Long Tran
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.network;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.android.gms.analytics.HitBuilders;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.BuildConfig;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetPriceHistory extends AsyncTask<Void, Void, Integer> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = GetPriceHistory.class.getSimpleName();

    private Context mContext;

    private Item item;

    private OnPriceHistoryListener listener;

    private String errorMessage;

    private List<Price> result = new LinkedList<>();

    public GetPriceHistory(Context context, Item item) {
        this.item = item;
        mContext = context;
    }

    @Override
    protected Integer doInBackground(Void... params) {

        try {
            final String BASE_URL = "http://backpack.tf/api/IGetPriceHistory/v1/";
            final String KEY_DEV = "key";
            final String KEY_ITEM = "item";
            final String KEY_QUALITY = "quality";
            final String KEY_TRADABLE = "tradable";
            final String KEY_CRAFTABLE = "craftable";
            final String KEY_PRICE_INDEX = "priceindex";

            //Build the URI
            Uri.Builder builder = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_DEV, BuildConfig.BACKPACK_TF_API_KEY)
                    .appendQueryParameter(KEY_ITEM, String.valueOf(item.isAustralium() ? "Australium " + item.getName() : item.getDefindex()))
                    .appendQueryParameter(KEY_QUALITY, String.valueOf(item.getQuality()))
                    .appendQueryParameter(KEY_TRADABLE, String.valueOf(item.isTradable() ? 1 : 0))
                    .appendQueryParameter(KEY_CRAFTABLE, String.valueOf(item.isCraftable() ? 1 : 0))
                    .appendQueryParameter(KEY_PRICE_INDEX, String.valueOf(item.getPriceIndex()));

            Uri uri = builder.build();

            //Initialize the URL
            URL url = new URL(uri.toString());

            Log.v(LOG_TAG, "Built uri: " + uri.toString());

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            int statusCode = response.code();

            if (statusCode >= 500) {
                errorMessage = "Server error: " + statusCode;
                return -1;
            } else if (statusCode >= 400) {
                errorMessage = "Client error: " + statusCode;
                return -1;
            }

            //Get the input stream
            InputStream inputStream = response.body().byteStream();

            if (inputStream == null) {
                // Stream was empty. Nothing to do.
                return -1;
            }

            return parseJsonString(inputStream);

        } catch (JsonParseException e) {
            e.printStackTrace();

            ((BptfApplication) mContext.getApplicationContext()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("JSON exception:GetPriceHistory, Message: " + e.getMessage())
                    .setFatal(true)
                    .build());

        } catch (IOException e) {
            e.printStackTrace();

            ((BptfApplication) mContext.getApplicationContext()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Network exception:GetPriceHistory, Message: " + e.getMessage())
                    .setFatal(false)
                    .build());
        }

        return -1;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            if (integer >= 0) {
                //Notify the listener that the update finished
                listener.onPriceHistoryFinished(result);
            } else {
                listener.onPriceHistoryFailed(errorMessage);
            }
        }
    }

    private int parseJsonString(InputStream inputStream) throws IOException {

        //All the JSON keys needed to parse
        final String KEY_SUCCESS = "success";
        final String KEY_MESSAGE = "message";
        final String KEY_VALUE = "value";
        final String KEY_VALUE_HIGH = "value_high";
        final String KEY_CURRENCY = "currency";
        final String KEY_TIMESTAMP = "timestamp";

        //Create a parser from the input stream for fast parsing and low impact on memory
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(inputStream);

        //Not a JSON if it doesn't start with START OBJECT
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            return -1;
        }

        JsonToken token;
        while ((token = parser.nextToken()) != JsonToken.END_OBJECT) {

            //success object
            if (parser.getCurrentName().equals(KEY_SUCCESS)) {
                parser.nextToken();
                if (parser.getIntValue() == 0) {
                    //Unsuccessful query, nothing to do

                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        if (parser.getCurrentName().equals(KEY_MESSAGE)) {
                            errorMessage = parser.getText();
                            Log.e(LOG_TAG, errorMessage);
                        }
                    }
                    parser.close();
                    return -1;
                }
            }

            //Start of the items that contains the items
            if (token == JsonToken.START_ARRAY) {
                //Keep iterating while the array hasn't ended
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    double value = 0;
                    double valueHigh = 0;
                    String currency = null;
                    long timestamp = 0;

                    //Parse an attribute and get the value of it
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        parser.nextToken();
                        switch (parser.getCurrentName()) {
                            case KEY_VALUE:
                                value = parser.getDoubleValue();
                                break;
                            case KEY_VALUE_HIGH:
                                valueHigh = parser.getDoubleValue();
                                break;
                            case KEY_CURRENCY:
                                currency = parser.getText();
                                break;
                            case KEY_TIMESTAMP:
                                timestamp = parser.getLongValue() * 1000L;
                                break;
                        }
                    }

                    // TODO: 2015. 11. 15. don't know what to do with hat currencies yet
                    if (currency != null && !currency.equals("hat")) {
                        result.add(new Price(value, valueHigh, -1, timestamp, -1, currency));
                    }
                }

                return 0;
            }
        }

        return -1;
    }

    /**
     * Register a listener which will be notified when the fetching finishes.
     *
     * @param listener the listener to be notified
     */
    public void setListener(OnPriceHistoryListener listener) {
        this.listener = listener;
    }

    /**
     * Listener interface
     */
    public interface OnPriceHistoryListener {

        /**
         * Notify the listener, that the fetching has stopped.
         */
        void onPriceHistoryFinished(List<Price> prices);

        void onPriceHistoryFailed(String errorMessage);
    }
}
