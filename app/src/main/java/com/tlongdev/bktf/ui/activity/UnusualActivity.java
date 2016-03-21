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
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.UnusualAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.activity.UnusualPresenter;
import com.tlongdev.bktf.ui.view.activity.UnusualView;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Activity for showing unusual prices for specific effects or hats.
 */
public class UnusualActivity extends AppCompatActivity implements UnusualView, TextWatcher {

    /**
     * Log tag for logging.
     */
    private static final String LOG_TAG = UnusualActivity.class.getSimpleName();

    //Intent extra keys
    public static final String DEFINDEX_KEY = "defindex";
    public static final String NAME_KEY = "name";
    public static final String PRICE_INDEX_KEY = "index";

    @Inject Tracker mTracker;

    @Bind(R.id.search) EditText mSearchInput;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;

    //Adapter for the gridView
    private UnusualAdapter mAdapter;

    //The defindex and index of the item to be viewed
    private int defindex;
    private int index;

    private UnusualPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unusual);
        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        application.getActivityComponent().inject(this);

        mPresenter = new UnusualPresenter(application);
        mPresenter.attachView(this);

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }

        setSupportActionBar(mToolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Get necessary data from intent
        Intent i = getIntent();
        defindex = i.getIntExtra(DEFINDEX_KEY, -1);
        index = i.getIntExtra(PRICE_INDEX_KEY, -1);

        //Set the action bar title to the current hat/effect name
        setTitle(i.getStringExtra(NAME_KEY));

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int columnCount = Math.max(3, (int) Math.floor(dpWidth / 120.0));

        //Initialize adapter
        mAdapter = new UnusualAdapter(this);
        mAdapter.setType(UnusualAdapter.TYPE_SPECIFIC_HAT);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));

        mSearchInput.addTextChangedListener(this);
        mSearchInput.setHint(defindex != -1 ? "Effect" : "Name");

        mPresenter.loadUnusuals(defindex, index, "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mPresenter.loadUnusuals(defindex, index, s.toString());
    }

    @Override
    public void showToast(CharSequence message, int duration) {
        Toast.makeText(this, message, duration).show();
    }

    @Override
    public void showUnusuals(List<Item> unusuals) {
        mAdapter.setDataSet(unusuals);
        mAdapter.notifyDataSetChanged();
    }
}
