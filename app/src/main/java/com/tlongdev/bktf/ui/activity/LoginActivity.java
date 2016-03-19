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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.interactor.GetUserDataInteractor;
import com.tlongdev.bktf.interactor.Tf2UserBackpackInteractor;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.util.ProfileManager;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements GetUserDataInteractor.Callback, Tf2UserBackpackInteractor.Callback {

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    @Bind(R.id.steam_id) EditText steamIdInput;

    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            // Required because this activity doesn't have a parent activity
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.what_is_id)
    public void showSteamIdInstructions() {
        startActivity(new Intent(this, SteamIdActivity.class));
    }

    @OnClick(R.id.enter)
    public void submit() {
        if (steamIdInput.getText().toString().isEmpty()) {
            steamIdInput.setError("You didn't enter anything!");
        } else {
            String steamId = steamIdInput.getText().toString();

            User user = new User();
            user.setSteamId(steamId);

            GetUserDataInteractor task = new GetUserDataInteractor((BptfApplication) getApplication(),
                    user, true, LoginActivity.this);
            task.execute();

            loadingDialog = ProgressDialog.show(LoginActivity.this, null, "Please wait...", true, false);

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("UserData")
                    .build());
        }
    }

    @Override
    public void onUserInfoFinished(User user) {
        Tf2UserBackpackInteractor task = new Tf2UserBackpackInteractor((BptfApplication) getApplication(), this);
        task.execute(user.getResolvedSteamId());

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Request")
                .setAction("UserBackpack")
                .build());
    }

    @Override
    public void onUserInfoFailed(String errorMessage) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }

        new ProfileManager((BptfApplication) getApplication()).logOut();

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserBackpackFinished() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        finish();
    }

    @Override
    public void onPrivateBackpack() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        finish();
    }

    @Override
    public void onUserBackpackFailed(String errorMessage) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }

        new ProfileManager((BptfApplication) getApplication()).logOut();

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
