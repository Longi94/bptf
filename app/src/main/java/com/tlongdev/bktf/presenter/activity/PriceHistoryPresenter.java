package com.tlongdev.bktf.presenter.activity;

import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.BackpackTfPriceHistoryInteractor;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.PriceHistoryView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 23.
 */
public class PriceHistoryPresenter implements Presenter<PriceHistoryView>,BackpackTfPriceHistoryInteractor.Callback {

    private PriceHistoryView mView;

    private final BptfApplication mApplication;

    public PriceHistoryPresenter(BptfApplication application) {
        mApplication = application;
    }

    @Override
    public void attachView(PriceHistoryView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadPriceHistory(Item item) {
        BackpackTfPriceHistoryInteractor interactor = new BackpackTfPriceHistoryInteractor(
                mApplication, item, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onPriceHistoryFinished(List<Price> prices) {
        if (mView != null) {
            mView.showHistory(prices);
        }
    }

    @Override
    public void onPriceHistoryFailed(String errorMessage) {
        if (mView != null) {
            mView.showError(errorMessage);
        }
    }
}
