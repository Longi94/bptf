package com.tlongdev.bktf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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


public class UserInfoActivity extends ActionBarActivity implements View.OnClickListener {

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
    private boolean isInGroup = false;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff5787c5));

        Intent i = getIntent();
        steamId = i.getStringExtra(STEAM_ID_KEY);

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

        new FetchUserInfoTask().execute(i.getStringExtra(JSON_USER_SUMMARIES_KEY));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*getMenuInflater().inflate(R.menu.menu_user_info, menu);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        String url;
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
            case R.id.button_backpack:
                url = "http://backpack.tf/profiles/";
                break;
            default:
                return;
        }

        Uri webPage = Uri.parse(url + steamId);

        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void parseUserInfoJson(String jsonString, String steamId) throws JSONException {

        final String OWM_RESPONSE = "response";
        final String OWM_SUCCESS = "success";
        final String OWM_CURRENT_TIME = "current_time";
        final String OWM_PLAYERS = "players";
        final String OWM_BACKPACK_VALUE = "backpack_value";
        final String OWM_BACKPACK_VALUE_TF2 = "440";
        final String OWM_BACKPACK_UPDATE = "backpack_update";
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

        JSONObject current_user = players.getJSONObject(steamId);

        if (current_user.getInt(OWM_SUCCESS) == 1) {

            playerNameString = current_user.getString(OWM_PLAYER_NAME);
            if (current_user.has(OWM_PLAYER_REPUTATION)) {
                playerReputationValue = current_user.getInt(OWM_PLAYER_REPUTATION);
            }
            backpackValue =  current_user.getJSONObject(OWM_BACKPACK_VALUE).getDouble(OWM_BACKPACK_VALUE_TF2);

            isInGroup = current_user.has(OWM_PLAYER_GROUP);
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
    
    private void updateUI() {
        getSupportActionBar().setTitle(playerNameString + "'s profile");
        playerName.setText(playerNameString);
        
        if (isBanned){
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
                lastOnlineText.setText("Looking to playe");
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
            if ((int) backpackValue == backpackValue)
                backpackValueRefined.setText("" + (int) backpackValue);
            else if (("" + backpackValue).substring(("" + backpackValue).indexOf('.') + 1).length() > 2)
                backpackValueRefined.setText("" + new DecimalFormat("#0.00").format(backpackValue));
            else
                backpackValueRefined.setText("" + backpackValue);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            float bpValueUsd = (float)backpackValue * prefs.getFloat(getString(R.string.pref_metal_raw_usd), 1);
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

        avatar.setImageDrawable(Drawable.createFromPath(getFilesDir().toString() + "/avatar_search.png"));

        findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private class FetchUserInfoTask extends AsyncTask<String, Void, Void> {

        String errorMessage = "";

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

                uri = Uri.parse(USER_INFO_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_STEAM_ID, steamId)
                        .appendQueryParameter(KEY_COMPRESS, "1")
                        .build();

                url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                buffer = new StringBuffer();

                String line;
                if (inputStream != null) {

                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line);
                    }

                    if (buffer.length() > 0) {
                        jsonString = buffer.toString();
                        parseUserInfoJson(jsonString, steamId);
                    }
                    parseUserSummariesJson(params[0]);
                }
            } catch (IOException e) {
                errorMessage = "network error";
                publishProgress();
                if (Utility.isDebugging())
                    e.printStackTrace();
                return null;
            } catch (JSONException e) {
                errorMessage = "error while parsing data";
                publishProgress();
                if (Utility.isDebugging())
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
            Toast.makeText(UserInfoActivity.this, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateUI();
        }
    }
}
