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