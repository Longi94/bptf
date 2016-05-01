/**
 * Copyright 2016 Long Tran
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

package com.tlongdev.bktf.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.customtabs.CustomTabActivityHelper;
import com.tlongdev.bktf.customtabs.WebViewFallback;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.presenter.fragment.UserPresenter;
import com.tlongdev.bktf.ui.activity.MainActivity;
import com.tlongdev.bktf.ui.activity.SearchActivity;
import com.tlongdev.bktf.ui.activity.UserBackpackActivity;
import com.tlongdev.bktf.ui.view.fragment.UserView;
import com.tlongdev.bktf.util.ProfileManager;
import com.tlongdev.bktf.util.Utility;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment for displaying the user profile.
 */
public class UserFragment extends BptfFragment implements UserView, View.OnClickListener, MainActivity.OnDrawerOpenedListener {

    private static final String USER_PARAM = "user_param";

    @Inject UserPresenter mPresenter;
    @Inject SharedPreferences mPrefs;
    @Inject ProfileManager mProfileManager;
    @Inject Context mContext;

    @BindView(R.id.text_view_player_reputation) TextView playerReputation;
    @BindView(R.id.trust_positive) TextView trustPositive;
    @BindView(R.id.trust_negative) TextView trustNegative;
    @BindView(R.id.steamrep_status) ImageView steamRepStatus;
    @BindView(R.id.vac_status) ImageView vacStatus;
    @BindView(R.id.trade_status) ImageView tradeStatus;
    @BindView(R.id.community_status) ImageView communityStatus;
    @BindView(R.id.text_view_bp_refined) TextView backpackValueRefined;
    @BindView(R.id.text_view_bp_raw_metal) TextView backpackRawMetal;
    @BindView(R.id.text_view_bp_raw_keys) TextView backpackRawKeys;
    @BindView(R.id.text_view_bp_usd) TextView backpackValueUsd;
    @BindView(R.id.text_view_bp_slots) TextView backpackSlots;
    @BindView(R.id.text_view_user_since) TextView userSinceText;
    @BindView(R.id.text_view_user_last_online) TextView lastOnlineText;
    @BindView(R.id.avatar) ImageView avatar;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.ad_view) AdView mAdView;
    @BindView(R.id.private_text) TextView mPrivateBackpackText;
    @BindView(R.id.backpack) ImageView mBackpackButton;
    @BindView(R.id.backpack_content) View mBackpackContent;

    //Stores whether the backpack is private or not
    private boolean privateBackpack = false;

    private User mUser;
    private boolean searchedUser;

    private Unbinder mUnbinder;

    /**
     * Constructor
     */
    public UserFragment() {
        //Required empty constructor.
    }

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    public static UserFragment newInstance(User user) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putParcelable(USER_PARAM, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            mUser = getArguments().getParcelable(USER_PARAM);
            searchedUser = true;
        }

        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mApplication.getFragmentComponent().inject(this);

        mPresenter.attachView(this);
        mPresenter.setSearchedUser(searchedUser);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        //Set the color of the refreshing animation
        if (!searchedUser) {
            mSwipeRefreshLayout.setColorSchemeColors(Utility.getColor(mContext, R.color.accent));
            mSwipeRefreshLayout.setOnRefreshListener(mPresenter);

            mUser = mProfileManager.getUser();
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }

        //Update all the views to show te user data
        updateUserPage(mUser);

        mAdManager.addAdView(mAdView);

        return rootView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getUserDataIfNeeded();
        mTracker.setScreenName(UserFragment.class.getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
        mAdManager.removeAdView(mAdView);
        mUnbinder.unbind();
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

    @OnClick({R.id.button_bazaar_tf, R.id.button_steamrep, R.id.button_tf2op, R.id.button_tf2tp,
            R.id.button_steam_community})
    public void onClick(View v) {
        //Handle all the buttons here

        //Get the steam id, do nothing if there is no steam id
        String steamId = mUser.getResolvedSteamId();
        if (steamId.equals("")) {
            showToast("bptf: " + getString(R.string.error_no_steam_id), Toast.LENGTH_SHORT);
            return;
        }

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
        CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
        CustomTabActivityHelper.openCustomTab(getActivity(), intent, webPage,
                new WebViewFallback());
    }

    @OnClick(R.id.backpack)
    public void onBackpackClick() {
        Intent i = new Intent(getActivity(), UserBackpackActivity.class);
        i.putExtra(UserBackpackActivity.EXTRA_NAME, mCollapsingToolbarLayout.getTitle());
        i.putExtra(UserBackpackActivity.EXTRA_GUEST, searchedUser);
        startActivity(i);
    }

    @Override
    public void updateUserPage(User user) {
        //Download avatar if needed.
        Glide.with(this)
                .load(mUser.getAvatarUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(avatar);

        //Set the player name
        String name = mUser.getName();
        mCollapsingToolbarLayout.setTitle(name);

        if (mUser.isBanned()) {
            //Set player name to red and cross name out if banned
            // TODO: 2015. 10. 22.
        }

        //Set the player reputation.
        playerReputation.setText(String.valueOf(mUser.getReputation()));

        //Set the 'user since' text
        long profileCreated = mUser.getProfileCreated();
        if (profileCreated == -1) {
            userSinceText.setText(getString(R.string.filler_unknown));
        } else {
            userSinceText.setText(Utility.formatUnixTimeStamp(profileCreated));
        }

        //Switch for the player's state
        switch (mUser.getState()) {
            case 0:
                long lastOnline = mUser.getLastOnline();
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

        //Load drawables for player statuses
        Drawable statusOk = getResources().getDrawable(R.drawable.ic_done_white_48dp);
        Drawable statusBad = getResources().getDrawable(R.drawable.ic_close_white_48dp);
        if (statusOk != null) statusOk.setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
        if (statusBad != null) statusBad.setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

        //Steamrep information
        if (mUser.isScammer()) {
            steamRepStatus.setImageDrawable(statusBad);
        } else {
            steamRepStatus.setImageDrawable(statusOk);
        }

        //Trade status
        if (mUser.isEconomyBanned()) {
            tradeStatus.setImageDrawable(statusBad);
        } else {
            tradeStatus.setImageDrawable(statusOk);
        }

        //VAC status
        if (mUser.isVacBanned()) {
            vacStatus.setImageDrawable(statusBad);
        } else {
            vacStatus.setImageDrawable(statusOk);
        }

        //Community status
        if (mUser.isCommunityBanned()) {
            communityStatus.setImageDrawable(statusBad);
        } else {
            communityStatus.setImageDrawable(statusOk);
        }

        //Backpack value
        double bpValue = mUser.getBackpackValue();

        backpack(privateBackpack);

        if (bpValue == -1) {
            //Value is unknown (probably private)
            backpackValueRefined.setText("?");
            backpackValueUsd.setText("?");
        } else {
            //Properly format the backpack value (is int, does it have a fraction smaller than 0.01)
            backpackValueRefined.setText(String.valueOf(Math.round(bpValue)));

            //Convert the value into USD and format it like above
            double bpValueUsd = bpValue * Utility.getDouble(mPrefs, getString(R.string.pref_metal_raw_usd), 1);
            backpackValueUsd.setText(String.valueOf(Math.round(bpValueUsd)));
        }

        //Set the trust score and color the background according to it.
        trustPositive.setText(String.format(Locale.ENGLISH, "+%d", mUser.getTrustPositive()));
        trustNegative.setText(String.format(Locale.ENGLISH, "-%d", mUser.getTrustNegative()));

        //Raw keys
        int rawKeys = mUser.getRawKeys();
        if (rawKeys >= 0)
            backpackRawKeys.setText(String.valueOf(rawKeys));
        else
            backpackRawKeys.setText("?");

        //Raw metal
        double rawMetal = mUser.getRawMetal();
        if (rawMetal >= 0)
            backpackRawMetal.setText(String.valueOf(Utility.formatDouble(rawMetal)));
        else
            backpackRawMetal.setText("?");

        //Number of slots and slots used
        int itemNumber = mUser.getItemCount();
        int backpackSlotNumber = mUser.getBackpackSlots();
        if (itemNumber >= 0 && backpackSlotNumber >= 0)
            backpackSlots.setText(String.format(Locale.ENGLISH, "%s/%d", String.valueOf(itemNumber), backpackSlotNumber));
        else
            backpackSlots.setText("?/?");
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
    }

    /**
     * Fully expand the toolbar with animation.
     */
    private void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

    @Override
    public void showRefreshingAnimation() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void hideRefreshingAnimation() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void backpack(boolean _private) {
        privateBackpack = _private;

        if (mPrivateBackpackText != null) {
            if (_private) {
                mPrivateBackpackText.setVisibility(View.VISIBLE);
                mBackpackButton.setEnabled(false);
                mBackpackButton.setVisibility(View.GONE);
                mBackpackContent.setVisibility(View.GONE);
            } else {
                mPrivateBackpackText.setVisibility(View.GONE);
                mBackpackButton.setEnabled(true);
                mBackpackButton.setVisibility(View.VISIBLE);
                mBackpackContent.setVisibility(View.VISIBLE);
            }
        }
    }
}