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

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public class LoadSelectorItemsInteractor extends AsyncTask<Void, Void, Cursor> {

    @Inject ContentResolver mContentResolver;

    private final String mQuery;
    private final Callback mCallback;

    public LoadSelectorItemsInteractor(BptfApplication application, String query, Callback callback) {
        application.getInteractorComponent().inject(this);
        mQuery = query;
        mCallback = callback;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        String[] selectionArgs = mQuery != null && mQuery.length() > 0 ?
                new String[]{"%" + mQuery + "%"} : new String[]{"ASDASD"}; //stupid

        Cursor cursor = mContentResolver.query(
                ItemSchemaEntry.CONTENT_URI,
                new String[]{
                        ItemSchemaEntry._ID,
                        ItemSchemaEntry.COLUMN_DEFINDEX,
                        ItemSchemaEntry.COLUMN_ITEM_NAME
                },
                ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ?",
                selectionArgs,
                ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC"
        );

        if (cursor != null) {
            cursor.getCount();
        }
        return cursor;
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
