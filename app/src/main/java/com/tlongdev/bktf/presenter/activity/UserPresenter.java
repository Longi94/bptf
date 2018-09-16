package com.tlongdev.bktf.presenter.activity;

import android.os.AsyncTask;
import android.widget.Toast;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.GetSearchedUserDataInteractor;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.UserView;

/**
 * @author Long
 * @since 2016. 03. 19.
 */
public class UserPresenter implements Presenter<UserView>, GetSearchedUserDataInteractor.Callback {

    private UserView mView;
    private final BptfApplication mApplication;

    private boolean mLoaded = false;
    private String mSteamId = "";

    private User mUserCache;

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
        if (!steamId.equals(mSteamId) || mUserCache == null) {
            mLoaded = false;
            mSteamId = steamId;
        }

        if (!mLoaded) {
            GetSearchedUserDataInteractor interactor = new GetSearchedUserDataInteractor(
                    mApplication, steamId, this
            );
            interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mLoaded = true;
        } else {
            onUserInfoFinished(mUserCache);
        }
    }

    @Override
    public void onUserInfoFinished(User user) {
        mUserCache = user;
        if (mView != null) {
            mView.showData(user);
        }
    }

    @Override
    public void onSteamInfo(User user) {
        if (mView != null) {
            mView.showPartial(user);
        }
    }

    @Override
    public void onUserInfoFailed(String errorMessage) {
        mLoaded = false;
        if (mView != null) {
            mView.showError();
            mView.showToast(errorMessage, Toast.LENGTH_SHORT);
        }
    }
}