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
    private BptfApplication mApplication;

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
        mUserInteractor.execute();
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
        interactor.execute();
    }
}