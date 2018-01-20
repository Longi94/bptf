package com.tlongdev.bktf.presenter.fragment;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.model.CurrencyRates;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.fragment.CurrencyView;
import com.tlongdev.bktf.util.CurrencyRatesManager;

/**
 * @author lngtr
 * @since 2016. 05. 09.
 */
public class CurrencyPresenter implements Presenter<CurrencyView>,CurrencyRatesManager.Callback {

    private BptfApplication mApplication;

    private CurrencyView mView;

    private CurrencyRatesManager mManager;

    public CurrencyPresenter(BptfApplication application) {
        mApplication = application;
        mManager = CurrencyRatesManager.getInstance(application);
    }

    @Override
    public void attachView(CurrencyView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void getCurrencyRates() {
        mManager.getCurrencyRates(this);
    }

    @Override
    public void currencyRates(CurrencyRates rates, String errorMessage) {
        if (mView != null) {
            if (rates != null) {
                mView.showCurrencyRates(rates);
            } else {
                mView.showError(errorMessage);
            }
        }
    }
}