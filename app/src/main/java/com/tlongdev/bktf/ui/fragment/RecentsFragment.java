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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.RecentsAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.fragment.RecentsPresenter;
import com.tlongdev.bktf.ui.activity.MainActivity;
import com.tlongdev.bktf.ui.activity.PriceHistoryActivity;
import com.tlongdev.bktf.ui.activity.SearchActivity;
import com.tlongdev.bktf.ui.view.fragment.RecentsView;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * recents fragment. Shows a list of all the prices orderd by the time of the price update.
 */
public class RecentsFragment extends BptfFragment implements RecentsView,
        SwipeRefreshLayout.OnRefreshListener, MainActivity.OnDrawerOpenedListener, RecentsAdapter.OnMoreListener {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = RecentsFragment.class.getSimpleName();

    @Bind(R.id.progress_bar) ProgressBar progressBar;
    @Bind(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.text_view_metal_price) TextView mMetalPrice;
    @Bind(R.id.text_view_key_price) TextView mKeyPrice;
    @Bind(R.id.text_view_buds_price) TextView mBudsPrice;
    @Bind(R.id.image_view_metal_price) View metalPriceImage;
    @Bind(R.id.image_view_key_price) View keyPriceImage;
    @Bind(R.id.image_view_buds_price) View budsPriceImage;

    /**
     * Adapter of the recycler view
     */
    private RecentsAdapter adapter;

    private ProgressDialog loadingDialog;

    private Context mContext;

    private RecentsPresenter mPresenter;

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
        mPresenter = new RecentsPresenter(mApplication);
        mPresenter.attachView(this);

        View rootView = inflater.inflate(R.layout.fragment_recents, container, false);
        ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) mContext).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        adapter = new RecentsAdapter(mApplication, null);
        adapter.setListener(this);

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

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.loadPrices();
        mPresenter.loadCurrencyPrices();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("Latest Changes");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        mPresenter.downloadPricesIfNeeded();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
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
        mPresenter.downloadPrices();
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
    public void updateCurrencyHeader(Price metalPrice, Price keyPrice, Price budPrice) {
        mMetalPrice.setText(metalPrice.getFormattedPrice(getActivity()));
        mKeyPrice.setText(keyPrice.getFormattedPrice(getActivity()));
        mBudsPrice.setText(budPrice.getFormattedPrice(getActivity()));

        if (metalPrice.getDifference() > 0.0) {
            metalPriceImage.setBackgroundColor(0xff008504);
        } else {
            metalPriceImage.setBackgroundColor(0xff850000);
        }
        if (keyPrice.getDifference() > 0.0) {
            keyPriceImage.setBackgroundColor(0xff008504);
        } else {
            keyPriceImage.setBackgroundColor(0xff850000);
        }
        if (budPrice.getDifference() > 0) {
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
            loadingDialog.dismiss();
            showErrorDialog();
        } else {
            showToast("bptf: " + errorMessage, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void showPricesError(String errorMessage) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            showErrorDialog();
        } else {
            showToast("bptf: " + errorMessage, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void showErrorDialog() {
        //Quit the app if the download failed.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mContext.getString(R.string.message_database_fail_network)).setCancelable(false).
                setPositiveButton(mContext.getString(R.string.action_close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onMoreClicked(View view, final Item item) {
        PopupMenu menu = new PopupMenu(getActivity(), view);

        menu.getMenuInflater().inflate(R.menu.popup_item, menu.getMenu());

        menu.getMenu().getItem(0).setTitle(
                Utility.isFavorite(mContext, item) ? "Remove from favorites" : "Add to favorites");

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.history:
                        Intent i = new Intent(getActivity(), PriceHistoryActivity.class);
                        i.putExtra(PriceHistoryActivity.EXTRA_ITEM, item);
                        startActivity(i);
                        break;
                    case R.id.favorite:
                        if (Utility.isFavorite(mContext, item)) {
                            Utility.removeFromFavorites(mContext, item);
                        } else {
                            Utility.addToFavorites(mContext, item);
                        }
                        break;
                    case R.id.backpack_tf:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getBackpackTfUrl())));
                        break;
                }
                return true;
            }
        });

        menu.show();
    }
}
