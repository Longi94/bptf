package com.tlongdev.bktf.presenter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.interactor.LoadAllPricesInteractor;
import com.tlongdev.bktf.network.GetItemSchema;
import com.tlongdev.bktf.network.GetPriceList;
import com.tlongdev.bktf.ui.RecentsView;
import com.tlongdev.bktf.util.Utility;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class RecentsPresenter implements Presenter<RecentsView>,LoadAllPricesInteractor.Callback, GetPriceList.OnPriceListListener, GetItemSchema.OnItemSchemaListener {

    private Tracker mTracker;

    private RecentsView mView;

    @Override
    public void attachView(RecentsView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void onFinish(Cursor prices) {
        if (mView != null) {
            mView.showPrices(prices);
        }
    }

    @Override
    public void onFail() {
        if (mView != null) {
            mView.showError();
        }
    }

    public void setTracker(Tracker tracker) {
        mTracker = tracker;
    }

    public void loadPrices() {
        LoadAllPricesInteractor interactor = new LoadAllPricesInteractor(
                mView.getContext(),
                this
        );
        interactor.execute();
    }

    public void downloadPrices() {

        //Manual update
        if (Utility.isNetworkAvailable(mView.getContext())) {
            GetPriceList task = new GetPriceList(mView.getContext(), true, true);
            task.setOnPriceListFetchListener(this);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mView.getContext());
        //Download whole database when the app is first opened.
        if (prefs.getBoolean(mView.getContext().getString(R.string.pref_initial_load_v2), true)) {
            if (Utility.isNetworkAvailable(mView.getContext())) {
                GetPriceList task = new GetPriceList(mView.getContext(), false, true);
                task.setOnPriceListFetchListener(this);
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
            if (System.currentTimeMillis() - prefs.getLong(mView.getContext().getString(R.string.pref_last_price_list_update), 0) >= 3600000L
                    && Utility.isNetworkAvailable(mView.getContext())) {
                GetPriceList task = new GetPriceList(mView.getContext(), true, false);
                task.setOnPriceListFetchListener(this);
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

    @Override
    public void onPriceListFinished(int newItems, long sinceParam) {
        if (newItems > 0) {
            Utility.notifyPricesWidgets(mView.getContext());
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mView.getContext());

        if (mView != null) {
            mView.dismissLoadingDialog();
        }

        if (prefs.getBoolean(mView.getContext().getString(R.string.pref_initial_load_v2), true)) {

            GetItemSchema task = new GetItemSchema(mView.getContext());
            task.setListener(this);
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


            if (System.currentTimeMillis() - prefs.getLong(mView.getContext().getString(R.string.pref_last_item_schema_update), 0) >= 172800000L //2days
                    && Utility.isNetworkAvailable(mView.getContext())) {
                GetItemSchema task = new GetItemSchema(mView.getContext());
                task.setListener(this);
                task.execute();

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Request")
                        .setAction("Refresh")
                        .setLabel("ItemSchema")
                        .build());
            } else {
                if (mView != null) {
                    mView.hideRefreshingAnimation();

                    mView.updateCurrencyHeader();
                }
            }
        }

        //Get the shared preferences
        SharedPreferences.Editor editor = prefs.edit();

        //Save when the update finished
        editor.putLong(mView.getContext().getString(R.string.pref_last_price_list_update),
                System.currentTimeMillis());
        editor.putBoolean(mView.getContext().getString(R.string.pref_initial_load_v2), false);
        editor.apply();
    }

    @Override
    public void onPriceListUpdate(int max) {
        if (mView != null) {
            mView.updateLoadingDialog(max, mView.getContext().getString(R.string.message_database_create));
        }
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

        //Update the header with currency prices
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mView.getContext());

        //Get the shared preferences
        SharedPreferences.Editor editor = prefs.edit();

        //Save when the update finished
        editor.putLong(mView.getContext().getString(R.string.pref_last_item_schema_update),
                System.currentTimeMillis());
        editor.putBoolean(mView.getContext().getString(R.string.pref_initial_load_v2), false);
        editor.apply();

        if (mView != null) {
            //Stop animation
            mView.hideRefreshingAnimation();
            mView.updateCurrencyHeader();
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
}
