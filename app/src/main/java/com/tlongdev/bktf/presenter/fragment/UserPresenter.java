package com.tlongdev.bktf.presenter.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.interactor.GetUserDataInteractor;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.fragment.UserView;
import com.tlongdev.bktf.util.ProfileManager;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 15.
 */
public class UserPresenter implements Presenter<UserView>, GetUserDataInteractor.Callback,
        SwipeRefreshLayout.OnRefreshListener {

    @Inject SharedPreferences mPrefs;
    @Inject SharedPreferences.Editor mEditor;
    @Inject Context mContext;
    @Inject ProfileManager mProfileManager;

    private UserView mView;
    private final BptfApplication mApplication;
    private boolean mSearchedUser;

    private boolean mLoading = false;

    public UserPresenter(BptfApplication application) {
        mApplication = application;
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(UserView view) {
        mView = view;

        if (mView != null && mLoading) {
            mView.showRefreshingAnimation();
        }
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void getUserDataIfNeeded() {
        User user = mProfileManager.getUser();
        //Update user info if last update was more than 30 minutes ago
        if (!mSearchedUser && Utility.isNetworkAvailable(mContext) && System.currentTimeMillis()
                - user.getLastUpdated() >= 3600000L) {

            //Start the task and listen for the end
            getUserData(false);
            mView.showRefreshingAnimation();
        }
    }

    @Override
    public void onUserInfoFinished(User user) {
        mLoading = false;
        if (mView != null) {
            mView.updateUserPage(mProfileManager.getUser());
            mView.hideRefreshingAnimation();
        }
    }

    @Override
    public void onUserInfoFailed(String errorMessage) {
        mLoading = false;
        if (mView != null) {
            mView.hideRefreshingAnimation();
            mView.showToast("bptf: " + errorMessage, Toast.LENGTH_SHORT);
        }
    }

    private void getUserData(boolean isGuest) {
        //Start fetching the data and listen for the end
        GetUserDataInteractor interactor = new GetUserDataInteractor(mApplication,
                mProfileManager.getUser(), isGuest, this);
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mLoading = true;
    }

    @Override
    public void onRefresh() {
        if (Utility.isNetworkAvailable(mContext)) {
            getUserData(true);
        } else {
            //There is no internet connection, notify the user
            mView.showToast("bptf: " + mContext.getString(R.string.error_no_network), Toast.LENGTH_SHORT);
            mView.hideRefreshingAnimation();
        }
    }

    public void setSearchedUser(boolean searchedUser) {
        mSearchedUser = searchedUser;
    }
}