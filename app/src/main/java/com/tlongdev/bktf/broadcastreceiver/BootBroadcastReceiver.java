package com.tlongdev.bktf.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver for starting services on device startup.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = BootBroadcastReceiver.class.getSimpleName();

    /**
     * Constructor
     */
    public BootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    }
}
