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

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.google.android.gms.analytics.HitBuilders;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.HistoryAdapter;
import com.tlongdev.bktf.interactor.BackpackTfPriceHistoryInteractor;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PriceHistoryActivity extends BptfActivity implements BackpackTfPriceHistoryInteractor.Callback {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = BackpackTfPriceHistoryInteractor.class.getSimpleName();

    public static final String EXTRA_ITEM = "item";

    @InjectExtra(EXTRA_ITEM) Item mItem;

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.progress_bar) ProgressBar progressBar;
    @Bind(R.id.fail_text) TextView failText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_history);
        ButterKnife.bind(this);
        Dart.inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (Utility.isNetworkAvailable(this)) {
            BackpackTfPriceHistoryInteractor task = new BackpackTfPriceHistoryInteractor((BptfApplication) getApplication(), mItem, this);
            task.execute();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("PriceHistory")
                    .build());
        } else {
            progressBar.setVisibility(View.GONE);
            failText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPriceHistoryFinished(List<Price> prices) {
        HistoryAdapter adapter = new HistoryAdapter(this, prices, mItem);
        mRecyclerView.setAdapter(adapter);

        //Animate in the recycler view, so it's not that abrupt
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.simple_fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.simple_fade_in);

        fadeIn.setDuration(500);
        fadeOut.setDuration(500);

        mRecyclerView.startAnimation(fadeIn);
        mRecyclerView.setVisibility(View.VISIBLE);

        progressBar.startAnimation(fadeOut);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPriceHistoryFailed(String errorMessage) {
        if (errorMessage != null) {
            Log.d(LOG_TAG, errorMessage);
        }

        //Animate in the recycler view, so it's not that abrupt
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.simple_fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.simple_fade_in);

        fadeIn.setDuration(500);
        fadeOut.setDuration(500);

        failText.startAnimation(fadeIn);
        failText.setVisibility(View.VISIBLE);

        progressBar.startAnimation(fadeOut);
        progressBar.setVisibility(View.GONE);
    }
}
