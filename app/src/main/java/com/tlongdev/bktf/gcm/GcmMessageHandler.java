package com.tlongdev.bktf.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by Long on 2015. 12. 18..
 */
public class GcmMessageHandler extends GcmListenerService {

    public static final String LOG_TAG = GcmMessageHandler.class.getSimpleName();

    public void onMessageReceived(String from, Bundle data) {
        Log.d(LOG_TAG, "Message received");

        if (from.startsWith("/topics/")) {
            // TODO: 2015. 12. 18. handle stuff
        } else {
            // normal downstream message.
        }
    }
}
