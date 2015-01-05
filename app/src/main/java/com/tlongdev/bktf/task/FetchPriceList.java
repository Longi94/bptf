package com.tlongdev.bktf.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
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

public class FetchPriceList extends AsyncTask<String, Integer, Void>{

    private static final String LOG_TAG = FetchPriceList.class.getSimpleName();

    private final Context mContext;
    private boolean updateDatabase;
    private boolean manualSync;
    private ProgressDialog loadingDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout header;
    private String errorMessage;

    public FetchPriceList(Context context, boolean updateDatabase, boolean manualSync, SwipeRefreshLayout swipeRefreshLayout, LinearLayout header) {
        mContext = context;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.header = header;
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

            Log.v(LOG_TAG, "Built Uri = " + uri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.length() == 0) {
                return null;
            }
            itemsJsonStr = buffer.toString();

        } catch (IOException e) {
            errorMessage = "network error";
            publishProgress(-1);
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
                    e.printStackTrace();
                }

            }
        }
        try {

            getItemsFromJson(itemsJsonStr);

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
            editor.putLong(mContext.getString(R.string.pref_last_price_list_update), System.currentTimeMillis());
            editor.putBoolean(mContext.getString(R.string.pref_initial_load), false);
            editor.apply();


        } catch (JSONException e) {
            errorMessage = "error while parsing data";
            publishProgress(-1);
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (loadingDialog != null) {
            switch (values[0]) {
                case 0:
                    loadingDialog.dismiss();
                    loadingDialog = new ProgressDialog(mContext, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    loadingDialog.setIndeterminate(false);
                    loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    loadingDialog.setMessage("Creating database...");
                    loadingDialog.setMax(values[1]);
                    loadingDialog.show();
                    break;
                case 1:
                    loadingDialog.incrementProgressBy(1);
                    break;
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
            Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPostExecute(Void pVoid) {
        if (loadingDialog != null && !updateDatabase)
            loadingDialog.dismiss();
        else if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

        if (header != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

            ((TextView) header.findViewById(R.id.text_view_metal_price))
                    .setText(prefs.getString(mContext.getString(R.string.pref_metal_price), ""));
            ((TextView) header.findViewById(R.id.text_view_key_price))
                    .setText(prefs.getString(mContext.getString(R.string.pref_key_price), ""));
            ((TextView) header.findViewById(R.id.text_view_buds_price))
                    .setText(prefs.getString(mContext.getString(R.string.pref_buds_price), ""));

            if (prefs.getFloat(mContext.getString(R.string.pref_metal_diff), 0) > 0) {
                header.findViewById(R.id.image_view_metal_price).setBackgroundColor(0xff008504);
            } else {
                header.findViewById(R.id.image_view_metal_price).setBackgroundColor(0xff850000);
            }
            if (prefs.getFloat(mContext.getString(R.string.pref_key_diff), 0) > 0) {
                header.findViewById(R.id.image_view_key_price).setBackgroundColor(0xff008504);
            } else {
                header.findViewById(R.id.image_view_key_price).setBackgroundColor(0xff850000);
            }
            if (prefs.getFloat(mContext.getString(R.string.pref_buds_diff), 0) > 0) {
                header.findViewById(R.id.image_view_buds_price).setBackgroundColor(0xff008504);
            } else {
                header.findViewById(R.id.image_view_buds_price).setBackgroundColor(0xff850000);
            }
        }
    }

    private void getItemsFromJson(String jsonString) throws JSONException {

        final String OWM_RESPONSE = "response";
        final String OWM_SUCCESS = "success";
        final String OWM_MESSAGE = "message";
        final String OWM_CURRENT_TIME = "current_time";
        final String OWM_RAW_USD_VALUE = "raw_usd_value";
        final String OWM_USD_CURRENCY = "usd_currency";
        final String OWM_USD_CURRENCY_INDEX = "usd_currency_index";
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
		}
		
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);
		

        if (response.getInt(OWM_SUCCESS) == 0) {
            Log.e(LOG_TAG, response.getString(OWM_MESSAGE));
            return;
        }

        JSONObject items = response.getJSONObject(OWM_ITEMS);

        publishProgress(0, items.length());

        Iterator<String> i = items.keys();

        Vector<ContentValues> cVVector = new Vector<>();

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

                        if (craftability.get(craftable) instanceof JSONObject) {
                            JSONObject priceIndexes = craftability.getJSONObject(craftable);
                            Iterator<String> priceIndexIterator = priceIndexes.keys();

                            while (priceIndexIterator.hasNext()) {

                                String priceIndex = (String) priceIndexIterator.next();
                                JSONObject price = priceIndexes.getJSONObject(priceIndex);
								
								if (latestUpdate <= price.getInt(OWM_LAST_UPDATE)) {

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

							if (latestUpdate <= price.getInt(OWM_LAST_UPDATE)) {
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
                                    editor.putFloat(mContext.getString(R.string.pref_buds_diff), (float) price.getDouble(OWM_DIFFERENCE));
                                    editor.putFloat(mContext.getString(R.string.pref_buds_raw), (float) price.getDouble(OWM_VALUE_RAW));

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
                                    editor.putFloat(mContext.getString(R.string.pref_metal_diff), (float) price.getDouble(OWM_DIFFERENCE));

                                    if (price.has(OWM_VALUE_HIGH)){
                                        editor.putFloat(mContext.getString(R.string.pref_metal_raw_usd),
                                                (float)((price.getDouble(OWM_VALUE) + price.getDouble(OWM_VALUE_HIGH)) / 2));
                                    } else {
                                        editor.putFloat(mContext.getString(R.string.pref_metal_raw_usd), (float) price.getDouble(OWM_VALUE));
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
                                    editor.putFloat(mContext.getString(R.string.pref_key_diff), (float) price.getDouble(OWM_DIFFERENCE));
                                    editor.putFloat(mContext.getString(R.string.pref_key_raw), (float) price.getDouble(OWM_VALUE_RAW));

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
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(PriceEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows");
            // Use a DEBUG variable to gate whether or not you do this, so you can easily
            // turn it on and off, and so that it's easy to see what you can rip out if
            // you ever want to remove it.
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
            } else {
                Log.v(LOG_TAG, "Query failed! :( **********");
            }
        }
    }

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
}