package com.tlongdev.bktf.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class BptfInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify of changes
        Intent intent = new Intent(this, GcmRegisterPriceUpdatesService.class);
        startService(intent);
    }
}
