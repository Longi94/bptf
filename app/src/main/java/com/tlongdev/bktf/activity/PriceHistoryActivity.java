package com.tlongdev.bktf.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.HistoryAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.network.GetPriceHistory;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

public class PriceHistoryActivity extends AppCompatActivity implements GetPriceHistory.OnPriceHistoryListener {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = GetPriceHistory.class.getSimpleName();

    public static final String EXTRA_ITEM = "item";

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    private Item mItem;

    /**
     * Views
     */
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView failText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_history);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }

        Intent i = getIntent();

        mItem = (Item) i.getSerializableExtra(EXTRA_ITEM);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        failText = (TextView) findViewById(R.id.fail_text);
        if (Utility.isNetworkAvailable(this)) {
            GetPriceHistory task = new GetPriceHistory(this, mItem);
            task.setListener(this);
            task.execute();
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
        recyclerView.setAdapter(adapter);

        //Animate in the recycler view, so it's not that abrupt
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.simple_fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.simple_fade_in);

        fadeIn.setDuration(500);
        fadeOut.setDuration(500);

        recyclerView.startAnimation(fadeIn);
        recyclerView.setVisibility(View.VISIBLE);

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
