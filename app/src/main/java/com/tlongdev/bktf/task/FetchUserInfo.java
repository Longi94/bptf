package com.tlongdev.bktf.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.UserFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Task for fetching data about the user in the background.
 */
public class FetchUserInfo extends AsyncTask<String, Void, Void> {
    private Context mContext;

    //Whether it was a user initiated update
    private boolean manualSync;
    //Stored for stopping the animation.
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //Stored for a callback to update the UI.
    private UserFragment mFragment;

    private String errorMessage;

    public FetchUserInfo(Context mContext, boolean manualSync, UserFragment fragment, SwipeRefreshLayout swipeRefreshLayout) {
        this.mContext = mContext;
        this.manualSync = manualSync;
        mSwipeRefreshLayout = swipeRefreshLayout;
        mFragment = fragment;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (System.currentTimeMillis() - PreferenceManager.getDefaultSharedPreferences(mContext)
                .getLong(mContext.getString(R.string.pref_last_user_data_update), 0) < 3600000L && !manualSync){
            //This task ran less than an hour ago and wasn't a manual sync, nothing to do.
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String jsonString;
        String steamId;

        try {
            final String VANITY_BASE_URL = "http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/";
            final String KEY_API = "key";
            final String KEY_VANITY_URL = "vanityurl";

            final String USER_INFO_BASE_URL = "http://backpack.tf/api/IGetUsers/v3/";
            final String KEY_STEAM_ID = "steamids";
            final String KEY_COMPRESS = "compress";

            final String USER_SUMMARIES_BASE_URL = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/";

            steamId = Utility.getResolvedSteamId(mContext);
            //Check if there is a resolve steamId saved
            if (steamId == null || steamId.equals("")) {
                steamId = Utility.getSteamId(mContext);
                if (steamId == null) {
                    errorMessage = "no steamID provided";
                    publishProgress();
                    return null;
                }
            }
            Uri uri;
            URL url;
            InputStream inputStream;
            StringBuffer buffer;
            if (!Utility.isSteamId(steamId)) {
                //First we try to resolve the steamId if the provided one isn't a 64bit steamId
                uri = Uri.parse(VANITY_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_API, mContext.getString(R.string.steam_web_api_key))
                        .appendQueryParameter(KEY_VANITY_URL, steamId).build();
                url = new URL(uri.toString());

                //Open connection.
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                buffer = new StringBuffer();

                // Nothing to do if the stream was empty.
                if (inputStream != null) {
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    //Read input
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    if (buffer.length() != 0) {
                        // If the stream was empty there is no point in parsing.
                        jsonString = buffer.toString();
                        steamId = Utility.parseSteamIdFromVanityJson(jsonString);
                    }

                }
            }

            if (!Utility.isSteamId(steamId)) {
                //Still couldn't get the steamId, there was an error. Nothing to do.
                errorMessage = steamId;
                publishProgress();
                return null;
            }

            //Save the resolved steamId
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(mContext
                    .getString(R.string.pref_resolved_steam_id), steamId).apply();

            //Build user info uri
            uri = Uri.parse(USER_INFO_BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_STEAM_ID, steamId)
                    .appendQueryParameter(KEY_COMPRESS, "1")
                    .build();

            url = new URL(uri.toString());

            //Open connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            buffer = new StringBuffer();

            String line;
            //Parse only if the stream isn't empty
            if (inputStream != null) {

                reader = new BufferedReader(new InputStreamReader(inputStream));

                //Read input
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                //Only parse if the input isn't empty
                if (buffer.length() > 0) {
                    jsonString = buffer.toString();
                    parseUserInfoJson(jsonString, steamId);
                }
            }

            //Build user summaries uri
            uri = Uri.parse(USER_SUMMARIES_BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_API, mContext.getString(R.string.steam_web_api_key))
                    .appendQueryParameter(KEY_STEAM_ID, steamId)
                    .build();

            url = new URL(uri.toString());

            //Open connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            buffer = new StringBuffer();

            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() > 0) {
                    jsonString = buffer.toString();
                    parseUserSummariesJson(jsonString);
                }
            }

            //Save the update time
            PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                    .putLong(mContext.getString(R.string.pref_last_user_data_update),
                            System.currentTimeMillis()).apply();

        } catch (IOException e) {
            errorMessage = "network error";
            publishProgress();
            if (Utility.isDebugging(mContext))
                e.printStackTrace();
            return null;
        } catch (JSONException e) {
            errorMessage = "error while parsing data";
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
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Void param) {
        //Stop the refreshing animation and update the UI
        if(mFragment.isAdded()){
            mFragment.updateUserPage();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Get all the data from JSON and save them into the default shared preferences
     */
    private void parseUserSummariesJson(String jsonString) throws JSONException {
        final String OWM_RESPONSE = "response";
        final String OWM_PLAYERS = "players";
        final String OWM_NAME = "personaname";
        final String OWM_LAST_ONLINE = "lastlogoff";
        final String OWM_AVATAR = "avatarfull";
        final String OWM_PLAYER_STATE = "personastate";
        final String OWM_PROFILE_CREATED = "timecreated";
        final String OWM_IN_GAME = "gameid";

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);
        JSONArray players = response.getJSONArray(OWM_PLAYERS);
        JSONObject player = players.getJSONObject(0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(mContext.getString(R.string.pref_player_name), player.getString(OWM_NAME));
        editor.putLong(mContext.getString(R.string.pref_player_last_online), player.getLong(OWM_LAST_ONLINE));
        if (!prefs.getString(mContext.getString(R.string.pref_player_avatar_url), "").equals(player.getString(OWM_AVATAR))) {
            editor.putBoolean(mContext.getString(R.string.pref_new_avatar), true);
        }
        editor.putString(mContext.getString(R.string.pref_player_avatar_url), player.getString(OWM_AVATAR));
        editor.putInt(mContext.getString(R.string.pref_player_state), player.getInt(OWM_PLAYER_STATE));

        if (player.has(OWM_PROFILE_CREATED)) {
            editor.putLong(mContext.getString(R.string.pref_player_profile_created), player.getLong(OWM_PROFILE_CREATED));
        }
        if (player.has(OWM_IN_GAME)){
            editor.putInt(mContext.getString(R.string.pref_player_state), 7);
        }

        editor.apply();
    }

    /**
     * Get all the data needed from the JSON string and save them to the default shared preferences.
     */
    private void parseUserInfoJson(String jsonString, String steamId) throws JSONException {

        final String OWM_RESPONSE = "response";
        final String OWM_SUCCESS = "success";
        final String OWM_PLAYERS = "players";
        final String OWM_BACKPACK_VALUE = "backpack_value";
        final String OWM_BACKPACK_VALUE_TF2 = "440";
        final String OWM_PLAYER_NAME = "name";
        final String OWM_PLAYER_REPUTATION = "backpack_tf_reputation";
        final String OWM_PLAYER_GROUP = "backpack_tf_group";
        final String OWM_PLAYER_BANNED = "backpack_tf_banned";
        final String OWM_PLAYER_TRUST = "backpack_tf_trust";
        final String OWM_PLAYER_TRUST_FOR = "for";
        final String OWM_PLAYER_TRUST_AGAINST = "against";
        final String OWM_PLAYER_SCAMMER = "steamrep_scammer";
        final String OWM_PLAYER_BAN_ECONOMY = "ban_economy";
        final String OWM_PLAYER_BAN_COMMUNITY = "ban_community";
        final String OWM_PLAYER_BAN_VAC = "ban_vac";

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

        if (response.getInt(OWM_SUCCESS) == 0) {
            return;
        }

        JSONObject players = response.getJSONObject(OWM_PLAYERS);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        JSONObject current_user = players.getJSONObject(steamId);

        if (current_user.getInt(OWM_SUCCESS) == 1) {

            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(mContext.getString(R.string.pref_player_name), current_user.getString(OWM_PLAYER_NAME));
            if (current_user.has(OWM_PLAYER_REPUTATION)) {
                editor.putInt(mContext.getString(R.string.pref_player_reputation), current_user.getInt(OWM_PLAYER_REPUTATION));
            }
            Utility.putDouble(editor, mContext.getString(R.string.pref_player_backpack_value_tf2),
                    current_user.getJSONObject(OWM_BACKPACK_VALUE).getDouble(OWM_BACKPACK_VALUE_TF2));

            if (current_user.has(OWM_PLAYER_GROUP)) {
                editor.putInt(mContext.getString(R.string.pref_player_group), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_group), 0);
            }
            if (current_user.has(OWM_PLAYER_BANNED)) {
                editor.putInt(mContext.getString(R.string.pref_player_banned), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_banned), 0);
            }
            if (current_user.has(OWM_PLAYER_SCAMMER)) {
                editor.putInt(mContext.getString(R.string.pref_player_scammer), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_scammer), 0);
            }
            if (current_user.has(OWM_PLAYER_BAN_COMMUNITY)) {
                editor.putInt(mContext.getString(R.string.pref_player_community_banned), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_community_banned), 0);
            }
            if (current_user.has(OWM_PLAYER_BAN_ECONOMY)) {
                editor.putInt(mContext.getString(R.string.pref_player_economy_banned), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_economy_banned), 0);
            }
            if (current_user.has(OWM_PLAYER_BAN_VAC)) {
                editor.putInt(mContext.getString(R.string.pref_player_vac_banned), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_vac_banned), 0);
            }
            if (current_user.has(OWM_PLAYER_TRUST)) {
                JSONObject trustScore = current_user.getJSONObject(OWM_PLAYER_TRUST);
                editor.putInt(mContext.getString(R.string.pref_player_trust_positive), trustScore.getInt(OWM_PLAYER_TRUST_FOR));
                editor.putInt(mContext.getString(R.string.pref_player_trust_negative), trustScore.getInt(OWM_PLAYER_TRUST_AGAINST));
            }

            editor.apply();
        }
    }
}