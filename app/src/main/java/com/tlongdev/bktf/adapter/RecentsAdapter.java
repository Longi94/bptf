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

package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.analytics.HitBuilders;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.ui.activity.PriceHistoryActivity;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler view in the recents fragment.
 */
public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.ViewHolder> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = RecentsAdapter.class.getSimpleName();

    /**
     * The context
     */
    private Context mContext;

    /**
     * The data set
     */
    private Cursor mDataSet;

    /**
     * Constructor.
     *
     * @param context the context
     * @param dataSet the data set
     */
    public RecentsAdapter(Context context, Cursor dataSet) {
        this.mContext = context;
        this.mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_recents, parent, false);
        return new ViewHolder(v);
    }

    @SuppressWarnings("WrongConstant")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {

            //Get all the data from the cursor
            final Item item = new Item(
                    mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_DEFINDEX)),
                    mDataSet.getString(mDataSet.getColumnIndex(ItemSchemaEntry.COLUMN_ITEM_NAME)),
                    mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_ITEM_QUALITY)),
                    mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_ITEM_TRADABLE)) == 1,
                    mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_ITEM_CRAFTABLE)) == 1,
                    mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_AUSTRALIUM)) == 1,
                    mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE_INDEX)),
                    new Price(
                            mDataSet.getDouble(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE)),
                            mDataSet.getDouble(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)),
                            mDataSet.getDouble(mDataSet.getColumnIndex("raw_price")),
                            0 /* TODO last update */,
                            mDataSet.getDouble(mDataSet.getColumnIndex(PriceEntry.COLUMN_DIFFERENCE)),
                            mDataSet.getString(mDataSet.getColumnIndex(PriceEntry.COLUMN_CURRENCY))
                    )
            );

            holder.more.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    PopupMenu menu = new PopupMenu(mContext, holder.more);

                    menu.getMenuInflater().inflate(R.menu.popup_item, menu.getMenu());

                    menu.getMenu().getItem(0).setTitle(
                            Utility.isFavorite(mContext, item) ? "Remove from favorites" : "Add to favorites");

                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.history:

                                    Intent i = new Intent(mContext, PriceHistoryActivity.class);

                                    i.putExtra(PriceHistoryActivity.EXTRA_ITEM, item);

                                    mContext.startActivity(i);
                                    break;
                                case R.id.favorite:
                                    if (Utility.isFavorite(mContext, item)) {
                                        Utility.removeFromFavorites(mContext, item);
                                    } else {
                                        Utility.addToFavorites(mContext, item);
                                    }
                                    break;
                                case R.id.backpack_tf:
                                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                            item.getBackpackTfUrl())));
                                    break;
                            }
                            return true;
                        }
                    });

                    menu.show();
                }
            });

            holder.name.setText(item.getFormattedName(mContext, false));

            //Set the change indicator of the item
            holder.difference.setTextColor(item.getPrice().getDifferenceColor());
            holder.difference.setText(item.getPrice().getFormattedDifference(mContext));

            holder.icon.setImageDrawable(null);
            holder.background.setBackgroundColor(item.getColor(mContext, true));

            if (!item.isTradable()) {
                holder.quality.setVisibility(View.VISIBLE);
                if (!item.isCraftable()) {
                    holder.quality.setImageResource(R.drawable.uncraft_untrad);
                } else {
                    holder.quality.setImageResource(R.drawable.untrad);
                }
            } else if (!item.isCraftable()) {
                holder.quality.setVisibility(View.VISIBLE);
                holder.quality.setImageResource(R.drawable.uncraft);
            } else {
                holder.quality.setVisibility(View.GONE);
            }

            //Set the item icon
            Glide.with(mContext)
                    .load(item.getIconUrl(mContext))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.icon);

            if (item.getPriceIndex() != 0 && item.canHaveEffects()) {
                Glide.with(mContext)
                        .load(item.getEffectUrl(mContext))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.effect);
            } else {
                Glide.clear(holder.effect);
                holder.effect.setImageDrawable(null);
            }

            try {
                //Properly format the price
                holder.price.setText(item.getPrice().getFormattedPrice(mContext));
            } catch (Throwable t) {
                t.printStackTrace();

                ((BptfApplication)mContext.getApplicationContext()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                        .setDescription("Formatter exception:RecentsAdapter, Message: " + t.getMessage())
                        .setFatal(false)
                        .build());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.getCount();
    }

    /**
     * Replaces the cursor of the adapter
     *
     * @param data          the cursor that will replace the current one
     * @param closePrevious whether to close the previous cursor
     */
    public void swapCursor(Cursor data, boolean closePrevious) {
        if (closePrevious && mDataSet != null) mDataSet.close();
        mDataSet = data;
        notifyDataSetChanged();
    }

    /**
     * The view holder.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        View root;
        @Bind(R.id.more) View more;
        @Bind(R.id.icon_background) View background;
        @Bind(R.id.icon) ImageView icon;
        @Bind(R.id.effect) ImageView effect;
        @Bind(R.id.quality) ImageView quality;
        @Bind(R.id.name) TextView name;
        @Bind(R.id.price) TextView price;
        @Bind(R.id.difference) TextView difference;

        public ViewHolder(View view) {
            super(view);
            root = view;
            ButterKnife.bind(this, view);
        }
    }
}