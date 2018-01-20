package com.tlongdev.bktf.presenter.fragment;

import android.os.AsyncTask;
import android.support.annotation.IntDef;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.LoadUnusualEffectsInteractor;
import com.tlongdev.bktf.interactor.LoadUnusualHatCategoriesInteractor;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.fragment.UnusualView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class UnusualPresenter implements Presenter<UnusualView>,LoadUnusualHatCategoriesInteractor.Callback, LoadUnusualEffectsInteractor.Callback {

    private final BptfApplication mApplication;

    private UnusualView mView;

    public UnusualPresenter(BptfApplication application) {
        mApplication = application;
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(UnusualView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadUnusualHats(String filter, @UnusualOrder int orderBy) {
        LoadUnusualHatCategoriesInteractor interactor = new LoadUnusualHatCategoriesInteractor(
                mApplication, filter, orderBy, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void loadUnusualEffects(String filter, @UnusualOrder int orderBy) {
        LoadUnusualEffectsInteractor interactor = new LoadUnusualEffectsInteractor(
                mApplication, filter, orderBy, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onUnusualHatsLoadFinished(List<Item> items) {
        if (mView != null) {
            mView.showUnusualHats(items);
        }
    }

    @Override
    public void onUnusualEffectsLoadFinished(List<Item> items) {
        if (mView != null) {
            mView.showUnusualEffects(items);
        }
    }

    @IntDef({ORDER_BY_NAME, ORDER_BY_PRICE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UnusualOrder{}

    public static final int ORDER_BY_NAME = 0;
    public static final int ORDER_BY_PRICE = 1;
}