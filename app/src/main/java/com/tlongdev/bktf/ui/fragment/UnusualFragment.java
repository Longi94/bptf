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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.UnusualAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.fragment.UnusualPresenter;
import com.tlongdev.bktf.ui.activity.MainActivity;
import com.tlongdev.bktf.ui.activity.SearchActivity;
import com.tlongdev.bktf.ui.view.fragment.UnusualView;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * The unusual fragment, that shows a list of unusual item categories. Either categorized by
 * hats or effects.
 */
public class UnusualFragment extends Fragment implements UnusualView,
        MainActivity.OnDrawerOpenedListener, TextWatcher {

    /**
     * Log tag for logging.
     */
    private static final String LOG_TAG = UnusualFragment.class.getSimpleName();

    @Inject Tracker mTracker;

    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.search) EditText mSearchInput;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;

    /**
     * the current sort type
     */
    @UnusualPresenter.UnusualOrder
    private int mCurrentSort = UnusualPresenter.ORDER_BY_PRICE;

    /**
     * Whether to show effects or hats
     */
    private boolean showEffect = false;

    /**
     * The adapter of the recycler view
     */
    private UnusualAdapter mAdapter;

    /**
     * the menu item that switches between effects and hats
     */
    private MenuItem effectMenuItem;

    private UnusualPresenter mPresenter;

    /**
     * Constructor.
     */
    public UnusualFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) (getActivity()).getApplication();
        application.getFragmentComponent().inject(this);

        mPresenter = new UnusualPresenter(application);
        mPresenter.attachView(this);

        View rootView = inflater.inflate(R.layout.fragment_unusual, container, false);

        ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int columnCount = Math.max(3, (int) Math.floor(dpWidth / 120.0));

        //init the recycler view
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        mAdapter = new UnusualAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mSearchInput.addTextChangedListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.loadUnusualHats("", UnusualPresenter.ORDER_BY_PRICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("Unusual Prices");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void showUnusualHats(List<Item> unusuals) {
        mAdapter.setType(UnusualAdapter.TYPE_HATS);
        mAdapter.setDataSet(unusuals);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showUnusualEffects(List<Item> unusuals) {
        mAdapter.setType(UnusualAdapter.TYPE_EFFECTS);
        mAdapter.setDataSet(unusuals);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_unusual, menu);
        effectMenuItem = menu.findItem(R.id.action_effect);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String filter = mSearchInput.getText().toString();

        switch (item.getItemId()) {
            case R.id.menu_sort_name:
                if (showEffect) {
                    if (mCurrentSort != UnusualPresenter.ORDER_BY_NAME) {
                        mCurrentSort = UnusualPresenter.ORDER_BY_NAME;
                        mPresenter.loadUnusualEffects(filter, mCurrentSort);
                    }
                } else {
                    if (mCurrentSort != UnusualPresenter.ORDER_BY_NAME) {
                        mCurrentSort = UnusualPresenter.ORDER_BY_NAME;
                        mPresenter.loadUnusualHats(filter, mCurrentSort);
                    }
                }
                return true;
            case R.id.menu_sort_price:
                if (showEffect) {
                    if (mCurrentSort != UnusualPresenter.ORDER_BY_NAME) {
                        mCurrentSort = UnusualPresenter.ORDER_BY_NAME;
                        mPresenter.loadUnusualEffects(filter, mCurrentSort);
                    }
                } else {
                    if (mCurrentSort != UnusualPresenter.ORDER_BY_PRICE) {
                        mCurrentSort = UnusualPresenter.ORDER_BY_PRICE;
                        mPresenter.loadUnusualHats(filter, mCurrentSort);
                    }
                }
                return true;
            case R.id.action_effect:
                if (showEffect) {
                    //Show hats sorted by their average price
                    mPresenter.loadUnusualHats("", mCurrentSort);

                    effectMenuItem.setIcon(R.drawable.ic_star_outline_white);
                    getActivity().setTitle(getString(R.string.title_unusuals));

                    mSearchInput.setHint("Name");
                } else {
                    //Show effects
                    mPresenter.loadUnusualEffects("", mCurrentSort);

                    effectMenuItem.setIcon(R.drawable.ic_star_white);
                    getActivity().setTitle(getString(R.string.title_effects));

                    mSearchInput.setHint("Effect");
                }
                showEffect = !showEffect;
                mSearchInput.setText("");
                return true;
            case R.id.action_search:
                //Start the search activity
                startActivity(new Intent(getActivity(), SearchActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
        Utility.hideKeyboard(getActivity());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String filter = s.toString();
        if (showEffect) {
            mPresenter.loadUnusualEffects(filter, mCurrentSort);
        } else {
            mPresenter.loadUnusualHats(filter, mCurrentSort);
        }
    }
}
