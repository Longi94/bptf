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

package com.tlongdev.bktf.presenter.activity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.Toast;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.DatabaseContract.CalculatorEntry;
import com.tlongdev.bktf.interactor.LoadUnusualEffectsInteractor;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.presenter.fragment.UnusualPresenter;
import com.tlongdev.bktf.ui.view.activity.ItemChooserView;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 24.
 */
public class ItemChooserPresenter implements Presenter<ItemChooserView>,LoadUnusualEffectsInteractor.Callback {

    @Inject ContentResolver mContentResolver;

    private ItemChooserView mView;
    private final BptfApplication mApplication;

    public ItemChooserPresenter(BptfApplication application) {
        application.getPresenterComponent().inject(this);
        mApplication = application;
    }

    @Override
    public void attachView(ItemChooserView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadEffects() {
        LoadUnusualEffectsInteractor interactor = new LoadUnusualEffectsInteractor(
                mApplication, "", UnusualPresenter.ORDER_BY_NAME, this
        );
        interactor.execute();
    }

    @Override
    public void onUnusualEffectsLoadFinished(List<Item> items) {
        if (mView != null) {
            mView.showEffects(items);
        }
    }

    public boolean checkCalculator(Item item) {
        Cursor cursor = mContentResolver.query(
                CalculatorEntry.CONTENT_URI,
                null,
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
                },
                null
        );

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                mView.showToast("You have already added this item", Toast.LENGTH_SHORT);
                return false;
            }
            cursor.close();
        }
        return true;
    }
}
