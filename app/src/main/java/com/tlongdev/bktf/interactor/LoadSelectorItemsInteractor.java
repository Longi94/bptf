package com.tlongdev.bktf.interactor;

import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.dao.ItemSchemaDao;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public class LoadSelectorItemsInteractor extends AsyncTask<Void, Void, Cursor> {

    @Inject
    ItemSchemaDao mItemSchemaDao;

    private final String mQuery;
    private final Callback mCallback;

    public LoadSelectorItemsInteractor(BptfApplication application, String query, Callback callback) {
        application.getInteractorComponent().inject(this);
        mQuery = query;
        mCallback = callback;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        if (mQuery != null && mQuery.length() > 0) {
            return mItemSchemaDao.find(mQuery);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        if (mCallback != null) {
            mCallback.onSelectorItemsLoaded(cursor);
        }
    }

    public interface Callback {
        void onSelectorItemsLoaded(Cursor items);
    }
}
