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

package com.tlongdev.bktf.presenter.activity;

import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.GetUserDataInteractor;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.LoginView;
import com.tlongdev.bktf.util.ProfileManager;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 23.
 */
public class LoginPresenter implements Presenter<LoginView>, GetUserDataInteractor.Callback {

    @Inject Tracker mTracker;
    @Inject ProfileManager mProfileManager;

    private LoginView mView;

    private final BptfApplication mApplication;

    public LoginPresenter(BptfApplication application) {
        mApplication = application;
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(LoginView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void login(String id) {
        User user = new User();
        user.setSteamId(id);

        GetUserDataInteractor interactor = new GetUserDataInteractor(mApplication, user, true, this);
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Request")
                .setAction("UserData")
                .build());
    }

    @Override
    public void onUserInfoFinished(User user) {
        if (mView != null) {
            mView.dismissDialog();
            mView.finish();
        }
    }

    @Override
    public void onUserInfoFailed(String errorMessage) {
        mProfileManager.logOut();
        if (mView != null) {
            mView.dismissDialog();
            mView.showToast(errorMessage, Toast.LENGTH_SHORT);
        }
    }
}
