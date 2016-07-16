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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.FavoritesEntry;
import com.tlongdev.bktf.interactor.LoadFavoritesInteractor;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.fragment.FavoritesView;

import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 12.
 */
public class FavoritesPresenter implements Presenter<FavoritesView>,LoadFavoritesInteractor.Callback {

    private static final String LOG_TAG = FavoritesPresenter.class.getSimpleName();

    @Inject ContentResolver mContentResolver;

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
        //Iterator that will iterate through the items
        Vector<ContentValues> cVVector = new Vector<>();

        int[] defindexes = new int[]{143, 5002, 5021};

        for (int defindex : defindexes) {

            ContentValues cv = new ContentValues();

            cv.put(FavoritesEntry.COLUMN_DEFINDEX, defindex);
            cv.put(FavoritesEntry.COLUMN_ITEM_QUALITY, Quality.UNIQUE);
            cv.put(FavoritesEntry.COLUMN_ITEM_TRADABLE, 1);
            cv.put(FavoritesEntry.COLUMN_ITEM_CRAFTABLE, 1);
            cv.put(FavoritesEntry.COLUMN_PRICE_INDEX, 0);
            cv.put(FavoritesEntry.COLUMN_AUSTRALIUM, 0);
            cv.put(FavoritesEntry.COLUMN_WEAPON_WEAR, 0);

            cVVector.add(cv);
        }

        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        //Insert all the data into the database
        int rowsInserted = mContentResolver.bulkInsert(FavoritesEntry.CONTENT_URI, cvArray);
        Log.v(LOG_TAG, "inserted " + rowsInserted + " rows");
        loadFavorites();
    }
}