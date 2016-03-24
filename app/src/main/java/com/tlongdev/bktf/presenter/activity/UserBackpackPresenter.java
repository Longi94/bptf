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

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.LoadBackpackItemsInteractor;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.UserBackpackView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 18.
 */
public class UserBackpackPresenter implements Presenter<UserBackpackView>,LoadBackpackItemsInteractor.Callback {

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
}