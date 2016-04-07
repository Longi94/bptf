package com.tlongdev.bktf.ui.view;

import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;

/**
 * @author longi
 * @since 2016.03.30.
 */
public class AppearAdListener extends AdListener {

    private AdView mAdView;

    public AppearAdListener(AdView adView) {
        mAdView = adView;
    }

    @Override
    public void onAdLoaded() {
        super.onAdLoaded();
        mAdView.setVisibility(View.VISIBLE);
    }
}
