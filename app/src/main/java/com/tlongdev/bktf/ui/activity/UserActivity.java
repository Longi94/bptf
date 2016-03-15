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

package com.tlongdev.bktf.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.interactor.Tf2UserBackpackInteractor;
import com.tlongdev.bktf.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Profile page activity.
 */
public class UserActivity extends AppCompatActivity implements Tf2UserBackpackInteractor.Callback {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = UserActivity.class.getSimpleName();

    //Keys for extra data in the intent.
    public static final String STEAM_ID_KEY = "steamid";
    public static final String JSON_USER_SUMMARIES_KEY = "json_user_summaries";

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    //Progress bar that indicates downloading user data.
    @Bind(R.id.progress_bar) ProgressBar progressBar;

    //Reference too all the views that needs to be updated
    @Bind(R.id.text_view_player_reputation) TextView playerReputation;
    @Bind(R.id.trust_positive) TextView trustPositive;
    @Bind(R.id.trust_negative) TextView trustNegative;
    @Bind(R.id.steamrep_status) ImageView steamRepStatus;
    @Bind(R.id.vac_status) ImageView vacStatus;
    @Bind(R.id.trade_status) ImageView tradeStatus;
    @Bind(R.id.community_status) ImageView communityStatus;
    @Bind(R.id.text_view_bp_refined) TextView backpackValueRefined;
    @Bind(R.id.text_view_bp_raw_metal) TextView backpackRawMetal;
    @Bind(R.id.text_view_bp_raw_keys) TextView backpackRawKeys;
    @Bind(R.id.text_view_bp_usd) TextView backpackValueUsd;
    @Bind(R.id.text_view_bp_slots) TextView backpackSlots;
    @Bind(R.id.text_view_user_since) TextView userSinceText;
    @Bind(R.id.text_view_user_last_online) TextView lastOnlineText;
    @Bind(R.id.avatar) ImageView avatar;

    //Steam id of the player, whose profile page is currently shown.
    private String steamId;

    //Stores whether the backpack is private or not
    private boolean privateBackpack = false;

    //All these values store info about the user, and the info that is shown to the user.
    //Also initial values.
    private boolean isBanned = false;
    private boolean isCommunityBanned = false;
    private boolean isScammer = false;
    private boolean isTradeBanned = false;
    private boolean isVacBanned = false;
    private double backpackValue = -1;
    private double rawMetal = 0;
    private int backpackSlotNumber = 0;
    private int itemNumber = 0;
    private int playerReputationValue = 0;
    private int playerState = 0;
    private int ratingPositive = 0;
    private int ratingNegative = 0;
    private int rawKeys = 0;
    private long lastOnline = -1;
    private long profileCreated = -1;
    private String playerNameString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent i = getIntent();

        //Retrieve steamId from intent
        steamId = i.getStringExtra(STEAM_ID_KEY);
        Log.d(LOG_TAG, "steamID: " + steamId);

        //Start downloading remaining info if the user.
        new DownloadUserInfoTask().execute(i.getStringExtra(JSON_USER_SUMMARIES_KEY));

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Request")
                .setAction("UserDataGuest")
                .build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @OnClick({R.id.button_bazaar_tf, R.id.button_steamrep, R.id.button_tf2op, R.id.button_tf2tp,
            R.id.button_steam_community, R.id.backpack})
    public void onLinkClick(View v) {

        //All buttons (except the backpack one) open a link in the browser
        if (v.getId() != R.id.backpack) {

            String url;

            //Get the link that needs to be opened.
            switch (v.getId()) {
                case R.id.button_bazaar_tf:
                    url = getString(R.string.link_bazaar_tf);
                    break;
                case R.id.button_steamrep:
                    url = getString(R.string.link_steamrep);
                    break;
                case R.id.button_tf2op:
                    url = getString(R.string.link_tf2op);
                    break;
                case R.id.button_tf2tp:
                    url = getString(R.string.link_tf2tp);
                    break;
                case R.id.button_steam_community:
                    url = getString(R.string.link_steam_community);
                    break;
                default:
                    return;
            }

            //Create an URI for the intent.
            Uri webPage = Uri.parse(url + steamId);

            //Open the link using the device default web browser.
            Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            if (privateBackpack) {
                //If the backpack is private, show a toast and do nothing.
                Toast.makeText(this, getString(R.string.message_private_backpack, playerNameString),
                        Toast.LENGTH_SHORT).show();
            } else {
                //Else the user clicked on the backpack button. Start the backpack activity. Pass
                //the steamId and whether it's the user's backpack or not
                Intent i = new Intent(this, UserBackpackActivity.class);
                i.putExtra(UserBackpackActivity.EXTRA_NAME, playerNameString);
                i.putExtra(UserBackpackActivity.EXTRA_GUEST, !steamId.equals(PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getString(getString(R.string.pref_resolved_steam_id), "")));
                startActivity(i);
            }
        }
    }

    @Override
    public void onUserBackpackFinished(int rawKeys, double rawMetal, int backpackSlots, int itemNumber) {
        //Store all the data
        this.rawKeys = rawKeys;
        this.rawMetal = rawMetal;
        this.backpackSlotNumber = backpackSlots;
        this.itemNumber = itemNumber;

        //Update the UI if both tasks have stopped
        updateUI();
    }

    @Override
    public void onPrivateBackpack() {
        //Backpack is private. All data bout the backpack is unkown and will be set to '?'
        rawKeys = -1;
        rawMetal = -1;
        backpackSlotNumber = -1;
        itemNumber = -1;
        backpackValue = -1;

        //Backpack is private
        privateBackpack = true;

        //Update the UI if both tasks have stopped
        updateUI();
    }

    @Override
    public void onUserBackpackFailed(String errorMessage) {

    }

    /**
     * Get all relevant info from a properly formatted JSON string returned by the GetUsers api.
     *
     * @param jsonString json string
     * @param steamId    steam id of the player
     * @throws JSONException
     * @see <a href="http://backpack.tf/api/users">Users API</a>
     */
    private void parseUserInfoJson(String jsonString, String steamId) throws JSONException {

        //All the JSON keys that is needed to retrieve all the necessary data.
        final String OWM_RESPONSE = "response";
        final String OWM_SUCCESS = "success";
        final String OWM_PLAYERS = "players";
        final String OWM_BACKPACK_VALUE = "backpack_value";
        final String OWM_BACKPACK_VALUE_TF2 = "440";
        final String OWM_PLAYER_NAME = "name";
        final String OWM_PLAYER_REPUTATION = "backpack_tf_reputation";
        final String OWM_PLAYER_BANNED = "backpack_tf_banned";
        final String OWM_PLAYER_TRUST = "backpack_tf_trust";
        final String OWM_PLAYER_TRUST_FOR = "for";
        final String OWM_PLAYER_TRUST_AGAINST = "against";
        final String OWM_PLAYER_SCAMMER = "steamrep_scammer";
        final String OWM_PLAYER_BAN_ECONOMY = "ban_economy";
        final String OWM_PLAYER_BAN_COMMUNITY = "ban_community";
        final String OWM_PLAYER_BAN_VAC = "ban_vac";

        //The main jason objects
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

        //Return if JSON response is unsuccessful
        if (response.getInt(OWM_SUCCESS) == 0) {
            Log.e(LOG_TAG, "Response unsuccessful");
            return;
        }

        //Get the current player JSON with the steamID
        JSONObject players = response.getJSONObject(OWM_PLAYERS);
        JSONObject current_user = players.getJSONObject(steamId);

        if (current_user.getInt(OWM_SUCCESS) == 1) {

            //Get the name
            if (current_user.has(OWM_PLAYER_NAME)) {
                playerNameString = current_user.getString(OWM_PLAYER_NAME);
            }

            //Get the player reputation
            if (current_user.has(OWM_PLAYER_REPUTATION)) {
                playerReputationValue = current_user.getInt(OWM_PLAYER_REPUTATION);
            }

            //Get the value of the backpack
            if (current_user.has(OWM_BACKPACK_VALUE)) {
                JSONObject backpackValues = current_user.getJSONObject(OWM_BACKPACK_VALUE);
                if (backpackValues.has(OWM_BACKPACK_VALUE_TF2)) {
                    backpackValue = backpackValues.getDouble(OWM_BACKPACK_VALUE_TF2);
                }
            }

            //If these are set, then the value is true
            isBanned = current_user.has(OWM_PLAYER_BANNED);
            isScammer = current_user.has(OWM_PLAYER_SCAMMER);
            isCommunityBanned = current_user.has(OWM_PLAYER_BAN_COMMUNITY);
            isTradeBanned = current_user.has(OWM_PLAYER_BAN_ECONOMY);
            isVacBanned = current_user.has(OWM_PLAYER_BAN_VAC);

            //Get the player's trust score
            if (current_user.has(OWM_PLAYER_TRUST)) {
                JSONObject trustScore = current_user.getJSONObject(OWM_PLAYER_TRUST);
                ratingPositive = trustScore.getInt(OWM_PLAYER_TRUST_FOR);
                ratingNegative = trustScore.getInt(OWM_PLAYER_TRUST_AGAINST);
            }
        }
    }

    /**
     * Get all relevant info from properly formatted JSON string returned by the GetSUerSummaries
     * api.
     *
     * @param jsonString json string
     * @throws JSONException
     */
    private void parseUserSummariesJson(String jsonString) throws JSONException {

        //All the necessary JSON keys for parsing
        final String OWM_RESPONSE = "response";
        final String OWM_PLAYERS = "players";
        final String OWM_NAME = "personaname";
        final String OWM_LAST_ONLINE = "lastlogoff";
        final String OWM_PLAYER_STATE = "personastate";
        final String OWM_PROFILE_CREATED = "timecreated";
        final String OWM_IN_GAME = "gameid";

        //Main JSON object, get the player json.
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);
        JSONArray players = response.getJSONArray(OWM_PLAYERS);
        JSONObject player = players.getJSONObject(0);

        //Get the player's name, this is the more relevant one.
        if (player.has(OWM_NAME)) {
            playerNameString = player.getString(OWM_NAME);
        }
        //Get the last online timestamp
        if (player.has(OWM_LAST_ONLINE)) {
            lastOnline = player.getLong(OWM_LAST_ONLINE);
        }
        //Get the state of the player
        if (player.has(OWM_PLAYER_STATE)) {
            playerState = player.getInt(OWM_PLAYER_STATE);
        }
        //Get the time when the steam account was created
        if (player.has(OWM_PROFILE_CREATED)) {
            profileCreated = player.getLong(OWM_PROFILE_CREATED);
        }
        //Check whether the user is playing a game currently or not
        if (player.has(OWM_IN_GAME)) {
            playerState = 7;
        }
    }

    /**
     * Update the UI with all available data.
     */
    private void updateUI() {

        //Set the title to X's profile
        setTitle(playerNameString);

        //Set the player reputation. Reputation: X
        playerReputation.setText(String.valueOf(playerReputationValue));

        //Set the 'user since' text
        if (profileCreated == -1) {
            userSinceText.setText(getString(R.string.filler_unknown));
        } else {
            userSinceText.setText(Utility.formatUnixTimeStamp(profileCreated));
        }

        //Switch for the player's state
        switch (playerState) {
            case 0:
                if (lastOnline == -1) {
                    //Weird
                    lastOnlineText.setText(String.format("%s %s", getString(R.string.user_page_last_online), getString(R.string.filler_unknown)));
                } else {
                    //Player is offline, show how long was it since the player was last online
                    lastOnlineText.setText(String.format("%s %s", getString(R.string.user_page_last_online), Utility.formatLastOnlineTime(this,
                            System.currentTimeMillis() - lastOnline * 1000L)));
                }
                break;
            case 1:
                lastOnlineText.setText(getString(R.string.user_page_status_online));
                lastOnlineText.setTextColor(Utility.getColor(this, R.color.player_online));
                break;
            case 2:
                lastOnlineText.setText(getString(R.string.user_page_status_busy));
                lastOnlineText.setTextColor(Utility.getColor(this, R.color.player_online));
                break;
            case 3:
                lastOnlineText.setText(getString(R.string.user_page_status_away));
                lastOnlineText.setTextColor(Utility.getColor(this, R.color.player_online));
                break;
            case 4:
                lastOnlineText.setText(getString(R.string.user_page_status_snooze));
                lastOnlineText.setTextColor(Utility.getColor(this, R.color.player_online));
                break;
            case 5:
                lastOnlineText.setText(getString(R.string.user_page_status_trade));
                lastOnlineText.setTextColor(Utility.getColor(this, R.color.player_online));
                break;
            case 6:
                lastOnlineText.setText(getString(R.string.user_page_status_play));
                lastOnlineText.setTextColor(Utility.getColor(this, R.color.player_online));
                break;
            case 7:
                lastOnlineText.setText(getString(R.string.user_page_status_in_game));
                lastOnlineText.setTextColor(Utility.getColor(this, R.color.player_in_game));
                break;
        }

        Drawable statusOk = getResources().getDrawable(R.drawable.ic_done_white);
        Drawable statusBad = getResources().getDrawable(R.drawable.ic_close_white);
        if (statusOk != null) statusOk.setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
        if (statusBad != null) statusBad.setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

        //Steamrep information
        if (!isScammer) {
            steamRepStatus.setImageDrawable(statusOk);
        } else {
            steamRepStatus.setImageDrawable(statusBad);
        }

        //Trade status
        if (!isTradeBanned) {
            tradeStatus.setImageDrawable(statusOk);
        } else {
            tradeStatus.setImageDrawable(statusBad);
        }

        //VAC status
        if (!isVacBanned) {
            vacStatus.setImageDrawable(statusOk);
        } else {
            vacStatus.setImageDrawable(statusBad);
        }

        //Community status
        if (!isCommunityBanned) {
            communityStatus.setImageDrawable(statusOk);
        } else {
            communityStatus.setImageDrawable(statusBad);
        }

        //Backpack value
        if (backpackValue == -1) {
            //Value is unknown (probably private)
            backpackValueRefined.setText("?");
            backpackValueUsd.setText("?");
        } else if (!privateBackpack) {
            backpackValueRefined.setText(String.valueOf(backpackValue));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            //Convert the value into USD and format it like above
            double bpValueUsd = backpackValue * Utility.getDouble(prefs, getString(R.string.pref_metal_raw_usd), 1);
            backpackValueUsd.setText(String.valueOf(bpValueUsd));
        }

        //Set the trust score and color the background according to it.
        trustPositive.setText(String.format("+%d", ratingPositive));
        trustNegative.setText(String.format("-%d", ratingNegative));

        //Image should be available in data folder by the time this method is called.
        Glide.with(this)
                .load(PreferenceManager.getDefaultSharedPreferences(this).
                        getString(getString(R.string.pref_search_avatar_url), ""))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(avatar);

        //Raw keys
        if (rawKeys >= 0)
            backpackRawKeys.setText(String.valueOf(rawKeys));
        else
            backpackRawKeys.setText("?");

        //Raw metal
        if (rawMetal >= 0)
            backpackRawMetal.setText(String.valueOf(Utility.formatDouble(rawMetal)));
        else
            backpackRawMetal.setText("?");

        //Number of slots and slots used
        if (itemNumber >= 0 && backpackSlotNumber >= 0)
            backpackSlots.setText(String.valueOf(itemNumber + "/" + backpackSlotNumber));
        else
            backpackSlots.setText("?/?");

        //Reveal all the info and remove the progress bar.
        findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Task the retrieves all the necessary user data in the background.
     */
    private class DownloadUserInfoTask extends AsyncTask<String, String, Void> {

        public final String LOG_TAG = DownloadUserInfoTask.class.getSimpleName();

        /**
         * {@inheritDoc}
         */
        @Override
        protected Void doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            String jsonString;

            try {

                //The url of the api and parameter keys
                final String USER_INFO_BASE_URL = getString(R.string.backpack_tf_get_users);
                final String KEY_STEAM_ID = "steamids";
                final String KEY_COMPRESS = "compress";

                //Build the uri
                Uri uri = Uri.parse(USER_INFO_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_STEAM_ID, steamId)
                        .appendQueryParameter(KEY_COMPRESS, "1")
                        .build();

                Log.d(LOG_TAG, "Built uri: " + uri.toString());

                URL url = new URL(uri.toString());

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                int statusCode = response.code();

                if (statusCode >= 500) {
                    return null;
                } else if (statusCode >= 400) {
                    return null;
                }

                jsonString = response.body().string();
                parseUserInfoJson(jsonString, steamId);

                //JSON that arrived with the intent
                parseUserSummariesJson(params[0]);

            } catch (IOException e) {
                //There was a network error
                publishProgress(getString(R.string.error_network));
                e.printStackTrace();

                mTracker.send(new HitBuilders.ExceptionBuilder()
                        .setDescription("Network exception:UserActivity, Message: " + e.getMessage())
                        .setFatal(false)
                        .build());
                return null;
            } catch (JSONException e) {
                //JSON was improperly formatted, pls no
                publishProgress(getString(R.string.error_data_parse));
                e.printStackTrace();

                mTracker.send(new HitBuilders.ExceptionBuilder()
                        .setDescription("JSON exception:UserActivity, Message: " + e.getMessage())
                        .setFatal(true)
                        .build());
                return null;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //only used for showing error messages to the user.
            if (values.length > 0) {
                Toast.makeText(UserActivity.this, "bptf: " + values[0], Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Tf2UserBackpackInteractor task = new Tf2UserBackpackInteractor(UserActivity.this, (BptfApplication) getApplication(), UserActivity.this);
            task.execute(steamId);

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("UserBackpackGuest")
                    .build());
        }
    }
}