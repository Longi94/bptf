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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tlongdev.bktf.BuildConfig;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.service.NotificationsService;
import com.tlongdev.bktf.service.UpdateDatabaseService;

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
@SuppressWarnings("deprecation")
public class SettingsActivity extends AppCompatPreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
            = new Preference.OnPreferenceChangeListener() {
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

    //These variables are used for the hidden developer options
    private boolean secretSwitch = true;
    private int secretCounter = 0;

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

    /**
     * {@inheritDoc}
     */
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
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        if (!PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_user_developer), false)) {
            addPreferencesFromResource(R.xml.pref_general);
        } else {
            addPreferencesFromResource(R.xml.pref_general_dev);
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_notification_interval)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sync_interval)));

        findPreference(getString(R.string.pref_title_feedback)).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //Start an email intent with my email as the target
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto", "tlongdev@gmail.com", null));
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

        findPreference(getString(R.string.pref_title_translate)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.link_one_sky))));
                return true;
            }
        });

        findPreference(getString(R.string.pref_title_changelog)).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //Open the GitHub changelog page in the browser
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                getString(R.string.link_github_help))));
                        return true;
                    }
                });

        //Set the version name to the summary, so I don't have to change it manually every goddamn
        //update
        findPreference(getString(R.string.pref_title_version)).setSummary(BuildConfig.VERSION_NAME);

        //Show the developer options when activated
        if (!PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_user_developer), false)) {
            findPreference(getString(R.string.pref_title_version)).setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            if (secretSwitch) {
                                secretCounter++;
                                secretSwitch = false;
                            }
                            return true;
                        }
                    });

            findPreference(getString(R.string.pref_title_developer)).setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            if (!secretSwitch) {
                                secretCounter++;
                                secretSwitch = true;
                                if (secretCounter == 6) {
                                    //Show a toast that the user revealed the developer options.
                                    Toast.makeText(SettingsActivity.this,
                                            "You're now the developer of this app!", Toast.LENGTH_SHORT)
                                            .show();
                                    findPreference(getString(R.string.pref_title_developer)).setOnPreferenceClickListener(null);
                                    findPreference(getString(R.string.pref_title_version)).setOnPreferenceClickListener(null);
                                    PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).
                                            edit().putBoolean(getString(R.string.pref_user_developer), true).apply();
                                }
                            }
                            return true;
                        }
                    });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_steam_id))) {

            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (Utility.isSteamId(sharedPreferences.getString(key, ""))) {
                //If the user entered the 64bit steamId, save it
                editor.putString(getString(R.string.pref_resolved_steam_id),
                        sharedPreferences.getString(key, ""));
            }

            //Remove all data assosiaged with the previous id
            editor.remove(getString(R.string.pref_player_avatar_url));
            editor.remove(getString(R.string.pref_player_name));
            editor.remove(getString(R.string.pref_player_reputation));
            editor.remove(getString(R.string.pref_player_profile_created));
            editor.remove(getString(R.string.pref_player_state));
            editor.remove(getString(R.string.pref_player_last_online));
            editor.remove(getString(R.string.pref_player_banned));
            editor.remove(getString(R.string.pref_player_scammer));
            editor.remove(getString(R.string.pref_player_economy_banned));
            editor.remove(getString(R.string.pref_player_vac_banned));
            editor.remove(getString(R.string.pref_player_community_banned));
            editor.remove(getString(R.string.pref_player_backpack_value_tf2));
            editor.remove(getString(R.string.pref_new_avatar));
            editor.remove(getString(R.string.pref_last_user_data_update));
            editor.remove(getString(R.string.pref_resolved_steam_id));
            editor.remove(getString(R.string.pref_player_trust_negative));
            editor.remove(getString(R.string.pref_player_trust_positive));
            editor.remove(getString(R.string.pref_user_raw_key));
            editor.remove(getString(R.string.pref_user_raw_metal));
            editor.remove(getString(R.string.pref_user_slots));
            editor.remove(getString(R.string.pref_user_items));

            editor.apply();

            //Inform the main activity, that the steam is has been changed
            Intent i = new Intent();
            i.putExtra("preference_changed", true);
            setResult(RESULT_OK, i);
        }

        //Start the appropiate services if these settings have been changed
        else if (key.equals(getString(R.string.pref_notification))) {
            if (sharedPreferences.getBoolean(key, false)) {
                startService(new Intent(this, NotificationsService.class));
            }
        } else if (key.equals(getString(R.string.pref_notification_interval))) {
            if (sharedPreferences.getBoolean(getString(R.string.pref_notification), false)) {
                startService(new Intent(this, NotificationsService.class));
            }
        } else if (key.equals(getString(R.string.pref_background_sync))) {
            if (sharedPreferences.getBoolean(key, false)) {
                startService(new Intent(this, UpdateDatabaseService.class));
            }
        } else if (key.equals(getString(R.string.pref_notification_interval))) {
            if (sharedPreferences.getBoolean(getString(R.string.pref_background_sync), false)) {
                startService(new Intent(this, UpdateDatabaseService.class));
            }
        }
    }
}
