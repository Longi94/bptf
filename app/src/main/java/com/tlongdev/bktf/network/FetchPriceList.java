package com.tlongdev.bktf.network;

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
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    //The listener that will be notified when the fetching finishes
    private OnPriceListFetchListener listener;

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
            final String PRICES_BASE_URL = mContext.getString(R.string.tlongdev_prices);
            final String KEY_SINCE = "since";

            //Build the URI
            Uri.Builder builder = Uri.parse(PRICES_BASE_URL).buildUpon();

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

                builder.appendQueryParameter(KEY_SINCE, String.valueOf(latestUpdate));
            }
            Uri uri = builder.build();

            //Initialize the URL
            URL url = new URL(uri.toString());

            /*if (Utility.isDebugging(mContext))
                Log.v(LOG_TAG, "Built uri: " + uri.toString());*/

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

        if (listener != null) {
            //Notify the listener that the update finished
            listener.onPriceListFetchFinished();
        }
    }

    /**
     * Register a listener which will be notified when the fetching finishes.
     *
     * @param listener the listener to be notified
     */
    public void setOnPriceListFetchListener(OnPriceListFetchListener listener) {
        this.listener = listener;
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
        final String KEY_SUCCESS = "success";
        final String KEY_MESSAGE = "message";
        final String KEY_PRICES = "prices";
        final String KEY_DEFINDEX = "defindex";
        final String KEY_NAME = "item_name";
        final String KEY_QUALITY = "quality";
        final String KEY_TRADABLE = "tradable";
        final String KEY_CRAFTABLE = "craftable";
        final String KEY_PRICE_INDEX = "price_index";
        final String KEY_AUSTRALIUM = "australium";
        final String KEY_CURRENCY = "currency";
        final String KEY_VALUE = "value";
        final String KEY_VALUE_HIGH = "value_high";
        final String KEY_VALUE_RAW = "value_raw";
        final String KEY_LAST_UPDATE = "last_update";
        final String KEY_DIFFERENCE = "difference";

        //Get the response JSON
        JSONObject response = new JSONObject(jsonString);

        if (response.getInt(KEY_SUCCESS) == 0) {
            publishProgress(-2);
            //Unsuccessful query, nothing to do

            errorMessage = response.getString(KEY_MESSAGE);
            if (Utility.isDebugging(mContext))
                Log.e(LOG_TAG, errorMessage);
            return false;
        }

        //Get the items
        JSONArray prices = response.getJSONArray(KEY_PRICES);

        //Notify the task that the download finished and the processing begins
        publishProgress(0, prices.length());

        //Iterator that will iterate through the items
        Vector<ContentValues> cVVector = new Vector<>();

        for (int i = 0; i < prices.length(); i++) {
            JSONObject price = prices.getJSONObject(i);

            int defindex = price.getInt(KEY_DEFINDEX);
            String name = price.getString(KEY_NAME);
            int quality = price.getInt(KEY_QUALITY);
            int tradable = price.getInt(KEY_TRADABLE);
            int craftable = price.getInt(KEY_CRAFTABLE);
            int priceIndex = price.getInt(KEY_PRICE_INDEX);
            int australium = price.getInt(KEY_AUSTRALIUM);
            double value = price.getDouble(KEY_VALUE);
            String currency = price.getString(KEY_CURRENCY);
            long lastUpdate = price.getLong(KEY_LAST_UPDATE);
            double difference = price.getDouble(KEY_DIFFERENCE);
            Double high = null;
            if (price.has(KEY_VALUE_HIGH)) {
                high = price.getDouble(KEY_VALUE_HIGH);
            }

            //Add the price to the CV vector
            cVVector.add(buildContentValues(defindex, name,
                    quality, tradable, craftable, priceIndex, australium,
                    currency, value, high, lastUpdate,
                    difference
            ));

            //Currency prices a processed slightly differently, some more info is
            //saved to the default shared preferences
            if (quality == 6 && tradable == 1 && craftable == 1) {
                //Save extra info about the buds price
                if (defindex == 143) {

                    //Get the sharedpreferences
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(mContext).edit();

                    //Store the price in a string so it can be displayed in the
                    //header in the latest changes page
                    double highPrice = high == null ? 0 : high;

                    String priceString = Utility.formatPrice(
                            mContext, price.getDouble(KEY_VALUE), highPrice,
                            Utility.CURRENCY_KEY, Utility.CURRENCY_KEY, false
                    );
                    editor.putString(mContext.getString(R.string.pref_buds_price), priceString);

                    //Save the difference
                    Utility.putDouble(editor, mContext.getString(R.string.pref_buds_diff), difference);
                    //Save the raw price
                    Utility.putDouble(editor, mContext.getString(R.string.pref_buds_raw), price.getDouble(KEY_VALUE_RAW));

                    editor.apply();

                } else if (defindex == 5002) {//Save extra info about the refined price

                    //Get the sharedpreferences
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(mContext).edit();

                    //Store the price in a string so it can be displayed in the
                    //header in the latest changes page
                    double highPrice = high == null ? 0 : high;

                    String priceString = Utility.formatPrice(
                            mContext, price.getDouble(KEY_VALUE), highPrice,
                            Utility.CURRENCY_USD, Utility.CURRENCY_USD, false
                    );
                    editor.putString(mContext.getString(R.string.pref_metal_price), priceString);

                    //Save the difference
                    Utility.putDouble(editor, mContext.getString(R.string.pref_metal_diff), difference);

                    if (highPrice > value) {
                        //If the metal has a high price, save the average as raw.
                        Utility.putDouble(editor, mContext.getString(R.string.pref_metal_raw_usd), ((value + highPrice) / 2));
                    } else {
                        //save as raw price
                        Utility.putDouble(editor, mContext.getString(R.string.pref_metal_raw_usd), value);
                    }

                    editor.apply();
                } else if (defindex == 5021) {//Save extra info about the key price

                    //Get the sharedpreferences
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(mContext).edit();

                    //Store the price in a string so it can be displayed in the
                    //header in the latest changes page
                    double highPrice = high == null ? 0 : high;

                    String priceString = Utility.formatPrice(
                            mContext, price.getDouble(KEY_VALUE), highPrice,
                            Utility.CURRENCY_METAL, Utility.CURRENCY_METAL, false
                    );
                    editor.putString(mContext.getString(R.string.pref_key_price), priceString);

                    //Save the difference
                    Utility.putDouble(editor, mContext.getString(R.string.pref_key_diff), difference);
                    //Save the raw price
                    Utility.putDouble(editor, mContext.getString(R.string.pref_key_raw), price.getDouble(KEY_VALUE_RAW));

                    editor.apply();
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
     * @param quality    quality of the item
     * @param tradable   tradability if the item
     * @param craftable  craftability of the item
     * @param priceIndex price index of the item
     * @param currency   currency of the price
     * @param value      price of the item
     * @param high       higher price of the item
     * @param update     time of the update
     * @param difference difference of the the new and old price
     * @return the ContentValues object containing the data
     */
    private ContentValues buildContentValues(int defindex, String name, int quality, int tradable, int craftable, int priceIndex, int australium,
                                             String currency, double value, Double high, long update, double difference) {
        //Fix the defindex for pricing
        defindex = Utility.fixDefindex(defindex);

        //The DV that will contain all the data
        ContentValues itemValues = new ContentValues();

        //Put all the data into the content values
        itemValues.put(PriceEntry.COLUMN_DEFINDEX, defindex);
        itemValues.put(PriceEntry.COLUMN_ITEM_NAME, name);
        itemValues.put(PriceEntry.COLUMN_ITEM_QUALITY, quality);
        itemValues.put(PriceEntry.COLUMN_ITEM_TRADABLE, tradable);
        itemValues.put(PriceEntry.COLUMN_ITEM_CRAFTABLE, craftable);
        itemValues.put(PriceEntry.COLUMN_PRICE_INDEX, priceIndex);
        itemValues.put(PriceEntry.COLUMN_AUSTRALIUM, australium);
        itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_CURRENCY, currency);
        itemValues.put(PriceEntry.COLUMN_ITEM_PRICE, value);
        itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_MAX, high);
        itemValues.put(PriceEntry.COLUMN_LAST_UPDATE, update);
        itemValues.put(PriceEntry.COLUMN_DIFFERENCE, difference);
        itemValues.put(PriceEntry.COLUMN_WEAPON_WEAR, 0);

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