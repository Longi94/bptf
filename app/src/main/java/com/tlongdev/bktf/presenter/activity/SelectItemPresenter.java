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

import android.database.Cursor;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.LoadSelectorItemsInteractor;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.SelectItemView;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public class SelectItemPresenter implements Presenter<SelectItemView>,LoadSelectorItemsInteractor.Callback {

    private SelectItemView mView;
    private BptfApplication mApplication;

    public SelectItemPresenter(BptfApplication application) {
        mApplication = application;
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(SelectItemView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadItems(String query) {
        LoadSelectorItemsInteractor interactor = new LoadSelectorItemsInteractor(
                mApplication, query, this
        );
        interactor.execute();
    }

    @Override
    public void onSelectorItemsLoaded(Cursor items) {
        if (mView != null) {
            mView.showItems(items);
        }
    }
}