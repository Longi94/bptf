package com.tlongdev.bktf.interactor;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.util.Utility;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public class LoadUnusualsInteractor extends AsyncTask<Void, Void, Void> {

    @Inject @Named("readable")
    SupportSQLiteDatabase mDatabase;
    @Inject Context mContext;

    private final Callback mCallback;
    private final String mFilter;
    private final int mDefindex;
    private final int mIndex;

    private final List<Item> mItems = new LinkedList<>();

    public LoadUnusualsInteractor(BptfApplication application, int defindex, int index, String filter,
                                  Callback callback) {
        application.getInteractorComponent().inject(this);
        mFilter = filter;
        mCallback = callback;
        mDefindex = defindex;
        mIndex = index;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected Void doInBackground(Void... params) {

        int index;
        String selection = "pricelist.quality = ?";
        String nameColumn;
        String joinOn;

        //If defindex is -1, user is browsing by effects
        if (mDefindex != -1) {
            index = mDefindex;
            nameColumn = "unusual_schema.name";
            joinOn = "unusual_schema ON pricelist.price_index = unusual_schema._id";
            selection = selection + " AND pricelist.defindex = ? AND " +
                    "unusual_schema.name LIKE ?";
        } else {
            index = mIndex;
            nameColumn = "item_schema.item_name";
            joinOn = "item_schema ON pricelist.defindex = item_schema.defindex";
            selection = selection + " AND pricelist.price_index = ? AND " +
                    "item_schema.item_name LIKE ?";
        }

        String sql = "SELECT " +
                "pricelist._id," +
                "pricelist.defindex," +
                "price_index," +
                "currency," +
                "price," +
                "max," +
                "last_update," +
                "difference," +
                nameColumn + " name" +
                " FROM pricelist" +
                " LEFT JOIN " + joinOn +
                " WHERE " + selection +
                " ORDER BY " + Utility.getRawPriceQueryString(mContext) + " DESC";

        String[] selectionArgs = {"5", String.valueOf(index), "%" + mFilter + "%"};

        Cursor cursor = mDatabase.query(sql, selectionArgs);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Price price = new Price();
                price.setValue(cursor.getDouble(cursor.getColumnIndex("price")));
                price.setHighValue(cursor.getDouble(cursor.getColumnIndex("max")));
                price.setCurrency(cursor.getString(cursor.getColumnIndex("currency")));
                price.setDifference(cursor.getDouble(cursor.getColumnIndex("difference")));
                price.setLastUpdate(cursor.getLong(cursor.getColumnIndex("last_update")));

                Item item = new Item();
                item.setName(cursor.getString(cursor.getColumnIndex("name")));
                item.setTradable(true);
                item.setCraftable(true);
                item.setAustralium(false);
                item.setDefindex(cursor.getInt(cursor.getColumnIndex("defindex")));
                item.setPriceIndex(cursor.getInt(cursor.getColumnIndex("price_index")));
                item.setQuality(Quality.UNUSUAL);
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
            mCallback.onUnusualsLoadFinished(mItems);
        }
    }

    public interface Callback {
        void onUnusualsLoadFinished(List<Item> items);
    }
}
