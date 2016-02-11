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


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.BuildConfig;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class AboutActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = AboutActivity.class.getSimpleName();

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
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
        }
    };

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

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

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }

        //Re-add actionbar that was removed in recent build tools.
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list)
                .getParent().getParent().getParent();
        View toolbar = LayoutInflater.from(this)
                .inflate(R.layout.settings_toolbar, root, false);
        // insert at top
        root.addView(toolbar, 0);

        setSupportActionBar((Toolbar) toolbar.findViewById(R.id.toolbar));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }


    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_about);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.

        findPreference(getString(R.string.pref_title_feedback)).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //Start an email intent with my email as the target
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto", "dev@tlongdev.com", null));
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(Intent.createChooser(intent, getString(R.string.message_send_email)));
                        }
                        return true;
                    }
                });

        findPreference(getString(R.string.pref_title_rate)).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //Open the Play Store page of the app
                        final String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException e) {
                            //Play store is not present on the phone. Open the browser
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "https://play.google.com/store/apps/details?id=" +
                                            appPackageName)));
                        }
                        return true;
                    }
                });

        //Set the version name to the summary, so I don't have to change it manually every goddamn
        //update
        findPreference(getString(R.string.pref_title_version)).setSummary(BuildConfig.VERSION_NAME);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
