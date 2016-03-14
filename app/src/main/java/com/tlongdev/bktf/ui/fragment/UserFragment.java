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

package com.tlongdev.bktf.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.interactor.GetUserDataInteractor;
import com.tlongdev.bktf.network.GetUserBackpack;
import com.tlongdev.bktf.ui.activity.MainActivity;
import com.tlongdev.bktf.ui.activity.SearchActivity;
import com.tlongdev.bktf.ui.activity.UserBackpackActivity;
import com.tlongdev.bktf.util.Profile;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment for displaying the user profile.
 */
public class UserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener, GetUserDataInteractor.OnUserInfoListener, MainActivity.OnDrawerOpenedListener,
        GetUserBackpack.OnUserBackpackListener {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = UserFragment.class.getSimpleName();

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    private Context mContext;

    //Reference too all the views that need to be updated
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

    //Swipe refresh layout for refreshing the user data manually
    @Bind(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    //Stores whether the backpack is private or not
    private boolean privateBackpack = false;

    /**
     * Only needed for manually expanding the toolbar
     */
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;

    /**
     * the collapsing toolbar layout
     */
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;

    private boolean layoutRefreshing = false;

    /**
     * Constructor
     */
    public UserFragment() {
        //Required empty constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) (getActivity()).getApplication();
        mTracker = application.getDefaultTracker();

        View rootView = inflater.inflate(R.layout.fragment_user, container, false);

        ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) mContext).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        //Set the color of the refreshing animation
        mSwipeRefreshLayout.setColorSchemeColors(Utility.getColor(mContext, R.color.accent));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(layoutRefreshing);
            }
        });


        //Update all the views to show te user data
        updateUserPage();

        return rootView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("User Profile");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        //Update user info if last update was more than 30 minutes ago
        if (Utility.isNetworkAvailable(mContext) && System.currentTimeMillis()
                - PreferenceManager.getDefaultSharedPreferences(mContext)
                .getLong(getString(R.string.pref_last_user_data_update), 0) >= 3600000L) {

            //Start the task and listne for the end
            GetUserDataInteractor task = new GetUserDataInteractor(mContext,(BptfApplication) getActivity().getApplication(), false);
            task.registerFetchUserInfoListener(this);
            task.execute(Profile.getSteamId(mContext), Profile.getResolvedSteamId(mContext));

            //Workaround for the circle not appearing
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            layoutRefreshing = true;

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("UserData")
                    .build());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRefresh() {
        layoutRefreshing = true;
        if (Utility.isNetworkAvailable(mContext)) {
            //Start fetching the data and listen for the end
            GetUserDataInteractor fetchTask = new GetUserDataInteractor(mContext, (BptfApplication) getActivity().getApplication(), true);
            fetchTask.registerFetchUserInfoListener(this);
            fetchTask.execute(Profile.getSteamId(mContext), Profile.getResolvedSteamId(mContext));

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("UserData")
                    .build());
        } else {
            //There is no internet connection, notify the user
            Toast.makeText(mContext, "bptf: " + getString(R.string.error_no_network),
                    Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
            layoutRefreshing = false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_search:
                //Start the search activity
                startActivity(new Intent(mContext, SearchActivity.class));
                break;
        }
        return true;
    }

    @OnClick({R.id.button_bazaar_tf, R.id.button_steamrep, R.id.button_tf2op, R.id.button_tf2tp,
            R.id.button_steam_community, R.id.backpack})
    public void onClick(View v) {
        //Handle all the buttons here

        //Get the steam id, do nothing if there is no steam id
        String steamId = PreferenceManager.getDefaultSharedPreferences(mContext).getString(
                getString(R.string.pref_resolved_steam_id), "");
        if (steamId.equals("")) {
            Toast.makeText(mContext, "bptf: " + getString(R.string.error_no_steam_id),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //All buttons (except the backpack one) open a link in the browser
        if (v.getId() != R.id.backpack) {

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
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            if (privateBackpack) {
                //The backpack is private, do nothing
                Toast.makeText(mContext, getString(R.string.message_private_backpack_own),
                        Toast.LENGTH_SHORT).show();
            } else {
                //Else the user clicked on the backpack button. Start the backpack activity. Pass
                //the steamId and whether it's the user's backpack or not
                Intent i = new Intent(mContext, UserBackpackActivity.class);
                i.putExtra(UserBackpackActivity.EXTRA_NAME, mCollapsingToolbarLayout.getTitle());
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        //Download avatar if needed.
        if (prefs.contains(getString(R.string.pref_new_avatar)) &&
                Utility.isNetworkAvailable(mContext)) {
            Glide.with(this)
                    .load(PreferenceManager.getDefaultSharedPreferences(mContext).
                            getString(getString(R.string.pref_player_avatar_url), ""))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(avatar);

        }

        //Set the player name
        String name = prefs.getString(getString(R.string.pref_player_name), "");
        mCollapsingToolbarLayout.setTitle(name);

        if (prefs.getInt(getString(R.string.pref_player_banned), 0) == 1) {
            //Set player name to red and cross name out if banned
            // TODO: 2015. 10. 22.
        }

        //Set the player reputation.
        playerReputation.setText(String.valueOf(prefs.getInt(getString(R.string.pref_player_reputation), 0)));

        //Set the 'user since' text
        long profileCreated = prefs.getLong(getString(R.string.pref_player_profile_created), -1L);
        if (profileCreated == -1) {
            userSinceText.setText(getString(R.string.filler_unknown));
        } else {
            userSinceText.setText(Utility.formatUnixTimeStamp(profileCreated));
        }

        //Switch for the player's state
        switch (prefs.getInt(getString(R.string.pref_player_state), 0)) {
            case 0:
                long lastOnline = prefs.getLong(getString(R.string.pref_player_last_online), -1L);
                if (lastOnline == -1) {
                    //Weird
                    lastOnlineText.setText(String.format("%s %s", getString(R.string.user_page_last_online), getString(R.string.filler_unknown)));
                } else {
                    //Player is offline, show how long was it since the player was last online
                    lastOnlineText.setText(String.format("%s %s", getString(R.string.user_page_last_online), Utility.formatLastOnlineTime(mContext,
                            System.currentTimeMillis() - lastOnline * 1000L)));
                }
                lastOnlineText.setTextColor(Utility.getColor(mContext, R.color.text_primary));
                break;
            case 1:
                lastOnlineText.setText(getString(R.string.user_page_status_online));
                lastOnlineText.setTextColor(Utility.getColor(mContext, R.color.player_online));
                break;
            case 2:
                lastOnlineText.setText(getString(R.string.user_page_status_busy));
                lastOnlineText.setTextColor(Utility.getColor(mContext, R.color.player_online));
                break;
            case 3:
                lastOnlineText.setText(getString(R.string.user_page_status_away));
                lastOnlineText.setTextColor(Utility.getColor(mContext, R.color.player_online));
                break;
            case 4:
                lastOnlineText.setText(getString(R.string.user_page_status_snooze));
                lastOnlineText.setTextColor(Utility.getColor(mContext, R.color.player_online));
                break;
            case 5:
                lastOnlineText.setText(getString(R.string.user_page_status_trade));
                lastOnlineText.setTextColor(Utility.getColor(mContext, R.color.player_online));
                break;
            case 6:
                lastOnlineText.setText(getString(R.string.user_page_status_play));
                lastOnlineText.setTextColor(Utility.getColor(mContext, R.color.player_online));
                break;
            case 7:
                lastOnlineText.setText(getString(R.string.user_page_status_in_game));
                lastOnlineText.setTextColor(Utility.getColor(mContext, R.color.player_in_game));
                break;
        }

        //Load drawables for palyer statuses
        Drawable statusUnknown = getResources().getDrawable(R.drawable.ic_help_white);
        Drawable statusOk = getResources().getDrawable(R.drawable.ic_done_white);
        Drawable statusBad = getResources().getDrawable(R.drawable.ic_close_white);
        if (statusOk != null) statusOk.setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
        if (statusBad != null) statusBad.setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

        //Steamrep information
        switch (prefs.getInt(getString(R.string.pref_player_scammer), -1)) {
            case -1:
                steamRepStatus.setImageDrawable(statusUnknown);
                break;
            case 0:
                steamRepStatus.setImageDrawable(statusOk);
                break;
            case 1:
                steamRepStatus.setImageDrawable(statusBad);
                break;
        }

        //Trade status
        switch (prefs.getInt(getString(R.string.pref_player_economy_banned), -1)) {
            case -1:
                tradeStatus.setImageDrawable(statusUnknown);
                break;
            case 0:
                tradeStatus.setImageDrawable(statusOk);
                break;
            case 1:
                tradeStatus.setImageDrawable(statusBad);
                break;
        }

        //VAC status
        switch (prefs.getInt(getString(R.string.pref_player_vac_banned), -1)) {
            case -1:
                vacStatus.setImageDrawable(statusUnknown);
                break;
            case 0:
                vacStatus.setImageDrawable(statusOk);
                break;
            case 1:
                vacStatus.setImageDrawable(statusBad);
                break;
        }

        //Community status
        switch (prefs.getInt(getString(R.string.pref_player_community_banned), -1)) {
            case -1:
                communityStatus.setImageDrawable(statusUnknown);
                break;
            case 0:
                communityStatus.setImageDrawable(statusOk);
                break;
            case 1:
                communityStatus.setImageDrawable(statusBad);
                break;
        }

        //Backpack value
        double bpValue = Utility.getDouble(prefs,
                getString(R.string.pref_player_backpack_value_tf2), -1);
        if (bpValue == -1) {
            //Value is unknown (probably private)
            backpackValueRefined.setText("?");
            backpackValueUsd.setText("?");
        } else if (!privateBackpack) {
            //Properly format the backpack value (is int, does it have a fraction smaller than 0.01)
            backpackValueRefined.setText(String.valueOf(Math.round(bpValue)));

            //Convert the value into USD and format it like above
            double bpValueUsd = bpValue * Utility.getDouble(prefs, getString(R.string.pref_metal_raw_usd), 1);
            backpackValueUsd.setText(String.valueOf(Math.round(bpValueUsd)));
        }

        //Set the trust score and color the background according to it.
        trustPositive.setText(String.format("+%d", prefs.getInt(getString(R.string.pref_player_trust_positive), 0)));
        trustNegative.setText(String.format("-%d", prefs.getInt(getString(R.string.pref_player_trust_negative), 0)));

        //Raw keys
        int rawKeys = prefs.getInt(getString(R.string.pref_user_raw_key), -1);
        if (rawKeys >= 0)
            backpackRawKeys.setText(String.valueOf(rawKeys));
        else
            backpackRawKeys.setText("?");

        //Raw metal
        double rawMetal = Utility.getDouble(prefs, getString(R.string.pref_user_raw_metal), -1);
        if (rawMetal >= 0)
            backpackRawMetal.setText(String.valueOf(Utility.formatDouble(rawMetal)));
        else
            backpackRawMetal.setText("?");

        //Number of slots and slots used
        int itemNumber = prefs.getInt(getString(R.string.pref_user_items), -1);
        int backpackSlotNumber = prefs.getInt(getString(R.string.pref_user_slots), -1);
        if (itemNumber >= 0 && backpackSlotNumber >= 0)
            backpackSlots.setText(String.format("%s/%d", String.valueOf(itemNumber), backpackSlotNumber));
        else
            backpackSlots.setText("?/?");
    }

    @Override
    public void onUserInfoFinished(String steamId) {

        //Save the update time
        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putLong(mContext.getString(R.string.pref_last_user_data_update),
                        System.currentTimeMillis()).apply();

        GetUserBackpack task = new GetUserBackpack(mContext);
        task.registerOnFetchUserBackpackListener(this);
        task.execute(steamId);

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Request")
                .setAction("UserBackpack")
                .build());
    }

    @Override
    public void onUserInfoFailed(String errorMessage) {
        //Stop the refreshing animation and update the UI
        if (isAdded()) {
            updateUserPage();
            mSwipeRefreshLayout.setRefreshing(false);
            layoutRefreshing = false;
        }

        Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
    }

    @Override
    public void onUserBackpackFinished(int rawKeys, double rawMetal, int backpackSlots, int itemNumber) {

        //Start editing the preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();

        //Save all the data passed
        editor.putInt(mContext.getString(R.string.pref_user_slots), backpackSlots);
        editor.putInt(mContext.getString(R.string.pref_user_items), itemNumber);
        editor.putInt(mContext.getString(R.string.pref_user_raw_key), rawKeys);
        Utility.putDouble(editor, mContext.getString(R.string.pref_user_raw_metal), rawMetal);
        editor.apply();

        privateBackpack = false;
        //Stop the refreshing animation and update the UI
        if (isAdded()) {
            updateUserPage();
            mSwipeRefreshLayout.setRefreshing(false);
            layoutRefreshing = false;
        }

        ((MainActivity) mContext).updateDrawer();
    }

    @Override
    public void onPrivateBackpack() {

        //Start editing the preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();

        //Save all data that represent a private backpack
        editor.putInt(mContext.getString(R.string.pref_user_slots), -1);
        editor.putInt(mContext.getString(R.string.pref_user_items), -1);
        editor.putInt(mContext.getString(R.string.pref_user_raw_key), -1);
        Utility.putDouble(editor, mContext.getString(R.string.pref_user_raw_metal), -1);
        Utility.putDouble(editor, mContext.getString(R.string.pref_player_backpack_value_tf2), -1);
        editor.apply();

        privateBackpack = true;
        //Stop the refreshing animation and update the UI
        if (isAdded()) {
            updateUserPage();
            mSwipeRefreshLayout.setRefreshing(false);
            layoutRefreshing = false;
        }

        ((MainActivity) mContext).updateDrawer();
    }

    @Override
    public void onUserBackpackFailed(String errorMessage) {
        //Stop the refreshing animation and update the UI
        if (isAdded()) {
            updateUserPage();
            mSwipeRefreshLayout.setRefreshing(false);
            layoutRefreshing = false;
        }

        Toast.makeText(mContext, "failed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

}