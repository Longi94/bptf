package com.tlongdev.bktf.ui;

import android.database.Cursor;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public interface RecentsView extends BaseView {
    void showPrices(Cursor prices);

    void showError();

    void showRefreshAnimation();

    void hideRefreshingAnimation();

    void finishActivity();

    void updateCurrencyHeader();

    void dismissLoadingDialog();

    void showLoadingDialog(String message);

    void updateLoadingDialog(int max, String message);

    void showItemSchemaError(String errorMessage);

    void showPricesError(String errorMessage);
}
