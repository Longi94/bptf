/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.presenter.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.interactor.LoadAllPricesInteractor;
import com.tlongdev.bktf.interactor.LoadCurrencyPricesInteractor;
import com.tlongdev.bktf.interactor.TlongdevItemSchemaInteractor;
import com.tlongdev.bktf.interactor.TlongdevPriceListInteractor;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.fragment.RecentsView;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class RecentsPresenter implements Presenter<RecentsView>, LoadAllPricesInteractor.Callback, TlongdevPriceListInteractor.Callback, TlongdevItemSchemaInteractor.Callback, LoadCurrencyPricesInteractor.Callback {

    @Inject SharedPreferences mPrefs;
    @Inject SharedPreferences.Editor mEditor;
    @Inject Tracker mTracker;

    private RecentsView mView;
    private BptfApplication mApplication;

    public RecentsPresenter(BptfApplication application) {
        mApplication = application;
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(RecentsView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void onLoadPricesFinished(Cursor prices) {
        if (mView != null) {
            mView.showPrices(prices);
        }
    }

    @Override
    public void onLoadPricesFailed() {
        if (mView != null) {
            mView.showError();
        }
    }

    public void loadPrices() {
        LoadAllPricesInteractor interactor = new LoadAllPricesInteractor(
                mView.getContext(), mApplication, this
        );
        interactor.execute();
    }

    public void downloadPrices() {

        //Manual update
        if (Utility.isNetworkAvailable(mView.getContext())) {
            TlongdevPriceListInteractor task = new TlongdevPriceListInteractor(mView.getContext(), mApplication, true, true, this);
            task.execute();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("Refresh")
                    .setLabel("Prices")
                    .build());
        } else {
            Toast.makeText(mView.getContext(), "bptf: " + mView.getContext().getString(R.string.error_no_network),
                    Toast.LENGTH_SHORT).show();

            mView.hideRefreshingAnimation();
        }
    }

    public void downloadPricesIfNeeded() {
        //Download whole database when the app is first opened.
        if (mPrefs.getBoolean(mView.getContext().getString(R.string.pref_initial_load_v2), true)) {
            if (Utility.isNetworkAvailable(mView.getContext())) {
                TlongdevPriceListInteractor task = new TlongdevPriceListInteractor(mView.getContext(), mApplication, false, true, this);
                task.execute();

                //Show the progress dialog
                mView.showLoadingDialog("Downloading prices...");

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Request")
                        .setAction("Refresh")
                        .setLabel("Prices")
                        .build());
            } else {
                //Quit the app if the download failed.
                AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());
                builder.setMessage(mView.getContext().getString(R.string.message_database_fail_network)).setCancelable(false).
                        setPositiveButton(mView.getContext().getString(R.string.action_close), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mView.finishActivity();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        } else {

            //Update database if the last update happened more than an hour ago
            if (System.currentTimeMillis() - mPrefs.getLong(mView.getContext().getString(R.string.pref_last_price_list_update), 0) >= 3600000L
                    && Utility.isNetworkAvailable(mView.getContext())) {
                TlongdevPriceListInteractor task = new TlongdevPriceListInteractor(mView.getContext(), mApplication, true, false, this);
                task.execute();

                mView.showRefreshAnimation();

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Request")
                        .setAction("Refresh")
                        .setLabel("Prices")
                        .build());
            }
        }
    }

    public void loadCurrencyPrices() {
        LoadCurrencyPricesInteractor interactor = new LoadCurrencyPricesInteractor(
                mApplication, this
        );
        interactor.execute();
    }

    @Override
    public void onPriceListFinished(int newItems, long sinceParam) {
        if (newItems > 0) {
            Utility.notifyPricesWidgets(mView.getContext());
        }

        if (mView != null) {
            mView.dismissLoadingDialog();
        }

        if (mPrefs.getBoolean(mView.getContext().getString(R.string.pref_initial_load_v2), true)) {

            TlongdevItemSchemaInteractor task = new TlongdevItemSchemaInteractor(mView.getContext(), mApplication, this);
            task.execute();

            if (mView != null) {
                mView.showLoadingDialog("Downloading item schema...");
            }

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("Refresh")
                    .setLabel("ItemSchema")
                    .build());
        } else {
            if (newItems > 0) {
                loadPrices();
            }

            if (System.currentTimeMillis() - mPrefs.getLong(mView.getContext().getString(R.string.pref_last_item_schema_update), 0) >= 172800000L //2days
                    && Utility.isNetworkAvailable(mView.getContext())) {
                TlongdevItemSchemaInteractor task = new TlongdevItemSchemaInteractor(mView.getContext(), mApplication, this);
                task.execute();

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Request")
                        .setAction("Refresh")
                        .setLabel("ItemSchema")
                        .build());
            } else {
                if (mView != null) {
                    mView.hideRefreshingAnimation();
                    loadCurrencyPrices();
                }
            }
        }

        //Save when the update finished
        mEditor.putLong(mView.getContext().getString(R.string.pref_last_price_list_update), System.currentTimeMillis());
        mEditor.putBoolean(mView.getContext().getString(R.string.pref_initial_load_v2), false);
        mEditor.apply();
    }

    @Override
    public void onPriceListFailed(String errorMessage) {
        if (mView != null) {
            mView.showPricesError(errorMessage);
        }
    }

    @Override
    public void onItemSchemaFinished() {

        if (mView != null) {
            mView.dismissLoadingDialog();
        }

        loadPrices();

        //Save when the update finished
        mEditor.putLong(mView.getContext().getString(R.string.pref_last_item_schema_update), System.currentTimeMillis());
        mEditor.putBoolean(mView.getContext().getString(R.string.pref_initial_load_v2), false);
        mEditor.apply();

        if (mView != null) {
            //Stop animation
            mView.hideRefreshingAnimation();
            loadCurrencyPrices();
        }
    }

    @Override
    public void onItemSchemaUpdate(int max) {
        if (mView != null) {
            mView.updateLoadingDialog(max, mView.getContext().getString(R.string.message_item_schema_create));
        }
    }

    @Override
    public void onItemSchemaFailed(String errorMessage) {
        if (mView != null) {
            mView.showItemSchemaError(errorMessage);
        }
    }

    @Override
    public void onLoadCurrencyPricesFinished(Price metalPrice, Price keyPrice, Price budPrice) {
        mView.updateCurrencyHeader(metalPrice, keyPrice, budPrice);
    }
}
