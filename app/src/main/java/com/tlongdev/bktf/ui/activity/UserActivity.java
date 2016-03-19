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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.ui.fragment.UserFragment;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Profile page activity.
 */
public class UserActivity extends AppCompatActivity {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = UserActivity.class.getSimpleName();

    //Keys for extra data in the intent.
    public static final String STEAM_ID_KEY = "steamid";
    public static final String JSON_USER_SUMMARIES_KEY = "json_user_summaries";

    /**
     * The {@link Tracker} used to record screen views.
     */
    @Inject Tracker mTracker;

    //Progress bar that indicates downloading user data.
    @Bind(R.id.progress_bar) ProgressBar progressBar;

    //Steam id of the player, whose profile page is currently shown.
    private String steamId;

    private UserFragment mUserFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);

        mUserFragment = (UserFragment) getSupportFragmentManager().findFragmentById(R.id.user_fragment);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        application.getActivityComponent().inject(this);

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent i = getIntent();

        //Retrieve steamId from intent
        steamId = i.getStringExtra(STEAM_ID_KEY);
        Log.d(LOG_TAG, "steamID: " + steamId);

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Request")
                .setAction("UserDataGuest")
                .build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}