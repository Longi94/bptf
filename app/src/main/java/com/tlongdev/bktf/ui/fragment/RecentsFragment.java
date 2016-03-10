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

package com.tlongdev.bktf.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.RecentsAdapter;
import com.tlongdev.bktf.presenter.RecentsPresenter;
import com.tlongdev.bktf.ui.RecentsView;
import com.tlongdev.bktf.ui.activity.MainActivity;
import com.tlongdev.bktf.ui.activity.SearchActivity;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * recents fragment. Shows a list of all the prices orderd by the time of the price update.
 */
public class RecentsFragment extends Fragment implements RecentsView,
        SwipeRefreshLayout.OnRefreshListener, MainActivity.OnDrawerOpenedListener {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = RecentsFragment.class.getSimpleName();

    /**
     * Indexes for the columns
     */
    public static final int COL_PRICE_LIST_DEFI = 0;
    public static final int COL_PRICE_LIST_NAME = 1;
    public static final int COL_PRICE_LIST_QUAL = 2;
    public static final int COL_PRICE_LIST_TRAD = 3;
    public static final int COL_PRICE_LIST_CRAF = 4;
    public static final int COL_PRICE_LIST_INDE = 5;
    public static final int COL_PRICE_LIST_CURR = 6;
    public static final int COL_PRICE_LIST_PRIC = 7;
    public static final int COL_PRICE_LIST_PMAX = 8;
    public static final int COL_PRICE_LIST_PRAW = 9;
    public static final int COL_PRICE_LIST_DIFF = 10;
    public static final int COL_AUSTRALIUM = 11;

    @Inject Tracker mTracker;
    @Inject SharedPreferences mPrefs;

    /**
     * Loading indicator
     */
    @Bind(R.id.progress_bar) ProgressBar progressBar;

    /**
     * the swipe refresh layout
     */
    @Bind(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * The recycler view
     */
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;

    /**
     * Only needed for manually expanding the toolbar
     */
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;

    /**
     * Views
     */
    @Bind(R.id.text_view_metal_price) TextView metalPrice;
    @Bind(R.id.text_view_key_price) TextView keyPrice;
    @Bind(R.id.text_view_buds_price) TextView budsPrice;
    @Bind(R.id.image_view_metal_price) View metalPriceImage;
    @Bind(R.id.image_view_key_price) View keyPriceImage;
    @Bind(R.id.image_view_buds_price) View budsPriceImage;

    /**
     * Adapter of the recycler view
     */
    private RecentsAdapter adapter;

    //Dialog to indicate the download progress
    private ProgressDialog loadingDialog;

    private Context mContext;

    private RecentsPresenter presenter;

    /**
     * Constructor
     */
    public RecentsFragment() {
        //Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getActivity().getApplication();
        application.getFragmentComponent().inject(this);

        presenter = new RecentsPresenter();
        presenter.attachView(this);
        presenter.setTracker(mTracker);

        View rootView = inflater.inflate(R.layout.fragment_recents, container, false);
        ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) mContext).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        adapter = new RecentsAdapter(mContext, null);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        int columnCount = 1;
        if (dpWidth >= 720) {
            columnCount = 3;
        } else if (dpWidth >= 600) {
            columnCount = 2;
        }

        //Setup the recycler view
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, columnCount));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setVisibility(View.GONE);

        //Set up the swipe refresh layout (color and listener)
        mSwipeRefreshLayout.setColorSchemeColors(Utility.getColor(mContext, R.color.accent));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //Populate the toolbar header
        updateCurrencyHeader();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.loadPrices();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("Latest Changes");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        presenter.downloadPricesIfNeeded();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_recents, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //Start the search activity
                startActivity(new Intent(mContext, SearchActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        presenter.downloadPrices();
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
    }

    /**
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

    @Override
    public void showPrices(Cursor prices) {
        adapter.swapCursor(prices, true);

        //Animate in the recycler view, so it's not that abrupt
        Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.simple_fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(mContext, R.anim.simple_fade_in);

        fadeIn.setDuration(500);
        fadeOut.setDuration(500);

        mRecyclerView.startAnimation(fadeIn);
        mRecyclerView.setVisibility(View.VISIBLE);

        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.startAnimation(fadeOut);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError() {
        adapter.swapCursor(null, true);
    }

    @Override
    public void showRefreshAnimation() {
        //Workaround for the circle not appearing
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
    public void finishActivity() {
        getActivity().finish();
    }

    @Override
    public void updateCurrencyHeader() {
        metalPrice.setText(mPrefs.getString(getString(R.string.pref_metal_price), ""));
        keyPrice.setText(mPrefs.getString(getString(R.string.pref_key_price), ""));
        budsPrice.setText(mPrefs.getString(getString(R.string.pref_buds_price), ""));

        if (Utility.getDouble(mPrefs, getString(R.string.pref_metal_diff), 0.0) > 0.0) {
            metalPriceImage.setBackgroundColor(0xff008504);
        } else {
            metalPriceImage.setBackgroundColor(0xff850000);
        }
        if (Utility.getDouble(mPrefs, getString(R.string.pref_key_diff), 0.0) > 0.0) {
            keyPriceImage.setBackgroundColor(0xff008504);
        } else {
            keyPriceImage.setBackgroundColor(0xff850000);
        }
        if (Utility.getDouble(mPrefs, getString(R.string.pref_buds_diff), 0.0) > 0) {
            budsPriceImage.setBackgroundColor(0xff008504);
        } else {
            budsPriceImage.setBackgroundColor(0xff850000);
        }
    }

    @Override
    public void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    @Override
    public void showLoadingDialog(String message) {
        loadingDialog = ProgressDialog.show(getActivity(), null, message, true);
        loadingDialog.setCancelable(false);
    }

    @Override
    public void updateLoadingDialog(int max, String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            if (loadingDialog.isIndeterminate()) {
                loadingDialog.dismiss();
                loadingDialog = new ProgressDialog(mContext);
                loadingDialog.setIndeterminate(false);
                loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                loadingDialog.setMessage(message);
                loadingDialog.setMax(max);
                loadingDialog.setCancelable(false);
                loadingDialog.show();
            } else {
                loadingDialog.incrementProgressBy(1);
            }
        }
    }

    @Override
    public void showItemSchemaError(String errorMessage) {
        if (loadingDialog != null && loadingDialog.isShowing()) {

            AlertDialog.Builder builder;
            AlertDialog alertDialog;
            builder = new AlertDialog.Builder(mContext);
            builder.setMessage(mContext.getString(R.string.message_database_fail_network))
                    .setCancelable(false)
                    .setPositiveButton(mContext.getString(R.string.action_close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Close app
                            getActivity().finish();
                        }
                    });
            alertDialog = builder.create();
            loadingDialog.dismiss();
            alertDialog.show();
        } else {
            Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showPricesError(String errorMessage) {
        if (loadingDialog != null && loadingDialog.isShowing()) {

            AlertDialog.Builder builder;
            AlertDialog alertDialog;
            builder = new AlertDialog.Builder(mContext);
            builder.setMessage(mContext.getString(R.string.message_database_fail_network))
                    .setCancelable(false)
                    .setPositiveButton(mContext.getString(R.string.action_close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Close app
                            getActivity().finish();
                        }
                    });
            alertDialog = builder.create();
            loadingDialog.dismiss();
            alertDialog.show();
        } else {
            Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public BptfApplication getBptfApplication() {
        return (BptfApplication) getActivity().getApplication();
    }
}
