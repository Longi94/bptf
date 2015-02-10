package com.tlongdev.bktf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.task.FetchUserBackpack;

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
public class UserInfoActivity extends ActionBarActivity implements View.OnClickListener, FetchUserBackpack.OnFetchUserBackpackListener {

    private static final String LOG_TAG = UserInfoActivity.class.getSimpleName();

    public static final String STEAM_ID_KEY = "steamid";
    public static final String JSON_USER_SUMMARIES_KEY = "json_user_summaries";

    private ProgressBar progressBar;

    private TextView playerName;
    private TextView playerReputation;
    private TextView trustStatus;
    private TextView steamRepStatus;
    private TextView vacStatus;
    private TextView tradeStatus;
    private TextView communityStatus;
    private TextView backpackValueRefined;
    //TODO fix these after implementing backpack viewer.
    private TextView backpackRawMetal;
    private TextView backpackRawKeys;
    private TextView backpackValueUsd;
    private TextView backpackSlots;
    private TextView userSinceText;
    private TextView lastOnlineText;
    private ImageView avatar;

    private String steamId;
    private String playerNameString;
    private int playerReputationValue = 0;
    private double backpackValue = 0;
    private boolean isBanned = false;
    private boolean isScammer = false;
    private boolean isCommunityBanned = false;
    private boolean isTradeBanned = false;
    private boolean isVacBanned = false;
    private int ratingPositive = 0;
    private int ratingNegative = 0;
    private long lastOnline = -1;
    private int playerState = 0;
    private long profileCreated = -1;
    private int backpackSlotNumber;
    private int itemNumber;
    private int rawKeys;
    private double rawMetal;

    private FetchUserBackpack fetchTask;
    private boolean backpackFetching = false;
    private boolean userFetching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        //Retrieve steamId from intent
        Intent i = getIntent();
        steamId = i.getStringExtra(STEAM_ID_KEY);
        if (Utility.isDebugging(this)){
            Log.d(LOG_TAG, "steamID: " + steamId);
        }
        //Start downloading remaining info if the user.
        new FetchUserInfoTask().execute(i.getStringExtra(JSON_USER_SUMMARIES_KEY));
        userFetching = true;
        fetchTask = new FetchUserBackpack(this, false);
        fetchTask.registerOnFetchUserBackpackListener(this);
        fetchTask.execute(steamId);
        backpackFetching = true;

        //Set the title to X's profile
        getSupportActionBar().setTitle(playerNameString + "'s profile");

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

        findViewById(R.id.button_bazaar_tf).setOnClickListener(this);
        findViewById(R.id.button_steamrep).setOnClickListener(this);
        findViewById(R.id.button_tf2op).setOnClickListener(this);
        findViewById(R.id.button_tf2tp).setOnClickListener(this);
        findViewById(R.id.button_steam_community).setOnClickListener(this);
        findViewById(R.id.button_backpack).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.button_backpack) {
            String url;
            //Handle all the buttons.
            switch (v.getId()) {
                case R.id.button_bazaar_tf:
                    url = "http://bazaar.tf/profiles/";
                    break;
                case R.id.button_steamrep:
                    url = "http://steamrep.com/profiles/";
                    break;
                case R.id.button_tf2op:
                    url = "http://www.tf2outpost.com/user/";
                    break;
                case R.id.button_tf2tp:
                    url = "http://tf2tp.com/user/";
                    break;
                case R.id.button_steam_community:
                    url = "http://steamcommunity.com/profiles/";
                    break;
                default:
                    return;
            }


            Uri webPage = Uri.parse(url + steamId);

            //Open the link using the device default web browser.
            Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {

            Intent i = new Intent(this, UserBackpackActivity.class);
            i.putExtra(UserBackpackActivity.EXTRA_NAME, playerNameString);
            i.putExtra(UserBackpackActivity.EXTRA_GUEST, !steamId.equals(PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.pref_resolved_steam_id), "")));
            startActivity(i);
        }
    }

    /**
     * Get all relevant info from a properly formatted JSON string.
     * @see <a href="http://backpack.tf/api/users">Users API</a>
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
            if (Utility.isDebugging(this)){
                Log.e(LOG_TAG, "Response unsuccessful");
            }
            return;
        }

        JSONObject players = response.getJSONObject(OWM_PLAYERS);
        JSONObject current_user = players.getJSONObject(steamId);

        if (current_user.getInt(OWM_SUCCESS) == 1) {

            playerNameString = current_user.getString(OWM_PLAYER_NAME);
            if (current_user.has(OWM_PLAYER_REPUTATION)) {
                playerReputationValue = current_user.getInt(OWM_PLAYER_REPUTATION);
            }
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

            if (current_user.has(OWM_PLAYER_TRUST)) {
                JSONObject trustScore = current_user.getJSONObject(OWM_PLAYER_TRUST);
                ratingPositive = trustScore.getInt(OWM_PLAYER_TRUST_FOR);
                ratingNegative = trustScore.getInt(OWM_PLAYER_TRUST_AGAINST);
            }
        }
    }

    /**
     * Get all relevant info from properly formatted JSON string.
     * @see <a href="https://wiki.teamfortress.com/wiki/WebAPI/GetPlayerSummaries">GetUserSummaries API</a>
     */
    private void parseUserSummariesJson(String jsonString) throws JSONException {

        final String OWM_RESPONSE = "response";
        final String OWM_PLAYERS = "players";
        final String OWM_NAME = "personaname";
        final String OWM_LAST_ONLINE = "lastlogoff";
        final String OWM_PLAYER_STATE = "personastate";
        final String OWM_PROFILE_CREATED = "timecreated";
        final String OWM_IN_GAME = "gameid";

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);
        JSONArray players = response.getJSONArray(OWM_PLAYERS);
        JSONObject player = players.getJSONObject(0);

        playerNameString = player.getString(OWM_NAME);
        lastOnline = player.getLong(OWM_LAST_ONLINE);
        playerState =  player.getInt(OWM_PLAYER_STATE);

        if (player.has(OWM_PROFILE_CREATED)) {
            profileCreated = player.getLong(OWM_PROFILE_CREATED);
        }
        if (player.has(OWM_IN_GAME)){
            playerState = 7;
        }
    }

    /**
     * Update the UI with all available data.
     */
    private void updateUI() {
        playerName.setText(playerNameString);
        
        if (isBanned){
            //Set player name to red and cross it out.
            playerName.setTextColor(0xffdd4c44);
            playerName.setPaintFlags(playerName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        
        playerReputation.setText("Reputation: " + playerReputationValue);

        if (profileCreated == -1){
            userSinceText.setText("Steam user since:\nunknown");
        } else {
            userSinceText.setText("Steam user since:\n" + Utility.formatUnixTimeStamp(profileCreated));
        }

        switch (playerState) {
            case 0:
                if (lastOnline == -1){
                    lastOnlineText.setText("Last online: unknown");
                } else {
                    lastOnlineText.setText("Last online: " + Utility.formatLastOnlineTime(System.currentTimeMillis() - lastOnline * 1000L));
                }
                lastOnlineText.setTextColor(0xff6E6E6E);
                break;
            case 1:
                lastOnlineText.setText("Online");
                lastOnlineText.setTextColor(0xff24a9de);
                break;
            case 2:
                lastOnlineText.setText("Busy");
                lastOnlineText.setTextColor(0xff24a9de);
                break;
            case 3:
                lastOnlineText.setText("Away");
                lastOnlineText.setTextColor(0xff24a9de);
                break;
            case 4:
                lastOnlineText.setText("Snooze");
                lastOnlineText.setTextColor(0xff24a9de);
                break;
            case 5:
                lastOnlineText.setText("Looking to trade");
                lastOnlineText.setTextColor(0xff24a9de);
                break;
            case 6:
                lastOnlineText.setText("Looking to play");
                lastOnlineText.setTextColor(0xff24a9de);
                break;
            case 7:
                lastOnlineText.setText("In-Game");
                lastOnlineText.setTextColor(0xff8fb93b);
                break;
        }

        if (!isScammer) {
            steamRepStatus.setText("NORMAL");
            steamRepStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_neutral));
        }
        else {
            steamRepStatus.setText("SCAMMER");
            steamRepStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_bad));
        }

        if (!isTradeBanned) {
            tradeStatus.setText("OK");
            tradeStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_good));
        } else {
            tradeStatus.setText("BAN");
            tradeStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_bad));
        }

        if (!isVacBanned) {
            vacStatus.setText("OK");
            vacStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_good));
        } else {
            vacStatus.setText("BAN");
            vacStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_bad));
        }

        if (!isCommunityBanned) {
            communityStatus.setText("OK");
            communityStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_good));
        } else {
            communityStatus.setText("BAN");
            communityStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_bad));
        }

        if (backpackValue == -1) {
            backpackValueRefined.setText("?");
            backpackValueUsd.setText("?");
        } else {
            //Properly format the backpack value (is it int, does it have a fraction smaller than 0.01)
            if ((int) backpackValue == backpackValue)
                backpackValueRefined.setText("" + (int) backpackValue);
            else if (("" + backpackValue).substring(("" + backpackValue).indexOf('.') + 1).length() > 2)
                backpackValueRefined.setText("" + new DecimalFormat("#0.00").format(backpackValue));
            else
                backpackValueRefined.setText("" + backpackValue);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            double bpValueUsd = backpackValue * Utility.getDouble(prefs, getString(R.string.pref_metal_raw_usd), 1);
            if ((int) bpValueUsd == bpValueUsd)
                backpackValueUsd.setText("" + (int) bpValueUsd);
            else if (("" + bpValueUsd).substring(("" + bpValueUsd).indexOf('.') + 1).length() > 2)
                backpackValueUsd.setText("" + new DecimalFormat("#0.00").format(bpValueUsd));
            else
                backpackValueUsd.setText("" + bpValueUsd);
        }

        switch (playerState){
            case 0:
                avatar.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_user_state_offline));
                break;
            case 7:
                avatar.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_user_state_in_game));
                break;
            default:
                avatar.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_user_state_online));
                break;
        }

        trustStatus.setText("" + (ratingPositive - ratingNegative));
        if (ratingNegative > ratingPositive){
            trustStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_bad));
        } else if (ratingPositive > ratingNegative && ratingNegative == 0){
            trustStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_good));
        } else if (ratingPositive > ratingNegative && ratingNegative >= 0){
            trustStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_caution));
        } else {
            trustStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_neutral));
        }

        //Image should be available in data folder by the time this method is called.
        avatar.setImageDrawable(Drawable.createFromPath(getFilesDir().toString() + "/avatar_search.png"));

        backpackRawKeys.setText("" + rawKeys);
        backpackRawMetal.setText("" + Utility.roundDouble(rawMetal, 2));

        backpackSlots.setText("" + itemNumber + "/" + backpackSlotNumber);

        //Reveal all the info and remove the progress bar.
        findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onFetchFinished(int rawKeys, double rawMetal, int backpackSlots, int itemNumber) {
        this.rawKeys = rawKeys;
        this.rawMetal = rawMetal;
        this.backpackSlotNumber = backpackSlots;
        this.itemNumber = itemNumber;
        backpackFetching = false;
        if (!userFetching){
            updateUI();
        }
    }

    /**
     * Task the retrieves all the necessary user data in the background.
     */
    private class FetchUserInfoTask extends AsyncTask<String, Void, Void> {

        public final String LOG_TAG = FetchUserInfoTask.class.getSimpleName();

        String errorMessage;

        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonString;

            try {

                final String USER_INFO_BASE_URL = "http://backpack.tf/api/IGetUsers/v3/";
                final String KEY_STEAM_ID = "steamids";
                final String KEY_COMPRESS = "compress";

                Uri uri;
                URL url;
                InputStream inputStream;
                StringBuffer buffer;

                //Build uri
                uri = Uri.parse(USER_INFO_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_STEAM_ID, steamId)
                        .appendQueryParameter(KEY_COMPRESS, "1")
                        .build();

                if (Utility.isDebugging(UserInfoActivity.this)){
                    Log.d(LOG_TAG, "Built uri: " + uri.toString());
                }
                url = new URL(uri.toString());

                //Open connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                buffer = new StringBuffer();

                String line;
                if (inputStream != null) {

                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    //Read the input
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    if (buffer.length() > 0) {
                        jsonString = buffer.toString();
                        parseUserInfoJson(jsonString, steamId);
                    }
                    //JSON that arrived with the intent
                    parseUserSummariesJson(params[0]);
                }
            } catch (IOException e) {
                errorMessage = "network error";
                publishProgress();
                if (Utility.isDebugging(UserInfoActivity.this))
                    e.printStackTrace();
                return null;
            } catch (JSONException e) {
                errorMessage = "error while parsing data";
                publishProgress();
                if (Utility.isDebugging(UserInfoActivity.this))
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
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //only used for showing error messages to the user.
            Toast.makeText(UserInfoActivity.this, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            userFetching = false;
            if (!backpackFetching)
                updateUI();
        }
    }
}
