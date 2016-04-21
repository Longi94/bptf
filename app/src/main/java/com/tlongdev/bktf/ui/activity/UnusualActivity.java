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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.google.android.gms.ads.AdView;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.UnusualAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.activity.UnusualPresenter;
import com.tlongdev.bktf.ui.view.activity.UnusualView;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Activity for showing unusual prices for specific effects or hats.
 */
public class UnusualActivity extends BptfActivity implements UnusualView, TextWatcher, UnusualAdapter.OnItemClickListener {

    public static final String EXTRA_DEFINDEX = "defindex";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_PRICE_INDEX = "index";

    @SuppressWarnings("NullableProblems")
    @Nullable
    @InjectExtra(EXTRA_DEFINDEX) int mDefindex = -1;
    @SuppressWarnings("NullableProblems")
    @Nullable
    @InjectExtra(EXTRA_PRICE_INDEX) int mIndex = -1;
    @InjectExtra(EXTRA_NAME) String mName;

    @Bind(R.id.search) EditText mSearchInput;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.ad_view) AdView mAdView;

    private UnusualAdapter mAdapter;
    private UnusualPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unusual);
        ButterKnife.bind(this);
        Dart.inject(this);

        mPresenter = new UnusualPresenter(mApplication);
        mPresenter.attachView(this);

        setSupportActionBar(mToolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Set the action bar title to the current hat/effect name
        setTitle(mName);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int columnCount = Math.max(3, (int) Math.floor(dpWidth / 120.0));

        //Initialize adapter
        mAdapter = new UnusualAdapter(mApplication);
        mAdapter.setType(UnusualAdapter.TYPE_SPECIFIC_HAT);
        mAdapter.setListener(this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));

        mSearchInput.addTextChangedListener(this);
        mSearchInput.setHint(mDefindex != -1 ? "Effect" : "Name");

        mAdManager.addAdView(mAdView);

        mPresenter.loadUnusuals(mDefindex, mIndex, "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
        mAdManager.removeAdView(mAdView);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mPresenter.loadUnusuals(mDefindex, mIndex, s.toString());
    }

    @Override
    public void showUnusuals(List<Item> unusuals) {
        mAdapter.setDataSet(unusuals);
        mAdapter.notifyDataSetChanged();
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
                        Intent i = new Intent(UnusualActivity.this, PriceHistoryActivity.class);
                        i.putExtra(PriceHistoryActivity.EXTRA_ITEM, item);
                        startActivity(i);
                        break;
                    case R.id.favorite:
                        if (Utility.isFavorite(UnusualActivity.this, item)) {
                            Utility.removeFromFavorites(UnusualActivity.this, item);
                        } else {
                            Utility.addToFavorites(UnusualActivity.this, item);
                        }
                        break;
                    case R.id.backpack_tf:
                        Intent intent = new Intent(UnusualActivity.this, WebActivity.class);
                        intent.putExtra(WebActivity.EXTRA_URL, item.getBackpackTfUrl());
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
        menu.show();
    }

    @Override
    public void onItemClicked(int index, String name, boolean effect) {
    }
}
