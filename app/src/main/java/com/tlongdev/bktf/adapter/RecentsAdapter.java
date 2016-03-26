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
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler view in the recents fragment.
 */
public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.ViewHolder> {

    @Inject Context mContext;

    private Cursor mDataSet;
    private OnMoreListener mListener;

    public RecentsAdapter(BptfApplication application) {
        application.getAdapterComponent().inject(this);
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
            
            Price price = new Price();
            price.setValue(mDataSet.getDouble(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE)));
            price.setHighValue(mDataSet.getDouble(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)));
            price.setDifference(mDataSet.getDouble(mDataSet.getColumnIndex(PriceEntry.COLUMN_DIFFERENCE)));
            price.setCurrency(mDataSet.getString(mDataSet.getColumnIndex(PriceEntry.COLUMN_CURRENCY)));
            price.setRawValue(mDataSet.getDouble(mDataSet.getColumnIndex("raw_price")));

            //Get all the data from the cursor
            final Item item = new Item();
            item.setDefindex(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_DEFINDEX)));
            item.setName(mDataSet.getString(mDataSet.getColumnIndex(ItemSchemaEntry.COLUMN_ITEM_NAME)));
            item.setQuality(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_ITEM_QUALITY)));
            item.setTradable(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_ITEM_TRADABLE)) == 1);
            item.setCraftable(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_ITEM_CRAFTABLE)) == 1);
            item.setAustralium(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_AUSTRALIUM)) == 1);
            item.setPriceIndex(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE_INDEX)));
            item.setPrice(price);

            holder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onMoreClicked(v, item);
                    }
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
            Glide.with(mContext).load(item.getIconUrl()).into(holder.icon);

            if (item.getPriceIndex() != 0 && item.canHaveEffects()) {
                Glide.with(mContext).load(item.getEffectUrl()).into(holder.effect);
            } else {
                Glide.clear(holder.effect);
                holder.effect.setImageDrawable(null);
            }

            try {
                //Properly format the price
                holder.price.setText(item.getPrice().getFormattedPrice(mContext));
            } catch (Throwable t) {
                Crashlytics.logException(t);
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
     */
    public void swapCursor(Cursor data) {
        if (mDataSet != null) mDataSet.close();
        mDataSet = data;
        notifyDataSetChanged();
    }

    public void setListener(OnMoreListener listener) {
        mListener = listener;
    }

    /**
     * The view holder.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View root;
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

    public interface OnMoreListener {
        void onMoreClicked(View view, Item item);
    }
}