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

package com.tlongdev.bktf.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.google.android.gms.ads.AdView;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.SearchAdapter;
import com.tlongdev.bktf.customtabs.CustomTabActivityHelper;
import com.tlongdev.bktf.customtabs.WebViewFallback;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.presenter.activity.SearchPresenter;
import com.tlongdev.bktf.util.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends BptfActivity implements com.tlongdev.bktf.ui.view.activity.SearchView, SearchAdapter.OnSearchClickListener {

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.ad_view) AdView mAdView;

    //The adapter of the recyclerview
    private SearchAdapter mAdapter;

    private boolean filterEnabled = false;
    private boolean filterTradable = true;
    private boolean filterCraftable = true;
    private boolean filterAustralium = false;
    private int filterQuality = Quality.UNIQUE;
    private String mQuery;

    private SearchPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        mPresenter = new SearchPresenter(mApplication);
        mPresenter.attachView(this);

        setSupportActionBar(mToolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        int columnCount = 1;
        if (dpWidth >= 720) {
            columnCount = 3;
        } else if (dpWidth >= 600) {
            columnCount = 2;
        }

        //Initialize the list
        mAdapter = new SearchAdapter(mApplication);
        mAdapter.setListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));
        mRecyclerView.setAdapter(mAdapter);

        mAdManager.addAdView(mAdView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
        mAdManager.removeAdView(mAdView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        //Setup the search widget
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setQueryHint("Items and users...");

        //Restart the loader every time the query string is changed
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mQuery = s;
                mPresenter.search(mQuery, filterEnabled, filterQuality, filterTradable,
                        filterCraftable, filterAustralium);

                mAdapter.setLoading(true);
                mAdapter.setUser(null);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mQuery = s;
                mPresenter.search(mQuery, filterEnabled, filterQuality, filterTradable,
                        filterCraftable, filterAustralium);

                mAdapter.setLoading(true);
                mAdapter.setUser(null);
                return true;
            }
        });

        //Auto expand the search view
        mSearchView.setIconified(false);

        return true;
    }

    @Override
    public void showItems(Cursor items) {
        mAdapter.swapCursor(items);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void userFound(User user) {
        mAdapter.setUser(user);
        mAdapter.setLoading(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void userNotFound() {
        mAdapter.setUser(null);
        mAdapter.setLoading(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            filterEnabled = data.getBooleanExtra(SearchFilterActivity.EXTRA_ENABLED, false);
            filterQuality = data.getIntExtra(SearchFilterActivity.EXTRA_QUALITY, Quality.UNIQUE);
            filterTradable = data.getBooleanExtra(SearchFilterActivity.EXTRA_TRADABLE, true);
            filterCraftable = data.getBooleanExtra(SearchFilterActivity.EXTRA_CRAFTABLE, true);
            filterAustralium = data.getBooleanExtra(SearchFilterActivity.EXTRA_AUSTRALIUM, false);

            mPresenter.search(mQuery, filterEnabled, filterQuality, filterTradable,
                    filterCraftable, filterAustralium);

            mAdapter.setLoading(true);
            mAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.fab)
    public void onClick(View v) {
        Intent intent = new Intent(this, SearchFilterActivity.class);
        intent.putExtra(SearchFilterActivity.EXTRA_ENABLED, filterEnabled);
        intent.putExtra(SearchFilterActivity.EXTRA_TRADABLE, filterTradable);
        intent.putExtra(SearchFilterActivity.EXTRA_CRAFTABLE, filterCraftable);
        intent.putExtra(SearchFilterActivity.EXTRA_QUALITY, filterQuality);
        intent.putExtra(SearchFilterActivity.EXTRA_AUSTRALIUM, filterAustralium);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onMoreClicked(View view, final Item item) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.popup_item, menu.getMenu());
        menu.getMenu().getItem(0).setTitle(
                Utility.isFavorite(this, item) ? "Remove from favorites" : "Add to favorites");
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.history:

                        Intent i = new Intent(SearchActivity.this, PriceHistoryActivity.class);

                        i.putExtra(PriceHistoryActivity.EXTRA_ITEM, item);

                        startActivity(i);
                        break;
                    case R.id.favorite:
                        if (Utility.isFavorite(SearchActivity.this, item)) {
                            Utility.removeFromFavorites(SearchActivity.this, item);
                        } else {
                            Utility.addToFavorites(SearchActivity.this, item);
                        }
                        break;
                    case R.id.backpack_tf:
                        Uri uri = Uri.parse(item.getBackpackTfUrl());
                        CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
                        CustomTabActivityHelper.openCustomTab(SearchActivity.this, intent, uri,
                                new WebViewFallback());
                        break;
                }
                return true;
            }
        });

        menu.show();
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra(UserActivity.STEAM_ID_KEY, user.getResolvedSteamId());
        startActivity(intent);
    }
}