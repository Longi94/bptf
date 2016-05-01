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
import android.net.Uri;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.UserBackpackEntry;
import com.tlongdev.bktf.model.BackpackItem;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 18.
 */
public class LoadBackpackItemsInteractor extends AsyncTask<Void, Void, Void> {

    private static final String[] PROJECTION = new String[]{
            UserBackpackEntry._ID,
            UserBackpackEntry.COLUMN_DEFINDEX,
            UserBackpackEntry.COLUMN_QUALITY,
            UserBackpackEntry.COLUMN_CRAFT_NUMBER,
            UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE,
            UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT,
            UserBackpackEntry.COLUMN_ITEM_INDEX,
            UserBackpackEntry.COLUMN_PAINT,
            UserBackpackEntry.COLUMN_AUSTRALIUM,
            UserBackpackEntry.COLUMN_DECORATED_WEAPON_WEAR
    };

    @Inject ContentResolver mContentResolver;

    private final Callback mCallback;
    private final boolean mGuest;

    private List<BackpackItem> mItems;
    private List<BackpackItem> mNewItems;

    public LoadBackpackItemsInteractor(BptfApplication application, boolean guest, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
        mGuest = guest;
    }

    @Override
    protected Void doInBackground(Void... params) {

        mItems = new LinkedList<>();
        mNewItems = new LinkedList<>();

        Uri uri = mGuest ? UserBackpackEntry.CONTENT_URI_GUEST : UserBackpackEntry.CONTENT_URI;

        Cursor cursor = mContentResolver.query(uri, PROJECTION,
                UserBackpackEntry.COLUMN_POSITION + " >= ?",
                new String[]{"1"},
                UserBackpackEntry.COLUMN_POSITION + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                mItems.add(mapCursor(cursor));
            }
            cursor.close();
        }

        cursor = mContentResolver.query(uri, PROJECTION,
                UserBackpackEntry.COLUMN_POSITION + " = ?",
                new String[]{"-1"},
                UserBackpackEntry.COLUMN_POSITION + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                mNewItems.add(mapCursor(cursor));
            }
            cursor.close();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mCallback != null) {
            mCallback.onLoadBackpackItemFinished(mItems, mNewItems);
        }
    }

    @SuppressWarnings("WrongConstant")
    private BackpackItem mapCursor(Cursor cursor) {
        BackpackItem backpackItem = new BackpackItem();
        backpackItem.setId(cursor.getInt(0));
        backpackItem.setDefindex(cursor.getInt(1));
        backpackItem.setQuality(cursor.getInt(2));
        backpackItem.setCraftNumber(cursor.getInt(3));
        backpackItem.setTradable(cursor.getInt(4) == 0);
        backpackItem.setCraftable(cursor.getInt(5) == 0);
        backpackItem.setPriceIndex(cursor.getInt(6));
        backpackItem.setPaint(cursor.getInt(7));
        backpackItem.setAustralium(cursor.getInt(8) == 1);
        backpackItem.setWeaponWear(cursor.getInt(9));
        return backpackItem;
    }

    public interface Callback {
        void onLoadBackpackItemFinished(List<BackpackItem> items, List<BackpackItem> newItems);
    }
}
