package com.tlongdev.bktf.interactor;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
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
public class LoadUnusualHatCategoriesInteractor extends AsyncTask<Void, Void, Void> {

    @Inject @Named("readable")
    SupportSQLiteDatabase mDatabase;
    @Inject SharedPreferences mPrefs;
    @Inject Context mContext;

    @UnusualPresenter.UnusualOrder
    private final int mOrderBy;
    private final Callback mCallback;
    private final String mFilter;

    private final List<Item> mItems = new LinkedList<>();

    public LoadUnusualHatCategoriesInteractor(BptfApplication application, String filter,
                                              @UnusualPresenter.UnusualOrder int orderBy,
                                              Callback callback) {
        application.getInteractorComponent().inject(this);
        mFilter = filter;
        mOrderBy = orderBy;
        mCallback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {

        String sql = "SELECT " +
                "pricelist.defindex," +
                "item_schema.item_name," +
                " AVG(" + Utility.getRawPriceQueryString(mContext) + ") avg_price " +
                " FROM pricelist" +
                " LEFT JOIN item_schema" +
                " ON pricelist.defindex = item_schema.defindex" +
                " WHERE item_schema.item_name LIKE ? AND " +
                "pricelist.quality = ? AND " +
                "price_index != ? " +
                " GROUP BY pricelist.defindex";

        String[] selectionArgs = {"%" + mFilter + "%", "5", "0"};

        switch (mOrderBy) {
            case UnusualPresenter.ORDER_BY_NAME:
                sql += " ORDER BY item_schema.item_name ASC";
                break;
            case UnusualPresenter.ORDER_BY_PRICE:
                sql += " ORDER BY avg_price DESC";
                break;
        }

        Cursor cursor = mDatabase.query(sql, selectionArgs);

        double rawKeyPrice = Utility.getDouble(mPrefs, mContext.getString(R.string.pref_key_raw), 1);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Price price = new Price();
                price.setValue(cursor.getDouble(cursor.getColumnIndex("avg_price")) / rawKeyPrice);

                Item item = new Item();
                item.setDefindex(cursor.getInt(cursor.getColumnIndex("defindex")));
                item.setName(cursor.getString(cursor.getColumnIndex("item_name")));
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
            mCallback.onUnusualHatsLoadFinished(mItems);
        }
    }

    public interface Callback {
        void onUnusualHatsLoadFinished(List<Item> items);
    }
}
