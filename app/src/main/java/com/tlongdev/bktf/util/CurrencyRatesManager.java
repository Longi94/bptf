package com.tlongdev.bktf.util;

import com.tlongdev.bktf.BptfApplication;

/**
 * @author lngtr
 * @since 2016. 05. 09.
 */
public class CurrencyRatesManager {
    private static CurrencyRatesManager ourInstance;

    public static CurrencyRatesManager getInstance(BptfApplication application) {
        if (ourInstance == null) {
            ourInstance = new CurrencyRatesManager(application);
        }
        return ourInstance;
    }

    private BptfApplication mApplication;

    private CurrencyRatesManager(BptfApplication application) {
        mApplication = application;
    }
}
