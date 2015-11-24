package com.tlongdev.bktf.fragment;

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
import com.tlongdev.bktf.activity.MainActivity;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.activity.UserBackpackActivity;
import com.tlongdev.bktf.network.GetUserBackpack;
import com.tlongdev.bktf.network.GetUserInfo;
import com.tlongdev.bktf.util.Profile;
import com.tlongdev.bktf.util.Utility;

/**
 * Fragment for displaying the user profile.
 */
public class UserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener, GetUserInfo.OnUserInfoListener, MainActivity.OnDrawerOpenedListener,
        AppBarLayout.OnOffsetChangedListener, GetUserBackpack.OnUserBackpackListener {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = UserFragment.class.getSimpleName();

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    //Reference too all the views that need to be updated
    private TextView playerReputation;
    private TextView trustPositive;
    private TextView trustNegative;
    private ImageView steamRepStatus;
    private ImageView vacStatus;
    private ImageView tradeStatus;
    private ImageView communityStatus;
    private TextView backpackValueRefined;
    private TextView backpackRawMetal;
    private TextView backpackRawKeys;
    private TextView backpackValueUsd;
    private TextView backpackSlots;
    private TextView userSinceText;
    private TextView lastOnlineText;
    private ImageView avatar;

    //Swipe refresh layout for refreshing the user data manually
    private SwipeRefreshLayout mSwipeRefreshLayout;

    //Stores whether the backpack is private or not
    private boolean privateBackpack = false;

    /**
     * Only needed for manually expanding the toolbar
     */
    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;

    /**
     * the collapsing toolbar layout
     */
    private CollapsingToolbarLayout mCollapsingToolbarLayout;

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

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        //Views used for toolbar behavior
        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar_layout);
        mAppBarLayout.addOnOffsetChangedListener(this);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);

        //Find all the views
        playerReputation = (TextView) rootView.findViewById(R.id.text_view_player_reputation);
        trustPositive = (TextView) rootView.findViewById(R.id.trust_positive);
        trustNegative = (TextView) rootView.findViewById(R.id.trust_negative);
        steamRepStatus = (ImageView) rootView.findViewById(R.id.steamrep_status);
        vacStatus = (ImageView) rootView.findViewById(R.id.vac_status);
        tradeStatus = (ImageView) rootView.findViewById(R.id.trade_status);
        communityStatus = (ImageView) rootView.findViewById(R.id.community_status);
        backpackValueRefined = (TextView) rootView.findViewById(R.id.text_view_bp_refined);
        backpackRawMetal = (TextView) rootView.findViewById(R.id.text_view_bp_raw_metal);
        backpackRawKeys = (TextView) rootView.findViewById(R.id.text_view_bp_raw_keys);
        backpackValueUsd = (TextView) rootView.findViewById(R.id.text_view_bp_usd);
        backpackSlots = (TextView) rootView.findViewById(R.id.text_view_bp_slots);
        userSinceText = (TextView) rootView.findViewById(R.id.text_view_user_since);
        lastOnlineText = (TextView) rootView.findViewById(R.id.text_view_user_last_online);
        avatar = (ImageView) rootView.findViewById(R.id.avatar);

        //Set the color of the refreshing animation
        mSwipeRefreshLayout.setColorSchemeColors(Utility.getColor(getActivity(), R.color.accent));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //Set the on click listeners for the buttons
        rootView.findViewById(R.id.button_bazaar_tf).setOnClickListener(this);
        rootView.findViewById(R.id.button_steamrep).setOnClickListener(this);
        rootView.findViewById(R.id.button_tf2op).setOnClickListener(this);
        rootView.findViewById(R.id.button_tf2tp).setOnClickListener(this);
        rootView.findViewById(R.id.button_steam_community).setOnClickListener(this);
        rootView.findViewById(R.id.backpack).setOnClickListener(this);

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
        if (Utility.isNetworkAvailable(getActivity()) && System.currentTimeMillis()
                - PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getLong(getString(R.string.pref_last_user_data_update), 0) >= 3600000L) {

            //Start the task and listne for the end
            GetUserInfo task = new GetUserInfo(getActivity(), false);
            task.registerFetchUserInfoListener(this);
            task.execute(Profile.getSteamId(getActivity()), Profile.getResolvedSteamId(getActivity()));

            //Workaround for the circle not appearing
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });

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
        if (Utility.isNetworkAvailable(getActivity())) {
            //Start fetching the data and listen for the end
            GetUserInfo fetchTask = new GetUserInfo(getActivity(), true);
            fetchTask.registerFetchUserInfoListener(this);
            fetchTask.execute(Profile.getSteamId(getActivity()), Profile.getResolvedSteamId(getActivity()));

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("UserData")
                    .build());
        } else {
            //There is no internet connection, notify the user
            Toast.makeText(getActivity(), "bptf: " + getString(R.string.error_no_network),
                    Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
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
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
        }
        return true;
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
        if (steamId.equals("")) {
            Toast.makeText(getActivity(), "bptf: " + getString(R.string.error_no_steam_id),
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
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            if (privateBackpack) {
                //The backpack is private, do nothing
                Toast.makeText(getActivity(), getString(R.string.message_private_backpack_own),
                        Toast.LENGTH_SHORT).show();
            } else {
                //Else the user clicked on the backpack button. Start the backpack activity. Pass
                //the steamId and whether it's the user's backpack or not
                Intent i = new Intent(getActivity(), UserBackpackActivity.class);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //Download avatar if needed.
        if (prefs.contains(getString(R.string.pref_new_avatar)) &&
                Utility.isNetworkAvailable(getActivity())) {
            Glide.with(this)
                    .load(PreferenceManager.getDefaultSharedPreferences(getActivity()).
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
                    lastOnlineText.setText(String.format("%s %s", getString(R.string.user_page_last_online), Utility.formatLastOnlineTime(getActivity(),
                            System.currentTimeMillis() - lastOnline * 1000L)));
                }
                lastOnlineText.setTextColor(Utility.getColor(getActivity(), R.color.text_primary));
                break;
            case 1:
                lastOnlineText.setText(getString(R.string.user_page_status_online));
                lastOnlineText.setTextColor(Utility.getColor(getActivity(), R.color.player_online));
                break;
            case 2:
                lastOnlineText.setText(getString(R.string.user_page_status_busy));
                lastOnlineText.setTextColor(Utility.getColor(getActivity(), R.color.player_online));
                break;
            case 3:
                lastOnlineText.setText(getString(R.string.user_page_status_away));
                lastOnlineText.setTextColor(Utility.getColor(getActivity(), R.color.player_online));
                break;
            case 4:
                lastOnlineText.setText(getString(R.string.user_page_status_snooze));
                lastOnlineText.setTextColor(Utility.getColor(getActivity(), R.color.player_online));
                break;
            case 5:
                lastOnlineText.setText(getString(R.string.user_page_status_trade));
                lastOnlineText.setTextColor(Utility.getColor(getActivity(), R.color.player_online));
                break;
            case 6:
                lastOnlineText.setText(getString(R.string.user_page_status_play));
                lastOnlineText.setTextColor(Utility.getColor(getActivity(), R.color.player_online));
                break;
            case 7:
                lastOnlineText.setText(getString(R.string.user_page_status_in_game));
                lastOnlineText.setTextColor(Utility.getColor(getActivity(), R.color.player_in_game));
                break;
        }

        //Load drawables for palyer statuses
        Drawable statusUnknown = getResources().getDrawable(R.drawable.ic_help_outline_white_48dp);
        Drawable statusOk = getResources().getDrawable(R.drawable.ic_done_white_48dp);
        Drawable statusBad = getResources().getDrawable(R.drawable.ic_close_white_48dp);
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
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .putLong(getActivity().getString(R.string.pref_last_user_data_update),
                        System.currentTimeMillis()).apply();

        GetUserBackpack task = new GetUserBackpack(getActivity());
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
        }

        Toast.makeText(getActivity(), "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        //Disable the swipe refresh layout when the toolbar is not fully visible
        if (i == 0) {
            mSwipeRefreshLayout.setEnabled(true);
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
    }

    @Override
    public void onUserBackpackFinished(int rawKeys, double rawMetal, int backpackSlots, int itemNumber) {

        Context context = getActivity();

        //Start editing the preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        //Save all the data passed
        editor.putInt(context.getString(R.string.pref_user_slots), backpackSlots);
        editor.putInt(context.getString(R.string.pref_user_items), itemNumber);
        editor.putInt(context.getString(R.string.pref_user_raw_key), rawKeys);
        Utility.putDouble(editor, context.getString(R.string.pref_user_raw_metal), rawMetal);
        editor.apply();

        privateBackpack = false;
        //Stop the refreshing animation and update the UI
        if (isAdded()) {
            updateUserPage();
            mSwipeRefreshLayout.setRefreshing(false);
        }

        ((MainActivity) getActivity()).updateDrawer();
    }

    @Override
    public void onPrivateBackpack() {

        Context context = getActivity();

        //Start editing the preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        //Save all data that represent a private backpack
        editor.putInt(context.getString(R.string.pref_user_slots), -1);
        editor.putInt(context.getString(R.string.pref_user_items), -1);
        editor.putInt(context.getString(R.string.pref_user_raw_key), -1);
        Utility.putDouble(editor, context.getString(R.string.pref_user_raw_metal), -1);
        Utility.putDouble(editor, context.getString(R.string.pref_player_backpack_value_tf2), -1);
        editor.apply();

        privateBackpack = true;
        //Stop the refreshing animation and update the UI
        if (isAdded()) {
            updateUserPage();
            mSwipeRefreshLayout.setRefreshing(false);
        }

        ((MainActivity) getActivity()).updateDrawer();
    }

    @Override
    public void onUserBackpackFailed(String errorMessage) {
        //Stop the refreshing animation and update the UI
        if (isAdded()) {
            updateUserPage();
            mSwipeRefreshLayout.setRefreshing(false);
        }

        Toast.makeText(getActivity(), "failed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

}