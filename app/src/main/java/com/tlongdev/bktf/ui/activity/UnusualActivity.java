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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.google.android.gms.analytics.HitBuilders;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.UnusualAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.activity.UnusualPresenter;
import com.tlongdev.bktf.ui.view.activity.UnusualView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Activity for showing unusual prices for specific effects or hats.
 */
public class UnusualActivity extends BptfActivity implements UnusualView, TextWatcher, UnusualAdapter.OnItemClickListener {

    private static final String EXTRA_DEFINDEX = "defindex";
    private static final String EXTRA_NAME = "name";
    private static final String EXTRA_PRICE_INDEX = "index";

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

        mPresenter.loadUnusuals(mDefindex, mIndex, "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
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
    public void onMoreClicked(View view, Item item) {

    }

    @Override
    public void onItemClicked(int index, String name, boolean effect) {
        Intent i = new Intent(this, UnusualActivity.class);
        if (effect) {
            i.putExtra(UnusualActivity.EXTRA_PRICE_INDEX, index);
        } else {
            i.putExtra(UnusualActivity.EXTRA_DEFINDEX, index);
        }
        i.putExtra(UnusualActivity.EXTRA_NAME, name);
        startActivity(i);
    }
}
