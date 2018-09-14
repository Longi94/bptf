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
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.customtabs.CustomTabActivityHelper;
import com.tlongdev.bktf.customtabs.WebViewFallback;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.presenter.fragment.UserPresenter;
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
public class UserFragment extends BptfFragment implements UserView, View.OnClickListener {

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
    @BindView(R.id.text_view_bp_usd) TextView backpackValueUsd;
    @BindView(R.id.text_view_user_since) TextView userSinceText;
    @BindView(R.id.text_view_user_last_online) TextView lastOnlineText;
    @BindView(R.id.avatar) ImageView avatar;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.backpack) ImageView mBackpackButton;

    private User mUser;
    private boolean mSearchedUser;

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
            mSearchedUser = true;
        }

        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mApplication.getFragmentComponent().inject(this);

        mPresenter.attachView(this);
        mPresenter.setSearchedUser(mSearchedUser);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        //Set the color of the refreshing animation
        if (!mSearchedUser) {
            mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.accent));
            mSwipeRefreshLayout.setOnRefreshListener(mPresenter);

            mUser = mProfileManager.getUser();
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }

        //Update all the views to show te user data
        updateUserPage(mUser);

        return rootView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getUserDataIfNeeded();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
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
        if (steamId == null || steamId.equals("")) {
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
        i.putExtra(UserBackpackActivity.EXTRA_GUEST, mSearchedUser);
        i.putExtra(UserBackpackActivity.EXTRA_STEAM_ID, mUser.getResolvedSteamId());
        startActivity(i);
    }

    @Override
    public void updateUserPage(User user) {
        //Download avatar if needed.
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(this)
                .load(mUser.getAvatarUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(options)
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
                    lastOnlineText.setText(String.format("%s %s", getString(R.string.user_page_last_online),
                            DateUtils.getRelativeTimeSpanString(System.currentTimeMillis() - lastOnline * 1000L).toString()));
                }
                lastOnlineText.setTextColor(ContextCompat.getColor(mContext, R.color.text_primary));
                break;
            case 1:
                lastOnlineText.setText(getString(R.string.user_page_status_online));
                lastOnlineText.setTextColor(ContextCompat.getColor(mContext, R.color.player_online));
                break;
            case 2:
                lastOnlineText.setText(getString(R.string.user_page_status_busy));
                lastOnlineText.setTextColor(ContextCompat.getColor(mContext, R.color.player_online));
                break;
            case 3:
                lastOnlineText.setText(getString(R.string.user_page_status_away));
                lastOnlineText.setTextColor(ContextCompat.getColor(mContext, R.color.player_online));
                break;
            case 4:
                lastOnlineText.setText(getString(R.string.user_page_status_snooze));
                lastOnlineText.setTextColor(ContextCompat.getColor(mContext, R.color.player_online));
                break;
            case 5:
                lastOnlineText.setText(getString(R.string.user_page_status_trade));
                lastOnlineText.setTextColor(ContextCompat.getColor(mContext, R.color.player_online));
                break;
            case 6:
                lastOnlineText.setText(getString(R.string.user_page_status_play));
                lastOnlineText.setTextColor(ContextCompat.getColor(mContext, R.color.player_online));
                break;
            case 7:
                lastOnlineText.setText(getString(R.string.user_page_status_in_game));
                lastOnlineText.setTextColor(ContextCompat.getColor(mContext, R.color.player_in_game));
                break;
        }

        //Load drawables for player statuses
        Drawable statusOk = ContextCompat.getDrawable(getActivity(), R.drawable.ic_check);
        Drawable statusBad = ContextCompat.getDrawable(getActivity(), R.drawable.ic_close);
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
    }

    @Override
    public void showRefreshingAnimation() {
        mSwipeRefreshLayout.post(() -> {
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void hideRefreshingAnimation() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}