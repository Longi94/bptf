package com.tlongdev.bktf.presenter.activity;

import android.os.AsyncTask;
import android.util.SparseArray;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.Tf2UserBackpackInteractor;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.UserBackpackView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 18.
 */
public class UserBackpackPresenter implements Presenter<UserBackpackView>,
        Tf2UserBackpackInteractor.Callback {

    private UserBackpackView mView;

    private final BptfApplication mApplication;

    public UserBackpackPresenter(BptfApplication application) {
        application.getPresenterComponent().inject(this);
        mApplication = application;
    }

    @Override
    public void attachView(UserBackpackView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void getBackpackItems(String steamId) {
        Tf2UserBackpackInteractor interactor = new Tf2UserBackpackInteractor(
                mApplication, steamId, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onUserBackpackFinished(List<BackpackItem> newItems, SparseArray<BackpackItem> items,
                                       double rawMetal, int rawKeys, int backpackSlots, int itemCount) {
        if (mView != null) {
            mView.showItems(items, newItems, backpackSlots);
        }
    }

    @Override
    public void onPrivateBackpack() {
        if (mView != null) {
            mView.privateBackpack();
        }
    }

    @Override
    public void onUserBackpackFailed(String errorMessage) {
        if (mView != null) {
            mView.showError(errorMessage);
        }
    }
}