package com.tlongdev.bktf.activity;

import android.os.Bundle;

import com.tlongdev.bktf.R;

/**
 * A vary simple preference activity for giving credits for used libraries, sources etc.
 */
public class CreditsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_credits);
    }
}
