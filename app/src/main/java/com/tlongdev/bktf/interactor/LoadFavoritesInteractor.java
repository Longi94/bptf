package com.tlongdev.bktf.interactor;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 12.
 */
public class LoadFavoritesInteractor extends AsyncTask<Void, Void, Void> {

    @Inject @Named("readable")
    SupportSQLiteDatabase mDatabase;
    @Inject Context mContext;

    private final Callback mCallback;

    private final List<Item> mItems = new LinkedList<>();

    public LoadFavoritesInteractor(BptfApplication application, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected Void doInBackground(Void... params) {

        String sql = "SELECT " +
                "favorites.defindex," +
                "item_schema.item_name," +
                "favorites.quality," +
                "favorites.tradable," +
                "favorites.craftable," +
                "favorites.price_index," +
                "pricelist.currency," +
                "pricelist.price," +
                "pricelist.max," +
                Utility.getRawPriceQueryString(mContext) + " price_raw," +
                "pricelist.difference," +
                "favorites.australium" +
                " FROM favorites" +
                " LEFT JOIN pricelist" +
                " ON favorites.defindex = pricelist.defindex" +
                " AND favorites.tradable = pricelist.tradable" +
                " AND favorites.craftable = pricelist.craftable" +
                " AND favorites.price_index = pricelist.price_index" +
                " AND favorites.quality = pricelist.quality" +
                " AND favorites.australium = pricelist.australium" +
                " LEFT JOIN item_schema" +
                " ON favorites.defindex = item_schema.defindex" +
                " ORDER BY item_name ASC";

        Cursor cursor = mDatabase.query(sql, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Item item = new Item();
                item.setDefindex(cursor.getInt(cursor.getColumnIndex("defindex")));
                item.setName(cursor.getString(cursor.getColumnIndex("item_name")));
                item.setQuality(cursor.getInt(cursor.getColumnIndex("quality")));
                item.setTradable(cursor.getInt(cursor.getColumnIndex("tradable")) == 1);
                item.setCraftable(cursor.getInt(cursor.getColumnIndex("craftable")) == 1);
                item.setAustralium(cursor.getInt(cursor.getColumnIndex("australium")) == 1);
                item.setPriceIndex(cursor.getInt(cursor.getColumnIndex("pric_index")));

                if (cursor.getString(cursor.getColumnIndex("currency")) != null) {
                    Price price = new Price();
                    price.setValue(cursor.getDouble(cursor.getColumnIndex("price")));
                    price.setHighValue(cursor.getDouble(cursor.getColumnIndex("max")));
                    price.setRawValue(cursor.getDouble(cursor.getColumnIndex("price_raw")));
                    price.setDifference(cursor.getDouble(cursor.getColumnIndex("difference")));
                    price.setCurrency(cursor.getString(cursor.getColumnIndex("currency")));
                    item.setPrice(price);
                }

                mItems.add(item);
            }
            cursor.close();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mCallback != null) {
            mCallback.onLoadFavoritesFinished(mItems);
        }
    }

    public interface Callback {
        void onLoadFavoritesFinished(List<Item> items);
    }
}
