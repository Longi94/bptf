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

import android.support.annotation.IntDef;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.LoadUnusualEffectsInteractor;
import com.tlongdev.bktf.interactor.LoadUnusualHatCategoriesInteractor;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.fragment.UnusualView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class UnusualPresenter implements Presenter<UnusualView>,LoadUnusualHatCategoriesInteractor.Callback, LoadUnusualEffectsInteractor.Callback {

    private BptfApplication mApplication;

    private UnusualView mView;

    public UnusualPresenter(BptfApplication application) {
        mApplication = application;
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(UnusualView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadUnusualHats(String filter, @UnusualOrder int orderBy) {
        LoadUnusualHatCategoriesInteractor interactor = new LoadUnusualHatCategoriesInteractor(
                mView.getContext(), mApplication, filter, orderBy, this
        );
        interactor.execute();
    }

    public void loadUnusualEffects(String filter, @UnusualOrder int orderBy) {
        LoadUnusualEffectsInteractor interactor = new LoadUnusualEffectsInteractor(
                mView.getContext(), mApplication, filter, orderBy, this
        );
        interactor.execute();
    }

    @Override
    public void onUnusualHatsLoadFinished(List<Item> items) {
        if (mView != null) {
            mView.showUnusualHats(items);
        }
    }

    @Override
    public void onUnusualHatsLoadFailed() {

    }

    @Override
    public void onUnusualEffectsLoadFinished(List<Item> items) {
        if (mView != null) {
            mView.showUnusualEffects(items);
        }
    }

    @Override
    public void onUnusualEffectsLoadFailed() {

    }

    @IntDef({ORDER_BY_NAME, ORDER_BY_PRICE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UnusualOrder{}

    public static final int ORDER_BY_NAME = 0;
    public static final int ORDER_BY_PRICE = 1;
}