package com.tlongdev.bktf.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.network.FetchUserBackpack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * Profile page activity.
 */
public class UserActivity extends AppCompatActivity implements View.OnClickListener,
        FetchUserBackpack.OnFetchUserBackpackListener {

    private static final String LOG_TAG = UserActivity.class.getSimpleName();

    //Keys for extra data in the intent.
    public static final String STEAM_ID_KEY = "steamid";
    public static final String JSON_USER_SUMMARIES_KEY = "json_user_summaries";

    //Progress bar that indicates donwloading user data.
    private ProgressBar progressBar;

    //Reference too all the views that needs to be updated
    private TextView playerName;
    private TextView playerReputation;
    private TextView trustStatus;
    private TextView steamRepStatus;
    private TextView vacStatus;
    private TextView tradeStatus;
    private TextView communityStatus;
    private TextView backpackValueRefined;
    private TextView backpackRawMetal;
    private TextView backpackRawKeys;
    private TextView backpackValueUsd;
    private TextView backpackSlots;
    private TextView userSinceText;
    private TextView lastOnlineText;
    private ImageView avatar;

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

    //Booleans to indicate whether that task are running or not
    private boolean backpackFetching = false;
    private boolean userFetching = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();

        //Retrieve steamId from intent
        steamId = i.getStringExtra(STEAM_ID_KEY);
        if (Utility.isDebugging(this)) {
            Log.d(LOG_TAG, "steamID: " + steamId);
        }

        //Start downloading remaining info if the user.
        new FetchUserInfoTask().execute(i.getStringExtra(JSON_USER_SUMMARIES_KEY));
        FetchUserBackpack fetchTask = new FetchUserBackpack(this);
        fetchTask.registerOnFetchUserBackpackListener(this);
        fetchTask.execute(steamId);

        //Indicate that the tasks have started
        backpackFetching = true;
        userFetching = true;

        //Find all the views
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        playerName = (TextView) findViewById(R.id.text_view_player_name);
        playerReputation = (TextView) findViewById(R.id.text_view_player_reputation);
        trustStatus = (TextView) findViewById(R.id.text_view_trust_status);
        steamRepStatus = (TextView) findViewById(R.id.text_view_steamrep_status);
        vacStatus = (TextView) findViewById(R.id.text_view_vac_status);
        tradeStatus = (TextView) findViewById(R.id.text_view_trade_status);
        communityStatus = (TextView) findViewById(R.id.text_view_community_status);
        backpackValueRefined = (TextView) findViewById(R.id.text_view_bp_refined);
        backpackRawMetal = (TextView) findViewById(R.id.text_view_bp_raw_metal);
        backpackRawKeys = (TextView) findViewById(R.id.text_view_bp_raw_keys);
        backpackValueUsd = (TextView) findViewById(R.id.text_view_bp_usd);
        backpackSlots = (TextView) findViewById(R.id.text_view_bp_slots);
        userSinceText = (TextView) findViewById(R.id.text_view_user_since);
        lastOnlineText = (TextView) findViewById(R.id.text_view_user_last_online);
        avatar = (ImageView) findViewById(R.id.player_avatar);

        //Register this onclicklistener
        findViewById(R.id.button_bazaar_tf).setOnClickListener(this);
        findViewById(R.id.button_steamrep).setOnClickListener(this);
        findViewById(R.id.button_tf2op).setOnClickListener(this);
        findViewById(R.id.button_tf2tp).setOnClickListener(this);
        findViewById(R.id.button_steam_community).setOnClickListener(this);
        findViewById(R.id.button_backpack).setOnClickListener(this);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {

        //All buttons (except the backpack one) open a link in the browser
        if (v.getId() != R.id.button_backpack) {

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
            if (Utility.isDebugging(this)) {
                Log.e(LOG_TAG, "Response unsuccessful");
            }
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
        getSupportActionBar().setTitle(getString(R.string.title_custom_profile, playerNameString));

        //Set the player name
        playerName.setText(playerNameString);

        if (isBanned) {
            //Set player name to red and cross it out.
            playerName.setTextColor(0xffdd4c44);
            playerName.setPaintFlags(playerName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        //Set the player reputation. Reputation: X
        playerReputation.setText(getString(R.string.user_page_reputation) + " "
                + playerReputationValue);

        //Set the 'user since' text
        if (profileCreated == -1) {
            userSinceText.setText(getString(R.string.user_page_user_since)
                    + getString(R.string.filler_unknown));
        } else {
            userSinceText.setText(getString(R.string.user_page_user_since) + Utility
                    .formatUnixTimeStamp(profileCreated));
        }

        //Switch for the player's state
        switch (playerState) {
            case 0:
                if (lastOnline == -1) {
                    //Weird
                    lastOnlineText.setText(getString(R.string.user_page_last_online) + " "
                            + getString(R.string.filler_unknown));
                } else {
                    //Player is offline, show how long was it since the player was last online
                    lastOnlineText.setText(getString(R.string.user_page_last_online) + " "
                            + Utility.formatLastOnlineTime(this,
                            System.currentTimeMillis() - lastOnline * 1000L));
                }
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_offline));
                break;
            case 1:
                lastOnlineText.setText(getString(R.string.user_page_status_online));
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 2:
                lastOnlineText.setText(getString(R.string.user_page_status_busy));
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 3:
                lastOnlineText.setText(getString(R.string.user_page_status_away));
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 4:
                lastOnlineText.setText(getString(R.string.user_page_status_snooze));
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 5:
                lastOnlineText.setText(getString(R.string.user_page_status_trade));
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 6:
                lastOnlineText.setText(getString(R.string.user_page_status_play));
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 7:
                lastOnlineText.setText(getString(R.string.user_page_status_in_game));
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_in_game));
                break;
        }

        //Steamrep information
        if (!isScammer) {
            steamRepStatus.setText(getString(R.string.user_page_status_normal));
            steamRepStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_neutral));
        } else {
            steamRepStatus.setText(getString(R.string.user_page_status_scammer));
            steamRepStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_bad));
        }

        //Trade status
        if (!isTradeBanned) {
            tradeStatus.setText(getString(R.string.user_page_status_ok));
            tradeStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_good));
        } else {
            tradeStatus.setText(getString(R.string.user_page_status_ban));
            tradeStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_bad));
        }

        //VAC status
        if (!isVacBanned) {
            vacStatus.setText(getString(R.string.user_page_status_ok));
            vacStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_good));
        } else {
            vacStatus.setText(getString(R.string.user_page_status_ban));
            vacStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_bad));
        }

        //Community status
        if (!isCommunityBanned) {
            communityStatus.setText(getString(R.string.user_page_status_ok));
            communityStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_good));
        } else {
            communityStatus.setText(getString(R.string.user_page_status_ban));
            communityStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_bad));
        }

        //Backpack value
        if (backpackValue == -1) {
            //Value is unknown (probably private)
            backpackValueRefined.setText("?");
            backpackValueUsd.setText("?");
        } else if (!privateBackpack) {
            //Properly format the backpack value (is int, does it have a fraction smaller than 0.01)
            if ((int) backpackValue == backpackValue)
                backpackValueRefined.setText(String.valueOf((int) backpackValue));
            else if ((String.valueOf(backpackValue)).substring((String.valueOf(backpackValue)).indexOf('.') + 1)
                    .length() > 2)
                backpackValueRefined.setText(String.valueOf(new DecimalFormat("#0.00").format(backpackValue)));
            else
                backpackValueRefined.setText(String.valueOf(backpackValue));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            //Convert the value into USD and format it like above
            double bpValueUsd = backpackValue * Utility.getDouble(prefs, getString(R.string.pref_metal_raw_usd), 1);
            if ((int) bpValueUsd == bpValueUsd)
                backpackValueUsd.setText(String.valueOf((int) bpValueUsd));
            else if ((String.valueOf(bpValueUsd).substring((String.valueOf(bpValueUsd).indexOf('.') + 1)).length() > 2))
                backpackValueUsd.setText(String.valueOf(new DecimalFormat("#0.00").format(bpValueUsd)));
            else
                backpackValueUsd.setText(String.valueOf(bpValueUsd));
        }

        //Set the border of the avatar according to the player's state
        switch (playerState) {
            case 0:
                avatar.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.frame_user_state_offline));
                break;
            case 7:
                avatar.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.frame_user_state_in_game));
                break;
            default:
                avatar.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.frame_user_state_online));
                break;
        }

        //Set the trust score and color the background according to it.
        trustStatus.setText(String.valueOf((ratingPositive - ratingNegative)));
        if (ratingNegative > ratingPositive) {
            trustStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_bad));
        } else if (ratingPositive > ratingNegative && ratingNegative == 0) {
            trustStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_good));
        } else if (ratingPositive > ratingNegative && ratingNegative >= 0) {
            trustStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_caution));
        } else {
            trustStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_neutral));
        }

        //Image should be available in data folder by the time this method is called.
        avatar.setImageDrawable(Drawable.createFromPath(getFilesDir().toString() +
                "/avatar_search.png"));

        //Raw keys
        if (rawKeys >= 0)
            backpackRawKeys.setText(String.valueOf(rawKeys));
        else
            backpackRawKeys.setText("?");

        //Raw metal
        if (rawMetal >= 0)
            backpackRawMetal.setText(String.valueOf(Utility.roundDouble(rawMetal, 2)));
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
     * {@inheritDoc}
     */
    @Override
    public void onFetchFinished(int rawKeys, double rawMetal, int backpackSlots, int itemNumber) {
        //Store all the data
        this.rawKeys = rawKeys;
        this.rawMetal = rawMetal;
        this.backpackSlotNumber = backpackSlots;
        this.itemNumber = itemNumber;

        //Backpack fetching finished
        backpackFetching = false;

        //Update the UI if both tasks have stopped
        if (!userFetching) {
            updateUI();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrivateBackpack() {
        //Backpack is private. All data bout the backpack is unkown and will be set to '?'
        rawKeys = -1;
        rawMetal = -1;
        backpackSlotNumber = -1;
        itemNumber = -1;
        backpackValue = -1;

        //Backpack fetshing finished
        backpackFetching = false;

        //Backpack is private
        privateBackpack = true;

        //Update the UI if both tasks have stopped
        if (!userFetching) {
            updateUI();
        }
    }

    /**
     * Task the retrieves all the necessary user data in the background.
     */
    private class FetchUserInfoTask extends AsyncTask<String, String, Void> {

        public final String LOG_TAG = FetchUserInfoTask.class.getSimpleName();

        /**
         * {@inheritDoc}
         */
        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

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

                if (Utility.isDebugging(UserActivity.this)) {
                    Log.d(LOG_TAG, "Built uri: " + uri.toString());
                }

                URL url = new URL(uri.toString());

                //Open connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //Initialize input stream for reading
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                String line;

                if (inputStream != null) {

                    //Initialize the reader
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    //Read the input
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    if (buffer.length() > 0) {
                        //put the json string into a string and read the data out of it.
                        jsonString = buffer.toString();
                        parseUserInfoJson(jsonString, steamId);
                    }

                    //JSON that arrived with the intent
                    parseUserSummariesJson(params[0]);
                }
            } catch (IOException e) {
                //There was a network error
                //TODO distinguish all network errors: timeout, connection refused, api down, etc.
                publishProgress(getString(R.string.error_network));
                if (Utility.isDebugging(UserActivity.this))
                    e.printStackTrace();
                return null;
            } catch (JSONException e) {
                //JSON was improperly formatted, pls no
                publishProgress(getString(R.string.error_data_parse));
                if (Utility.isDebugging(UserActivity.this))
                    e.printStackTrace();
                return null;
            } finally {

                //close the connection
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                //close the input reader
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        publishProgress(e.getMessage());
                        if (Utility.isDebugging(UserActivity.this)) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onProgressUpdate(String... values) {
            //only used for showing error messages to the user.
            if (values.length > 0) {
                Toast.makeText(UserActivity.this, "bptf: " + values[0], Toast.LENGTH_SHORT)
                        .show();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            userFetching = false;
            //Populate the UI with the new data
            if (!backpackFetching)
                updateUI();
        }
    }
}
