package com.tlongdev.bktf.task;

import android.content.ContentValues;
import android.content.Context;
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
import java.util.ArrayList;
import java.util.Vector;

public class FetchUserBackpack extends AsyncTask<String, Void, Boolean> {
    
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
    private boolean isGuest;

    private String errorMessage;

    private static ArrayList<Integer> slotNumbers;

    private int rawKeys = 0;
    private int rawRef = 0;
    private int rawRec = 0;
    private int rawScraps = 0;
    private OnFetchUserBackpackListener listener = null;
    private int backpackSlots = 0;
    private int itemNumber = 0;

    public FetchUserBackpack(Context context) {
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        isGuest = !params[0].equals(PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(mContext.getString(R.string.pref_resolved_steam_id), ""));

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
                    .appendQueryParameter(ID_PARAM, params[0])
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
                return false;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            //Read the input
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.length() == 0) {
                //Stream was empty, nothing to do.
                return false;
            }
            jsonStr = buffer.toString();

        } catch (IOException e) {
            errorMessage = "network error";
            publishProgress();
            if (Utility.isDebugging(mContext))
                e.printStackTrace();
            return false;
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
            return getItemsFromJson(jsonStr, params[0]);
        } catch (JSONException e) {
            errorMessage = "error while parsing data";
            publishProgress();
            if (Utility.isDebugging(mContext))
                e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean isPrivate) {
        if (listener!= null){
            if (isPrivate){
                listener.onPrivateBackpack();
            } else {
                listener.onFetchFinished(rawKeys, Utility.getRawMetal(rawRef, rawRec, rawScraps), backpackSlots, itemNumber);
            }
        }
    }

    private boolean getItemsFromJson(String jsonStr, String steamId) throws JSONException {
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


                backpackSlots = response.getInt(OWM_SLOTS);
                itemNumber = items.length();

                slotNumbers = new ArrayList<>();

                for (int i = 1; i <= backpackSlots; i++){
                    slotNumbers.add(i);
                }

                for (int i = 0; i < items.length(); i++){
                    ContentValues values = buildContentValues(items.getJSONObject(i));
                    if (values != null)
                        cVVector.add(values);
                }

                fillInEmptySlots(cVVector);

                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);

                    Uri contentUri;
                    if (!isGuest) {
                        contentUri = UserBackpackEntry.CONTENT_URI;
                    } else {
                        contentUri = UserBackpackEntry.CONTENT_URI_GUEST;
                    }

                    int rowsDeleted = mContext.getContentResolver().delete(contentUri, null, null);
                    if (Utility.isDebugging(mContext))
                        Log.v(LOG_TAG, "deleted " + rowsDeleted + " rows");
                    //Insert all the data into the database
                    int rowsInserted = mContext.getContentResolver()
                            .bulkInsert(contentUri, cvArray);
                    if (Utility.isDebugging(mContext))
                        Log.v(LOG_TAG, "inserted " + rowsInserted + " rows");
                }
                return false;
            case 8: //Invalid ID, shouldn't reach
                throw new IllegalStateException("Steam ID provided for backpack fetching was invalid: " + steamId);
            case 15:
                return true;
            case 18: //ID doesn't exist, shouldn't reach
                throw new IllegalStateException("Steam ID provided for backpack fetching doesn't exist: " + steamId);
            default: //Shouldn't reach
                throw new IllegalStateException("Unknown status returned by GetPlayerItems api: " + response.getInt(OWM_STATUS));
        }
    }

    private static void fillInEmptySlots(Vector<ContentValues> cVVector) {
        for (int i : slotNumbers){
            ContentValues values = new ContentValues();
            values.put(UserBackpackEntry.COLUMN_UNIQUE_ID, 0);
            values.put(UserBackpackEntry.COLUMN_ORIGINAL_ID, 0);
            values.put(UserBackpackEntry.COLUMN_DEFINDEX, 0);
            values.put(UserBackpackEntry.COLUMN_LEVEL, 0);
            values.put(UserBackpackEntry.COLUMN_ORIGIN, 0);
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE, 0);
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT, 0);
            values.put(UserBackpackEntry.COLUMN_POSITION, i);
            values.put(UserBackpackEntry.COLUMN_QUALITY, 0);
            values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, 0);
            values.put(UserBackpackEntry.COLUMN_CRAFT_NUMBER, 0);
            values.put(UserBackpackEntry.COLUMN_AUSTRALIUM, 0);
            values.put(UserBackpackEntry.COLUMN_EQUIPPED, 0);

            cVVector.add(values);
        }
    }

    private ContentValues buildContentValues(JSONObject item) throws JSONException {

        long inventoryToken = item.getLong(OWM_INVENTORY_TOKEN);
        if (inventoryToken == 0){
            return null;
        }

        ContentValues values = new ContentValues();

        int defindex = item.getInt(OWM_DEFINDEX);

        switch (defindex){
            case 5021:
                rawKeys++;
                break;
            case 5000:
                rawScraps++;
                break;
            case 5001:
                rawRec++;
                break;
            case 5002:
                rawRef++;
                break;
        }

        defindex = Utility.fixDefindex(defindex);

        values.put(UserBackpackEntry.COLUMN_UNIQUE_ID, item.getLong(OWM_UNIQUE_ID));
        values.put(UserBackpackEntry.COLUMN_ORIGINAL_ID, item.getLong(OWM_ORIGINAL_ID));
        values.put(UserBackpackEntry.COLUMN_DEFINDEX, defindex);
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

        if (inventoryToken >= 3221225472L /*11000000000000000000000000000000*/){
            values.put(UserBackpackEntry.COLUMN_POSITION, -1);
        } else {
            int position = (int)(inventoryToken % ((Double)Math.pow(2, 16)).intValue());
            values.put(UserBackpackEntry.COLUMN_POSITION, position);
            slotNumbers.remove(Integer.valueOf(position));
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

    public void registerOnFetchUserBackpackListener(OnFetchUserBackpackListener listener){
        this.listener = listener;
    }

    public static interface OnFetchUserBackpackListener{
        public void onFetchFinished(int rawKeys, double rawMetal, int backpackSlots, int itemNumber);
        public void onPrivateBackpack();
    }
}
