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

/**
 * Task for fetchin the user's backpack in the background
 */
public class FetchUserBackpack extends AsyncTask<String, Void, Boolean> {

    public static final String LOG_TAG = FetchUserBackpack.class.getSimpleName();

    //All the json keys needed to parse the data out of the json string
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

    //A list containing all the possible positions. This is needed to fill in the empty item slots
    //with empty items so the backpack is correctly shown.
    private static ArrayList<Integer> slotNumbers;

    //The context the task was launched in
    private Context mContext;

    //Indicates which table to insert data into
    private boolean isGuest;

    //Error message to be displayed to the user
    private String errorMessage;

    //Number of raw currencies in the user's backpack
    private int rawKeys = 0;
    private int rawRef = 0;
    private int rawRec = 0;
    private int rawScraps = 0;

    //The number of slots and items in the backpack
    private int backpackSlots = 0;
    private int itemNumber = 0;

    //The listener that will be notified when the fetching finishes
    private OnFetchUserBackpackListener listener = null;

    /**
     * Constructor
     *
     * @param context the context the task was launched in
     */
    public FetchUserBackpack(Context context) {
        mContext = context;
    }

    /**
     * Fill in the given vector with empty item slots
     *
     * @param cVVector the vector to be filled
     */
    private static void fillInEmptySlots(Vector<ContentValues> cVVector) {

        //Add an empty item to each empty slot
        for (int i : slotNumbers) {
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

    /**
     * Add all the attributes of the item to the contentvalues
     *
     * @param values the ContentValues the attributes will be added to
     * @param item   json string containing the attributes
     * @return the extended contentvalues
     * @throws JSONException
     */
    private static ContentValues addAttributes(ContentValues values, JSONObject item)
            throws JSONException {
        if (item.has(OWM_ATTRIBUTES)) {
            //Get the attributes from the json
            JSONArray attributes = item.getJSONArray(OWM_ATTRIBUTES);

            //iterate through them and add them to the cv
            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attribute = attributes.getJSONObject(i);
                switch (attribute.getInt(OWM_DEFINDEX)) {
                    case 133://Medal number
                        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX,
                                attribute.getInt(OWM_FLOAT_VALUE));
                        break;
                    case 134://Particle effect
                        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX,
                                attribute.getInt(OWM_FLOAT_VALUE));
                        break;
                    case 142://Painted
                        values.put(UserBackpackEntry.COLUMN_PAINT,
                                attribute.getInt(OWM_FLOAT_VALUE));
                        break;
                    case 186://Gifted by
                        values.put(UserBackpackEntry.COLUMN_GIFTER_NAME,
                                attribute.getJSONObject(OWM_ACCOUNT_INFO)
                                        .getString(OWM_PERSONA_NAME));
                        break;
                    case 187://Crate series
                        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX,
                                attribute.getInt(OWM_FLOAT_VALUE));
                        break;
                    case 228://Crafted by
                        values.put(UserBackpackEntry.COLUMN_CREATOR_NAME,
                                attribute.getJSONObject(OWM_ACCOUNT_INFO)
                                        .getString(OWM_PERSONA_NAME));
                        break;
                    case 229://Craft number
                        values.put(UserBackpackEntry.COLUMN_CRAFT_NUMBER,
                                attribute.getInt(OWM_VALUE));
                        break;
                    case 725://Decorated weapon wear
                        /*
                        1045220557 - Factory New
                        1053609165 - Minimal Wear
                        1058642330 - Field-Tested
                        1061997773 - Well Worn
                        1065353216 - Battle Scarred
                        */
                        values.put(UserBackpackEntry.COLUMN_DECORATED_WEAPON_WEAR,
                                attribute.getLong(OWM_VALUE));
                        break;
                    case 2013://TODO Killstreaker
                        break;
                    case 2014://TODO Killstreak sheen
                        break;
                    case 2025://TODO Killstreak tier
                        break;
                    case 2027://Is australium
                        values.put(UserBackpackEntry.COLUMN_AUSTRALIUM,
                                attribute.getInt(OWM_FLOAT_VALUE));
                        break;
                    default:
                        //Unused attribute
                        break;
                }
            }
        }
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean doInBackground(String... params) {
        //if the given steam id is not the same as the saved resolved steam_id, then this is a guest
        //backpack
        isGuest = !params[0].equals(PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(mContext.getString(R.string.pref_resolved_steam_id), ""));

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String jsonStr = null;

        try {
            //Keys of the input parameters
            final String KEY_PARAM = "key";
            final String ID_PARAM = "SteamID";

            //Build the URI
            Uri uri = Uri.parse(mContext.getString(R.string.steam_get_player_items_url))
                    .buildUpon()
                    .appendQueryParameter(KEY_PARAM, mContext.getString(R.string.steam_web_api_key))
                    .appendQueryParameter(ID_PARAM, params[0])
                    .build();

            //Convert the URI into a URL
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
            //Get the json string
            jsonStr = buffer.toString();

        } catch (IOException e) {
            //There was a network error, notify the user TODO, proper error handling
            errorMessage = mContext.getString(R.string.error_network);
            publishProgress();
            if (Utility.isDebugging(mContext))
                e.printStackTrace();
            return false;
        } finally {
            //Disconnect
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                //Close the input stream
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
            //Start parsing the json String
            return getItemsFromJson(jsonStr, params[0]);
        } catch (JSONException e) {
            //Something went wrong while parsing the data
            errorMessage = mContext.getString(R.string.error_data_parse);
            publishProgress();
            if (Utility.isDebugging(mContext))
                e.printStackTrace();
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(Boolean isPrivate) {
        if (listener != null) {
            if (isPrivate) {
                //Notify the listener that the backpack was private
                listener.onPrivateBackpack();
            } else {
                //Notify the user that the fetching finished and pass on the data
                listener.onFetchFinished(rawKeys, Utility.getRawMetal(rawRef, rawRec, rawScraps),
                        backpackSlots, itemNumber);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onProgressUpdate(Void... values) {
        //only used for showing error messages to the user
        Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * Parse all the items from the JSON strings
     *
     * @param jsonStr the json string
     * @param steamId the steam id of the owner of the backpack
     * @return true if the backpack is private
     * @throws JSONException
     */
    private boolean getItemsFromJson(String jsonStr, String steamId) throws JSONException {

        //JSON keys
        final String OWM_RESULT = "result";
        final String OWM_STATUS = "status";
        final String OWM_SLOTS = "num_backpack_slots";
        final String OWM_ITEMS = "items";

        //Get the response
        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONObject response = jsonObject.getJSONObject(OWM_RESULT);

        switch (response.getInt(OWM_STATUS)) {
            case 1: //Backpack is public, successfully queried
                //The vector that will contain all items
                Vector<ContentValues> cVVector = new Vector<>();
                JSONArray items = response.getJSONArray(OWM_ITEMS);

                //Get the number of slots and items
                backpackSlots = response.getInt(OWM_SLOTS);
                itemNumber = items.length();

                //Create a list containing all the possible position
                slotNumbers = new ArrayList<>();
                for (int i = 1; i <= backpackSlots; i++) {
                    slotNumbers.add(i);
                }

                //Iterate through the items and add them to the CV
                for (int i = 0; i < items.length(); i++) {
                    ContentValues values = buildContentValues(items.getJSONObject(i));
                    if (values != null)
                        cVVector.add(values);
                }

                //Fill in the empty slots with empty items
                fillInEmptySlots(cVVector);

                //Add the items to the database
                if (cVVector.size() > 0) {

                    //Create an array
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);

                    //Content uri based on which talbe to insert into
                    Uri contentUri;
                    if (!isGuest) {
                        contentUri = UserBackpackEntry.CONTENT_URI;
                    } else {
                        contentUri = UserBackpackEntry.CONTENT_URI_GUEST;
                    }

                    //Clear the databse first
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
                throw new IllegalStateException(
                        "Steam ID provided for backpack fetching was invalid: " + steamId);
            case 15: //Backpack is private
                return true;
            case 18: //ID doesn't exist, shouldn't reach
                throw new IllegalStateException(
                        "Steam ID provided for backpack fetching doesn't exist: " + steamId);
            default: //Shouldn't reach
                throw new IllegalStateException("Unknown status returned by GetPlayerItems api: "
                        + response.getInt(OWM_STATUS));
        }
    }

    /**
     * Create a ContentValues object from the JSONObject
     *
     * @param item the object containing the data
     * @return the ContentValues object containing the data from the JSONObject
     * @throws JSONException
     */
    private ContentValues buildContentValues(JSONObject item) throws JSONException {

        //Get the inventory token
        long inventoryToken = item.getLong(OWM_INVENTORY_TOKEN);
        if (inventoryToken == 0) {
            //Item hasn't been found yet
            return null;
        }

        //The CV object that will contain the data
        ContentValues values = new ContentValues();

        //Get the defindex
        int defindex = item.getInt(OWM_DEFINDEX);

        //Raw currency values
        switch (defindex) {
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

        //Fix the defindex for pricing purposes
        defindex = Utility.fixDefindex(defindex);

        //Save the unique ID
        values.put(UserBackpackEntry.COLUMN_UNIQUE_ID, item.getLong(OWM_UNIQUE_ID));

        //Save the original ID
        values.put(UserBackpackEntry.COLUMN_ORIGINAL_ID, item.getLong(OWM_ORIGINAL_ID));

        //Save the defindex
        values.put(UserBackpackEntry.COLUMN_DEFINDEX, defindex);

        //Save the level
        values.put(UserBackpackEntry.COLUMN_LEVEL, item.getInt(OWM_LEVEL));

        //Save the origin type
        if (item.has(OWM_ORIGIN))
            values.put(UserBackpackEntry.COLUMN_ORIGIN, item.getInt(OWM_ORIGIN));
        else
            values.put(UserBackpackEntry.COLUMN_ORIGIN, -1);

        //Save the tradability
        if (item.has(OWM_UNTRADABLE) && item.getBoolean(OWM_UNTRADABLE))
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE, 1);
        else
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE, 0);

        //Save the craftability
        if (item.has(OWM_UNCRAFTABLE) && item.getBoolean(OWM_UNCRAFTABLE))
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT, 1);
        else
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT, 0);

        if (inventoryToken >= 3221225472L /*11000000000000000000000000000000*/) {
            //The item doesn't have a designated place i the backpack yet. It's a new item.
            values.put(UserBackpackEntry.COLUMN_POSITION, -1);
        } else {
            //Save the position of the item
            int position = (int) (inventoryToken % ((Double) Math.pow(2, 16)).intValue());
            values.put(UserBackpackEntry.COLUMN_POSITION, position);

            //The position doens't need to be filled with an empty item.
            slotNumbers.remove(Integer.valueOf(position));
        }

        //Save the quality of the item
        values.put(UserBackpackEntry.COLUMN_QUALITY, item.getInt(OWM_QUALITY));

        //Save the custom name of the item
        if (item.has(OWM_CUSTOM_NAME))
            values.put(UserBackpackEntry.COLUMN_CUSTOM_NAME, item.getString(OWM_CUSTOM_NAME));

        //Save the custom description of the item
        if (item.has(OWM_CUSTOM_DESCRIPTION))
            values.put(UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION,
                    item.getString(OWM_CUSTOM_DESCRIPTION));

        //Save the content of the item TODO show the content of a gift
        if (item.has(OWM_CONTENT))
            values.put(UserBackpackEntry.COLUMN_CONTAINED_ITEM,
                    item.getJSONObject(OWM_CONTENT).toString());

        //Save the index of the item
        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, 0);

        //Save the craftnumber of the item
        values.put(UserBackpackEntry.COLUMN_CRAFT_NUMBER, 0);

        //Save the australium property of the item
        values.put(UserBackpackEntry.COLUMN_AUSTRALIUM, 0);

        //Get the other attributes from the attributes JSON object
        values = addAttributes(values, item);

        //Save the equipped property of the item
        if (item.has(OWM_EQUIPPED))
            values.put(UserBackpackEntry.COLUMN_EQUIPPED, 1);
        else
            values.put(UserBackpackEntry.COLUMN_EQUIPPED, 0);

        return values;
    }

    /**
     * Register a listener that will be notified
     *
     * @param listener the listener to be notified
     */
    public void registerOnFetchUserBackpackListener(OnFetchUserBackpackListener listener) {
        this.listener = listener;
    }

    /**
     * Listener interface for listening for the end of the fetch.
     */
    public interface OnFetchUserBackpackListener {

        /**
         * Notify the listener, that the fetchin has finished. The backpack is public.
         *
         * @param rawKeys       the number of raw keys in the backpack
         * @param rawMetal      the number of raw metal in the backpack
         * @param backpackSlots the number of backpack slots
         * @param itemNumber    the number of items in the backpack
         */
        void onFetchFinished(int rawKeys, double rawMetal, int backpackSlots, int itemNumber);

        /**
         * Notify the listener that the backpack was private.
         */
        void onPrivateBackpack();
    }
}
