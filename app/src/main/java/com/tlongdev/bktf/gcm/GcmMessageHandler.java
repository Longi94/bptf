/**
 * Copyright 2015 Long Tran
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

package com.tlongdev.bktf.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.FavoritesEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.interactor.TlongdevPriceListInteractor;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;
import javax.inject.Named;

public class GcmMessageHandler extends GcmListenerService implements TlongdevPriceListInteractor.Callback {

    private static final String LOG_TAG = GcmMessageHandler.class.getSimpleName();

    private static final int NOTIFICATION_ID = 100;

    @Inject @Named("readable") SQLiteDatabase mDatabase;

    public void onMessageReceived(String from, Bundle data) {
        Log.d(LOG_TAG, "Message received");

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (from.startsWith("/topics/")) {
            if (from.endsWith("price_updates")) {
                String autoSync = prefs.getString(getString(R.string.pref_auto_sync), "1");

                if (autoSync.equals("2") || (autoSync.equals("1") && wifi.isConnected())) {
                    TlongdevPriceListInteractor interactor = new TlongdevPriceListInteractor((BptfApplication) getApplication(), true, true, this);
                    interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        } else {
            // normal downstream message.
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((BptfApplication) getApplication()).getServiceComponent().inject(this);
    }

    @Override
    public void onPriceListFinished(int newItems, long sinceParam) {

        if (newItems > 0) {
            Utility.notifyPricesWidgets(this);
        } else {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!prefs.getBoolean(getString(R.string.pref_price_notification), false)) return;

        String sql = "SELECT " +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry._ID + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_LAST_UPDATE +
                " FROM " + FavoritesEntry.TABLE_NAME +
                " LEFT JOIN " + PriceEntry.TABLE_NAME +
                " ON " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_TRADABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_CRAFTABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_PRICE_INDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_QUALITY + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_AUSTRALIUM + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM;

        Cursor cursor = mDatabase.rawQuery(sql, null);

        boolean newPrice = false;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long update = cursor.getLong(1);
                newPrice = newPrice || update > sinceParam;
            }
            cursor.close();
        }

        if (newPrice) {
            Utility.createSimpleNotification(this, NOTIFICATION_ID, "Prices updated", "The prices of your favorite items has been updated!");
        }
    }

    @Override
    public void onPriceListFailed(String errorMessage) {
        //do nothing
    }
}
