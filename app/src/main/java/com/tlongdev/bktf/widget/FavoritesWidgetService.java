/**
 * Copyright 2015 Long Tran
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

package com.tlongdev.bktf.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Binder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.FavoritesEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

public class FavoritesWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FavoritesRemoteViewsFactory((BptfApplication) getApplication(), intent);
    }

    /**
     * Created by Long on 2015. 12. 21..
     */
    public class FavoritesRemoteViewsFactory implements RemoteViewsFactory {

        /**
         * Indexes for the columns
         */
        public static final int COLUMN_DEFINDEX = 0;
        public static final int COLUMN_NAME = 1;
        public static final int COLUMN_QUALITY = 2;
        public static final int COLUMN_TRADABLE = 3;
        public static final int COLUMN_CRAFTABLE = 4;
        public static final int COLUMN_PRICE_INDEX = 5;
        public static final int COLUMN_CURRENCY = 6;
        public static final int COLUMN_PRICE = 7;
        public static final int COLUMN_PRICE_MAX = 8;
        public static final int COLUMN_PRICE_RAW = 9;
        public static final int COLUMN_DIFFERENCE = 10;
        public static final int COLUMN_AUSTRALIUM = 11;

        @Inject Context mContext;
        @Inject @Named("readable") SQLiteDatabase mDatabase;

        private Cursor mDataSet;

        private final int widgetId;

        private String sql;

        public FavoritesRemoteViewsFactory(BptfApplication application, Intent intent) {
            application.getServiceComponent().inject(this);
            widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        }

        @Override
        public void onCreate() {
            sql = "SELECT " +
                    FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + "," +
                    ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                    FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_QUALITY + "," +
                    FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_TRADABLE + "," +
                    FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_CRAFTABLE + "," +
                    FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_PRICE_INDEX + "," +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                    Utility.getRawPriceQueryString(mContext) + "," +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DIFFERENCE + "," +
                    FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_AUSTRALIUM +
                    " FROM " + FavoritesEntry.TABLE_NAME +
                    " LEFT JOIN " + PriceEntry.TABLE_NAME +
                    " ON " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX +
                    " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_TRADABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE +
                    " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_CRAFTABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE +
                    " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_PRICE_INDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX +
                    " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_QUALITY + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY +
                    " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_AUSTRALIUM + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                    " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                    " ON " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                    " ORDER BY " + ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC";


            final long token = Binder.clearCallingIdentity();
            try {
                mDataSet = mDatabase.rawQuery(sql, null);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        @Override
        public void onDataSetChanged() {
            if (mDataSet != null) {
                mDataSet.close();
            }
            final long token = Binder.clearCallingIdentity();
            try {
                mDataSet = mDatabase.rawQuery(sql, null);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        @Override
        public void onDestroy() {
            if (mDataSet != null) {
                mDataSet.close();
            }
        }

        @Override
        public int getCount() {
            return mDataSet == null ? 0 : mDataSet.getCount();
        }

        @SuppressWarnings("WrongConstant")
        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_favorites_widgets);

            if (mDataSet != null && mDataSet.moveToPosition(position)) {
                Item item = new Item();
                item.setDefindex(mDataSet.getInt(COLUMN_DEFINDEX));
                item.setName(mDataSet.getString(COLUMN_NAME));
                item.setQuality(mDataSet.getInt(COLUMN_QUALITY));
                item.setTradable(mDataSet.getInt(COLUMN_TRADABLE) == 1);
                item.setCraftable(mDataSet.getInt(COLUMN_CRAFTABLE) == 1);
                item.setAustralium(mDataSet.getInt(COLUMN_AUSTRALIUM) == 1);
                item.setPriceIndex(mDataSet.getInt(COLUMN_PRICE_INDEX));

                if (mDataSet.getString(COLUMN_CURRENCY) != null) {
                    Price price = new Price();
                    price.setValue(mDataSet.getDouble(COLUMN_PRICE));
                    price.setHighValue(mDataSet.getDouble(COLUMN_PRICE_MAX));
                    price.setRawValue(mDataSet.getDouble(COLUMN_PRICE_RAW));
                    price.setDifference(mDataSet.getDouble(COLUMN_DIFFERENCE));
                    price.setCurrency(mDataSet.getString(COLUMN_CURRENCY));
                    item.setPrice(price);
                }

                final long token = Binder.clearCallingIdentity();
                try {
                    rv.setTextViewText(R.id.name, item.getFormattedName(mContext));
                } finally {
                    Binder.restoreCallingIdentity(token);
                }

                if (item.getPrice() != null) {
                    rv.setTextViewText(R.id.price, item.getPrice().getFormattedPrice(mContext));
                } else {
                    rv.setTextViewText(R.id.price, "Price Unknown");
                }

                try {
                    Bitmap bitmap = Glide.with(mContext)
                            .asBitmap()
                            .load(item.getIconUrl(mContext))
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();
                    rv.setImageViewBitmap(R.id.icon, bitmap);

                    if (item.getPriceIndex() != 0 && item.canHaveEffects()) {
                        bitmap = Glide.with(mContext)
                                .asBitmap()
                                .load(item.getEffectUrl())
                                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .get();
                        rv.setImageViewBitmap(R.id.effect, bitmap);
                    } else {
                        rv.setImageViewBitmap(R.id.effect, null);
                    }
                } catch (InterruptedException e) {
                    Crashlytics.logException(e);
                    rv.setImageViewBitmap(R.id.effect, null);
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    rv.setImageViewBitmap(R.id.effect, null);
                    e.printStackTrace();
                }

                if (!item.isTradable()) {
                    rv.setViewVisibility(R.id.quality, View.VISIBLE);
                    if (!item.isCraftable()) {
                        rv.setImageViewResource(R.id.quality, R.drawable.uncraft_untrad);
                    } else {
                        rv.setImageViewResource(R.id.quality, R.drawable.untrad);
                    }
                } else if (!item.isCraftable()) {
                    rv.setViewVisibility(R.id.quality, View.VISIBLE);
                    rv.setImageViewResource(R.id.quality, R.drawable.uncraft);
                } else {
                    rv.setViewVisibility(R.id.quality, View.GONE);
                }

                rv.setInt(R.id.frame_layout, "setBackgroundColor", item.getColor(mContext, false));
            }
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
