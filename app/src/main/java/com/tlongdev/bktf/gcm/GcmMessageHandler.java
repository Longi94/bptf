package com.tlongdev.bktf.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.FavoritesEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.network.GetPriceList;
import com.tlongdev.bktf.util.Utility;

/**
 * Created by Long on 2015. 12. 18..
 */
public class GcmMessageHandler extends GcmListenerService implements GetPriceList.OnPriceListListener {

    public static final String LOG_TAG = GcmMessageHandler.class.getSimpleName();

    public static final int NOTIFICATION_ID = 100;

    public void onMessageReceived(String from, Bundle data) {
        Log.d(LOG_TAG, "Message received");

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (from.startsWith("/topics/")) {
            if (from.endsWith("price_updates")) {
                String autoSync = prefs.getString(getString(R.string.pref_auto_sync), "1");

                if (autoSync.equals("2") || (autoSync.equals("1") && wifi.isConnected())) {
                    GetPriceList task = new GetPriceList(this, true, true);
                    task.setOnPriceListFetchListener(this);
                    task.execute();
                }
            }
        } else {
            // normal downstream message.
        }
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

        Cursor cursor = getContentResolver().query(
                DatabaseContract.RAW_QUERY_URI,
                null, sql, null, null
        );

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
    public void onPriceListUpdate(int max) {
        //unused
    }

    @Override
    public void onPriceListFailed(String errorMessage) {
        //do nothing
    }
}
