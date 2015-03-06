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
import com.tlongdev.bktf.data.PriceListContract;
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
import java.util.Vector;

/**
 * Task for fetching all data for prices database and updating it in the background.
 */
public class FetchPriceList extends AsyncTask<String, Integer, Void>{

    private static final String LOG_TAG = FetchPriceList.class.getSimpleName();

    private final Context mContext;

    //Whether it's an update or full database download
    private boolean updateDatabase;

    //Whether it was a user initiated update
    private boolean manualSync;
    private ProgressDialog loadingDialog;

    private String errorMessage;

    private OnPriceListFetchListener listener;

    public FetchPriceList(Context context, boolean updateDatabase, boolean manualSync) {
        mContext = context;
        this.updateDatabase = updateDatabase;
        this.manualSync = manualSync;
    }

    @Override
    protected void onPreExecute() {
        if (!updateDatabase)
            loadingDialog = ProgressDialog.show(mContext, null, "Downloading data...", true);
    }

    @Override
    protected Void doInBackground(String... params) {

        if (System.currentTimeMillis() - PreferenceManager.getDefaultSharedPreferences(mContext)
                .getLong(mContext.getString(R.string.pref_last_price_list_update), 0) < 3600000L && !manualSync){
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

            if (Utility.isDebugging(mContext))
                Log.v(LOG_TAG, "Built uri: " + uri.toString());

            //Open connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

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
            itemsJsonStr = buffer.toString();

        } catch (IOException e) {
            errorMessage = "network error";
            publishProgress(-1);
            if (Utility.isDebugging(mContext))
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
                    errorMessage = e.getMessage();
                    publishProgress(-1);
                    if (Utility.isDebugging(mContext))
                        e.printStackTrace();
                }

            }
        }
        try {

            getItemsFromJson(itemsJsonStr);

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

            //Save when the update finished
            editor.putLong(mContext.getString(R.string.pref_last_price_list_update), System.currentTimeMillis());
            editor.putBoolean(mContext.getString(R.string.pref_initial_load), false);
            editor.apply();


        } catch (JSONException e) {
            errorMessage = "error while parsing data";
            publishProgress(-1);
            if (Utility.isDebugging(mContext))
                e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (loadingDialog != null) {
            switch (values[0]) {
                //Download finished. Replace dialog.
                case 0:
                    loadingDialog.dismiss();
                    loadingDialog = new ProgressDialog(mContext, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    loadingDialog.setIndeterminate(false);
                    loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    loadingDialog.setMessage("Creating database...");
                    loadingDialog.setMax(values[1]);
                    loadingDialog.show();
                    break;
                //One item processed
                case 1:
                    loadingDialog.incrementProgressBy(1);
                    break;
                //There was an error (exception) while trying to create initial database
                case -1:
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Failed to download database. Check your internet connection and try again.").setCancelable(false).
                            setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((Activity)mContext).finish();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    loadingDialog.dismiss();
                    alertDialog.show();
                    break;
            }
        } else if (values[0] == -1){
            //There was an error while trying to update database
            Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPostExecute(Void pVoid) {
        //Dismiss loading dialog
        if (loadingDialog != null && !updateDatabase)
            loadingDialog.dismiss();

        if (listener != null){
            listener.onPriceListFetchFinished();
        }
    }

    public void setOnPriceListFetchListener(OnPriceListFetchListener listener) {
        this.listener = listener;
    }

    //Parse all the items from the JSON string.
    private void getItemsFromJson(String jsonString) throws JSONException {

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

		int latestUpdate = 0;
		if (updateDatabase) {
			String[] columns = {PriceListContract.PriceEntry.COLUMN_LAST_UPDATE};
			Cursor cursor = mContext.getContentResolver().query(
					PriceListContract.PriceEntry.CONTENT_URI,
					columns,
					null,
					null,
					PriceListContract.PriceEntry.COLUMN_LAST_UPDATE + " DESC LIMIT 1"
			);
			if (cursor.moveToFirst())
				latestUpdate = cursor.getInt(0);
            cursor.close();
		}
		
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

        if (response.getInt(OWM_SUCCESS) == 0) {
            //Unsuccessful query, nothing to do
            if (Utility.isDebugging(mContext))
                Log.e(LOG_TAG, response.getString(OWM_MESSAGE));
            return;
        }

        JSONObject items = response.getJSONObject(OWM_ITEMS);
        publishProgress(0, items.length());
        Iterator<String> i = items.keys();
        Vector<ContentValues> cVVector = new Vector<>();

        // If any of the currencies was updated, the whole database needs to be updated.
        if (updateDatabase &&
                (items.getJSONObject("Mann Co. Supply Crate Key").getJSONObject("prices").getJSONObject("6").getJSONObject("Tradable")
                .getJSONArray("Craftable").getJSONObject(0).getInt(OWM_LAST_UPDATE) > latestUpdate ||
                items.getJSONObject("Earbuds").getJSONObject("prices").getJSONObject("6").getJSONObject("Tradable")
                        .getJSONArray("Craftable").getJSONObject(0).getInt(OWM_LAST_UPDATE) > latestUpdate ||
                items.getJSONObject("Refined Metal").getJSONObject("prices").getJSONObject("6").getJSONObject("Tradable")
                        .getJSONArray("Craftable").getJSONObject(0).getInt(OWM_LAST_UPDATE) > latestUpdate)) {
            updateDatabase = false;
            latestUpdate = 0;
        }

        while (i.hasNext()) {
            String name = (String)i.next();

            JSONObject prices = items.getJSONObject(name).getJSONObject(OWM_PRICES);
            JSONArray defindexes = items.getJSONObject(name).getJSONArray(OWM_DEFINDEX);

            int defindex = 0;

            //The api does not return any defindex for these items.
            if (defindexes.length() > 0) {
                defindex = defindexes.getInt(0);
            } else if (name.equals("Strange Part: Fires Survived")) {
                defindex = 6057;
            } else if (name.equals("Strange Part: Freezecam Taunt Appearances")) {
                defindex = 6055;
            }

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

                        //If there are multiple price indexes the api return a JSONObject, if there
                        //is only one (0) the api returns a JSONArray (great...)
                        if (craftability.get(craftable) instanceof JSONObject) {
                            JSONObject priceIndexes = craftability.getJSONObject(craftable);
                            Iterator<String> priceIndexIterator = priceIndexes.keys();

                            while (priceIndexIterator.hasNext()) {

                                String priceIndex = (String) priceIndexIterator.next();
                                JSONObject price = priceIndexes.getJSONObject(priceIndex);

                                //Check whether the price is new
								if (latestUpdate < price.getInt(OWM_LAST_UPDATE)) {

									Double high = null;
									if (price.has(OWM_VALUE_HIGH))
										high = price.getDouble(OWM_VALUE_HIGH);

									cVVector.add(buildContentValues(defindex,
											name, quality, tradable, craftable, priceIndex, price.getString(OWM_CURRENCY),
											price.getDouble(OWM_VALUE), high,
											price.getDouble(OWM_VALUE_RAW), price.getInt(OWM_LAST_UPDATE),
											price.getDouble(OWM_DIFFERENCE)
									));
								}
                            }
                        }
                        else {
                            JSONArray priceIndexes = craftability.getJSONArray(craftable);

                            JSONObject price = priceIndexes.getJSONObject(0);

                            //Check whether the price is new
							if (latestUpdate < price.getInt(OWM_LAST_UPDATE)) {
								Double high = null;
								if (price.has(OWM_VALUE_HIGH))
									high = price.getDouble(OWM_VALUE_HIGH);

								cVVector.add(buildContentValues(defindex,
										name, quality, tradable, craftable, "0", price.getString(OWM_CURRENCY),
										price.getDouble(OWM_VALUE), high,
										price.getDouble(OWM_VALUE_RAW), price.getInt(OWM_LAST_UPDATE),
										price.getDouble(OWM_DIFFERENCE)
								));
							}

                            //Currency prices a processed slightly differently, some more info is
                            //saved to the default shared preferences
                            if (quality.equals("6") && tradable.equals("Tradable") && craftable.equals("Craftable"))  {
                                if (defindex == 143) {
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

                                    String priceString = "";
                                    double itemPrice = price.getDouble(OWM_VALUE);

                                    if ((int)itemPrice == itemPrice)
                                        priceString = priceString + (int)itemPrice;
                                    else
                                        priceString = priceString + itemPrice;

                                    if (price.has(OWM_VALUE_HIGH)) {
                                        itemPrice = price.getDouble(OWM_VALUE_HIGH);

                                        if ((int)itemPrice == itemPrice)
                                            priceString = priceString + "-" + (int)itemPrice;
                                        else
                                            priceString = priceString + "-" + itemPrice;
                                    }

                                    priceString = priceString + " keys";

                                    editor.putString(mContext.getString(R.string.pref_buds_price), priceString);
                                    Utility.putDouble(editor, mContext.getString(R.string.pref_buds_diff), price.getDouble(OWM_DIFFERENCE));
                                    Utility.putDouble(editor, mContext.getString(R.string.pref_buds_raw), price.getDouble(OWM_VALUE_RAW));

                                    editor.apply();
                                } else if (defindex == 5002) {
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

                                    String priceString = "$";
                                    double itemPrice = price.getDouble(OWM_VALUE);

                                    if ((int)itemPrice == itemPrice)
                                        priceString = priceString + (int)itemPrice;
                                    else
                                        priceString = priceString + itemPrice;

                                    if (price.has(OWM_VALUE_HIGH)) {
                                        itemPrice = price.getDouble(OWM_VALUE_HIGH);

                                        if ((int)itemPrice == itemPrice)
                                            priceString = priceString + "-" + (int)itemPrice;
                                        else
                                            priceString = priceString + "-" + itemPrice;
                                    }

                                    editor.putString(mContext.getString(R.string.pref_metal_price), priceString);
                                    Utility.putDouble(editor, mContext.getString(R.string.pref_metal_diff), price.getDouble(OWM_DIFFERENCE));

                                    if (price.has(OWM_VALUE_HIGH)){
                                        Utility.putDouble(editor, mContext.getString(R.string.pref_metal_raw_usd),
                                                ((price.getDouble(OWM_VALUE) + price.getDouble(OWM_VALUE_HIGH)) / 2));
                                    } else {

                                        Utility.putDouble(editor, mContext.getString(R.string.pref_metal_raw_usd),
                                                price.getDouble(OWM_VALUE));
                                    }

                                    editor.apply();
                                } else if (defindex == 5021) {
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

                                    String priceString = "";
                                    double itemPrice = price.getDouble(OWM_VALUE);

                                    if ((int)itemPrice == itemPrice)
                                        priceString = priceString + (int)itemPrice;
                                    else
                                        priceString = priceString + itemPrice;

                                    if (price.has(OWM_VALUE_HIGH)) {
                                        itemPrice = price.getDouble(OWM_VALUE_HIGH);

                                        if ((int)itemPrice == itemPrice)
                                            priceString = priceString + "-" + (int)itemPrice;
                                        else
                                            priceString = priceString + "-" + itemPrice;
                                    }

                                    priceString = priceString + " ref";

                                    editor.putString(mContext.getString(R.string.pref_key_price), priceString);
                                    Utility.putDouble(editor, mContext.getString(R.string.pref_key_diff), price.getDouble(OWM_DIFFERENCE));
                                    Utility.putDouble(editor, mContext.getString(R.string.pref_key_raw), price.getDouble(OWM_VALUE_RAW));

                                    editor.apply();
                                }
                            }
                        }
                    }
                }
            }

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
    }

    //Convenient method for building content values which are to be inserted into the database
    private ContentValues buildContentValues(int defindex, String name, String quality, String tradable,
                                             String craftable, String priceIndex, String currency,
                                             double value, Double valueHigh, double valueRaw,
                                             int update, double difference){
        int itemTradable;
        int itemCraftable;

        if (tradable.equals("Tradable"))
            itemTradable = 1;
        else
            itemTradable = 0;

        if (craftable.equals("Craftable"))
            itemCraftable = 1;
        else
            itemCraftable = 0;

        defindex = Utility.fixDefindex(defindex);

        ContentValues itemValues = new ContentValues();

        itemValues.put(PriceEntry.COLUMN_DEFINDEX, defindex);
        itemValues.put(PriceEntry.COLUMN_ITEM_NAME, name);
        itemValues.put(PriceEntry.COLUMN_ITEM_QUALITY, Integer.parseInt(quality));
        itemValues.put(PriceEntry.COLUMN_ITEM_TRADABLE, itemTradable);
        itemValues.put(PriceEntry.COLUMN_ITEM_CRAFTABLE, itemCraftable);
        itemValues.put(PriceEntry.COLUMN_PRICE_INDEX, Integer.parseInt(priceIndex));
        itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_CURRENCY, currency);
        itemValues.put(PriceEntry.COLUMN_ITEM_PRICE, value);
        if (valueHigh != null) {
            itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_MAX, valueHigh);
        }
        itemValues.put(PriceEntry.COLUMN_ITEM_PRICE_RAW, valueRaw);
        itemValues.put(PriceEntry.COLUMN_LAST_UPDATE, update);
        itemValues.put(PriceEntry.COLUMN_DIFFERENCE, difference);

        return itemValues;
    }

    public static interface OnPriceListFetchListener{
        public void onPriceListFetchFinished();
    }
}