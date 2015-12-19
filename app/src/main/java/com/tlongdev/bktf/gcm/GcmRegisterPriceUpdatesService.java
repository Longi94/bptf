package com.tlongdev.bktf.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.tlongdev.bktf.R;

import java.io.IOException;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GcmRegisterPriceUpdatesService extends IntentService {

    public static final String TAG = "GcmPriceUpdatesService";

    public static final String EXTRA_SUBSCRIBE = "sub_or_unsub";

    public GcmRegisterPriceUpdatesService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        InstanceID instanceID = InstanceID.getInstance(this);
        String senderId = getResources().getString(R.string.gcm_sender_id);

        try {
            // request token that will be used by the server to send push notifications
            String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            Log.d(TAG, "GCM Registration Token: " + token);

            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            if (intent.getBooleanExtra(EXTRA_SUBSCRIBE, false)) {
                pubSub.subscribe(token, "/topics/price_updates", null);
                prefs.edit().putBoolean(getString(R.string.pref_registered_topic_price_updates), true)
                        .apply();
                Log.d(TAG, "Registered to /topics/price_updates");
            } else {
                pubSub.unsubscribe(token, "/topics/price_updates");
                prefs.edit().putBoolean(getString(R.string.pref_registered_topic_price_updates), false)
                        .apply();
                Log.d(TAG, "Unregistered from /topics/price_updates");
            }
        } catch (IOException e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            e.printStackTrace();
        }
    }
}
