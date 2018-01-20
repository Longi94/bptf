package com.tlongdev.bktf.util;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.interactor.GetCurrencyExchangeRatesInteractor;
import com.tlongdev.bktf.model.CurrencyRates;

import java.util.Map;

/**
 * @author lngtr
 * @since 2016. 05. 09.
 */
public class CurrencyRatesManager {

    public static final long SIX_HOURS = 21600000L;

    private static CurrencyRatesManager ourInstance;

    public static CurrencyRatesManager getInstance(BptfApplication application) {
        if (ourInstance == null) {
            ourInstance = new CurrencyRatesManager(application);
        }
        return ourInstance;
    }

    private BptfApplication mApplication;
    private Gson mGson;
    private SharedPreferences mPrefs;

    private CurrencyRates mCache;

    private FixerIoCallback mFixerIoCallback = new FixerIoCallback();

    private CurrencyRatesManager(@NonNull BptfApplication application) {
        mApplication = application;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(application);
        mGson = new Gson();
    }

    public void getCurrencyRates(final Callback callback) {

        if (mCache == null) {
            String json = mPrefs.getString(mApplication.getString(R.string.pref_currency_rates), null);
            if (json != null) {
                mCache = mGson.fromJson(json, CurrencyRates.class);
            }
        }

        if (mCache != null && System.currentTimeMillis() - mCache.getLastUpdate() < SIX_HOURS) {
            if (callback != null) {
                callback.currencyRates(mCache, null);
            }
            return;
        }

        if (Utility.isNetworkAvailable(mApplication)) {
            mFixerIoCallback.setCallback(callback);
            GetCurrencyExchangeRatesInteractor interactor = new GetCurrencyExchangeRatesInteractor(
                    mApplication, mFixerIoCallback
            );
            interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            if (callback != null) {
                callback.currencyRates(mCache, null);
            }
        }
    }

    public interface Callback {
        void currencyRates(CurrencyRates rates, String errorMessage);
    }

    private class FixerIoCallback implements GetCurrencyExchangeRatesInteractor.Callback {

        private Callback mCallback;

        @Override
        public void onGetCurrencyExchangeRatesFinished(Map<String, Double> rates) {
            if (mCache == null) {
                mCache = new CurrencyRates();
            }
            mCache.setRates(rates);
            mCache.setLastUpdate(System.currentTimeMillis());

            mPrefs.edit().putString(mApplication.getString(R.string.pref_currency_rates),
                    mGson.toJson(mCache)).apply();

            if (mCallback != null) {
                mCallback.currencyRates(mCache, null);
            }
        }

        @Override
        public void onGetCurrencyExchangeRatesFailed(String errorMessage) {
            if (mCallback != null) {
                mCallback.currencyRates(mCache, errorMessage);
            }
        }

        public void setCallback(Callback callback) {
            mCallback = callback;
        }
    }
}
