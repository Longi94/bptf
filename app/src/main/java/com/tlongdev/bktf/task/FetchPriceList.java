package com.tlongdev.bktf.task;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.tlongdev.bktf.adapter.PriceListAdapter;
import com.tlongdev.bktf.data.PriceListContract.PriceEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class FetchPriceList extends AsyncTask<String, Void, ArrayList<String>> implements LoaderManager.LoaderCallbacks{

    private static final String LOG_TAG = FetchPriceList.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private final Context mContext;
    private ProgressDialog loadingDialog;

    public FetchPriceList(Context context, RecyclerView recyclerView) {
        mContext = context;
        mRecyclerView = recyclerView;
    }

    @Override
    protected void onPreExecute() {
        loadingDialog = ProgressDialog.show(mContext, null, "Please wait...", true);
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String itemsJsonStr = null;

        try {
            final String PRICES_BASE_URL = "http://backpack.tf/api/IGetPrices/v4/";
            final String KEY_PARAM = "key";
            final String KEY_COMPRESS = "compress";
            final String KEY_APP_ID = "app_id";
            final String KEY_FORMAT = "format";
            final String KEY_RAW = "raw";

            Uri uri = Uri.parse(PRICES_BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_PARAM, params[0])
                    .appendQueryParameter(KEY_COMPRESS, "1")
                    .appendQueryParameter(KEY_APP_ID, "440")
                    .appendQueryParameter(KEY_FORMAT, "json")
                    .appendQueryParameter(KEY_RAW, "1")
                    .build();

            URL url = new URL(uri.toString());

            Log.v(LOG_TAG, "Built Uri = " + uri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            itemsJsonStr = buffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("HomeFragment", "Error closing stream", e);
                }
            }
        }
        try {
            return getItemsFromJson(itemsJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        mRecyclerView.setAdapter(new PriceListAdapter(strings));
        if (loadingDialog != null)
            loadingDialog.dismiss();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private ArrayList<String> getItemsFromJson(String jsonString) throws JSONException {

        final String OWM_RESPONSE = "response";
        final String OWM_SUCCESS = "success";
        final String OWM_MESSAGE = "message";
        final String OWM_CURRENT_TIME = "current_time";
        final String OWM_RAW_USD_VALUE = "raw_usd_value";
        final String OWM_USD_CURRENCY = "usd_currency";
        final String OWM_USD_CURRENCY_INDEX = "usd_currency_index";
        final String OWM_ITEMS = "items";
        final String OWM_PRICES = "prices";
        final String OWM_CURRENCY = "currency";
        final String OWM_VALUE = "value";
        final String OWM_VALUE_HIGH = "value_high";
        final String OWM_VALUE_RAW = "value_raw";
        final String OWM_LAST_UPDATE = "last_update";
        final String OWM_DIFFERENCE = "difference";

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

        if (response.getInt(OWM_SUCCESS) == 0) {
            Log.v(LOG_TAG, response.getString(OWM_MESSAGE));
            return null;
        }

        JSONObject items = response.getJSONObject(OWM_ITEMS);

        Iterator<String> i = items.keys();

        ArrayList<String> itemNames = new ArrayList<>();
        Vector<ContentValues> cVVector = new Vector<>();

        while (i.hasNext()) {
            String name = (String)i.next();

            JSONObject prices = items.getJSONObject(name).getJSONObject(OWM_PRICES);

            Iterator<String> qualityIterator = prices.keys();

            while (qualityIterator.hasNext()){

                String quality = (String)qualityIterator.next();
                JSONObject tradability = prices.getJSONObject(quality);
                Iterator<String> tradableIterator = tradability.keys();

                while (tradableIterator.hasNext()) {

                    String tradable = (String)tradableIterator.next();
                    JSONObject craftability = tradability.getJSONObject(tradable);
                    Iterator<String> craftableIterator = craftability.keys();

                    while (craftableIterator.hasNext()){

                        String craftable = (String)craftableIterator.next();
                        if (craftability.get(craftable) instanceof JSONObject) {
                            JSONObject priceIndexes = craftability.getJSONObject(craftable);
                            Iterator<String> priceIndexIterator = priceIndexes.keys();

                            while (priceIndexIterator.hasNext()) {

                                String priceIndex = (String) priceIndexIterator.next();
                                JSONObject price = priceIndexes.getJSONObject(priceIndex);

                                String temporary = "";

                                temporary = temporary + name + " - " + tradable + " - " + craftable + " - " + quality + " - " + priceIndex + "\n";
                                temporary = temporary + "Price: " + price.getDouble(OWM_VALUE);

                                if (price.has(OWM_VALUE_HIGH)) {
                                    temporary = temporary + " - " + price.getDouble(OWM_VALUE_HIGH);
                                }
                                temporary = temporary + " " + price.getString(OWM_CURRENCY) + "\n";
                                temporary = temporary + "Raw: " + price.getDouble(OWM_VALUE_RAW) + "\n";
                                temporary = temporary + "Last Update: " + price.getInt(OWM_LAST_UPDATE) + "\n";
                                temporary = temporary + "Difference: " + price.getDouble(OWM_DIFFERENCE);

                                itemNames.add(temporary);

                                ContentValues itemValues = new ContentValues();

                                itemValues.put(PriceEntry.COLUMN_ITEM_NAME, name);
                                itemValues.put(PriceEntry.COLUMN_ITEM_QUALITY, quality);
                                itemValues.put(PriceEntry.COLUMN_ITEM_TRADABLE, tradable);
                                itemValues.put(PriceEntry.COLUMN_ITEM_CRAFTABLE, craftable);
                                itemValues.put(PriceEntry.COLUMN_PRICE_INDEX, priceIndex);
                                itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_CURRENCY, price.getString(OWM_CURRENCY));
                                itemValues.put(PriceEntry.COLUMN_ITEM_PRICE, price.getDouble(OWM_VALUE));
                                if (price.has(OWM_VALUE_HIGH)) {
                                    itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_MAX, price.has(OWM_VALUE_HIGH));
                                }
                                itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_RAW, price.getDouble(OWM_VALUE_RAW));
                                itemValues.put(PriceEntry.COLUMN_LAST_UPDATE, price.getInt(OWM_LAST_UPDATE));
                                itemValues.put(PriceEntry.COLUMN_DIFFERENCE, price.getDouble(OWM_DIFFERENCE));

                                cVVector.add(itemValues);
                            }
                        }
                        else {
                            JSONArray priceIndexes = craftability.getJSONArray(craftable);

                            JSONObject price = priceIndexes.getJSONObject(0);
                            String temporary = "";

                            temporary = temporary + name + " - " + tradable + " - " + craftable + " - " + quality + " - 0" + "\n";
                            temporary = temporary + "Price: " + price.getDouble(OWM_VALUE);

                            if (price.has(OWM_VALUE_HIGH)) {
                                temporary = temporary + " - " + price.getDouble(OWM_VALUE_HIGH);
                            }
                            temporary = temporary + " " + price.getString(OWM_CURRENCY) + "\n";
                            temporary = temporary + "Raw: " + price.getDouble(OWM_VALUE_RAW) + "\n";
                            temporary = temporary + "Last Update: " + price.getInt(OWM_LAST_UPDATE) + "\n";
                            temporary = temporary + "Difference: " + price.getDouble(OWM_DIFFERENCE);

                            itemNames.add(temporary);

                            ContentValues itemValues = new ContentValues();

                            itemValues.put(PriceEntry.COLUMN_ITEM_NAME, name);
                            itemValues.put(PriceEntry.COLUMN_ITEM_QUALITY, quality);
                            itemValues.put(PriceEntry.COLUMN_ITEM_TRADABLE, tradable);
                            itemValues.put(PriceEntry.COLUMN_ITEM_CRAFTABLE, craftable);
                            itemValues.put(PriceEntry.COLUMN_PRICE_INDEX, 0);
                            itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_CURRENCY, price.getString(OWM_CURRENCY));
                            itemValues.put(PriceEntry.COLUMN_ITEM_PRICE, price.getDouble(OWM_VALUE));
                            if (price.has(OWM_VALUE_HIGH)) {
                                itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_MAX, price.has(OWM_VALUE_HIGH));
                            }
                            itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_RAW, price.getDouble(OWM_VALUE_RAW));
                            itemValues.put(PriceEntry.COLUMN_LAST_UPDATE, price.getInt(OWM_LAST_UPDATE));
                            itemValues.put(PriceEntry.COLUMN_DIFFERENCE, price.getDouble(OWM_DIFFERENCE));

                            cVVector.add(itemValues);
                        }
                    }
                }
            }
        }

        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(PriceEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows of weather data");
            // Use a DEBUG variable to gate whether or not you do this, so you can easily
            // turn it on and off, and so that it's easy to see what you can rip out if
            // you ever want to remove it.
            if (true) {
                Cursor priceListCursor = mContext.getContentResolver().query(
                        PriceEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );

                if (priceListCursor.moveToFirst()) {
                    ContentValues resultValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(priceListCursor, resultValues);
                    Log.v(LOG_TAG, "Query succeeded! **********");
                    for (String key : resultValues.keySet()) {
                        Log.v(LOG_TAG, key + ": " + resultValues.getAsString(key));
                    }
                } else {
                    Log.v(LOG_TAG, "Query failed! :( **********");
                }
            }
        }
        return itemNames;
    }
}