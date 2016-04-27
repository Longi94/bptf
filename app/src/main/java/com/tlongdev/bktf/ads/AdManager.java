package com.tlongdev.bktf.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.ui.view.AppearAdListener;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Long
 * @since 2016. 04. 20.
 */
public class AdManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context mContext;

    private boolean mAdsEnabled;

    private boolean mInit = true;

    private Set<AdView> mAdViews = new HashSet<>();

    public AdManager(Context context) {
        mContext = context;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);

        mAdsEnabled = prefs.getBoolean(mContext.getString(R.string.pref_ads_enabled), true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(mContext.getString(R.string.pref_ads_enabled))) {
            mAdsEnabled = sharedPreferences.getBoolean(
                    mContext.getString(R.string.pref_ads_enabled), true
            );

            for (AdView adView : mAdViews) {
                adView.setVisibility(mAdsEnabled ? View.VISIBLE : View.GONE);

                if (mAdsEnabled && !((AppearAdListener)adView.getAdListener()).isLoaded()) {
                    adView.loadAd(new AdRequest.Builder().build());
                }
            }
        }
    }

    public void addAdView(final AdView adView) {
        mAdViews.add(adView);
        adView.setAdListener(new AppearAdListener(adView));
        if (mAdsEnabled) {
            if (!mInit) {
                adView.loadAd(new AdRequest.Builder().build());
            } else {
                mInit = false;
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AdRequest adRequest = new AdRequest.Builder().build();
                        adView.loadAd(adRequest);
                    }
                }, 1000);
            }
        }
    }

    public void removeAdView(AdView adView) {
        mAdViews.remove(adView);
    }
}
