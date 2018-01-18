/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
