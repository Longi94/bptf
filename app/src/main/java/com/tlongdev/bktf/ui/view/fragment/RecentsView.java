package com.tlongdev.bktf.ui.view.fragment;

import android.support.v4.app.LoaderManager;
import android.database.Cursor;

import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public interface RecentsView extends BaseView {
    void showPrices(Cursor prices);

    void showRefreshAnimation();

    void hideRefreshingAnimation();

    void dismissLoadingDialog();

    void showLoadingDialog(String message);

    void updateLoadingDialog(int max, String message);

    void showItemSchemaError(String errorMessage);

    void showPricesError(String errorMessage);

    void updateCurrencyHeader(Price metalPrice, Price keyPrice, Price budPrice);

    void showErrorDialog();

    LoaderManager getLoaderManager();
}
