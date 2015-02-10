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
public class UserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener,
        FetchUserInfo.OnFetchUserInfoListener {

    private TextView playerName;
    private TextView playerReputation;
    private TextView trustStatus;
    private TextView steamRepStatus;
    private TextView vacStatus;
    private TextView tradeStatus;
    private TextView communityStatus;
    private TextView backpackValueRefined;
    private TextView backpackRawMetal; //TODO fix raw items after implementing backpack viewer
    private TextView backpackRawKeys;
    private TextView backpackValueUsd;
    private TextView backpackSlots;
    private TextView userSinceText;
    private TextView lastOnlineText;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ImageView avatar;
    private ProgressBar progressBar;

    private FetchUserInfo fetchTask;

    public UserFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mSwipeRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_user, container, false);

        playerName = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_player_name);
        playerReputation = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_player_reputation);
        trustStatus = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_trust_status);
        steamRepStatus = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_steamrep_status);
        vacStatus = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_vac_status);
        tradeStatus = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_trade_status);
        communityStatus = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_community_status);
        backpackValueRefined = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_bp_refined);
        backpackRawMetal = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_bp_raw_metal);
        backpackRawKeys = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_bp_raw_keys);
        backpackValueUsd = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_bp_usd);
        backpackSlots = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_bp_slots);
        userSinceText = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_user_since);
        lastOnlineText = (TextView) mSwipeRefreshLayout.findViewById(R.id.text_view_user_last_online);

        //Set the color of the refreshing animation
        mSwipeRefreshLayout.setColorSchemeColors(0xff5787c5);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //A workaround for the refreshing animation not appearing when calling setRefreshing(true)
        mSwipeRefreshLayout.setProgressViewOffset(false,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -55, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics()));

        mSwipeRefreshLayout.findViewById(R.id.button_bazaar_tf).setOnClickListener(this);
        mSwipeRefreshLayout.findViewById(R.id.button_steamrep).setOnClickListener(this);
        mSwipeRefreshLayout.findViewById(R.id.button_tf2op).setOnClickListener(this);
        mSwipeRefreshLayout.findViewById(R.id.button_tf2tp).setOnClickListener(this);
        mSwipeRefreshLayout.findViewById(R.id.button_steam_community).setOnClickListener(this);
        mSwipeRefreshLayout.findViewById(R.id.button_backpack).setOnClickListener(this);

        avatar = (ImageView)mSwipeRefreshLayout.findViewById(R.id.player_avatar);

        progressBar = (ProgressBar)mSwipeRefreshLayout.findViewById(R.id.progress_bar);

        updateUserPage();

        return mSwipeRefreshLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Update user info if needed
        if (Utility.isNetworkAvailable(getActivity()) &&
                System.currentTimeMillis() - PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getLong(getString(R.string.pref_last_user_data_update), 0) >= 3600000L) {

            fetchTask = new FetchUserInfo(getActivity(), false);
            fetchTask.registerFetchUserInfoListener(this);
            fetchTask.execute();

            mSwipeRefreshLayout.setRefreshing(true);
        }

    }

    @Override
    public void onRefresh() {
        if (Utility.isNetworkAvailable(getActivity())) {
            fetchTask = new FetchUserInfo(getActivity(), true);
            fetchTask.registerFetchUserInfoListener(this);
            fetchTask.execute();
        } else {
            Toast.makeText(getActivity(), "bptf: no connection", Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onClick(View v) {
        //Handle all the buttons here
        String steamId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                getString(R.string.pref_resolved_steam_id), "");
        if (steamId.equals("")){
            Toast.makeText(getActivity(), "bptf: no steamID provided", Toast.LENGTH_SHORT).show();
            return;
        }
        if (v.getId() != R.id.button_backpack) {

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
                default:
                    return;
            }

            Uri webPage = Uri.parse(url + steamId);

            //Open link in the device default web browser
            Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            Intent i = new Intent(getActivity(), UserBackpackActivity.class);
            i.putExtra(UserBackpackActivity.EXTRA_NAME, playerName.getText());
            i.putExtra(UserBackpackActivity.EXTRA_GUEST, false);
            startActivity(i);
        }
    }

    /**
     * Update all info on the user page
     */
    public void updateUserPage() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //Download avatar if needed.
        if (prefs.contains(getString(R.string.pref_new_avatar)) && Utility.isNetworkAvailable(getActivity())) {
            new AvatarDownLoader(PreferenceManager.getDefaultSharedPreferences(getActivity()).
                    getString(getString(R.string.pref_player_avatar_url), ""), getActivity()).execute();
        }

        playerName.setText(prefs.getString(getString(R.string.pref_player_name), ""));
        if (prefs.getInt(getString(R.string.pref_player_banned), 0) == 1){
            //Set player name to red and cross name out if banned
            playerName.setTextColor(0xffdd4c44);
            playerName.setPaintFlags(playerName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        playerReputation.setText("Reputation: " + prefs.getInt(getString(R.string.pref_player_reputation), 0));

        long profileCreated = prefs.getLong(getString(R.string.pref_player_profile_created), -1L);
        if (profileCreated == -1){
            userSinceText.setText("Steam user since:\nunknown");
        } else {
            userSinceText.setText("Steam user since:\n" + Utility.formatUnixTimeStamp(profileCreated));
        }

        switch (prefs.getInt(getString(R.string.pref_player_state), 0)) {
            case 0:
                long lastOnline = prefs.getLong(getString(R.string.pref_player_last_online), -1L);
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

        switch (prefs.getInt(getString(R.string.pref_player_scammer), -1)) {
            case -1:
                steamRepStatus.setText("?");
                steamRepStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_neutral));
                break;
            case 0:
                steamRepStatus.setText("NORMAL");
                steamRepStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_neutral));
                break;
            case 1:
                steamRepStatus.setText("SCAMMER");
                steamRepStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_bad));
                break;
        }

        switch (prefs.getInt(getString(R.string.pref_player_economy_banned), -1)) {
            case -1:
                tradeStatus.setText("?");
                tradeStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_neutral));
                break;
            case 0:
                tradeStatus.setText("OK");
                tradeStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_good));
                break;
            case 1:
                tradeStatus.setText("BAN");
                tradeStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_bad));
                break;
        }

        switch (prefs.getInt(getString(R.string.pref_player_vac_banned), -1)) {
            case -1:
                vacStatus.setText("?");
                vacStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_neutral));
                break;
            case 0:
                vacStatus.setText("OK");
                vacStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_good));
                break;
            case 1:
                vacStatus.setText("BAN");
                vacStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_bad));
                break;
        }

        switch (prefs.getInt(getString(R.string.pref_player_community_banned), -1)) {
            case -1:
                communityStatus.setText("?");
                communityStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_neutral));
                break;
            case 0:
                communityStatus.setText("OK");
                communityStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_good));
                break;
            case 1:
                communityStatus.setText("BAN");
                communityStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_bad));
                break;
        }

        double bpValue = Utility.getDouble(prefs, getString(R.string.pref_player_backpack_value_tf2), -1);
        if (bpValue == -1) {
            backpackValueRefined.setText("?");
            backpackValueUsd.setText("?");
        } else {
            if ((int) bpValue == bpValue)
                backpackValueRefined.setText("" + (int) bpValue);
            else if (("" + bpValue).substring(("" + bpValue).indexOf('.') + 1).length() > 2)
                backpackValueRefined.setText("" + new DecimalFormat("#0.00").format(bpValue));
            else
                backpackValueRefined.setText("" + bpValue);

            double bpValueUsd = bpValue * Utility.getDouble(prefs, getString(R.string.pref_metal_raw_usd), 1);
            if ((int) bpValueUsd == bpValueUsd)
                backpackValueUsd.setText("" + (int) bpValueUsd);
            else if (("" + bpValueUsd).substring(("" + bpValueUsd).indexOf('.') + 1).length() > 2)
                backpackValueUsd.setText("" + new DecimalFormat("#0.00").format(bpValueUsd));
            else
                backpackValueUsd.setText("" + bpValueUsd);
        }

        switch (prefs.getInt(getString(R.string.pref_player_state), 0)){
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

        int positiveScore = prefs.getInt(getString(R.string.pref_player_trust_positive), 0);
        int negativeScore = prefs.getInt(getString(R.string.pref_player_trust_negative), 0);
        trustStatus.setText("" + (positiveScore - negativeScore));
        if (negativeScore > positiveScore){
            trustStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_bad));
        } else if (positiveScore > negativeScore && negativeScore == 0){
            trustStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_good));
        } else if (positiveScore > negativeScore && negativeScore >= 0){
            trustStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_caution));
        } else {
            trustStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_background_neutral));
        }

        backpackRawKeys.setText("" + prefs.getInt(getString(R.string.pref_user_raw_key), 0));
        backpackRawMetal.setText("" + Utility.roundDouble(Utility.getDouble(prefs, getString(R.string.pref_user_raw_metal), 0.0), 2));

        backpackSlots.setText("" + prefs.getInt(getString(R.string.pref_user_items), 0) + "/" +
                prefs.getInt(getString(R.string.pref_user_slots), 0));
    }

    @Override
    public void onFetchFinished() {
        //Stop the refreshing animation and update the UI
        if(isAdded()){
            updateUserPage();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Asynctask for downloading the avatar in the background.
     */
    private class AvatarDownLoader extends AsyncTask<Void, Void, Void>{

        private String url;
        private Context mContext;
        private Bitmap bmp;
        private Drawable d;
        private String errorMessage;

        private AvatarDownLoader(String url, Context mContext) {
            this.url = url;
            this.mContext = mContext;
        }

        @Override
        protected void onPreExecute() {
            if (progressBar != null)
                progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (PreferenceManager.getDefaultSharedPreferences(mContext).
                    getBoolean(mContext.getString(R.string.pref_new_avatar), false)) {
                bmp = getBitmapFromURL(url);

                try {

                    //Save avatar as png into the private data folder
                    FileOutputStream fos = mContext.openFileOutput("avatar.png", Context.MODE_PRIVATE);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();

                } catch (IOException e) {
                    errorMessage = e.getMessage();
                    publishProgress();
                    if (Utility.isDebugging(mContext))
                        e.printStackTrace();
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }

        /**
         * Method to download the image
         */
        public Bitmap getBitmapFromURL(String link) {

            try {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                return BitmapFactory.decodeStream(input);

            } catch (IOException e) {
                errorMessage = e.getMessage();
                publishProgress();
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isAdded()){
                File path = mContext.getFilesDir();

                d = Drawable.createFromPath(path.toString() + "/avatar.png");
                avatar.setImageDrawable(d);

                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);

                //Save to preferences that we don't need to download the avatar again.
                PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(
                        mContext.getString(R.string.pref_new_avatar), false).apply();
            }
        }
    }
}