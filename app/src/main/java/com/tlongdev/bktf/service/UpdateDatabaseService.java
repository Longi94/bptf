package com.tlongdev.bktf.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.task.FetchPriceList;

public class UpdateDatabaseService extends Service {
    private static final String UPDATE_DATABASE = "update";

    public UpdateDatabaseService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("notification") &&
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_notification), false)) {

            new FetchPriceList(this, true, true, null, null).execute(getResources().getString(R.string.backpack_tf_api_key));
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_background_sync), false)) {
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            long delay = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.pref_sync_interval), "86400000"));
            Intent intent = new Intent(this, UpdateDatabaseService.class);
            intent.putExtra(UPDATE_DATABASE, true);
            alarm.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + delay,
                    PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            );
        }
    }
}
