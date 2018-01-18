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

package com.tlongdev.bktf.presenter.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.adapter.CalculatorAdapter;
import com.tlongdev.bktf.data.DatabaseContract.CalculatorEntry;
import com.tlongdev.bktf.data.dao.PriceDao;
import com.tlongdev.bktf.interactor.LoadCalculatorItemsInteractor;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.fragment.CalculatorView;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 11.
 */
public class CalculatorPresenter implements Presenter<CalculatorView>,LoadCalculatorItemsInteractor.Callback, CalculatorAdapter.OnItemEditListener {

    @Inject
    ContentResolver mContentResolver;
    @Inject
    PriceDao mPriceDao;

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
        com.tlongdev.bktf.data.entity.Price price = mPriceDao.findPrice(
                item.getDefindex(),
                item.getQuality(),
                item.isTradable(),
                item.isCraftable(),
                item.isAustralium(),
                item.getPriceIndex(),
                item.getWeaponWear()
        );

        if (price != null) {
            Price itemPrice = new Price();
            itemPrice.setRawValue(price.getRawValue());
            item.setPrice(itemPrice);
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
