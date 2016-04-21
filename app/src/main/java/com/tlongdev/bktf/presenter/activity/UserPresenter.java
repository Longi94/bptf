package com.tlongdev.bktf.presenter.activity;

import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.GetSearchedUserDataInteractor;
import com.tlongdev.bktf.interactor.Tf2UserBackpackInteractor;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.UserView;

/**
 * @author Long
 * @since 2016. 03. 19.
 */
public class UserPresenter implements Presenter<UserView>,GetSearchedUserDataInteractor.Callback, Tf2UserBackpackInteractor.Callback {

    private UserView mView;
    private final BptfApplication mApplication;
    private User mUser;

    public UserPresenter(BptfApplication application) {
        application.getPresenterComponent().inject(this);
        mApplication = application;
    }

    @Override
    public void attachView(UserView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadData(String steamId) {
        GetSearchedUserDataInteractor interactor = new GetSearchedUserDataInteractor(
                mApplication, steamId, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onUserInfoFinished(User user) {
        mUser = user;
        Tf2UserBackpackInteractor interactor = new Tf2UserBackpackInteractor(
                mApplication, user, true, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onUserInfoFailed(String errorMessage) {
    }

    @Override
    public void onUserBackpackFinished(User user) {
        mUser = user;
        if (mView != null) {
            mView.showData(user);
        }
    }

    @Override
    public void onPrivateBackpack() {
        mView.privateBackpack(mUser);
    }

    @Override
    public void onUserBackpackFailed(String errorMessage) {

    }
}