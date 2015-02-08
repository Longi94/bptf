package com.tlongdev.bktf.task;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;

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
        final String OWM_RESPONSE = "response";
        final String OWM_STATUS = "status";
        final String OWM_SLOTS = "num_backpack_slots";
        final String OWM_ITEMS = "items";
        final String OWM_UNIQUE_ID = "id";
        final String OWM_ORIGINAL_ID = "original_id";
        final String OWM_DEFINDEX = "defindex";
        final String OWM_LEVEL = "level";
        final String OWM_ORIGIN = "origin";
        final String OWM_TRADABLE = "flag_cannot_trade";
        final String OWM_CRAFTABLE = "flag_cannot_craft";
        final String OWM_INVENTORY_TOKEN = "inventory";
        final String OWM_QUALITY = "quality";
        final String OWM_CUSTOM_NAME = "custom_name";
        final String OWM_CUSTOM_DESCRIPTION = "custom_desc";
        final String OWM_CONTENT = "contained_item";
        final String OWM_ATTRIBUTES = "attributes";
        final String OWM_EQUIPPED = "equipped";

        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

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

    private ContentValues buildContentValues(JSONObject jsonObject) {

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
    }
}
