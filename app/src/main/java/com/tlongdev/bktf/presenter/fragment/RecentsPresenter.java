package com.tlongdev.bktf.presenter.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
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
public class RecentsPresenter implements Presenter<RecentsView>,
        TlongdevPriceListInteractor.Callback, TlongdevItemSchemaInteractor.Callback,
        LoadCurrencyPricesInteractor.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String STATE_METAL = "metal";
    public static final String STATE_KEY = "key";
    public static final String STATE_BUDS = "buds";

    @Inject SharedPreferences mPrefs;
    @Inject SharedPreferences.Editor mEditor;
    @Inject Context mContext;

    private RecentsView mView;
    private final BptfApplication mApplication;

    private boolean mLoading = false;
    private boolean mLoaderInitialized = false;

    private LoaderManager mLoaderManager;

    private Price mMetalCache;
    private Price mKeyCache;
    private Price mBudCache;

    public RecentsPresenter(BptfApplication application) {
        mApplication = application;
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(RecentsView view) {
        mView = view;

        if (mView != null) {
            if (mLoading) {
                mView.showRefreshAnimation();
            }
            mLoaderManager = mView.getLoaderManager();
        }
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadPrices() {
        //Download whole database when the app is first opened.
        if (mPrefs.getBoolean(mContext.getString(R.string.pref_initial_load_v2), true)) {
            if (Utility.isNetworkAvailable(mContext)) {
                callTlongdevPrices(false, true);
                //Show the progress dialog
                if (mView != null) {
                    mView.showLoadingDialog("Downloading prices...");
                }
            } else {
                if (mView != null) {
                    mView.showErrorDialog();
                }
            }
        } else {
            mLoaderManager.initLoader(0, null, this);
            mLoaderInitialized = true;

            loadCurrencyPrices();
            //Update database if the last update happened more than an hour ago
            if (System.currentTimeMillis() - mPrefs.getLong(mContext.getString(R.string.pref_last_price_list_update), 0) >= 3600000L
                    && Utility.isNetworkAvailable(mContext)) {
                callTlongdevPrices(true, false);
                if (mView != null) {
                    mView.showRefreshAnimation();
                }
            }
        }
    }

    public void downloadPrices() {
        //Manual update
        if (Utility.isNetworkAvailable(mContext)) {
            callTlongdevPrices(true, true);
        } else {
            mLoading = false;
            mView.showPricesError(mContext.getString(R.string.error_no_network));
            mView.hideRefreshingAnimation();
        }
    }

    private void callTlongdevPrices(boolean updateDatabase, boolean manualSync) {
        TlongdevPriceListInteractor interactor = new TlongdevPriceListInteractor(
                mApplication, updateDatabase, manualSync, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mLoading = true;
    }

    private void callTlongdevItemSchema() {
        TlongdevItemSchemaInteractor task = new TlongdevItemSchemaInteractor(mApplication, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void loadCurrencyPrices() {
        LoadCurrencyPricesInteractor interactor = new LoadCurrencyPricesInteractor(
                mApplication, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onPriceListFinished(int newItems, long sinceParam) {
        if (newItems > 0) {
            Utility.notifyPricesWidgets(mContext);
        }

        if (mView != null) {
            mView.dismissLoadingDialog();
        }

        if (mPrefs.getBoolean(mContext.getString(R.string.pref_initial_load_v2), true)) {
            callTlongdevItemSchema();
            if (mView != null) {
                mView.showLoadingDialog("Downloading item schema...");
            }
        } else {
            if (System.currentTimeMillis() - mPrefs.getLong(mContext.getString(R.string.pref_last_item_schema_update), 0) >= 172800000L //2days
                    && Utility.isNetworkAvailable(mContext)) {
                callTlongdevItemSchema();
            } else {
                mLoading = false;
                if (mView != null) {
                    mView.hideRefreshingAnimation();
                    loadCurrencyPrices();
                }

                if (!mLoaderInitialized) {
                    mLoaderManager.initLoader(0, null, this);
                }
            }
        }

        //Save when the update finished
        mEditor.putLong(mContext.getString(R.string.pref_last_price_list_update), System.currentTimeMillis());
        mEditor.putBoolean(mContext.getString(R.string.pref_initial_load_v2), false);
        mEditor.apply();
    }

    @Override
    public void onPriceListFailed(String errorMessage) {
        if (mView != null) {
            mView.showPricesError(errorMessage);
            mView.hideRefreshingAnimation();
        }
    }

    @Override
    public void onItemSchemaFinished() {
        if (mView != null) {
            mView.dismissLoadingDialog();
        }

        //Save when the update finished
        mEditor.putLong(mContext.getString(R.string.pref_last_item_schema_update), System.currentTimeMillis());
        mEditor.putBoolean(mContext.getString(R.string.pref_initial_load_v2), false);
        mEditor.apply();

        mLoading = false;
        if (mView != null) {
            //Stop animation
            mView.hideRefreshingAnimation();
            loadCurrencyPrices();
        }

        if (!mLoaderInitialized) {
            mLoaderManager.initLoader(0, null, this);
        }
    }

    @Override
    public void onItemSchemaUpdate(int max) {
        if (mView != null) {
            mView.updateLoadingDialog(max, mContext.getString(R.string.message_item_schema_create));
        }
    }

    @Override
    public void onItemSchemaFailed(String errorMessage) {
        if (mView != null) {
            mView.showItemSchemaError(errorMessage);
            mView.hideRefreshingAnimation();
        }
    }

    @Override
    public void onLoadCurrencyPricesFinished(Price metalPrice, Price keyPrice, Price budPrice) {
        mMetalCache = metalPrice;
        mKeyCache = keyPrice;
        mBudCache = budPrice;
        if (mView != null) {
            mView.updateCurrencyHeader(metalPrice, keyPrice, budPrice);
        }
    }

    public void saveCurrencyPrices(Bundle outState) {
        outState.putParcelable(STATE_KEY, mKeyCache);
        outState.putParcelable(STATE_METAL, mMetalCache);
        outState.putParcelable(STATE_BUDS, mBudCache);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mApplication,
                PriceEntry.ALL_PRICES_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mView != null) {
            mView.showPrices(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mView != null) {
            mView.showPrices(null);
        }
    }
}
