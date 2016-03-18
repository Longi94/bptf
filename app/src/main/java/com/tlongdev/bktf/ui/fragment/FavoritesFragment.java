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

import android.app.Activity;
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
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.FavoritesAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.fragment.FavoritesPresenter;
import com.tlongdev.bktf.ui.activity.ItemChooserActivity;
import com.tlongdev.bktf.ui.activity.MainActivity;
import com.tlongdev.bktf.ui.activity.SearchActivity;
import com.tlongdev.bktf.ui.view.fragment.FavoritesView;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends Fragment implements FavoritesView,
        MainActivity.OnDrawerOpenedListener {

    private static final String LOG_TAG = FavoritesFragment.class.getSimpleName();

    @Inject Tracker mTracker;

    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;

    private FavoritesPresenter mPresenter;
    private FavoritesAdapter mAdapter;

    /**
     * Constructor
     */
    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        BptfApplication application = (BptfApplication) (getActivity()).getApplication();
        application.getFragmentComponent().inject(this);

        mPresenter = new FavoritesPresenter(application);
        mPresenter.attachView(this);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        mAdapter = new FavoritesAdapter(getActivity(), null);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        int columnCount = 1;
        if (dpWidth >= 720) {
            columnCount = 3;
        } else if (dpWidth >= 600) {
            columnCount = 2;
        }

        //Setup the recycler view
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.loadFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("Favorites");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MainActivity.REQUEST_NEW_ITEM:
                if (resultCode == Activity.RESULT_OK) {
                    Utility.addToFavorites(getActivity(), (Item) data.getParcelableExtra(ItemChooserActivity.EXTRA_ITEM));
                    mPresenter.loadFavorites();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorites, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //Start the search activity
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.action_add_currencies:
                mPresenter.addCurrencies();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
    }

    @OnClick(R.id.fab)
    public void addItem() {
        startActivityForResult(new Intent(getActivity(), ItemChooserActivity.class), MainActivity.REQUEST_NEW_ITEM);
    }

    @Override
    public void showFavorites(List<Item> items) {
        mAdapter.setDataSet(items);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

    @Override
    public void showToast(CharSequence message, int duration) {
        Toast.makeText(getActivity(), message, duration).show();
    }
}
