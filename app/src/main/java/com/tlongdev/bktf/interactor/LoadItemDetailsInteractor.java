package com.tlongdev.bktf.interactor;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.DatabaseContract.UserBackpackEntry;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.model.Price;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 24.
 */
public class LoadItemDetailsInteractor extends AsyncTask<Void, Void, BackpackItem> {

    @Inject ContentResolver mContentResolver;

    private final int mId;
    private final boolean mGuest;
    private final Callback mCallback;

    public LoadItemDetailsInteractor(BptfApplication application, int id, boolean guest,
                                     Callback callback) {
        application.getInteractorComponent().inject(this);
        mId = id;
        mGuest = guest;
        mCallback = callback;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected BackpackItem doInBackground(Void... params) {

        BackpackItem item = new BackpackItem();

        Cursor cursor = mContentResolver.query(
                mGuest ? UserBackpackEntry.CONTENT_URI_GUEST : UserBackpackEntry.CONTENT_URI,
                new String[]{
                        UserBackpackEntry._ID,
                        UserBackpackEntry.COLUMN_DEFINDEX,
                        UserBackpackEntry.COLUMN_QUALITY,
                        UserBackpackEntry.COLUMN_CRAFT_NUMBER,
                        UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE,
                        UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT,
                        UserBackpackEntry.COLUMN_ITEM_INDEX,
                        UserBackpackEntry.COLUMN_PAINT,
                        UserBackpackEntry.COLUMN_AUSTRALIUM,
                        UserBackpackEntry.COLUMN_CREATOR_NAME,
                        UserBackpackEntry.COLUMN_GIFTER_NAME,
                        UserBackpackEntry.COLUMN_CUSTOM_NAME,
                        UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION,
                        UserBackpackEntry.COLUMN_LEVEL,
                        UserBackpackEntry.COLUMN_EQUIPPED,
                        UserBackpackEntry.COLUMN_ORIGIN,
                        UserBackpackEntry.COLUMN_DECORATED_WEAPON_WEAR
                },
                UserBackpackEntry._ID + " = ?",
                new String[]{String.valueOf(mId)},
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                item.setDefindex(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_DEFINDEX)));
                item.setQuality(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_QUALITY)));
                item.setCraftNumber(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_CRAFT_NUMBER)));
                item.setTradable(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE)) == 0);
                item.setCraftable(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT)) == 0);
                item.setPriceIndex(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_ITEM_INDEX)));
                item.setPaint(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_PAINT)));
                item.setAustralium(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_AUSTRALIUM)) == 1);
                item.setCreatorName(cursor.getString(cursor.getColumnIndex(UserBackpackEntry.COLUMN_CREATOR_NAME)));
                item.setGifterName(cursor.getString(cursor.getColumnIndex(UserBackpackEntry.COLUMN_GIFTER_NAME)));
                item.setCustomName(cursor.getString(cursor.getColumnIndex(UserBackpackEntry.COLUMN_CUSTOM_NAME)));
                item.setCustomDescription(cursor.getString(cursor.getColumnIndex(UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION)));
                item.setLevel(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_LEVEL)));
                item.setEquipped(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_EQUIPPED)) == 1);
                item.setOrigin(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_ORIGIN)));
                item.setWeaponWear(cursor.getInt(cursor.getColumnIndex(UserBackpackEntry.COLUMN_DECORATED_WEAPON_WEAR)));
            }
            cursor.close();
        }

        cursor = mContentResolver.query(
                ItemSchemaEntry.CONTENT_URI,
                new String[]{
                        ItemSchemaEntry._ID,
                        ItemSchemaEntry.COLUMN_IMAGE,
                },
                ItemSchemaEntry.COLUMN_DEFINDEX + " = ?",
                new String[]{String.valueOf(item.getDefindex())},
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                item.setImage(cursor.getString(cursor.getColumnIndex(ItemSchemaEntry.COLUMN_IMAGE)));
            }
            cursor.close();
        }

        cursor = mContentResolver.query(
                PriceEntry.CONTENT_URI,
                new String[]{
                        PriceEntry._ID,
                        PriceEntry.COLUMN_PRICE,
                        PriceEntry.COLUMN_PRICE_HIGH,
                        PriceEntry.COLUMN_CURRENCY
                },
                PriceEntry.COLUMN_DEFINDEX + " = ? AND " +
                        PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                        PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                        PriceEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                        PriceEntry.COLUMN_AUSTRALIUM + " = ?",
                new String[]{
                        String.valueOf(item.getDefindex()),
                        String.valueOf(item.getQuality()),
                        String.valueOf(item.isTradable() ? 1 : 0),
                        String.valueOf(item.isCraftable() ? 1 : 0),
                        String.valueOf(item.getPriceIndex()),
                        String.valueOf(item.isAustralium() ? 1 : 0)
                },
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Price price = new Price();
                price.setValue(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE)));
                price.setHighValue(cursor.getDouble(cursor.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)));
                price.setCurrency(cursor.getString(cursor.getColumnIndex(PriceEntry.COLUMN_CURRENCY)));

                item.setPrice(price);
            }
            cursor.close();
        }
        return item;
    }

    @Override
    protected void onPostExecute(BackpackItem item) {
        if (mCallback != null) {
            mCallback.onItemDetailsLoaded(item);
        }
    }

    public interface Callback {
        void onItemDetailsLoaded(BackpackItem item);
    }
}
