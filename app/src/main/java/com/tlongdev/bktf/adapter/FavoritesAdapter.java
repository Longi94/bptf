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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    @Inject Context mContext;
    @Inject Tracker mTracker;

    private List<Item> mDataSet;

    private OnMoreListener mListener;

    public FavoritesAdapter(BptfApplication application) {
        application.getAdapterComponent().inject(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_favorites, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.size() > position) {

            final Item item = mDataSet.get(position);

            holder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onMoreClicked(v, item);
                    }
                }
            });

            holder.name.setText(item.getFormattedName(mContext, false));
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

            if (item.getPrice() != null) {
                //Set the change indicator of the item
                holder.difference.setVisibility(View.VISIBLE);
                holder.difference.setTextColor(item.getPrice().getDifferenceColor());
                holder.difference.setText(item.getPrice().getFormattedDifference(mContext));

                try {
                    //Properly format the price
                    holder.price.setText(item.getPrice().getFormattedPrice(mContext));
                } catch (Throwable t) {
                    t.printStackTrace();

                    mTracker.send(new HitBuilders.ExceptionBuilder()
                            .setDescription("Formatter exception:RecentsAdapter, Message: " + t.getMessage())
                            .setFatal(false)
                            .build());
                }
            } else {
                holder.price.setText("Price Unknown");
                holder.difference.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    public void setDataSet(List<Item> dataSet) {
        this.mDataSet = dataSet;
    }

    public void setListener(OnMoreListener listener) {
        mListener = listener;
    }

    public void removeItem(Item item) {
        notifyItemRemoved(mDataSet.indexOf(item));
        mDataSet.remove(item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final View root;
        @Bind(R.id.more) View more;
        @Bind(R.id.icon_background) View background;

        @Bind(R.id.icon) ImageView icon;
        @Bind(R.id.effect) ImageView effect;
        @Bind(R.id.quality) ImageView quality;

        @Bind(R.id.name) TextView name;
        @Bind(R.id.price) TextView price;
        @Bind(R.id.difference) TextView difference;

        /**
         * Constructor.
         *
         * @param view the root view
         */
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
