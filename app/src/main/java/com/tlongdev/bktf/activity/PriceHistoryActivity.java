package com.tlongdev.bktf.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.network.GetPriceHistory;
import com.tlongdev.bktf.util.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    public static final String EXTRA_CURRENCY = "currency";

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    private Item mItem;

    /**
     * Views
     */
    private View mainContent;
    private ProgressBar progressBar;
    private LineChart historyChart;

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

        final String currency = i.getStringExtra(EXTRA_CURRENCY);

        mItem = new Item(
                i.getIntExtra(EXTRA_DEFINDEX, 0),
                null,
                i.getIntExtra(EXTRA_QUALITY, 0),
                i.getBooleanExtra(EXTRA_TRADABLE, true),
                i.getBooleanExtra(EXTRA_CRAFTABLE, true),
                false,
                i.getIntExtra(EXTRA_PRICE_INDEX, 0),
                new Price(0, currency)
        );

        GetPriceHistory task = new GetPriceHistory(this, mItem);
        task.setListener(this);
        task.execute();

        mainContent = findViewById(R.id.main_content);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        historyChart = (LineChart) findViewById(R.id.history_chart);

        historyChart.setLogEnabled(true);

        historyChart.setDescription(null);

        historyChart.setDrawGridBackground(false);

        historyChart.setTouchEnabled(true);

        historyChart.setDragEnabled(true);
        historyChart.setScaleYEnabled(false);
        historyChart.setScaleXEnabled(true);

        historyChart.setPinchZoom(true);

        historyChart.getAxisRight().setEnabled(false);
        historyChart.getLegend().setEnabled(false);

        int textColor = Utility.getColor(this, R.color.text_primary);

        XAxis xAxis = historyChart.getXAxis();
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(textColor);

        YAxis yAxis = historyChart.getAxisLeft();
        yAxis.setTextColor(textColor);
        yAxis.setStartAtZero(false);
        yAxis.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return String.format("%s %s", Utility.roundFloat(value, 2), currency);
            }
        });
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
        buildChart(prices);
        mainContent.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPriceHistoryFailed(String errorMessage) {
        Log.d(LOG_TAG, errorMessage);
    }

    private static Comparator<Price> priceAgeComparator = new Comparator<Price>() {
        @Override
        public int compare(Price lhs, Price rhs) {
            if (lhs.getLastUpdate() > rhs.getLastUpdate()) {
                return 1;
            } else if (lhs.getLastUpdate() < rhs.getLastUpdate()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    private void buildChart(List<Price> prices) {
        long first = Collections.min(prices, priceAgeComparator).getLastUpdate();
        long last = Collections.max(prices, priceAgeComparator).getLastUpdate();

        long days = TimeUnit.MILLISECONDS.toDays(last - first);

        int textColor = Utility.getColor(this, R.color.text_primary);

        //Setup the X axis of the chart
        ArrayList<String> xValues = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (long day = 0; day < days * 1.1; day++) {
            xValues.add(dateFormat.format(new Date(first + (day * 86400000L))));
        }

        if (prices.size() > 0) {
            ArrayList<Entry> entries = new ArrayList<>();

            for (Price price : prices) {

                int day = (int)TimeUnit.MILLISECONDS.toDays(price.getLastUpdate() - first);
                entries.add(new Entry((float)price.getConvertedPrice(this, mItem.getPrice().getCurrency(), false), day));

            }

            LineDataSet set = new LineDataSet(entries, mItem.getPrice().getCurrency());

            set.setColor(textColor);
            set.setLineWidth(2.0f);
            set.setCircleSize(2.2f);
            set.setCircleColor(textColor);
            set.setHighLightColor(textColor);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setDrawCubic(true);
            set.setCubicIntensity(0.1f);
            set.setDrawValues(false);

            //Add data to the chart
            LineData data = new LineData(xValues, set);

            historyChart.setData(data);
        }

        historyChart.notifyDataSetChanged();
        historyChart.animateX(2500, Easing.EasingOption.EaseOutCubic);
    }
}
