package com.tlongdev.bktf.gcm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.tlongdev.bktf.R;

public class BptfInstanceIDListenerService extends InstanceIDListenerService {

    public static final String LOG_TAG = "BptfInstanceIDListener";

    public void onTokenRefresh() {
        Log.v(LOG_TAG, "token refreshed");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Fetch updated Instance ID token and notify of changes
        Intent intent = new Intent(this, GcmRegisterPriceUpdatesService.class);
        intent.putExtra(GcmRegisterPriceUpdatesService.EXTRA_SUBSCRIBE,
                prefs.getBoolean(getString(R.string.pref_registered_topic_price_updates), false));
        startService(intent);
    }
}
