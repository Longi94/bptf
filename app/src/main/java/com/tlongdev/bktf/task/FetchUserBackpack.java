package com.tlongdev.bktf.task;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.data.UserBackpackContract.UserBackpackEntry;

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

public class FetchUserBackpack extends AsyncTask<Void, Void, Void> {
    
    public static final String LOG_TAG = FetchUserBackpack.class.getSimpleName();

    private static final String OWM_UNIQUE_ID = "id";
    private static final String OWM_ORIGINAL_ID = "original_id";
    private static final String OWM_DEFINDEX = "defindex";
    private static final String OWM_LEVEL = "level";
    private static final String OWM_ORIGIN = "origin";
    private static final String OWM_UNTRADABLE = "flag_cannot_trade";
    private static final String OWM_UNCRAFTABLE = "flag_cannot_craft";
    private static final String OWM_INVENTORY_TOKEN = "inventory";
    private static final String OWM_QUALITY = "quality";
    private static final String OWM_CUSTOM_NAME = "custom_name";
    private static final String OWM_CUSTOM_DESCRIPTION = "custom_desc";
    private static final String OWM_CONTENT = "contained_item";
    private static final String OWM_ATTRIBUTES = "attributes";
    private static final String OWM_EQUIPPED = "equipped";
    private static final String OWM_FLOAT_VALUE = "float_value";
    private static final String OWM_ACCOUNT_INFO = "account_info";
    private static final String OWM_PERSONA_NAME = "personaname";
    private static final String OWM_VALUE = "value";

    private Context mContext;
    private boolean manualSync;

    private String errorMessage;

    public FetchUserBackpack(Context context, boolean manualSync) {
        mContext = context;
        this.manualSync = manualSync;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (System.currentTimeMillis() - PreferenceManager.getDefaultSharedPreferences(mContext)
                .getLong(mContext.getString(R.string.pref_last_backpack_update), 0) < 3600000L && !manualSync){
            //This task ran less than an hour ago and wasn't a manual sync, nothing to do.
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String jsonStr = null;

        try {
            final String KEY_PARAM = "key";
            final String ID_PARAM = "SteamID";
            
            Uri uri = Uri.parse(mContext.getString(R.string.steam_get_player_items_url)).buildUpon()
                    .appendQueryParameter(KEY_PARAM, mContext.getString(R.string.steam_web_api_key))
                    .appendQueryParameter(ID_PARAM, PreferenceManager.getDefaultSharedPreferences(mContext)
                            .getString(mContext.getString(R.string.pref_resolved_steam_id), ""))
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
            jsonStr = buffer.toString();

        } catch (IOException e) {
            errorMessage = "network error";
            publishProgress();
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
                    publishProgress();
                    if (Utility.isDebugging(mContext))
                        e.printStackTrace();
                }

            }
        }
        try {

            getItemsFromJson(jsonStr);

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

            //Save when the update finished
            editor.putLong(mContext.getString(R.string.pref_last_backpack_update), System.currentTimeMillis()).apply();


        } catch (JSONException e) {
            errorMessage = "error while parsing data";
            publishProgress();
            if (Utility.isDebugging(mContext))
                e.printStackTrace();
            return null;
        }
        return null;
    }

    private void getItemsFromJson(String jsonStr) throws JSONException {
        final String OWM_RESULT = "result";
        final String OWM_STATUS = "status";
        final String OWM_SLOTS = "num_backpack_slots";
        final String OWM_ITEMS = "items";

        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONObject response = jsonObject.getJSONObject(OWM_RESULT);

        switch (response.getInt(OWM_STATUS)){
            case 1:
                Vector<ContentValues> cVVector = new Vector<>();
                JSONArray items = response.getJSONArray(OWM_ITEMS);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(mContext.getString(R.string.pref_user_slots), response.getInt(OWM_SLOTS));
                editor.putInt(mContext.getString(R.string.pref_user_items), items.length());
                editor.apply();

                for (int i = 0; i < items.length(); i++){
                    cVVector.add(buildContentValues(items.getJSONObject(i)));
                }

                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    //Insert all the data into the database
                    int rowsInserted = mContext.getContentResolver()
                            .bulkInsert(UserBackpackEntry.CONTENT_URI, cvArray);
                    if (Utility.isDebugging(mContext))
                        Log.v(LOG_TAG, "inserted " + rowsInserted + " rows");
                    // Use a DEBUG variable to gate whether or not you do this, so you can easily
                    // turn it on and off, and so that it's easy to see what you can rip out if
                    // you ever want to remove it.
                    Cursor cursor = mContext.getContentResolver().query(
                            UserBackpackEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null
                    );

                    if (cursor.moveToFirst()) {
                        ContentValues resultValues = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(cursor, resultValues);
                        if (Utility.isDebugging(mContext))
                            Log.v(LOG_TAG, "Query succeeded");
                    } else {
                        if (Utility.isDebugging(mContext))
                            Log.v(LOG_TAG, "Query failed");
                    }

                    cursor.close();
                }
                break;
            case 8: //Invalid ID
                break;
            case 15: //Private
                break;
            case 18: //ID doesn't exist
                break;
            default: //Shouldn't reach
                break;
        }
    }

    private static ContentValues buildContentValues(JSONObject item) throws JSONException {

        ContentValues values = new ContentValues();

        values.put(UserBackpackEntry.COLUMN_UNIQUE_ID, item.getLong(OWM_UNIQUE_ID));
        values.put(UserBackpackEntry.COLUMN_ORIGINAL_ID, item.getLong(OWM_ORIGINAL_ID));
        values.put(UserBackpackEntry.COLUMN_DEFINDEX, item.getInt(OWM_DEFINDEX));
        values.put(UserBackpackEntry.COLUMN_LEVEL, item.getInt(OWM_LEVEL));

        if (item.has(OWM_ORIGIN))
            values.put(UserBackpackEntry.COLUMN_ORIGIN, item.getInt(OWM_ORIGIN));
        else
            values.put(UserBackpackEntry.COLUMN_ORIGIN, -1);

        if (item.has(OWM_UNTRADABLE) && item.getBoolean(OWM_UNTRADABLE))
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE, 1);
        else
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE, 0);

        if (item.has(OWM_UNCRAFTABLE) && item.getBoolean(OWM_UNCRAFTABLE))
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT, 1);
        else
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT, 0);

        long inventoryToken = item.getLong(OWM_INVENTORY_TOKEN);
        if (inventoryToken == 0){
            values.put(UserBackpackEntry.COLUMN_POSITION, -1);
        } else {
            values.put(UserBackpackEntry.COLUMN_POSITION, inventoryToken % ((Double)Math.pow(2, 16)).intValue());
        }

        values.put(UserBackpackEntry.COLUMN_QUALITY, item.getInt(OWM_QUALITY));

        if (item.has(OWM_CUSTOM_NAME))
            values.put(UserBackpackEntry.COLUMN_CUSTOM_NAME, item.getString(OWM_CUSTOM_NAME));
        if (item.has(OWM_CUSTOM_DESCRIPTION))
            values.put(UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION, item.getString(OWM_CUSTOM_DESCRIPTION));

        if (item.has(OWM_CONTENT))
            values.put(UserBackpackEntry.COLUMN_CONTAINED_ITEM, item.getJSONObject(OWM_CONTENT).toString());

        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, 0);
        values.put(UserBackpackEntry.COLUMN_CRAFT_NUMBER, 0);
        values.put(UserBackpackEntry.COLUMN_AUSTRALIUM, 0);

        values = addAttributes(values, item);

        if (item.has(OWM_EQUIPPED))
            values.put(UserBackpackEntry.COLUMN_EQUIPPED, 1);
        else
            values.put(UserBackpackEntry.COLUMN_EQUIPPED, 0);

        return values;
    }

    private static ContentValues addAttributes(ContentValues values, JSONObject item) throws JSONException {
        if (item.has(OWM_ATTRIBUTES)) {
            JSONArray attributes = item.getJSONArray(OWM_ATTRIBUTES);

            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attribute = attributes.getJSONObject(i);
                switch (attribute.getInt(OWM_DEFINDEX)){
                    case 133://Medal number
                        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, attribute.getInt(OWM_FLOAT_VALUE));
                        break;
                    case 134://Particle effect
                        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, attribute.getInt(OWM_FLOAT_VALUE));
                        break;
                    case 142://Painted
                        values.put(UserBackpackEntry.COLUMN_PAINT, attribute.getInt(OWM_FLOAT_VALUE));
                        break;
					case 186://Gifted by
                        values.put(UserBackpackEntry.COLUMN_GIFTER_NAME,
                                attribute.getJSONObject(OWM_ACCOUNT_INFO).getString(OWM_PERSONA_NAME));
						break;
					case 187://Crate series
                        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, attribute.getInt(OWM_FLOAT_VALUE));
						break;
					case 228://Crafted by
                        values.put(UserBackpackEntry.COLUMN_CREATOR_NAME,
                                attribute.getJSONObject(OWM_ACCOUNT_INFO).getString(OWM_PERSONA_NAME));
						break;
					case 229://Craft number
                        values.put(UserBackpackEntry.COLUMN_CRAFT_NUMBER, attribute.getInt(OWM_VALUE));
						break;
					case 2013://Killstreaker
						break;
					case 2014://Killstreak sheen
						break;
					case 2025://Killstreak tier
						break;
					case 2027://Is australium
                        values.put(UserBackpackEntry.COLUMN_AUSTRALIUM, attribute.getInt(OWM_FLOAT_VALUE));
						break;
                    default:
                        break;
                }
            }
        }
        return values;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
    }
}
