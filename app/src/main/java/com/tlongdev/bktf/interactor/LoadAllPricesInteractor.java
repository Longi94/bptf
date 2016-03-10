package com.tlongdev.bktf.interactor;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseHelper;
import com.tlongdev.bktf.util.Utility;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class LoadAllPricesInteractor extends AsyncTask<Void, Void, Cursor> {

    private Context mContext;
    private Callback mCallback;

    public LoadAllPricesInteractor(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        SQLiteDatabase db = new DatabaseHelper(mContext).getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_DEFINDEX + "," +
                        DatabaseContract.ItemSchemaEntry.TABLE_NAME + "." + DatabaseContract.ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_ITEM_QUALITY + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_ITEM_TRADABLE + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_ITEM_CRAFTABLE + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_PRICE_INDEX + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_CURRENCY + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_PRICE + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_PRICE_HIGH + "," +
                        Utility.getRawPriceQueryString(mContext) + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_DIFFERENCE + "," +
                        DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_AUSTRALIUM +
                        " FROM " + DatabaseContract.PriceEntry.TABLE_NAME +
                        " LEFT JOIN " + DatabaseContract.ItemSchemaEntry.TABLE_NAME +
                        " ON " + DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_DEFINDEX + " = " + DatabaseContract.ItemSchemaEntry.TABLE_NAME + "." + DatabaseContract.ItemSchemaEntry.COLUMN_DEFINDEX +
                        " ORDER BY " + DatabaseContract.PriceEntry.COLUMN_LAST_UPDATE + " DESC",
                null
        );

        //Raw query is lazy, it won't actually query until we actually ask for the data.
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
            mCallback.onFinish(cursor);
        }
    }

    @Override
    protected void onCancelled(Cursor cursor) {
        if (mCallback != null) {
            mCallback.onFail();
        }
    }

    public interface Callback {
        void onFinish(Cursor prices);

        void onFail();
    }
}
