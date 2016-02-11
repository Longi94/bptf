/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
