package com.tlongdev.bktf.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;

import com.tlongdev.bktf.data.dao.PriceDao;
import com.tlongdev.bktf.util.Utility;

/**
 * Created by lngtr on 2017-12-04.
 */
public class PriceListLoader extends AsyncTaskLoader<Cursor> {

    private final PriceDao mPriceDao;

    private final ForceLoadContentObserver mObserver;

    private Cursor mCursor;
    private CancellationSignal mCancellationSignal;
    private final SupportSQLiteDatabase mDatabase;

    public PriceListLoader(Context context, PriceDao mPriceDao, SupportSQLiteDatabase mDatabase) {
        super(context);
        this.mPriceDao = mPriceDao;
        this.mDatabase = mDatabase;
        mObserver = new ForceLoadContentObserver();
    }

    @Override
    public Cursor loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
        }
        try {
            //Cursor cursor = mPriceDao.getPrices();
            Cursor cursor = queryAllPrices();
            cursor.registerContentObserver(mObserver);
            return cursor;
        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();

        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     *
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

    private Cursor queryAllPrices() {
        Cursor cursor = mDatabase.query("SELECT " +
                        "pricelist._id," +
                        "pricelist.defindex," +
                        "item_schema.item_name," +
                        "pricelist.quality," +
                        "pricelist.tradable," +
                        "pricelist.craftable," +
                        "pricelist.price_index," +
                        "pricelist.currency," +
                        "pricelist.price," +
                        "pricelist.max," +
                        Utility.getRawPriceQueryString(getContext()) + " raw_price," +
                        "pricelist.difference," +
                        "pricelist.australium" +
                        " FROM pricelist" +
                        " LEFT JOIN item_schema" +
                        " ON pricelist.defindex = item_schema.defindex" +
                        " ORDER BY last_update DESC",
                null
        );

        //Raw query is lazy, it won't actually query until we actually ask for the data.
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                return cursor;
            }
        }

        return cursor;
    }
}
