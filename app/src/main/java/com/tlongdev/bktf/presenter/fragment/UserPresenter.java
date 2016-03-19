/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.presenter.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.interactor.GetUserDataInteractor;
import com.tlongdev.bktf.interactor.Tf2UserBackpackInteractor;
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
public class UserPresenter implements Presenter<UserView>,GetUserDataInteractor.Callback, SwipeRefreshLayout.OnRefreshListener, Tf2UserBackpackInteractor.Callback {

    @Inject SharedPreferences mPrefs;
    @Inject SharedPreferences.Editor mEditor;
    @Inject Tracker mTracker;
    @Inject Context mContext;
    @Inject ProfileManager mProfileManager;

    private UserView mView;
    private BptfApplication mApplication;
    private boolean mSearchedUser;

    public UserPresenter(BptfApplication application) {
        mApplication = application;
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(UserView view) {
        mView = view;
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

            //Start the task and listne for the end
            GetUserDataInteractor task = new GetUserDataInteractor(mApplication,
                    mProfileManager.getUser(), false, this);
            task.execute();

            mView.showRefreshingAnimation();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("UserData")
                    .build());
        }
    }

    @Override
    public void onUserInfoFinished(User user) {
        Tf2UserBackpackInteractor interactor = new Tf2UserBackpackInteractor(
                mApplication, user, false, this
        );
        interactor.execute();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Request")
                .setAction("UserBackpack")
                .build());
    }

    @Override
    public void onUserInfoFailed(String errorMessage) {
        if (mView != null) {
            mView.hideRefreshingAnimation();
            mView.showToast("bptf: " + errorMessage, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onRefresh() {
        if (Utility.isNetworkAvailable(mContext)) {
            //Start fetching the data and listen for the end
            GetUserDataInteractor fetchTask = new GetUserDataInteractor(mApplication,
                    mProfileManager.getUser(), true, this);
            fetchTask.execute();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("UserData")
                    .build());
        } else {
            //There is no internet connection, notify the user
            mView.showToast("bptf: " + mContext.getString(R.string.error_no_network), Toast.LENGTH_SHORT);
            mView.hideRefreshingAnimation();
        }
    }

    @Override
    public void onUserBackpackFinished(User user) {
        if (mView != null) {
            mView.backpack(false);
            mView.updateUserPage(mProfileManager.getUser());
            mView.hideRefreshingAnimation();
            mView.updateDrawer();
        }
    }

    @Override
    public void onPrivateBackpack() {
        //Save all data that represent a private backpack
        User user = mProfileManager.getUser();
        user.setBackpackSlots(-1);
        user.setItemCount(-1);
        user.setRawKeys(-1);
        user.setRawMetal(-1);
        mProfileManager.saveUser(user);

        if (mView != null) {
            mView.backpack(true);
            mView.updateUserPage(user);
            mView.hideRefreshingAnimation();
            mView.updateDrawer();
        }
    }

    @Override
    public void onUserBackpackFailed(String errorMessage) {
        //Stop the refreshing animation and update the UI
        if (mView != null) {
            mView.updateUserPage(mProfileManager.getUser());
            mView.hideRefreshingAnimation();
            mView.showToast("failed", Toast.LENGTH_SHORT);
        }
    }

    public void setSearchedUser(boolean searchedUser) {
        mSearchedUser = searchedUser;
    }
}