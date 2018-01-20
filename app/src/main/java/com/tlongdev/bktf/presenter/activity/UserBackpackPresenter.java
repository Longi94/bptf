package com.tlongdev.bktf.presenter.activity;

import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.LoadBackpackItemsInteractor;
import com.tlongdev.bktf.interactor.Tf2UserBackpackInteractor;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.UserBackpackView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 18.
 */
public class UserBackpackPresenter implements Presenter<UserBackpackView>,LoadBackpackItemsInteractor.Callback, Tf2UserBackpackInteractor.Callback {

    private UserBackpackView mView;

    private final BptfApplication mApplication;

    private boolean isGuest;

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

    public void loadBackpackItems(boolean guest) {
        LoadBackpackItemsInteractor interactor = new LoadBackpackItemsInteractor(
                mApplication, guest, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onLoadBackpackItemFinished(List<BackpackItem> items, List<BackpackItem> newItems) {
        if (mView != null) {
            mView.showItems(items, newItems);
        }
    }

    public void getBackpackItems(String steamId, boolean isGuest) {
        this.isGuest = isGuest;
        Tf2UserBackpackInteractor interactor = new Tf2UserBackpackInteractor(
                mApplication, steamId, isGuest, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onUserBackpackFinished(double rawMetal, int rawKeys, int backpackSlots, int itemCount) {
        loadBackpackItems(isGuest);
    }

    @Override
    public void onPrivateBackpack() {
        if (mView != null) {
            mView.privateBackpack();
        }
    }

    @Override
    public void onUserBackpackFailed(String errorMessage) {

    }
}