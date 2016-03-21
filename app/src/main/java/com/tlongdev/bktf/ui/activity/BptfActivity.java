package com.tlongdev.bktf.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public abstract class BptfActivity extends AppCompatActivity {

    @Inject Tracker mTracker;

    protected BptfApplication mApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (BptfApplication) getApplication();
        mApplication.getActivityComponent().inject(this);

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }
    }
}
