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

import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.dao.FavoriteDao;
import com.tlongdev.bktf.data.entity.Favorite;
import com.tlongdev.bktf.interactor.LoadFavoritesInteractor;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.fragment.FavoritesView;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 12.
 */
public class FavoritesPresenter implements Presenter<FavoritesView>,LoadFavoritesInteractor.Callback {

    private static final String LOG_TAG = FavoritesPresenter.class.getSimpleName();

    @Inject
    FavoriteDao mFavoriteDao;

    private FavoritesView mView;

    private final BptfApplication mApplication;

    public FavoritesPresenter(BptfApplication application) {
        application.getPresenterComponent().inject(this);
        mApplication = application;
    }

    @Override
    public void attachView(FavoritesView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void onLoadFavoritesFinished(List<Item> items) {
        if (mView != null) {
            mView.showFavorites(items);
        }
    }

    public void loadFavorites() {
        LoadFavoritesInteractor interactor = new LoadFavoritesInteractor(mApplication, this);
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void addCurrencies() {
        int[] defindexes = new int[]{143, 5002, 5021};

        for (int defindex : defindexes) {

            Favorite favorite = new Favorite();

            favorite.setDefindex(defindex);
            favorite.setQuality(Quality.UNIQUE);
            favorite.setTradable(true);
            favorite.setCraftable(true);
            favorite.setPriceIndex(0);
            favorite.setAustralium(false);
            favorite.setWeaponWear(0);

            mFavoriteDao.insertFavorite(favorite);
        }

        loadFavorites();
    }
}