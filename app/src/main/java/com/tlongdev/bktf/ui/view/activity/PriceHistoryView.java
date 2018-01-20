package com.tlongdev.bktf.ui.view.activity;

import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.ui.view.BaseView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 23.
 */
public interface PriceHistoryView extends BaseView {
    void showHistory(List<Price> prices);

    void showError(String errorMessage);
}
