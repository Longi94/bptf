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

package com.tlongdev.bktf.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.tlongdev.bktf.network.GetUserBackpack;
import com.tlongdev.bktf.network.GetUserInfo;
import com.tlongdev.bktf.util.Profile;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements GetUserInfo.OnUserInfoListener, GetUserBackpack.OnUserBackpackListener {

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

            GetUserInfo task = new GetUserInfo(LoginActivity.this, true);
            task.registerFetchUserInfoListener(LoginActivity.this);
            task.execute(steamId, null);

            loadingDialog = ProgressDialog.show(LoginActivity.this, null, "Please wait...", true, false);

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("UserData")
                    .build());
        }
    }

    @Override
    public void onUserInfoFinished(String steamId) {

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putLong(getString(R.string.pref_last_user_data_update),
                        System.currentTimeMillis()).apply();

        GetUserBackpack task = new GetUserBackpack(this);
        task.registerOnFetchUserBackpackListener(this);
        task.execute(steamId);

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

        Profile.logOut(this);

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserBackpackFinished(int rawKeys, double rawMetal, int backpackSlots, int itemNumber) {

        //Start editing the preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        //Save all the data passed
        editor.putInt(getString(R.string.pref_user_slots), backpackSlots);
        editor.putInt(getString(R.string.pref_user_items), itemNumber);
        editor.putInt(getString(R.string.pref_user_raw_key), rawKeys);
        Utility.putDouble(editor, getString(R.string.pref_user_raw_metal), rawMetal);

        editor.putString(getString(R.string.pref_steam_id), steamIdInput.getText().toString());
        editor.apply();

        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }

        finish();
    }

    @Override
    public void onPrivateBackpack() {
        //Start editing the preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        //Save all data that represent a private backpack
        editor.putInt(getString(R.string.pref_user_slots), -1);
        editor.putInt(getString(R.string.pref_user_items), -1);
        editor.putInt(getString(R.string.pref_user_raw_key), -1);
        Utility.putDouble(editor, getString(R.string.pref_user_raw_metal), -1);
        Utility.putDouble(editor, getString(R.string.pref_player_backpack_value_tf2), -1);

        editor.putString(getString(R.string.pref_steam_id), steamIdInput.getText().toString());
        editor.apply();

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

        Profile.logOut(this);

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
