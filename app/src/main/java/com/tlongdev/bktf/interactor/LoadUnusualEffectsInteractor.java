package com.tlongdev.bktf.interactor;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.fragment.UnusualPresenter;
import com.tlongdev.bktf.util.Utility;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class LoadUnusualEffectsInteractor extends AsyncTask<Void, Void, Void> {

    @Inject @Named("readable") SQLiteDatabase mDatabase;
    @Inject SharedPreferences mPrefs;
    @Inject Context mContext;

    @UnusualPresenter.UnusualOrder
    private final int mOrderBy;
    private final Callback mCallback;
    private final String mFilter;

    private final List<Item> mItems = new LinkedList<>();

    public LoadUnusualEffectsInteractor(BptfApplication application, String filter,
                                        @UnusualPresenter.UnusualOrder int orderBy, Callback callback) {
        application.getInteractorComponent().inject(this);
        mFilter = filter;
        mOrderBy = orderBy;
        mCallback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {

        String sql = "SELECT " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + "," +
                UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME + "," +
                " AVG(" + Utility.getRawPriceQueryString(mContext) + ") avg_price " +
                " FROM " + PriceEntry.TABLE_NAME +
                " LEFT JOIN " + UnusualSchemaEntry.TABLE_NAME +
                " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + " = " + UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_ID +
                " WHERE " + UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME + " LIKE ? AND " +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                PriceEntry.COLUMN_PRICE_INDEX + " != ? " +
                " GROUP BY " + PriceEntry.COLUMN_PRICE_INDEX;

        String[] selectionArgs = {"%" + mFilter + "%", "5", "0"};

        switch (mOrderBy) {
            case UnusualPresenter.ORDER_BY_NAME:
                sql += " ORDER BY " + UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME + " ASC";
                break;
            case UnusualPresenter.ORDER_BY_PRICE:
                sql += " ORDER BY avg_price DESC";
                break;
        }

        Cursor cursor = mDatabase.rawQuery(sql, selectionArgs);

        double rawKeyPrice = Utility.getDouble(mPrefs, mContext.getString(R.string.pref_key_raw), 1);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Price price = new Price();
                price.setValue(cursor.getDouble(cursor.getColumnIndex("avg_price")) / rawKeyPrice);

                Item item = new Item();
                item.setPriceIndex(cursor.getInt(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE_INDEX)));
                item.setName(cursor.getString(cursor.getColumnIndex(UnusualSchemaEntry.COLUMN_NAME)));
                item.setPrice(price);

                mItems.add(item);
            }
            cursor.close();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mCallback != null) {
            mCallback.onUnusualEffectsLoadFinished(mItems);
        }
    }

    public interface Callback {
        void onUnusualEffectsLoadFinished(List<Item> items);
    }
}
