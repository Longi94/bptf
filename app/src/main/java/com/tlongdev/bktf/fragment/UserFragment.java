package com.tlongdev.bktf.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.UserBackpackActivity;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.task.FetchUserInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * Fragment for displaying the user profile.
 */
public class UserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener, FetchUserInfo.OnFetchUserInfoListener {

    //Reference too all the views that need to be updated
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
    private ProgressBar progressBar;

    //Swipe refresh layout for refreshing the user data manually
    private SwipeRefreshLayout mLayout;

    //The task for fetching user info
    private FetchUserInfo fetchTask;

    //Stores whether the backpack is private or not
    private boolean privateBackpack = false;

    /**
     * Constructor
     */
    public UserFragment() {
        //Required empty constructor.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_user,
                container, false);

        //Find all the views
        playerName = (TextView) mLayout.findViewById(R.id.text_view_player_name);
        playerReputation = (TextView) mLayout.findViewById(R.id.text_view_player_reputation);
        trustStatus = (TextView) mLayout.findViewById(R.id.text_view_trust_status);
        steamRepStatus = (TextView) mLayout.findViewById(R.id.text_view_steamrep_status);
        vacStatus = (TextView) mLayout.findViewById(R.id.text_view_vac_status);
        tradeStatus = (TextView) mLayout.findViewById(R.id.text_view_trade_status);
        communityStatus = (TextView) mLayout.findViewById(R.id.text_view_community_status);
        backpackValueRefined = (TextView) mLayout.findViewById(R.id.text_view_bp_refined);
        backpackRawMetal = (TextView) mLayout.findViewById(R.id.text_view_bp_raw_metal);
        backpackRawKeys = (TextView) mLayout.findViewById(R.id.text_view_bp_raw_keys);
        backpackValueUsd = (TextView) mLayout.findViewById(R.id.text_view_bp_usd);
        backpackSlots = (TextView) mLayout.findViewById(R.id.text_view_bp_slots);
        userSinceText = (TextView) mLayout.findViewById(R.id.text_view_user_since);
        lastOnlineText = (TextView) mLayout.findViewById(R.id.text_view_user_last_online);
        avatar = (ImageView) mLayout.findViewById(R.id.player_avatar);
        progressBar = (ProgressBar) mLayout.findViewById(R.id.progress_bar);

        //Set the color of the refreshing animation
        mLayout.setColorSchemeColors(0xff5787c5);
        mLayout.setOnRefreshListener(this);

        //A workaround for the refreshing animation not appearing when calling setRefreshing(true)
        mLayout.setProgressViewOffset(false,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -55,
                        getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25,
                        getResources().getDisplayMetrics()));

        //Set the on click listeners for the buttons
        mLayout.findViewById(R.id.button_bazaar_tf).setOnClickListener(this);
        mLayout.findViewById(R.id.button_steamrep).setOnClickListener(this);
        mLayout.findViewById(R.id.button_tf2op).setOnClickListener(this);
        mLayout.findViewById(R.id.button_tf2tp).setOnClickListener(this);
        mLayout.findViewById(R.id.button_steam_community).setOnClickListener(this);
        mLayout.findViewById(R.id.button_backpack).setOnClickListener(this);

        //Update all the views to show te user data
        updateUserPage();

        return mLayout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        //Update user info if last update was more than 30 minutes ago
        if (Utility.isNetworkAvailable(getActivity()) && System.currentTimeMillis()
                - PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getLong(getString(R.string.pref_last_user_data_update), 0) >= 3600000L) {

            //Start the task and listne for the end
            fetchTask = new FetchUserInfo(getActivity(), false);
            fetchTask.registerFetchUserInfoListener(this);
            fetchTask.execute();

            //Show the refreshing animation
            mLayout.setRefreshing(true);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRefresh() {
        if (Utility.isNetworkAvailable(getActivity())) {
            //Start fetching the data and listen for the end
            fetchTask = new FetchUserInfo(getActivity(), true);
            fetchTask.registerFetchUserInfoListener(this);
            fetchTask.execute();
        } else {
            //There is no internet connection, notify the user
            Toast.makeText(getActivity(), "bptf: no connection", Toast.LENGTH_SHORT).show();
            mLayout.setRefreshing(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        //Handle all the buttons here

        //Get the steam id, do nothing if there is no steam id
        String steamId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                getString(R.string.pref_resolved_steam_id), "");
        if (steamId != null && steamId.equals("")) {
            Toast.makeText(getActivity(), "bptf: no steamID provided", Toast.LENGTH_SHORT).show();
            return;
        }

        //All buttons (except the backpack one) open a link in the browser
        if (v.getId() != R.id.button_backpack) {

            String url;
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

            //Open link in the device default web browser
            Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            if (privateBackpack) {
                //The backpack is private, do nothing
                Toast.makeText(getActivity(), "Your backpack is private", Toast.LENGTH_SHORT).show();
            } else {
                //Else the user clicked on the backpack button. Start the backpack activity. Pass
                //the steamId and whether it's the user's backpack or not
                Intent i = new Intent(getActivity(), UserBackpackActivity.class);
                i.putExtra(UserBackpackActivity.EXTRA_NAME, playerName.getText());
                i.putExtra(UserBackpackActivity.EXTRA_GUEST, false);
                startActivity(i);
            }
        }
    }

    /**
     * Update all info on the user page.
     */
    public void updateUserPage() {
        //Get the default shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //Download avatar if needed.
        if (prefs.contains(getString(R.string.pref_new_avatar)) &&
                Utility.isNetworkAvailable(getActivity())) {
            //Start downloading the avatar in the background
            new AvatarDownLoader(PreferenceManager.getDefaultSharedPreferences(getActivity()).
                    getString(getString(R.string.pref_player_avatar_url), ""), getActivity()).
                    execute();
        }

        //Set the player name
        playerName.setText(prefs.getString(getString(R.string.pref_player_name), ""));

        if (prefs.getInt(getString(R.string.pref_player_banned), 0) == 1) {
            //Set player name to red and cross name out if banned
            playerName.setTextColor(0xffdd4c44);
            playerName.setPaintFlags(playerName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        //Set the player reputation. Reputation: X
        playerReputation.setText("Reputation: " +
                prefs.getInt(getString(R.string.pref_player_reputation), 0));

        //Set the 'user since' text
        long profileCreated = prefs.getLong(getString(R.string.pref_player_profile_created), -1L);
        if (profileCreated == -1) {
            userSinceText.setText("Steam user since:\nunknown");
        } else {
            userSinceText.setText("Steam user since:\n" +
                    Utility.formatUnixTimeStamp(profileCreated));
        }

        //Switch for the player's state
        switch (prefs.getInt(getString(R.string.pref_player_state), 0)) {
            case 0:
                long lastOnline = prefs.getLong(getString(R.string.pref_player_last_online), -1L);
                if (lastOnline == -1) {
                    //Weird
                    lastOnlineText.setText("Last online: unknown");
                } else {
                    //Player is offline, show how long was it since the player was last online
                    lastOnlineText.setText("Last online: " + Utility.formatLastOnlineTime(
                            System.currentTimeMillis() - lastOnline * 1000L));
                }
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_offline));
                break;
            case 1:
                lastOnlineText.setText("Online");
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 2:
                lastOnlineText.setText("Busy");
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 3:
                lastOnlineText.setText("Away");
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 4:
                lastOnlineText.setText("Snooze");
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 5:
                lastOnlineText.setText("Looking to trade");
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 6:
                lastOnlineText.setText("Looking to play");
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_online));
                break;
            case 7:
                lastOnlineText.setText("In-Game");
                lastOnlineText.setTextColor(getResources().getColor(R.color.player_in_game));
                break;
        }

        //Steamrep information
        switch (prefs.getInt(getString(R.string.pref_player_scammer), -1)) {
            case -1:
                steamRepStatus.setText("?");
                steamRepStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_neutral));
                break;
            case 0:
                steamRepStatus.setText("NORMAL");
                steamRepStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_neutral));
                break;
            case 1:
                steamRepStatus.setText("SCAMMER");
                steamRepStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_bad));
                break;
        }

        //Trade status
        switch (prefs.getInt(getString(R.string.pref_player_economy_banned), -1)) {
            case -1:
                tradeStatus.setText("?");
                tradeStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_neutral));
                break;
            case 0:
                tradeStatus.setText("OK");
                tradeStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_good));
                break;
            case 1:
                tradeStatus.setText("BAN");
                tradeStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_bad));
                break;
        }

        //VAC status
        switch (prefs.getInt(getString(R.string.pref_player_vac_banned), -1)) {
            case -1:
                vacStatus.setText("?");
                vacStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_neutral));
                break;
            case 0:
                vacStatus.setText("OK");
                vacStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_good));
                break;
            case 1:
                vacStatus.setText("BAN");
                vacStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_bad));
                break;
        }

        //Community status
        switch (prefs.getInt(getString(R.string.pref_player_community_banned), -1)) {
            case -1:
                communityStatus.setText("?");
                communityStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_neutral));
                break;
            case 0:
                communityStatus.setText("OK");
                communityStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_good));
                break;
            case 1:
                communityStatus.setText("BAN");
                communityStatus.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.status_background_bad));
                break;
        }

        //Backpack value
        double bpValue = Utility.getDouble(prefs,
                getString(R.string.pref_player_backpack_value_tf2), -1);
        if (bpValue == -1) {
            //Value is unknown (probably private)
            backpackValueRefined.setText("?");
            backpackValueUsd.setText("?");
        } else {
            //Properly format the backpack value (is int, does it have a fraction smaller than 0.01)
            if ((int) bpValue == bpValue)
                backpackValueRefined.setText("" + (int) bpValue);
            else if (("" + bpValue).substring(("" + bpValue).indexOf('.') + 1).length() > 2)
                backpackValueRefined.setText("" + new DecimalFormat("#0.00").format(bpValue));
            else
                backpackValueRefined.setText("" + bpValue);

            double bpValueUsd = bpValue * Utility.getDouble(prefs,
                    getString(R.string.pref_metal_raw_usd), 1);
            //Convert the value into USD and format it like above
            if ((int) bpValueUsd == bpValueUsd)
                backpackValueUsd.setText("" + (int) bpValueUsd);
            else if (("" + bpValueUsd).substring(("" + bpValueUsd).indexOf('.') + 1).length() > 2)
                backpackValueUsd.setText("" + new DecimalFormat("#0.00").format(bpValueUsd));
            else
                backpackValueUsd.setText("" + bpValueUsd);
        }

        //Set the border of the avatar according to the player's state
        switch (prefs.getInt(getString(R.string.pref_player_state), 0)) {
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
        int positiveScore = prefs.getInt(getString(R.string.pref_player_trust_positive), 0);
        int negativeScore = prefs.getInt(getString(R.string.pref_player_trust_negative), 0);
        trustStatus.setText("" + (positiveScore - negativeScore));
        if (negativeScore > positiveScore) {
            trustStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_bad));
        } else if (positiveScore > negativeScore && negativeScore == 0) {
            trustStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_good));
        } else if (positiveScore > negativeScore && negativeScore >= 0) {
            trustStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_caution));
        } else {
            trustStatus.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.status_background_neutral));
        }

        //Raw keys
        int rawKeys = prefs.getInt(getString(R.string.pref_user_raw_key), -1);
        if (rawKeys >= 0)
            backpackRawKeys.setText("" + rawKeys);
        else
            backpackRawKeys.setText("?");

        //Raw metal
        double rawMetal = Utility.roundDouble(Utility.getDouble(prefs,
                getString(R.string.pref_user_raw_metal), -1), 2);
        if (rawMetal >= 0)
            backpackRawMetal.setText("" + Utility.roundDouble(rawMetal, 2));
        else
            backpackRawMetal.setText("?");

        //Number of slots and slots used
        int itemNumber = prefs.getInt(getString(R.string.pref_user_items), -1);
        int backpackSlotNumber = prefs.getInt(getString(R.string.pref_user_slots), -1);
        if (itemNumber >= 0 && backpackSlotNumber >= 0)
            backpackSlots.setText("" + itemNumber + "/" + backpackSlotNumber);
        else
            backpackSlots.setText("?/?");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFetchFinished(boolean privateBackpack) {
        this.privateBackpack = privateBackpack;
        //Stop the refreshing animation and update the UI
        if (isAdded()) {
            updateUserPage();
            mLayout.setRefreshing(false);
        }
    }

    /**
     * Asynctask for downloading the avatar in the background.
     */
    private class AvatarDownLoader extends AsyncTask<Void, Void, Void> {

        //The url of the avatar
        private String url;

        //The context the task was launched in
        private Context mContext;

        //Bitmap to store the image
        private Bitmap bmp;

        //Drawable for the avatar
        private Drawable d;

        //Error message to be shown to the user
        private String errorMessage;

        /**
         * Constructor.
         *
         * @param url     url link for the avatar
         * @param context the context the task was launched in
         */
        private AvatarDownLoader(String url, Context context) {
            this.url = url;
            this.mContext = context;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPreExecute() {
            //Show the progressbar
            if (progressBar != null)
                progressBar.setVisibility(View.VISIBLE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Void doInBackground(Void... params) {

            //Check again if we really need to download the image
            if (PreferenceManager.getDefaultSharedPreferences(mContext).
                    getBoolean(mContext.getString(R.string.pref_new_avatar), false)) {

                //Get the image
                bmp = getBitmapFromURL(url);

                try {
                    //Save avatar as png into the private data folder
                    FileOutputStream fos = mContext.openFileOutput("avatar.png",
                            Context.MODE_PRIVATE);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (IOException e) {
                    //IO error, shouldn't reach
                    errorMessage = e.getMessage();
                    publishProgress();
                    if (Utility.isDebugging(mContext))
                        e.printStackTrace();
                }
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onProgressUpdate(Void... values) {
            //There was an error, notify the user
            Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }

        /**
         * Method to download the image.
         *
         * @param link the link to download the image from
         * @return the bitmap object containing the image
         */
        public Bitmap getBitmapFromURL(String link) {

            try {
                //Open connection
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();

                //Get the input stream
                InputStream input = connection.getInputStream();

                //Decode the image
                return BitmapFactory.decodeStream(input);

            } catch (IOException e) {
                //There was an error, notify the user
                errorMessage = e.getMessage();
                publishProgress();
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            if (isAdded()) {
                //Get the avatar from the private data folder and set it to the image view
                File path = mContext.getFilesDir();
                d = Drawable.createFromPath(path.toString() + "/avatar.png");
                avatar.setImageDrawable(d);

                //Remove the progressbar
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);

                //Save to preferences that we don't need to download the avatar again.
                PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(
                        mContext.getString(R.string.pref_new_avatar), false).apply();
            }
        }
    }
}