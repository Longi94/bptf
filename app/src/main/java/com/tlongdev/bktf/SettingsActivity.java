package com.tlongdev.bktf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean secretSwitch = true;
    private int secretCounter = 0;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();

        //Re-add actionbar that was removed in recent build tools.
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ViewCompat.setElevation(bar, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()));
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
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_user_developer), false)) {
            addPreferencesFromResource(R.xml.pref_general);
        } else {
            addPreferencesFromResource(R.xml.pref_general_dev);
        }
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_steam_id)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_notification_interval)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sync_interval)));

        findPreference(getString(R.string.pref_feedback_title)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","tlongdev@gmail.com", null));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(intent, "Send email..."));
                }
                return true;
            }
        });

        findPreference(getString(R.string.pref_rate_title)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                return true;
            }
        });

        //Set the version name to the summary, so I don't have to change it manually every goddamn
        //update
        findPreference(getString(R.string.pref_version_title)).setSummary(BuildConfig.VERSION_NAME);


        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_user_developer), false)) {
            findPreference(getString(R.string.pref_version_title)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (secretSwitch) {
                        secretCounter++;
                        secretSwitch = false;
                    }
                    return true;
                }
            });

            findPreference(getString(R.string.pref_developer_title)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (!secretSwitch) {
                        secretCounter++;
                        secretSwitch = true;
                        if (secretCounter == 6){
                            Toast.makeText(SettingsActivity.this, "You're now the developer of this app!", Toast.LENGTH_SHORT).show();
                            findPreference(getString(R.string.pref_developer_title)).setOnPreferenceClickListener(null);
                            findPreference(getString(R.string.pref_version_title)).setOnPreferenceClickListener(null);
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
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return true;
    }

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

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_steam_id))) {

            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (Utility.isSteamId(sharedPreferences.getString(key, ""))) {
                editor.putString(getString(R.string.pref_resolved_steam_id),
                        sharedPreferences.getString(key, ""));
            }

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
            editor.remove(getString(R.string.pref_last_backpack_update));

            editor.apply();

            Intent i = new Intent();
            i.putExtra("preference_changed", true);
            setResult(RESULT_OK, i);
        }
        else if (key.equals(getString(R.string.pref_notification))){
            if (sharedPreferences.getBoolean(key, false)) {
                startService(new Intent(this, NotificationsService.class));
            }
        }
        else if (key.equals(getString(R.string.pref_notification_interval))){
            if (sharedPreferences.getBoolean(getString(R.string.pref_notification), false)) {
                startService(new Intent(this, NotificationsService.class));
            }
        }
        else if (key.equals(getString(R.string.pref_background_sync))){
            if (sharedPreferences.getBoolean(key, false)) {
                startService(new Intent(this, UpdateDatabaseService.class));
            }
        }
        else if (key.equals(getString(R.string.pref_notification_interval))){
            if (sharedPreferences.getBoolean(getString(R.string.pref_background_sync), false)) {
                startService(new Intent(this, UpdateDatabaseService.class));
            }
        }
    }
}
