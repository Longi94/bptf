package com.tlongdev.bktf.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
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

    public static final String EXTRA_DEFINDEX = "defindex";
    public static final String EXTRA_QUALITY = "quality";
    public static final String EXTRA_TRADABLE = "tradable";
    public static final String EXTRA_CRAFTABLE = "craftable";
    public static final String EXTRA_PRICE_INDEX = "price_index";

    private Item mItem;

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

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

        mItem = new Item(
                i.getIntExtra(EXTRA_DEFINDEX, 0),
                null,
                i.getIntExtra(EXTRA_QUALITY, 0),
                i.getBooleanExtra(EXTRA_TRADABLE, true),
                i.getBooleanExtra(EXTRA_CRAFTABLE, true),
                false,
                i.getIntExtra(EXTRA_PRICE_INDEX, 0),
                null
        );

        GetPriceHistory task = new GetPriceHistory(mItem);
        task.setListener(this);
        task.execute();
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
        Log.d(LOG_TAG, "woooop");
    }

    @Override
    public void onPriceHistoryFailed(String errorMessage) {
        Log.d(LOG_TAG, errorMessage);
    }
}
