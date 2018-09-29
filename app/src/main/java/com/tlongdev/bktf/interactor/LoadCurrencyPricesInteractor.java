package com.tlongdev.bktf.interactor;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 15.
 */
public class LoadCurrencyPricesInteractor extends AsyncTask<Void, Void, Void> {

    @Inject
    ContentResolver mContentResolver;

    @Inject
    Application mContext;

    private Price mMetalPrice;
    private Price mKeyPrice;
    private Price mBudPrice;

    private final Callback mCallback;

    public LoadCurrencyPricesInteractor(BptfApplication application, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected Void doInBackground(Void... params) {

        Cursor cursor = mContentResolver.query(
                PriceEntry.CONTENT_URI,
                new String[]{
                        PriceEntry.COLUMN_DEFINDEX,
                        PriceEntry.COLUMN_PRICE,
                        PriceEntry.COLUMN_PRICE_HIGH,
                        PriceEntry.COLUMN_DIFFERENCE,
                        PriceEntry.COLUMN_CURRENCY,
                        Utility.getRawPriceQueryString(mContext)
                },
                PriceEntry.COLUMN_DEFINDEX + " IN (143, 5002, 5021) AND " +
                        PriceEntry.COLUMN_ITEM_QUALITY + " = 6 AND " +
                        PriceEntry.COLUMN_ITEM_TRADABLE + " = 1 AND " +
                        PriceEntry.COLUMN_ITEM_CRAFTABLE + " = 1",
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                switch (cursor.getInt(0)) {
                    case 143:
                        mBudPrice = new Price();
                        mBudPrice.setValue(cursor.getDouble(1));
                        mBudPrice.setHighValue(cursor.getDouble(2));
                        mBudPrice.setDifference(cursor.getDouble(3));
                        mBudPrice.setCurrency(cursor.getString(4));
                        mBudPrice.setRawValue(cursor.getDouble(5));
                        break;
                    case 5002:
                        mMetalPrice = new Price();
                        mMetalPrice.setValue(cursor.getDouble(1));
                        mMetalPrice.setHighValue(cursor.getDouble(2));
                        mMetalPrice.setDifference(cursor.getDouble(3));
                        mMetalPrice.setCurrency(cursor.getString(4));
                        mMetalPrice.setRawValue(cursor.getDouble(5));
                        break;
                    case 5021:
                        mKeyPrice = new Price();
                        mKeyPrice.setValue(cursor.getDouble(1));
                        mKeyPrice.setHighValue(cursor.getDouble(2));
                        mKeyPrice.setDifference(cursor.getDouble(3));
                        mKeyPrice.setCurrency(cursor.getString(4));
                        mKeyPrice.setRawValue(cursor.getDouble(5));
                        break;
                }
            }
            cursor.close();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mCallback != null) {
            mCallback.onLoadCurrencyPricesFinished(mMetalPrice, mKeyPrice, mBudPrice);
        }

    }

    public interface Callback {
        void onLoadCurrencyPricesFinished(Price metalPrice, Price keyPrice, Price budPrice);
    }
}
