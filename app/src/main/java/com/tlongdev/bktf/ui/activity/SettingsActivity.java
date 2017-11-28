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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.presenter.activity.SettingsPresenter;
import com.tlongdev.bktf.ui.view.activity.SettingsView;
import com.tlongdev.bktf.util.ProfileManager;

import javax.inject.Inject;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends AppCompatPreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, SettingsView {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    @Inject SettingsPresenter mPresenter;

    private ProgressDialog mLoadingDialog;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private final static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
            = (preference, value) -> {
                String stringValue = value.toString();

                if (preference instanceof ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.setSummary(stringValue);
                }
                return true;
            };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication.getActivityComponent().inject(this);

        //Re-add actionbar that was removed in recent build tools.
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list)
                .getParent().getParent().getParent();
        View toolbar = LayoutInflater.from(this)
                .inflate(R.layout.settings_toolbar, root, false);
        // insert at top
        root.addView(toolbar, 0);

        setSupportActionBar((Toolbar) toolbar.findViewById(R.id.toolbar));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_auto_sync)));

        updateLoginPreference();

        final SwitchPreference notifPref = (SwitchPreference) findPreference(getString(R.string.pref_price_notification));
        notifPref.setEnabled(!prefs.getString(getString(R.string.pref_auto_sync), "1").equals("0"));

        findPreference(getString(R.string.pref_auto_sync)).setOnPreferenceChangeListener((preference, newValue) -> {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, newValue);

            String value = (String) newValue;
            notifPref.setEnabled(!value.equals("0"));

            if (prefs.getBoolean(getString(R.string.pref_registered_topic_price_updates), false) == value.equals("0")) {
                if (!value.equals("0")) {
                    FirebaseMessaging.getInstance().subscribeToTopic("/topics/price_updates");
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/price_updates");
                }
            }

            return true;
        });

        findPreference(getString(R.string.pref_key_login)).setOnPreferenceClickListener(preference -> {
            ProfileManager manager = ProfileManager.getInstance(getApplication());
            if (manager.isSignedIn()) {
                manager.logOut();
                updateLoginPreference();
                Intent i = new Intent();
                i.putExtra("login_changed", true);
                setResult(RESULT_OK, i);
            } else {
                startActivityForResult(new Intent(SettingsActivity.this, WebLoginActivity.class), 0);
            }
            return true;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Start the appropriate services if these settings have been changed
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mPresenter.login(data.getStringExtra(WebLoginActivity.EXTRA_STEAM_ID));
            mLoadingDialog = ProgressDialog.show(this, null, "Please wait...", true, false);
        } else {
            updateLoginPreference();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateLoginPreference() {
        ProfileManager manager = ProfileManager.getInstance(getApplication());
        Preference login = findPreference(getString(R.string.pref_key_login));
        if (manager.isSignedIn()) {
            login.setTitle("Log out");
            login.setSummary(manager.getSteamId());
        } else {
            login.setTitle("Log in");
            login.setSummary(null);
        }
    }

    @Override
    public void dismissDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void userInfoDownloaded() {
        updateLoginPreference();
        Intent i = new Intent();
        i.putExtra("login_changed", true);
        setResult(RESULT_OK, i);
    }

    @Override
    public void showToast(CharSequence message, int duration) {
        Toast.makeText(this, message, duration).show();
    }
}
