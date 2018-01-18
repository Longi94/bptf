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

package com.tlongdev.bktf.fcm;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.FavoritesEntry;
import com.tlongdev.bktf.interactor.TlongdevPriceListInteractor;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author lngtr
 * @since 2016. 05. 20.
 */
public class BptfMessagingService extends FirebaseMessagingService {

    private static final String LOG_TAG = BptfMessagingService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 100;

    @Inject @Named("readable")
    SupportSQLiteDatabase mDatabase;
    @Inject SharedPreferences mPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        ((BptfApplication) getApplication()).getServiceComponent().inject(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(LOG_TAG, "Message received");

        String from = remoteMessage.getFrom();

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (from.startsWith("/topics/")) {
            if (from.endsWith("price_updates")) {
                String autoSync = mPrefs.getString(getString(R.string.pref_auto_sync), "1");

                if (autoSync.equals("2") || (autoSync.equals("1") && wifi.isConnected())) {
                    TlongdevPriceListInteractor interactor = new TlongdevPriceListInteractor(
                            (BptfApplication) getApplication(), true, true, null
                    );
                    interactor.run();
                    checkNewPrices(interactor);
                }
            }
        } else {
            // normal downstream message.
        }
    }

    private void checkNewPrices(TlongdevPriceListInteractor interactor) {
        if (interactor.getRowsInserted() > 0) {
            Utility.notifyPricesWidgets(this);
        } else {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!prefs.getBoolean(getString(R.string.pref_price_notification), false)) return;

        String sql = "SELECT " +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry._ID + "," +
                "pricelist.last_update" +
                " FROM " + FavoritesEntry.TABLE_NAME +
                " LEFT JOIN pricelist" +
                " ON " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + " = pricelist.defindex" +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_TRADABLE + " = pricelist.tradable" +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_CRAFTABLE + " = pricelist.craftable" +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_PRICE_INDEX + " = pricelist.price_index" +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_QUALITY + " = pricelist.quality" +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_AUSTRALIUM + " = pricelist.australium";

        Cursor cursor = mDatabase.query(sql, null);

        boolean newPrice = false;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long update = cursor.getLong(1);
                newPrice = newPrice || update > interactor.getSinceParam();
            }
            cursor.close();
        }

        if (newPrice) {
            Utility.createSimpleNotification(this, NOTIFICATION_ID, "Prices updated", "The prices of your favorite items has been updated!");
        }
    }
}