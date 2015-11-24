package com.tlongdev.bktf.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;
import com.tlongdev.bktf.network.GetPriceList;

/**
 * Service for updating the database in the background
 */
public class UpdateDatabaseService extends Service {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = UpdateDatabaseService.class.getSimpleName();

    //Intent extra keys for indicating the need for the update
    private static final String UPDATE_DATABASE = "update";

    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        mTracker = ((BptfApplication) getApplication()).getDefaultTracker();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Only update the database if the service was scheduled to start by itself and the option
        //is set by the user.
        if (intent.hasExtra(UPDATE_DATABASE) &&
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(getString(R.string.pref_notification), false)
                && Utility.isNetworkAvailable(this)) {

            //Start updating the database
            new GetPriceList(this, true, true).execute();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("RefreshBackground")
                    .setLabel("Prices")
                    .build());
        }

        //Stop the service
        stopSelf();
        return START_NOT_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        //It's a started service, so this is unused
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        //When the service stops schedule the service to start after the set time
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_background_sync), false)) {
            //Use alarm manager for scheduling
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

            //The amount of time to wait
            long delay = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.pref_sync_interval), "86400000"));

            //The intent that will be given to the service
            Intent intent = new Intent(this, UpdateDatabaseService.class);
            intent.putExtra(UPDATE_DATABASE, true);

            //Schedule
            alarm.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + delay,
                    PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            );
        }
    }
}
