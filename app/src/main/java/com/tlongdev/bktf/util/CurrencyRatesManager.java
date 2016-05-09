/**
 * Copyright 2016 Long Tran
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

package com.tlongdev.bktf.util;

import android.content.SharedPreferences;
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

    private static final long SIX_HOURS = 21600000L;

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

    private void getCurrencyRates(final Callback callback) {

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

        mFixerIoCallback.setCallback(callback);
        GetCurrencyExchangeRatesInteractor interactor = new GetCurrencyExchangeRatesInteractor(
                mApplication, mFixerIoCallback
        );
        interactor.execute();
    }

    public interface Callback {
        void currencyRates(CurrencyRates rates, String errorMessage);
    }

    private class FixerIoCallback implements GetCurrencyExchangeRatesInteractor.Callback {

        private Callback mCallback;

        @Override
        public void onGetCurrencyExchangeRatesFinished(Map<String, Double> rates) {
            mCache.setRates(rates);
            mCache.setLastUpdate(System.currentTimeMillis());
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
