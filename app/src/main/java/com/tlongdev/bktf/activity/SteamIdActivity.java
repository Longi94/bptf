package com.tlongdev.bktf.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.webkit.WebView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SteamIdActivity extends AppCompatActivity {

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    @Bind(R.id.web_view) WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steam_id);
        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView.loadUrl("http://tlongdev.com/");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
