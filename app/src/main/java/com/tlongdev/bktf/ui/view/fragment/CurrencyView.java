package com.tlongdev.bktf.ui.view.fragment;

import com.tlongdev.bktf.model.CurrencyRates;
import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author lngtr
 * @since 2016. 05. 09.
 */
public interface CurrencyView extends BaseView {
    void showCurrencyRates(CurrencyRates rates);

    void showError(String errorMessage);
}