package com.tlongdev.bktf.interactor;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.model.Price;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 24.
 */
public class LoadItemDetailsInteractor extends AsyncTask<Void, Void, Price> {

    @Inject ContentResolver mContentResolver;

    private BackpackItem mBackpackItem;
    private final Callback mCallback;

    public LoadItemDetailsInteractor(BptfApplication application, BackpackItem backpackItem,
                                     Callback callback) {
        application.getInteractorComponent().inject(this);
        mBackpackItem = backpackItem;
        mCallback = callback;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected Price doInBackground(Void... params) {

        Cursor cursor = mContentResolver.query(
                PriceEntry.CONTENT_URI,
                new String[]{
                        PriceEntry._ID,
                        PriceEntry.COLUMN_PRICE,
                        PriceEntry.COLUMN_PRICE_HIGH,
                        PriceEntry.COLUMN_CURRENCY
                },
                PriceEntry.COLUMN_DEFINDEX + " = ? AND " +
                        PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                        PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                        PriceEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                        PriceEntry.COLUMN_AUSTRALIUM + " = ?",
                new String[]{
                        String.valueOf(mBackpackItem.getDefindex()),
                        String.valueOf(mBackpackItem.getQuality()),
                        String.valueOf(mBackpackItem.isTradable() ? 1 : 0),
                        String.valueOf(mBackpackItem.isCraftable() ? 1 : 0),
                        String.valueOf(mBackpackItem.getPriceIndex()),
                        String.valueOf(mBackpackItem.isAustralium() ? 1 : 0)
                },
                null
        );

        Price price = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                price = new Price();
                price.setValue(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE)));
                price.setHighValue(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)));
                price.setCurrency(cursor.getString(cursor.getColumnIndex(PriceEntry.COLUMN_CURRENCY)));
            }
            cursor.close();
        }
        return price;
    }

    @Override
    protected void onPostExecute(Price price) {
        if (mCallback != null) {
            mCallback.onItemDetailsLoaded(price);
        }
    }

    public interface Callback {
        void onItemDetailsLoaded(Price price);
    }
}
