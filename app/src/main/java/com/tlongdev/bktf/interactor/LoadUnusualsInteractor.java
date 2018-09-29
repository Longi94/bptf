package com.tlongdev.bktf.interactor;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;
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

    @Inject
    @Named("readable")
    SQLiteDatabase mDatabase;

    @Inject
    Application mContext;

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
        String selection = PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + " = ?";
        String nameColumn;
        String joinOn = "";

        //If defindex is -1, user is browsing by effects
        if (mDefindex != -1) {
            index = mDefindex;
            nameColumn = UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME;
            joinOn = " LEFT JOIN " + UnusualSchemaEntry.TABLE_NAME + " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + " = " + UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_ID;
            selection = selection + " AND " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = ? AND " +
                    UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME + " LIKE ?";
        } else {
            index = mIndex;
            nameColumn = ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME;
            selection = selection + " AND " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                    ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ?";
        }

        String sql = "SELECT " +
                PriceEntry.TABLE_NAME + "." + PriceEntry._ID + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                PriceEntry.COLUMN_PRICE_INDEX + "," +
                PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.COLUMN_PRICE_HIGH + "," +
                PriceEntry.COLUMN_LAST_UPDATE + "," +
                PriceEntry.COLUMN_DIFFERENCE + "," +
                nameColumn + " name," +
                ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_IMAGE +
                " FROM " + PriceEntry.TABLE_NAME +
                " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME + " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                " " + joinOn +
                " WHERE " + selection +
                " ORDER BY " + Utility.getRawPriceQueryString(mContext) + " DESC";

        String[] selectionArgs = {"5", String.valueOf(index), "%" + mFilter + "%"};

        Cursor cursor = mDatabase.rawQuery(sql, selectionArgs);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Price price = new Price();
                price.setValue(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE)));
                price.setHighValue(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)));
                price.setCurrency(cursor.getString(cursor.getColumnIndex(PriceEntry.COLUMN_CURRENCY)));
                price.setDifference(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_DIFFERENCE)));
                price.setLastUpdate(cursor.getLong(cursor.getColumnIndex(PriceEntry.COLUMN_LAST_UPDATE)));

                Item item = new Item();
                item.setName(cursor.getString(cursor.getColumnIndex("name")));
                item.setTradable(true);
                item.setCraftable(true);
                item.setAustralium(false);
                item.setDefindex(cursor.getInt(cursor.getColumnIndex(PriceEntry.COLUMN_DEFINDEX)));
                item.setPriceIndex(cursor.getInt(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE_INDEX)));
                item.setQuality(Quality.UNUSUAL);
                item.setPrice(price);
                item.setImage(cursor.getString(cursor.getColumnIndex(ItemSchemaEntry.COLUMN_IMAGE)));

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
