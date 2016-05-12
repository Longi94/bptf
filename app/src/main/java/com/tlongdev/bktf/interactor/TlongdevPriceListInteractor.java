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

import com.google.gson.stream.JsonReader;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.network.TlongdevInterface;
import com.tlongdev.bktf.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.inject.Inject;

import okhttp3.ResponseBody;
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

            Response<ResponseBody> response = mTlongdevInterface.getPrices(latestUpdate).execute();

            if (response.body() != null) {
                return parseJson(response.body().byteStream());
            } else if (response.raw().code() >= 500) {
                errorMessage = "Server error: " + response.raw().code();
            } else if (response.raw().code() >= 400) {
                errorMessage = "Client error: " + response.raw().code();
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

        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));

        Vector<ContentValues> cVVector = new Vector<>();
        int retVal = 0;
        int count = 0;

        reader.beginObject();

        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "success":
                    if (reader.nextInt() == 0) {
                        retVal = 1;
                    }
                    break;
                case "message":
                    errorMessage = reader.nextString();
                    break;
                case "count":
                    count = reader.nextInt();
                    break;
                case "prices":
                    reader.beginArray();

                    while (reader.hasNext()) {
                        ContentValues values = buildContentValues(reader);
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

                    reader.endArray();
                    break;
                default:
                    reader.skipValue();;
                    break;
            }
        }

        reader.endObject();

        return retVal;
    }

    private ContentValues buildContentValues(JsonReader reader) throws IOException {
        ContentValues values = new ContentValues();

        int defindex = 0;
        int quality = 0;
        int tradable = 0;
        int craftable = 0;
        double value = 0;
        Double high = null;
        double raw = 0;

        reader.beginObject();

        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "defindex":
                    Item item = new Item();
                    item.setDefindex(reader.nextInt());
                    defindex = item.getFixedDefindex();
                    values.put(PriceEntry.COLUMN_DEFINDEX, defindex);
                    break;
                case "quality":
                    quality = reader.nextInt();
                    values.put(PriceEntry.COLUMN_ITEM_QUALITY, quality);
                    break;
                case "tradable":
                    tradable = reader.nextInt();
                    values.put(PriceEntry.COLUMN_ITEM_TRADABLE, tradable);
                    break;
                case "craftable":
                    craftable = reader.nextInt();
                    values.put(PriceEntry.COLUMN_ITEM_CRAFTABLE, craftable);
                    break;
                case "price_index":
                    values.put(PriceEntry.COLUMN_PRICE_INDEX, reader.nextInt());
                    break;
                case "australium":
                    values.put(PriceEntry.COLUMN_AUSTRALIUM, reader.nextInt());
                    break;
                case "currency":
                    values.put(PriceEntry.COLUMN_CURRENCY, reader.nextString());
                    break;
                case "value":
                    value = reader.nextDouble();
                    values.put(PriceEntry.COLUMN_PRICE, value);
                    break;
                case "value_high":
                    high = reader.nextDouble();
                    values.put(PriceEntry.COLUMN_PRICE_HIGH, high);
                    break;
                case "value_raw":
                    raw = reader.nextDouble();
                    break;
                case "last_update":
                    values.put(PriceEntry.COLUMN_LAST_UPDATE, reader.nextLong());
                    break;
                case "difference":
                    values.put(PriceEntry.COLUMN_DIFFERENCE, reader.nextDouble());
                    break;
                default:
                    reader.skipValue();;
                    break;
            }
        }
        reader.endObject();
        values.put(PriceEntry.COLUMN_WEAPON_WEAR, 0);

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