package com.tlongdev.bktf.presenter.activity;

import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.LoadUnusualsInteractor;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.UnusualView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public class UnusualPresenter implements Presenter<UnusualView>,LoadUnusualsInteractor.Callback {

    private UnusualView mView;
    private final BptfApplication mApplication;

    public UnusualPresenter(BptfApplication application) {
        application.getPresenterComponent().inject(this);
        mApplication = application;
    }

    @Override
    public void attachView(UnusualView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadUnusuals(int defindex, int index, String filter) {
        LoadUnusualsInteractor interactor = new LoadUnusualsInteractor(
                mApplication, defindex, index, filter, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onUnusualsLoadFinished(List<Item> items) {
        if (mView != null) {
            mView.showUnusuals(items);
        }
    }
}