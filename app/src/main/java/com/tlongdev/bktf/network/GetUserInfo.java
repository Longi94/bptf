/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.HitBuilders;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.BuildConfig;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Task for fetching data about the user in the background.
 */
public class GetUserInfo extends AsyncTask<String, Void, Integer> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = GetUserInfo.class.getSimpleName();

    //The context the task is running in
    private Context mContext;

    //Whether it was a user initiated update
    private boolean manualSync;

    //The error message that will be presented to the user, when an error occurs
    private String errorMessage;

    //The listenere that will be notified when the fetching finishes
    private OnUserInfoListener listener = null;

    private String steamId;

    /**
     * Contructor.
     *
     * @param context    the context the task will run in
     * @param manualSync whether the updated was initiated by the user
     */
    public GetUserInfo(Context context, boolean manualSync) {
        this.mContext = context;
        this.manualSync = manualSync;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer doInBackground(String... params) {
        if (System.currentTimeMillis() - PreferenceManager.getDefaultSharedPreferences(mContext)
                .getLong(mContext.getString(R.string.pref_last_user_data_update), 0) < 3600000L &&
                !manualSync) {
            //This task ran less than an hour ago and wasn't a manual sync, nothing to do.
            return -1;
        }

        // Will contain the raw JSON response as a string.
        String jsonString;

        try {
            //The vanity api and input keys
            final String VANITY_BASE_URL = mContext.getString(R.string.steam_resolve_vanity_url);
            final String KEY_API = "key";
            final String KEY_VANITY_URL = "vanityurl";

            //The GetUsers api and input keys
            final String USER_INFO_BASE_URL = mContext.getString(R.string.backpack_tf_get_users);
            final String KEY_STEAM_ID = "steamids";
            final String KEY_COMPRESS = "compress";

            //The GetPlayerSummaries api url, all input keys have been defined above
            final String USER_SUMMARIES_BASE_URL =
                    mContext.getString(R.string.steam_get_player_summaries_url);

            //Check if there is a resolve steamId saved
            steamId = params[1];
            if (steamId == null || steamId.equals("")) {
                //There is no resolved steam id saved
                steamId = params[0];
                if (steamId == null) {
                    //There is no steam id saved
                    errorMessage = mContext.getString(R.string.error_no_steam_id);
                    return -1;
                }
            }
            Uri uri;
            URL url;
            InputStream inputStream;
            StringBuffer buffer;

            //Check if the given steamid is a resolved 64bit steamId
            if (!Utility.isSteamId(steamId)) {
                //First we try to resolve the steamId if the provided one isn't a 64bit steamId

                //Build the URI
                uri = Uri.parse(VANITY_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_API, BuildConfig.STEAM_WEB_API_KEY)
                        .appendQueryParameter(KEY_VANITY_URL, steamId).build();

                //Initialize the URL
                url = new URL(uri.toString());

                //Open connection
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                // If the stream was empty there is no point in parsing.
                jsonString = response.body().string();
                //Get the resolved steamid from the returned json data
                steamId = Utility.parseSteamIdFromVanityJson(jsonString);
            }

            if (!Utility.isSteamId(steamId)) {
                //Still couldn't get the steamId, there was an error. Nothing to do.
                //The errore message is stored in the steamid variable.
                errorMessage = steamId;
                return -1;
            }

            //Save the resolved steamId
            PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                    .putString(mContext
                            .getString(R.string.pref_resolved_steam_id), steamId).apply();

            //Build user info uri
            uri = Uri.parse(USER_INFO_BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_STEAM_ID, steamId)
                    .appendQueryParameter(KEY_COMPRESS, "1")
                    .build();

            //Initialize the URL
            url = new URL(uri.toString());

            //Open connection
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            jsonString = response.body().string();
            //Parse the JSON
            parseUserInfoJson(jsonString, steamId);

            //Build user summaries uri
            uri = Uri.parse(USER_SUMMARIES_BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_API, BuildConfig.STEAM_WEB_API_KEY)
                    .appendQueryParameter(KEY_STEAM_ID, steamId)
                    .build();

            //Initialize the URL
            url = new URL(uri.toString());

            //Open connection
            client = new OkHttpClient();
            request = new Request.Builder().url(url).build();
            response = client.newCall(request).execute();

            jsonString = response.body().string();
            //Parse the JSON
            return parseUserSummariesJson(jsonString);

        } catch (IOException e) {
            //There was a network error
            errorMessage = mContext.getString(R.string.error_network);
            e.printStackTrace();

            ((BptfApplication) mContext.getApplicationContext()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Network exception:GetUserInfo, Message: " + e.getMessage())
                    .setFatal(false)
                    .build());

            return -1;
        } catch (JSONException e) {
            //The JSON string was incorrectly formatted
            errorMessage = mContext.getString(R.string.error_data_parse);
            e.printStackTrace();

            ((BptfApplication) mContext.getApplicationContext()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("JSON exception:GetUserInfo, Message: " + e.getMessage())
                    .setFatal(true)
                    .build());

            return -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(Integer integer) {
        //Finished fetching the backpack
        if (listener != null) {
            if (integer >= 0) {
                //notify the listener, that the fetching has finished.
                listener.onUserInfoFinished(steamId);
            } else {
                listener.onUserInfoFailed(errorMessage);
            }
        }
    }

    /**
     * Get all the data from JSON and save them into the default shared preferences.
     *
     * @param jsonString the json string to be parsed
     * @throws JSONException
     */
    private Integer parseUserSummariesJson(String jsonString) throws JSONException {

        //All the JSON keys needed to parse
        final String OWM_RESPONSE = "response";
        final String OWM_PLAYERS = "players";
        final String OWM_NAME = "personaname";
        final String OWM_LAST_ONLINE = "lastlogoff";
        final String OWM_AVATAR = "avatarfull";
        final String OWM_PLAYER_STATE = "personastate";
        final String OWM_PROFILE_CREATED = "timecreated";
        final String OWM_IN_GAME = "gameid";

        //Get the JSONObject that contains the data about the user
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);
        JSONArray players = response.getJSONArray(OWM_PLAYERS);
        JSONObject player = players.getJSONObject(0);

        //Start editing the sharedpreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();

        //Save the name of the user
        if (player.has(OWM_NAME)) {
            editor.putString(mContext.getString(R.string.pref_player_name),
                    player.getString(OWM_NAME));
        }

        //Save the user's last online time
        if (player.has(OWM_LAST_ONLINE)) {
            editor.putLong(mContext.getString(R.string.pref_player_last_online),
                    player.getLong(OWM_LAST_ONLINE));
        }

        //Only save the avatarUrl if it's different from the previous url
        String avatarUrl = prefs.getString(mContext.getString(R.string.pref_player_avatar_url), "");
        if (player.has(OWM_AVATAR) && !avatarUrl.equals(player.getString(OWM_AVATAR))) {
            //We also need to notify that app that the user has a new avatar
            editor.putBoolean(mContext.getString(R.string.pref_new_avatar), true);
            editor.putString(mContext.getString(R.string.pref_player_avatar_url),
                    player.getString(OWM_AVATAR));
        }

        //Save the state of the player
        if (player.has(OWM_PLAYER_STATE)) {
            editor.putInt(mContext.getString(R.string.pref_player_state),
                    player.getInt(OWM_PLAYER_STATE));
        }

        //If the player is in-game, the state is 7
        if (player.has(OWM_IN_GAME)) {
            editor.putInt(mContext.getString(R.string.pref_player_state), 7);
        }

        //Save the time of the profile creation
        if (player.has(OWM_PROFILE_CREATED)) {
            editor.putLong(mContext.getString(R.string.pref_player_profile_created),
                    player.getLong(OWM_PROFILE_CREATED));
        }

        //Save the edits
        editor.apply();

        return 0;
    }

    /**
     * Get all the data needed from the JSON string and save them to the default shared preferences.
     *
     * @param jsonString the json string to be parsed
     * @param steamId    the steam id of the user
     * @throws JSONException
     */
    private void parseUserInfoJson(String jsonString, String steamId) throws JSONException {

        //All the JSON keys needed to parse
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

        //Ge the response
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

        if (response.getInt(OWM_SUCCESS) == 0) {
            //The api query was unsuccessful, nothing to do.
            return;
        }

        //Get the JSONObject that contains the needed data
        JSONObject players = response.getJSONObject(OWM_PLAYERS);
        JSONObject current_user = players.getJSONObject(steamId);

        //Get the shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        //Check if the user JSON query was successful
        if (current_user.getInt(OWM_SUCCESS) == 1) {

            //Start the edit
            SharedPreferences.Editor editor = prefs.edit();

            //Save the user's name
            if (current_user.has(OWM_PLAYER_NAME)) {
                editor.putString(mContext.getString(R.string.pref_player_name), current_user.getString(OWM_PLAYER_NAME));
            }

            //Save the user's reputation
            if (current_user.has(OWM_PLAYER_REPUTATION)) {
                editor.putInt(mContext.getString(R.string.pref_player_reputation), current_user.getInt(OWM_PLAYER_REPUTATION));
            }

            //Save the value of the user's backpack if not private
            if (current_user.has(OWM_BACKPACK_VALUE) &&
                    current_user.getJSONObject(OWM_BACKPACK_VALUE).has(OWM_BACKPACK_VALUE_TF2)) {
                Utility.putDouble(editor, mContext.getString(R.string.pref_player_backpack_value_tf2),
                        current_user.getJSONObject(OWM_BACKPACK_VALUE).getDouble(OWM_BACKPACK_VALUE_TF2));
            }

            //Save whether the players is in the bp.tf group
            if (current_user.has(OWM_PLAYER_GROUP)) {
                editor.putInt(mContext.getString(R.string.pref_player_group), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_group), 0);
            }

            //Save if the player is banned
            if (current_user.has(OWM_PLAYER_BANNED)) {
                editor.putInt(mContext.getString(R.string.pref_player_banned), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_banned), 0);
            }

            //Save if the player is a scammer
            if (current_user.has(OWM_PLAYER_SCAMMER)) {
                editor.putInt(mContext.getString(R.string.pref_player_scammer), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_scammer), 0);
            }

            //Save if the player is community banned
            if (current_user.has(OWM_PLAYER_BAN_COMMUNITY)) {
                editor.putInt(mContext.getString(R.string.pref_player_community_banned), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_community_banned), 0);
            }

            //Save if the player is economy banned
            if (current_user.has(OWM_PLAYER_BAN_ECONOMY)) {
                editor.putInt(mContext.getString(R.string.pref_player_economy_banned), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_economy_banned), 0);
            }

            //Save if the player is VAC banned
            if (current_user.has(OWM_PLAYER_BAN_VAC)) {
                editor.putInt(mContext.getString(R.string.pref_player_vac_banned), 1);
            } else {
                editor.putInt(mContext.getString(R.string.pref_player_vac_banned), 0);
            }

            //Save the player's trust score
            if (current_user.has(OWM_PLAYER_TRUST)) {
                JSONObject trustScore = current_user.getJSONObject(OWM_PLAYER_TRUST);
                editor.putInt(mContext.getString(R.string.pref_player_trust_positive), trustScore.getInt(OWM_PLAYER_TRUST_FOR));
                editor.putInt(mContext.getString(R.string.pref_player_trust_negative), trustScore.getInt(OWM_PLAYER_TRUST_AGAINST));
            }

            //Save all edits
            editor.apply();
        }
    }

    /**
     * Register a listener, that will be notified when the fetching finishes
     *
     * @param listener the listener to be registered
     */
    public void registerFetchUserInfoListener(OnUserInfoListener listener) {
        this.listener = listener;
    }

    /**
     * Listener interface.
     */
    public interface OnUserInfoListener {

        /**
         * Callback which will be called when both tasks finish.
         *
         * @param steamId the steamid of the zser
         */
        void onUserInfoFinished(String steamId);

        void onUserInfoFailed(String errorMessage);
    }
}