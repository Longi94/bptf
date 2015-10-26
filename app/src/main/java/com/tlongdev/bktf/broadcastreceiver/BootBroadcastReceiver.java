package com.tlongdev.bktf.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.service.NotificationsService;
import com.tlongdev.bktf.service.UpdateDatabaseService;

/**
 * Broadcast receiver for starting services on device startup.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    /**
     * Constructor
     */
    public BootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_notification), false)) {
            context.startService(new Intent(context, NotificationsService.class));
        }
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_background_sync), false)) {
            context.startService(new Intent(context, UpdateDatabaseService.class));
        }
    }
}
