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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.model.User;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler view in the search activity
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private static final int VIEW_TYPE_PRICE = 0;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    @Inject Tracker mTracker;
    @Inject Context mContext;

    private Cursor mDataSet;
    private boolean mLoading;
    private User mUser;

    private OnSearchClickListener mListener;

    public SearchAdapter(BptfApplication application) {
        application.getAdapterComponent().inject(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_search, parent, false);
        return new ViewHolder(v);
    }

    @SuppressWarnings("WrongConstant")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {
            switch (getItemViewType(position)) {
                //We found a user, show some info
                case VIEW_TYPE_USER:
                    holder.more.setVisibility(View.GONE);
                    if (mUser != null) {
                        holder.loading.setVisibility(View.GONE);
                        holder.priceLayout.setVisibility(View.VISIBLE);

                        //open the user activity when the user clicks on the view
                        holder.root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mListener != null) {
                                    mListener.onUserClicked(mUser);
                                }
                            }
                        });

                        holder.name.setText(mUser.getName());
                        Glide.with(mContext)
                                .load(mUser.getAvatarUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(holder.icon);
                    }
                    break;
                //Lading view, searching for the user
                case VIEW_TYPE_LOADING:
                    holder.loading.setVisibility(View.VISIBLE);
                    holder.priceLayout.setVisibility(View.GONE);

                    holder.name.setText(null);
                    holder.icon.setImageDrawable(null);
                    break;
                //Simple price view
                case VIEW_TYPE_PRICE:
                    holder.priceLayout.setVisibility(View.VISIBLE);
                    holder.more.setVisibility(View.VISIBLE);
                    holder.loading.setVisibility(View.GONE);
                    
                    Price price = new Price();
                    price.setValue(mDataSet.getDouble(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE)));
                    price.setHighValue(mDataSet.getDouble(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)));
                    price.setCurrency(mDataSet.getString(mDataSet.getColumnIndex(PriceEntry.COLUMN_CURRENCY)));

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

                    holder.name.setText(item.getFormattedName(mContext));

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
                        holder.price.setText(item.getPrice().getFormattedPrice(mContext));
                    } catch (Throwable t) {
                        t.printStackTrace();

                        mTracker.send(new HitBuilders.ExceptionBuilder()
                                .setDescription("Formatter exception:SearchAdapter, Message: " + t.getMessage())
                                .setFatal(false)
                                .build());
                    }
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = mDataSet == null ? 0 : mDataSet.getCount();
        count += mUser != null || mLoading ? 1 : 0;
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (mLoading) {
            return position == 0 ? VIEW_TYPE_LOADING : VIEW_TYPE_PRICE;
        }
        return mUser != null && position == 0 ? VIEW_TYPE_USER : VIEW_TYPE_PRICE;
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
    }

    /**
     * Tells whether the list should show a loading animation or not
     *
     * @param loading whether it is loading or not
     */
    public void setLoading(boolean loading) {
        this.mLoading = loading;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public void setListener(OnSearchClickListener listener) {
        mListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View root;

        @Bind(R.id.loading_layout) View loading;
        @Bind(R.id.price_layout) View priceLayout;
        @Bind(R.id.icon) ImageView icon;
        @Bind(R.id.icon_background) View background;
        @Bind(R.id.effect) ImageView effect;
        @Bind(R.id.more) ImageView more;
        @Bind(R.id.quality) ImageView quality;
        @Bind(R.id.name) TextView name;
        @Bind(R.id.price) TextView price;

        public ViewHolder(View view) {
            super(view);
            root = view;
            ButterKnife.bind(this, view);
        }
    }

    public interface OnSearchClickListener {
        void onMoreClicked(View view, Item item);
        void onUserClicked(User user);
    }
}
