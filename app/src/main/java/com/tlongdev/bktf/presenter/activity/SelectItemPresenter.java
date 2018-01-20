package com.tlongdev.bktf.presenter.activity;

import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.LoadSelectorItemsInteractor;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.SelectItemView;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public class SelectItemPresenter implements Presenter<SelectItemView>,LoadSelectorItemsInteractor.Callback {

    private SelectItemView mView;
    private final BptfApplication mApplication;

    public SelectItemPresenter(BptfApplication application) {
        mApplication = application;
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(SelectItemView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadItems(String query) {
        LoadSelectorItemsInteractor interactor = new LoadSelectorItemsInteractor(
                mApplication, query, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onSelectorItemsLoaded(Cursor items) {
        if (mView != null) {
            mView.showItems(items);
        }
    }
}