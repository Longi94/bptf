/**
 * Copyright 2015 Long Tran
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.interactor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.BuildConfig;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.network.BackpackTfInterface;
import com.tlongdev.bktf.network.model.bptf.BackpackTfPayload;
import com.tlongdev.bktf.network.model.bptf.BackpackTfPlayer;
import com.tlongdev.bktf.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Task for fetching data about the user in the background.
 */
public class BackpackTfUserDataInteractor extends AsyncTask<String, Void, Integer> {

    private static final String LOG_TAG = BackpackTfUserDataInteractor.class.getSimpleName();

    @Inject BackpackTfInterface mBackpackTfInterface;
    @Inject Tracker mTracker;
    @Inject SharedPreferences.Editor mEditor;
    @Inject SharedPreferences mPrefs;

    //The context the task is running in
    private Context mContext;

    //Whether it was a user initiated update
    private boolean manualSync;

    //The error message that will be presented to the user, when an error occurs
    private String errorMessage;

    //The listener that will be notified when the fetching finishes
    private OnUserInfoListener listener = null;

    private String steamId;

    /**
     * Contructor.
     *
     * @param context    the context the task will run in
     * @param manualSync whether the updated was initiated by the user
     */
    public BackpackTfUserDataInteractor(Context context, BptfApplication application, boolean manualSync) {
        application.getInteractorComponent().inject(this);
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


            //The GetPlayerSummaries api url, all input keys have been defined above
            final String USER_SUMMARIES_BASE_URL =
                    mContext.getString(R.string.steam_get_player_summaries_url);
            final String KEY_STEAM_ID = "steamids";

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

                int statusCode = response.code();

                if (statusCode >= 500) {
                    errorMessage = "Server error: " + statusCode;
                    return -1;
                } else if (statusCode >= 400) {
                    errorMessage = "Client error: " + statusCode;
                    return -1;
                }

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
            mEditor.putString(mContext
                    .getString(R.string.pref_resolved_steam_id), steamId).apply();

            retrofit2.Response<BackpackTfPayload> responseTemp = mBackpackTfInterface.getUserData(steamId).execute();

            if (responseTemp.body() != null) {
                saveUserData(responseTemp.body());
            } else if (responseTemp.raw().code() >= 500) {
                errorMessage = "Server error: " + responseTemp.raw().code();
                return 1;
            } else if (responseTemp.raw().code() >= 400) {
                errorMessage = "Client error: " + responseTemp.raw().code();
                return 1;
            }

            //Build user summaries uri
            uri = Uri.parse(USER_SUMMARIES_BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_API, BuildConfig.STEAM_WEB_API_KEY)
                    .appendQueryParameter(KEY_STEAM_ID, steamId)
                    .build();

            Log.v(LOG_TAG, "Built uri: " + uri.toString());

            //Initialize the URL
            url = new URL(uri.toString());

            //Open connection
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            int statusCode = response.code();

            if (statusCode >= 500) {
                errorMessage = "Server error: " + statusCode;
                return -1;
            } else if (statusCode >= 400) {
                errorMessage = "Client error: " + statusCode;
                return -1;
            }

            jsonString = response.body().string();
            //Parse the JSON
            return parseUserSummariesJson(jsonString);

        } catch (IOException e) {
            //There was a network error
            errorMessage = mContext.getString(R.string.error_network);
            e.printStackTrace();

            mTracker.send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Network exception:GetUserInfo, Message: " + e.getMessage())
                    .setFatal(false)
                    .build());

            return -1;
        } catch (JSONException e) {
            //The JSON string was incorrectly formatted
            errorMessage = mContext.getString(R.string.error_data_parse);
            e.printStackTrace();

            mTracker.send(new HitBuilders.ExceptionBuilder()
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

        //Save the name of the user
        if (player.has(OWM_NAME)) {
            mEditor.putString(mContext.getString(R.string.pref_player_name),
                    player.getString(OWM_NAME));
        }

        //Save the user's last online time
        if (player.has(OWM_LAST_ONLINE)) {
            mEditor.putLong(mContext.getString(R.string.pref_player_last_online),
                    player.getLong(OWM_LAST_ONLINE));
        }

        //Only save the avatarUrl if it's different from the previous url
        String avatarUrl = mPrefs.getString(mContext.getString(R.string.pref_player_avatar_url), "");
        if (player.has(OWM_AVATAR) && !avatarUrl.equals(player.getString(OWM_AVATAR))) {
            //We also need to notify that app that the user has a new avatar
            mEditor.putBoolean(mContext.getString(R.string.pref_new_avatar), true);
            mEditor.putString(mContext.getString(R.string.pref_player_avatar_url),
                    player.getString(OWM_AVATAR));
        }

        //Save the state of the player
        if (player.has(OWM_PLAYER_STATE)) {
            mEditor.putInt(mContext.getString(R.string.pref_player_state),
                    player.getInt(OWM_PLAYER_STATE));
        }

        //If the player is in-game, the state is 7
        if (player.has(OWM_IN_GAME)) {
            mEditor.putInt(mContext.getString(R.string.pref_player_state), 7);
        }

        //Save the time of the profile creation
        if (player.has(OWM_PROFILE_CREATED)) {
            mEditor.putLong(mContext.getString(R.string.pref_player_profile_created),
                    player.getLong(OWM_PROFILE_CREATED));
        }

        //Save the edits
        mEditor.apply();

        return 0;
    }

    private void saveUserData(BackpackTfPayload payload) {
        if (payload.getResponse() == null) {
            return;
        }

        if (payload.getResponse().getSuccess() == 0) {
            //The api query was unsuccessful, nothing to do.
            return;
        }

        if (payload.getResponse().getPlayers() == null) {
            return;
        }

        BackpackTfPlayer player = payload.getResponse().getPlayers().get(steamId);
        if (player == null) {
            return;
        }

        if (player.getSuccess() == 1) {
            // TODO: 2016. 03. 14. save booleans?
            Utility.putDouble(mEditor, mContext.getString(R.string.pref_player_backpack_value_tf2),
                    player.getBackpackValue().get440())
                    .putString(mContext.getString(R.string.pref_player_name), player.getName())
                    .putInt(mContext.getString(R.string.pref_player_reputation), player.getBackpackTfReputation())
                    .putInt(mContext.getString(R.string.pref_player_group), player.getBackpackTfGroup() ? 1 : 0)
                    .putInt(mContext.getString(R.string.pref_player_banned), player.getBackpackTfBanned() ? 1 : 0)
                    .putInt(mContext.getString(R.string.pref_player_scammer), player.getSteamrepScammer() ? 1 : 0)
                    .putInt(mContext.getString(R.string.pref_player_community_banned), player.getBanCommunity() ? 1 : 0)
                    .putInt(mContext.getString(R.string.pref_player_economy_banned), player.getBanEconomy() ? 1 : 0)
                    .putInt(mContext.getString(R.string.pref_player_vac_banned), player.getBanVac() ? 1 : 0);

            if (player.getBackpackTfTrust() != null) {
                mEditor.putInt(mContext.getString(R.string.pref_player_trust_positive),
                        player.getBackpackTfTrust().getFor())
                        .putInt(mContext.getString(R.string.pref_player_trust_negative),
                                player.getBackpackTfTrust().getAgainst());
            }

            mEditor.apply();
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