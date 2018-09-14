package com.tlongdev.bktf.interactor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.UserBackpackEntry;
import com.tlongdev.bktf.model.BackpackItem;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Long
 * @since 2016. 03. 18.
 */
public class LoadBackpackItemsInteractor extends AsyncTask<Void, Void, Void> {

    @Inject @Named("readable")
    SQLiteDatabase mDatabase;

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

        String tableName = mGuest ? UserBackpackEntry.TABLE_NAME_GUEST : UserBackpackEntry.TABLE_NAME;

        String sql = "SELECT " +
                tableName + "." + UserBackpackEntry._ID + "," +
                tableName + "." + UserBackpackEntry.COLUMN_DEFINDEX + "," +
                tableName + "." + UserBackpackEntry.COLUMN_QUALITY + "," +
                tableName + "." + UserBackpackEntry.COLUMN_CRAFT_NUMBER + "," +
                tableName + "." + UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE + "," +
                tableName + "." + UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT + "," +
                tableName + "." + UserBackpackEntry.COLUMN_ITEM_INDEX + "," +
                tableName + "." + UserBackpackEntry.COLUMN_PAINT + "," +
                tableName + "." + UserBackpackEntry.COLUMN_AUSTRALIUM + "," +
                tableName + "." + UserBackpackEntry.COLUMN_DECORATED_WEAPON_WEAR + "," +
                tableName + "." + UserBackpackEntry.COLUMN_POSITION + "," +
                ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_IMAGE +
                " FROM " + tableName +
                " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                " ON " + tableName + "." + UserBackpackEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                " ORDER BY " + UserBackpackEntry.COLUMN_POSITION + " ASC";

        Cursor cursor = mDatabase.rawQuery(sql, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_POSITION)) == -1) {
                    mNewItems.add(mapCursor(cursor));
                } else {
                    mItems.add(mapCursor(cursor));
                }
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
        backpackItem.setImage(cursor.getString(11));
        return backpackItem;
    }

    public interface Callback {
        void onLoadBackpackItemFinished(List<BackpackItem> items, List<BackpackItem> newItems);
    }
}
