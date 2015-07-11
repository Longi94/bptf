package com.tlongdev.bktf.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Task for fetching all data for prices database and updating it in the background.
 */
public class FetchPriceList extends AsyncTask<Void, Integer, Void> {

    private static final String LOG_TAG = FetchPriceList.class.getSimpleName();

    //The context the task runs in
    private final Context mContext;

    //Whether it's an update or full database download
    private boolean updateDatabase;

    //Whether it was a user initiated update
    private boolean manualSync;

    //Dialog to indicate the download progress
    private ProgressDialog loadingDialog;

    //Error message to be displayed to the user
    private String errorMessage;

    //The developer api key used for querying
    private String apiKey;

    //The listener that will be notified when the fetching finishes
    private LinkedList<OnPriceListFetchListener> listeners = new LinkedList<>();

    //the variable that contains the birth time of the youngest price
    private int latestUpdate = 0;

    /**
     * Constructor
     *
     * @param context        the context the task was launched in
     * @param updateDatabase whether the database only needs an update
     * @param manualSync     whether this task was user initiated
     */
    public FetchPriceList(Context context, boolean updateDatabase, boolean manualSync) {
        this.mContext = context;
        this.updateDatabase = updateDatabase;
        this.manualSync = manualSync;

        //Load the api key from the preferences if set
        String savedKey = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_developer_key), "");

        if (!savedKey.equals("")) {
            apiKey = savedKey;
        } else {
            apiKey = context.getString(R.string.api_key_backpack_tf);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPreExecute() {
        if (!updateDatabase)
            //Show the progress dialog
            loadingDialog = ProgressDialog.show(mContext, null, "Downloading data...", true);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void doInBackground(Void... params) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (System.currentTimeMillis() - prefs.getLong(mContext
                .getString(R.string.pref_last_price_list_update), 0) < 3600000L
                && !manualSync) {
            //This task ran less than an hour ago and wasn't a manual sync, nothing to do.
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String itemsJsonStr = null;

        try {
            //The prices api and input keys
            final String PRICES_BASE_URL = mContext.getString(R.string.backpack_tf_get_prices);
            final String KEY_PARAM = "key";
            final String KEY_COMPRESS = "compress";
            final String KEY_APP_ID = "app_id";
            final String KEY_FORMAT = "format";
            final String KEY_RAW = "raw";
            final String KEY_SINCE = "since";

            //Build the URI
            Uri.Builder builder = Uri.parse(PRICES_BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_PARAM, apiKey)
                    .appendQueryParameter(KEY_COMPRESS, "1")
                    .appendQueryParameter(KEY_APP_ID, "440")
                    .appendQueryParameter(KEY_FORMAT, "json")
                    .appendQueryParameter(KEY_RAW, "1");

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
                if (cursor.moveToFirst())
                    latestUpdate = cursor.getInt(0);
                cursor.close();

                builder.appendQueryParameter(KEY_SINCE, String.valueOf(latestUpdate));
            }

            Uri uri = builder.build();

            //Initialize the URL
            URL url = new URL(uri.toString());

            if (Utility.isDebugging(mContext))
                Log.v(LOG_TAG, "Built uri: " + uri.toString());

            //Open connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Get the input stream
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();

            if (inputStream == null) {
                // Stream was empty. Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            //Read the input
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.length() == 0) {
                //Stream was empty, nothing to do.
                return null;
            }
            //Get the jason string
            itemsJsonStr = buffer.toString();

        } catch (IOException e) {
            //There was a network error
            errorMessage = mContext.getString(R.string.error_network);
            publishProgress(-1);
            if (Utility.isDebugging(mContext))
                e.printStackTrace();
            return null;
        } finally {
            //Close the connection
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    //Close the reader
                    reader.close();
                } catch (final IOException e) {
                    //This should never be reached
                    errorMessage = e.getMessage();
                    publishProgress(-1);
                    if (Utility.isDebugging(mContext))
                        e.printStackTrace();
                }

            }
        }
        try {
            //Get all the items from the JSON string
            if (getItemsFromJson(itemsJsonStr)) {
                //Get the shared preferences
                SharedPreferences.Editor editor = prefs.edit();

                //Save when the update finished
                editor.putLong(mContext.getString(R.string.pref_last_price_list_update),
                        System.currentTimeMillis());
                editor.putBoolean(mContext.getString(R.string.pref_initial_load), false);
                editor.apply();
            }

        } catch (JSONException e) {
            //There was an error parsing data
            errorMessage = "error while parsing data";
            publishProgress(-1);
            if (Utility.isDebugging(mContext))
                e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        if (loadingDialog != null) {
            AlertDialog.Builder builder;
            AlertDialog alertDialog;
            switch (values[0]) {
                //Download finished. Replace dialog.
                case 0:
                    loadingDialog.dismiss();
                    loadingDialog = new ProgressDialog(mContext,
                            ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    loadingDialog.setIndeterminate(false);
                    loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    loadingDialog.setMessage(mContext.getString(R.string.message_database_create));
                    loadingDialog.setMax(values[1]);
                    loadingDialog.show();
                    break;
                //One item processed
                case 1:
                    loadingDialog.incrementProgressBy(1);
                    break;
                //There was an error (exception) while trying to create initial database.
                //Show a dialog that the download failed.
                case -1:
                    builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(mContext.getString(R.string.message_database_fail_network))
                            .setCancelable(false)
                            .setPositiveButton(mContext.getString(R.string.action_close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Close app
                                    ((Activity) mContext).finish();
                                }
                            });
                    alertDialog = builder.create();
                    loadingDialog.dismiss();
                    alertDialog.show();
                    break;
                //Api returned 0, unsuccessful
                case -2:
                    builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(mContext.getString(R.string.message_database_fail_network))
                            .setCancelable(false)
                            .setPositiveButton(mContext.getString(R.string.action_close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Close app
                                    ((Activity) mContext).finish();
                                }
                            });
                    alertDialog = builder.create();
                    loadingDialog.dismiss();
                    alertDialog.show();
                    break;
            }
        } else if (values[0] == -1) {
            //There was an error while trying to update database
            Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        } else if (values[0] == -2) {
            Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        //Dismiss loading dialog
        if (loadingDialog != null && !updateDatabase)
            loadingDialog.dismiss();

        for (OnPriceListFetchListener listener : listeners) {
            if (listener != null)
                listener.onPriceListFetchFinished();
        }
    }

    /**
     * Register a listener which will be notified when the fetching finishes.
     *
     * @param listener the listener to be notified
     */
    public void addOnPriceListFetchListener(OnPriceListFetchListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to be removed
     */
    public void removeOnPriceListFetchListener(OnPriceListFetchListener listener) {
        listeners.remove(listener);
    }

    /**
     * Parse all the items from the JSON string.
     *
     * @param jsonString the string to parse from
     * @return whether the query was successful or not
     * @throws JSONException
     */
    private boolean getItemsFromJson(String jsonString) throws JSONException {

        //All the JSON keys needed to parse
        final String OWM_RESPONSE = "response";
        final String OWM_SUCCESS = "success";
        final String OWM_MESSAGE = "message";
        final String OWM_ITEMS = "items";
        final String OWM_PRICES = "prices";
        final String OWM_DEFINDEX = "defindex";
        final String OWM_CURRENCY = "currency";
        final String OWM_VALUE = "value";
        final String OWM_VALUE_HIGH = "value_high";
        final String OWM_VALUE_RAW = "value_raw";
        final String OWM_LAST_UPDATE = "last_update";
        final String OWM_DIFFERENCE = "difference";

        //Get the response JSON
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

        if (response.getInt(OWM_SUCCESS) == 0) {
            publishProgress(-2);
            //Unsuccessful query, nothing to do

            errorMessage = response.getString(OWM_MESSAGE);
            if (Utility.isDebugging(mContext))
                Log.e(LOG_TAG, errorMessage);
            return false;
        }

        //Get the items
        JSONObject items = response.getJSONObject(OWM_ITEMS);

        //Notify the task that the download finished and the processing begins
        publishProgress(0, items.length());

        //Iterator that will iterate through the items
        Iterator<String> i = items.keys();
        Vector<ContentValues> cVVector = new Vector<>();

        //Start iterating
        while (i.hasNext()) {

            //Casting is redundant, but or some reason it doesn't work without it
            String name = i.next();

            //Get the prices of the item
            JSONObject item = items.getJSONObject(name);

            //Check if the item has a price (fucking :weed:)
            if (!item.has(OWM_PRICES))
                continue;
            JSONObject prices = item.getJSONObject(OWM_PRICES);

            if (!item.has(OWM_DEFINDEX))
                continue;
            JSONArray defindexes = item.getJSONArray(OWM_DEFINDEX);

            int defindex = defindexes.getInt(0);

            //Iterate through the qualities
            Iterator<String> qualityIterator = prices.keys();
            while (qualityIterator.hasNext()) {

                //Casting is redundant, but or some reason it doesn't work without it
                String quality = qualityIterator.next();
                JSONObject tradability = prices.getJSONObject(quality);

                //Iterate through tradability
                Iterator<String> tradableIterator = tradability.keys();
                while (tradableIterator.hasNext()) {

                    //Casting is redundant, but or some reason it doesn't work without it
                    String tradable = tradableIterator.next();
                    JSONObject craftability = tradability.getJSONObject(tradable);

                    //Iterate through craftability
                    Iterator<String> craftableIterator = craftability.keys();
                    while (craftableIterator.hasNext()) {

                        //Casting is redundant, but or some reason it doesn't work without it
                        String craftable = craftableIterator.next();

                        JSONObject priceIndexes = craftability.getJSONObject(craftable);

                        //Iterate through the price indexes
                        Iterator<String> priceIndexIterator = priceIndexes.keys();
                        while (priceIndexIterator.hasNext()) {

                            //Casting is redundant but or some reason it doesn't work without it
                            String priceIndex = priceIndexIterator.next();

                            //Get the price
                            JSONObject price = priceIndexes.getJSONObject(priceIndex);

                            //Temporary variables so we can check if they even exist
                            Double high = null;
                            String currency = null;
                            double value = 0;
                            int lastUpdate = 0;
                            double difference = 0;

                            if (price.has(OWM_VALUE_HIGH))
                                high = price.getDouble(OWM_VALUE_HIGH);

                            if (price.has(OWM_CURRENCY))
                                currency = price.getString(OWM_CURRENCY);

                            if (price.has(OWM_VALUE))
                                value = price.getDouble(OWM_VALUE);

                            if (price.has(OWM_LAST_UPDATE))
                                lastUpdate = price.getInt(OWM_LAST_UPDATE);

                            if (price.has(OWM_DIFFERENCE))
                                difference = price.getDouble(OWM_DIFFERENCE);

                            //Add the price to the CV vector
                            cVVector.add(buildContentValues(defindex,
                                    name, quality, tradable, craftable, priceIndex,
                                    currency, value, high, lastUpdate,
                                    difference
                            ));

                            //Currency prices a processed slightly differently, some more info is
                            //saved to the default shared preferences
                            if (quality.equals("6") && tradable.equals("Tradable") &&
                                    craftable.equals("Craftable")) {
                                //Save extra info about the buds price
                                if (defindex == 143) {

                                    //Get the sharedpreferences
                                    SharedPreferences.Editor editor = PreferenceManager
                                            .getDefaultSharedPreferences(mContext).edit();

                                    //Store the price in a string so it can be displayed in the
                                    //header in the latest changes page
                                    double highPrice;
                                    if (price.has(OWM_VALUE_HIGH)) {
                                        highPrice = price.getDouble(OWM_VALUE_HIGH);
                                    } else {
                                        highPrice = 0;
                                    }
                                    String priceString = Utility.formatPrice(
                                            mContext, price.getDouble(OWM_VALUE), highPrice,
                                            Utility.CURRENCY_KEY, Utility.CURRENCY_KEY, false
                                    );
                                    editor.putString(mContext.getString(R.string.pref_buds_price),
                                            priceString);

                                    //Save the difference
                                    Utility.putDouble(editor,
                                            mContext.getString(R.string.pref_buds_diff),
                                            price.getDouble(OWM_DIFFERENCE));
                                    //Save the raw price
                                    Utility.putDouble(editor,
                                            mContext.getString(R.string.pref_buds_raw),
                                            price.getDouble(OWM_VALUE_RAW));

                                    editor.apply();

                                } else if (defindex == 5002) {//Save extra info about the refined price

                                    //Get the sharedpreferences
                                    SharedPreferences.Editor editor = PreferenceManager
                                            .getDefaultSharedPreferences(mContext).edit();

                                    //Store the price in a string so it can be displayed in the
                                    //header in the latest changes page
                                    double highPrice;
                                    if (price.has(OWM_VALUE_HIGH)) {
                                        highPrice = price.getDouble(OWM_VALUE_HIGH);
                                    } else {
                                        highPrice = 0;
                                    }
                                    String priceString = Utility.formatPrice(
                                            mContext, price.getDouble(OWM_VALUE), highPrice,
                                            Utility.CURRENCY_USD, Utility.CURRENCY_USD, false
                                    );
                                    editor.putString(mContext.getString(R.string.pref_metal_price),
                                            priceString);

                                    //Save the difference
                                    Utility.putDouble(editor,
                                            mContext.getString(R.string.pref_metal_diff),
                                            price.getDouble(OWM_DIFFERENCE));

                                    if (price.has(OWM_VALUE_HIGH)) {
                                        //If the metal has a high price, save the average as raw.
                                        Utility.putDouble(editor,
                                                mContext.getString(R.string.pref_metal_raw_usd),
                                                ((price.getDouble(OWM_VALUE) +
                                                        price.getDouble(OWM_VALUE_HIGH)) / 2));
                                    } else {
                                        //save as raw price
                                        Utility.putDouble(editor,
                                                mContext.getString(R.string.pref_metal_raw_usd),
                                                price.getDouble(OWM_VALUE));
                                    }

                                    editor.apply();
                                } else if (defindex == 5021) {//Save extra info about the key price

                                    //Get the sharedpreferences
                                    SharedPreferences.Editor editor = PreferenceManager
                                            .getDefaultSharedPreferences(mContext).edit();

                                    //Store the price in a string so it can be displayed in the
                                    //header in the latest changes page
                                    double highPrice;
                                    if (price.has(OWM_VALUE_HIGH)) {
                                        highPrice = price.getDouble(OWM_VALUE_HIGH);
                                    } else {
                                        highPrice = 0;
                                    }
                                    String priceString = Utility.formatPrice(
                                            mContext, price.getDouble(OWM_VALUE), highPrice,
                                            Utility.CURRENCY_METAL, Utility.CURRENCY_METAL, false
                                    );
                                    editor.putString(mContext.getString(R.string.pref_key_price),
                                            priceString);

                                    //Save the difference
                                    Utility.putDouble(editor,
                                            mContext.getString(R.string.pref_key_diff),
                                            price.getDouble(OWM_DIFFERENCE));
                                    //Save the raw price
                                    Utility.putDouble(editor,
                                            mContext.getString(R.string.pref_key_raw),
                                            price.getDouble(OWM_VALUE_RAW));

                                    editor.apply();
                                }
                            }
                        }
                    }
                }
            }

            //Notify the UI that we finished.
            publishProgress(1);
        }

        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            //Insert all the data into the database
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(PriceEntry.CONTENT_URI, cvArray);
            if (Utility.isDebugging(mContext))
                Log.v(LOG_TAG, "inserted " + rowsInserted + " rows");
        }

        return true;
    }


    /**
     * Convenient method for building content values which are to be inserted into the database
     *
     * @param defindex   defindex of the item
     * @param name       name of the item
     * @param quality    quality of the item
     * @param tradable   tradability if the item
     * @param craftable  craftability of the item
     * @param priceIndex price index of the item
     * @param currency   currency of the price
     * @param value      price of the item
     * @param valueHigh  higher price of the item
     * @param update     time of the update
     * @param difference difference of the the new and old price
     * @return the ContentValues object containing the data
     */
    private ContentValues buildContentValues(int defindex, String name, String quality,
                                             String tradable, String craftable, String priceIndex,
                                             String currency, double value, Double valueHigh, int update, double difference) {
        int itemTradable;
        int itemCraftable;

        //Tradability
        if (tradable.equals("Tradable"))
            itemTradable = 1;
        else
            itemTradable = 0;

        //Craftability
        if (craftable.equals("Craftable"))
            itemCraftable = 1;
        else
            itemCraftable = 0;

        //Fix the defindex for pricing
        defindex = Utility.fixDefindex(defindex);

        //The DV that will contain all the data
        ContentValues itemValues = new ContentValues();

        //Put all the data into the content values
        itemValues.put(PriceEntry.COLUMN_DEFINDEX, defindex);
        itemValues.put(PriceEntry.COLUMN_ITEM_NAME, name);
        itemValues.put(PriceEntry.COLUMN_ITEM_QUALITY, Integer.parseInt(quality));
        itemValues.put(PriceEntry.COLUMN_ITEM_TRADABLE, itemTradable);
        itemValues.put(PriceEntry.COLUMN_ITEM_CRAFTABLE, itemCraftable);
        try {
            itemValues.put(PriceEntry.COLUMN_PRICE_INDEX, Integer.parseInt(priceIndex));
        } catch (NumberFormatException e) {
            e.printStackTrace();

            String[] numbers = priceIndex.split("-");
            int formattedIndex = (Integer.parseInt(numbers[1]) << 16) + Integer.parseInt(numbers[0]);
            itemValues.put(PriceEntry.COLUMN_PRICE_INDEX, formattedIndex);
        }
        itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_CURRENCY, currency);
        itemValues.put(PriceEntry.COLUMN_ITEM_PRICE, value);
        if (valueHigh != null) {
            itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_MAX, valueHigh);
        }
        //TODO placeholder
        itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_RAW, 0);
        itemValues.put(PriceEntry.COLUMN_LAST_UPDATE, update);
        itemValues.put(PriceEntry.COLUMN_DIFFERENCE, difference);

        return itemValues;
    }

    /**
     * Listener interface for listening for the end of the fetch.
     */
    public interface OnPriceListFetchListener {

        /**
         * Notify the listener, that the fetching has stopped.
         */
        void onPriceListFetchFinished();
    }
}