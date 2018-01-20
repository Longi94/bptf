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
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.HistoryAdapter;
import com.tlongdev.bktf.interactor.BackpackTfPriceHistoryInteractor;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.activity.PriceHistoryPresenter;
import com.tlongdev.bktf.ui.view.activity.PriceHistoryView;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PriceHistoryActivity extends BptfActivity implements PriceHistoryView {

    private static final String LOG_TAG = BackpackTfPriceHistoryInteractor.class.getSimpleName();

    public static final String EXTRA_ITEM = "item";

    @Inject PriceHistoryPresenter mPresenter;

    @InjectExtra(EXTRA_ITEM) Item mItem;

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.fail_text) TextView failText;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_history);
        ButterKnife.bind(this);
        Dart.inject(this);

        mApplication.getActivityComponent().inject(this);

        mPresenter.attachView(this);

        setSupportActionBar(mToolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (Utility.isNetworkAvailable(this)) {
            mPresenter.loadPriceHistory(mItem);
        } else {
            progressBar.setVisibility(View.GONE);
            failText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showHistory(List<Price> prices) {
        mRecyclerView.setAdapter(new HistoryAdapter(mApplication, prices, mItem));

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
    public void showError(String errorMessage) {
        if (errorMessage != null) {
            Log.d(LOG_TAG, errorMessage);
        }

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
