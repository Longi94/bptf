package com.tlongdev.bktf.interactor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 22.
 */
public class LoadSearchItemsInteractor extends AsyncTask<Void, Void, Cursor> {

    @Inject @Named("readable") SQLiteDatabase mDatabase;

    private final String mQuery;
    private final boolean mFilter;
    private final int mFilterQuality;
    private final boolean mFilterTradable;
    private final boolean mFilterCraftable;
    private final boolean mFilterAustralium;
    private final Callback mCallback;

    public LoadSearchItemsInteractor(BptfApplication application, String query, boolean filter,
                                     int filterQuality, boolean filterTradable, boolean filterCraftable,
                                     boolean filterAustralium, Callback callback) {
        application.getInteractorComponent().inject(this);
        mQuery = query;
        mFilter = filter;
        mFilterQuality = filterQuality;
        mFilterTradable = filterTradable;
        mFilterCraftable = filterCraftable;
        mFilterAustralium = filterAustralium;
        mCallback = callback;
    }

    @Override
    protected Cursor doInBackground(Void... params) {

        String sql = "SELECT " +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_IMAGE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                " FROM " + PriceEntry.TABLE_NAME +
                " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                " WHERE ";

        String query = mQuery != null && mQuery.length() > 0 ? "%" + mQuery + "%" : "ASDASD"; //stupid
        String[] selectionArgs;

        if (mFilter) {
            sql += ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ? AND " +
                    PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                    PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                    PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                    PriceEntry.COLUMN_AUSTRALIUM + " = ?";
            selectionArgs = new String[]{query, String.valueOf(mFilterQuality),
                    mFilterTradable ? "1" : "0", mFilterCraftable ? "1" : "0",
                    mFilterAustralium ? "1" : "0"};
        } else {
            sql += ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ? AND " +
                    "NOT(" + PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                    PriceEntry.COLUMN_PRICE_INDEX + " != ?)";
            selectionArgs = new String[]{query, "5", "0"};
        }

        Cursor cursor = mDatabase.rawQuery(sql, selectionArgs);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                return cursor;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        if (mCallback != null) {
            mCallback.onSearchItemsLoaded(cursor);
        }
    }

    public interface Callback {
        void onSearchItemsLoaded(Cursor items);
    }
}
