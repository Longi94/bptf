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
import android.graphics.Bitmap;
import android.os.Binder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import java.util.concurrent.ExecutionException;

/**
 * Created by Long on 2015. 12. 21..
 */
public class FavoritesWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FavoritesRemoteViewsFactory(getApplicationContext(), intent);
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

        private Cursor mDataSet;

        private Context mContext;

        private int widgetId;

        private String sql;

        public FavoritesRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        }

        @Override
        public void onCreate() {
            sql = "SELECT " +
                    DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_DEFINDEX + "," +
                    DatabaseContract.ItemSchemaEntry.TABLE_NAME + "." + DatabaseContract.ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                    DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_ITEM_QUALITY + "," +
                    DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_ITEM_TRADABLE + "," +
                    DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_ITEM_CRAFTABLE + "," +
                    DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_PRICE_INDEX + "," +
                    DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_CURRENCY + "," +
                    DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_PRICE + "," +
                    DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_PRICE_HIGH + "," +
                    Utility.getRawPriceQueryString(mContext) + "," +
                    DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_DIFFERENCE + "," +
                    DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_AUSTRALIUM +
                    " FROM " + DatabaseContract.FavoritesEntry.TABLE_NAME +
                    " LEFT JOIN " + DatabaseContract.PriceEntry.TABLE_NAME +
                    " ON " + DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_DEFINDEX + " = " + DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_DEFINDEX +
                    " AND " + DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_ITEM_TRADABLE + " = " + DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_ITEM_TRADABLE +
                    " AND " + DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_ITEM_CRAFTABLE + " = " + DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_ITEM_CRAFTABLE +
                    " AND " + DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_PRICE_INDEX + " = " + DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_PRICE_INDEX +
                    " AND " + DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_ITEM_QUALITY + " = " + DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_ITEM_QUALITY +
                    " AND " + DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_AUSTRALIUM + " = " + DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry.COLUMN_AUSTRALIUM +
                    " LEFT JOIN " + DatabaseContract.ItemSchemaEntry.TABLE_NAME +
                    " ON " + DatabaseContract.FavoritesEntry.TABLE_NAME + "." + DatabaseContract.FavoritesEntry.COLUMN_DEFINDEX + " = " + DatabaseContract.ItemSchemaEntry.TABLE_NAME + "." + DatabaseContract.ItemSchemaEntry.COLUMN_DEFINDEX +
                    " ORDER BY " + DatabaseContract.ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC";


            final long token = Binder.clearCallingIdentity();
            try {
                mDataSet = mContext.getContentResolver().query(
                        DatabaseContract.RAW_QUERY_URI,
                        null, sql, null, null
                );
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
                mDataSet = mContext.getContentResolver().query(
                        DatabaseContract.RAW_QUERY_URI,
                        null, sql, null, null
                );
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

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_favorites_widgets);

            if (mDataSet != null && mDataSet.moveToPosition(position)) {
                Item item = new Item(mDataSet.getInt(COLUMN_DEFINDEX),
                        mDataSet.getString(COLUMN_NAME),
                        mDataSet.getInt(COLUMN_QUALITY),
                        mDataSet.getInt(COLUMN_TRADABLE) == 1,
                        mDataSet.getInt(COLUMN_CRAFTABLE) == 1,
                        mDataSet.getInt(COLUMN_AUSTRALIUM) == 1,
                        mDataSet.getInt(COLUMN_PRICE_INDEX),
                        null
                );

                if (mDataSet.getString(COLUMN_CURRENCY) != null) {
                    item.setPrice(new Price(mDataSet.getDouble(COLUMN_PRICE),
                            mDataSet.getDouble(COLUMN_PRICE_MAX),
                            mDataSet.getDouble(COLUMN_PRICE_RAW),
                            0,
                            mDataSet.getDouble(COLUMN_DIFFERENCE),
                            mDataSet.getString(COLUMN_CURRENCY)));
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
                            .load(item.getIconUrl(mContext))
                            .asBitmap()
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();
                    rv.setImageViewBitmap(R.id.icon, bitmap);

                    if (item.getPriceIndex() != 0 && item.canHaveEffects()) {
                        bitmap = Glide.with(mContext)
                                .load(item.getEffectUrl(mContext))
                                .asBitmap()
                                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .get();
                        rv.setImageViewBitmap(R.id.effect, bitmap);
                    } else {
                        rv.setImageViewBitmap(R.id.effect, null);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    rv.setImageViewBitmap(R.id.effect, null);
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
