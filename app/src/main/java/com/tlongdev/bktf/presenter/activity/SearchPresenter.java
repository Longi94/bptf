package com.tlongdev.bktf.presenter.activity;

import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.LoadSearchItemsInteractor;
import com.tlongdev.bktf.interactor.SearchUserInteractor;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.SearchView;

/**
 * @author Long
 * @since 2016. 03. 22.
 */
public class SearchPresenter implements Presenter<SearchView>,SearchUserInteractor.Callback,
        LoadSearchItemsInteractor.Callback {

    private SearchView mView;
    private final BptfApplication mApplication;

    private String mQuery;
    private SearchUserInteractor mUserInteractor;

    public SearchPresenter(BptfApplication application) {
        mApplication = application;
    }

    @Override
    public void attachView(SearchView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void onUserFound(User user) {
        if (mView != null) {
            mView.userFound(user);
        }
    }

    @Override
    public void onUserNotFound() {
        if (mView != null) {
            mView.userNotFound();
        }
    }

    @Override
    public void onSearchItemsLoaded(Cursor items) {
        if (mView != null) {
            mView.showItems(items);
        }
        mUserInteractor = new SearchUserInteractor(mApplication, mQuery, this);
        mUserInteractor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void search(String query, boolean filter, int filterQuality, boolean filterTradable,
                       boolean filterCraftable, boolean filterAustralium) {
        mQuery = query;

        if (mUserInteractor != null) {
            mUserInteractor.cancel(false);
        }

        LoadSearchItemsInteractor interactor = new LoadSearchItemsInteractor(
                mApplication, mQuery, filter, filterQuality, filterTradable, filterCraftable,
                filterAustralium, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}