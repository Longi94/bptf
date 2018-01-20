package com.tlongdev.bktf.presenter.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.adapter.CalculatorAdapter;
import com.tlongdev.bktf.data.DatabaseContract.CalculatorEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.interactor.LoadCalculatorItemsInteractor;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.fragment.CalculatorView;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 11.
 */
public class CalculatorPresenter implements Presenter<CalculatorView>,LoadCalculatorItemsInteractor.Callback, CalculatorAdapter.OnItemEditListener {

    /**
     * The columns to query
     */
    private static final String[] PRICE_LIST_COLUMNS = {
            PriceEntry.TABLE_NAME + "." + PriceEntry._ID,
            null,
    };

    /**
     * the selection
     */
    private static final String mSelection =
            PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_WEAPON_WEAR + " = ?";

    @Inject ContentResolver mContentResolver;

    private CalculatorView mView;

    private final BptfApplication mApplication;

    /**
     * The sum of the price of items in the list
     */
    private final Price mTotalPrice = new Price();

    public CalculatorPresenter(BptfApplication application) {
        application.getPresenterComponent().inject(this);
        mApplication = application;
        mTotalPrice.setCurrency(Currency.METAL);
        PRICE_LIST_COLUMNS[1] = Utility.getRawPriceQueryString(application) + " price_raw";
    }

    @Override
    public void attachView(CalculatorView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadItems() {
        LoadCalculatorItemsInteractor itemsInteractor = new LoadCalculatorItemsInteractor(
                mApplication, this);
        itemsInteractor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onLoadCalculatorItemsFinished(List<Item> items, List<Integer> count, double totalValue) {
        mTotalPrice.setValue(totalValue);
        if (mView != null) {
            mView.showItems(items, count, mTotalPrice);
        }
    }

    public void addItem(Item item) {
        Cursor cursor = mContentResolver.query(
                PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                mSelection,
                new String[]{
                        String.valueOf(item.getDefindex()),
                        String.valueOf(item.getQuality()),
                        String.valueOf(item.isTradable() ? 1 : 0),
                        String.valueOf(item.isCraftable() ? 1 : 0),
                        String.valueOf(item.getPriceIndex()),
                        String.valueOf(item.isAustralium() ? 1 : 0),
                        String.valueOf(item.getWeaponWear()),
                },
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Price price = new Price();
                price.setRawValue(cursor.getDouble(cursor.getColumnIndex("price_raw")));
                item.setPrice(price);
            }
            cursor.close();
        }

        if (item.getPrice() != null) {
            mTotalPrice.setValue(mTotalPrice.getValue() + item.getPrice().getRawValue());
        }

        if (mView != null) {
            mView.updatePrices(mTotalPrice);
        }

        ContentValues cv = new ContentValues();

        cv.put(CalculatorEntry.COLUMN_DEFINDEX, item.getDefindex());
        cv.put(CalculatorEntry.COLUMN_ITEM_QUALITY, item.getQuality());
        cv.put(CalculatorEntry.COLUMN_ITEM_TRADABLE, item.isTradable() ? 1 : 0);
        cv.put(CalculatorEntry.COLUMN_ITEM_CRAFTABLE, item.isCraftable() ? 1 : 0);
        cv.put(CalculatorEntry.COLUMN_PRICE_INDEX, item.getPriceIndex());
        cv.put(CalculatorEntry.COLUMN_AUSTRALIUM, item.isAustralium() ? 1 : 0);
        cv.put(CalculatorEntry.COLUMN_WEAPON_WEAR, item.getWeaponWear());
        cv.put(CalculatorEntry.COLUMN_COUNT, 1);

        mContentResolver.insert(CalculatorEntry.CONTENT_URI, cv);
        loadItems();
    }

    public void clearItems() {
        mTotalPrice.setValue(0);
        if (mView != null) {
            mView.updatePrices(mTotalPrice);
            mView.clearItems();
        }

        mContentResolver.delete(CalculatorEntry.CONTENT_URI, null, null);
    }

    @Override
    public void onItemDeleted(Item item, int count) {

        if (item.getPrice() != null) {
            mTotalPrice.setValue(mTotalPrice.getValue() - item.getPrice().getRawValue() * count);
        }

        if (mView != null) {
            mView.updatePrices(mTotalPrice);
        }

        mContentResolver.delete(CalculatorEntry.CONTENT_URI,
                CalculatorEntry.COLUMN_DEFINDEX + " = ? AND " +
                        CalculatorEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        CalculatorEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                        CalculatorEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                        CalculatorEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                        CalculatorEntry.COLUMN_AUSTRALIUM + " = ? AND " +
                        CalculatorEntry.COLUMN_WEAPON_WEAR + " = ?",
                new String[]{String.valueOf(item.getDefindex()),
                        String.valueOf(item.getQuality()),
                        item.isTradable() ? "1" : "0",
                        item.isCraftable() ? "1" : "0",
                        String.valueOf(item.getPriceIndex()),
                        item.isAustralium() ? "1" : "0",
                        String.valueOf(item.getWeaponWear())
                });
    }

    @Override
    public void onItemEdited(Item item, int oldCount, int newCount) {

        int diff = newCount - oldCount;
        if (diff == 0) return;

        if (item.getPrice() != null) {
            mTotalPrice.setValue(mTotalPrice.getValue() + diff * item.getPrice().getRawValue());
        }

        if (mView != null) {
            mView.updatePrices(mTotalPrice);
        }


        ContentValues values = new ContentValues();
        values.put(CalculatorEntry.COLUMN_COUNT, newCount);

        mContentResolver.update(CalculatorEntry.CONTENT_URI,
                values,
                CalculatorEntry.COLUMN_DEFINDEX + " = ? AND " +
                        CalculatorEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        CalculatorEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                        CalculatorEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                        CalculatorEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                        CalculatorEntry.COLUMN_AUSTRALIUM + " = ? AND " +
                        CalculatorEntry.COLUMN_WEAPON_WEAR + " = ?",
                new String[]{String.valueOf(item.getDefindex()),
                        String.valueOf(item.getQuality()),
                        item.isTradable() ? "1" : "0",
                        item.isCraftable() ? "1" : "0",
                        String.valueOf(item.getPriceIndex()),
                        item.isAustralium() ? "1" : "0",
                        String.valueOf(item.getWeaponWear())
                });
    }

    public Price getTotalPrice() {
        return mTotalPrice;
    }
}
