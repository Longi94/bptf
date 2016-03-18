/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler view in the backpack activity-
 */
public class BackpackAdapter extends RecyclerView.Adapter<BackpackAdapter.ViewHolder> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = BackpackAdapter.class.getSimpleName();

    /**
     * View types
     */
    public static final int VIEW_TYPE_ITEM = 0;
    public static final int VIEW_TYPE_HEADER = 1;

    /**
     * Cursor containing the non new items.
     */
    private List<BackpackItem> mDataSet;

    /**
     * Cursor containing the new items (items with no slot in the backpack)
     */
    private List<BackpackItem> mDataSetNew;

    /**
     * The context.
     */
    private Context mContext;

    /**
     * Determines which table to read from.
     */
    private boolean isGuest = false;

    /**
     * The number of new items.
     */
    private int newItemSlots = 0;

    private OnItemClickedListener mListener;

    /**
     * Constructor
     *
     * @param context the context
     * @param isGuest determines which table to read from
     */
    public BackpackAdapter(Context context, boolean isGuest) {
        this.mContext = context;
        this.isGuest = isGuest;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycler_backpack, parent, false);
                break;
            case VIEW_TYPE_HEADER:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycler_backpack_header, parent, false);
                break;
            default:
                return null;
        }

        return new ViewHolder(v, viewType);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            //Header type
            case VIEW_TYPE_HEADER:
                //Check if there are new items or not
                if (newItemSlots > 0) {
                    if (position == 0) {
                        //Header of the new items
                        holder.header.setText(mContext.getString(R.string.header_new_items));
                    } else {
                        //Header of pages
                        holder.header.setText(mContext.getString(R.string.header_page,
                                (position - newItemSlots) / 51 + 1));
                    }
                } else {
                    //Header of pages
                    holder.header.setText(mContext.getString(R.string.header_page,
                            position / 51 + 1));
                }
                break;
            //Backpack item type
            case VIEW_TYPE_ITEM:
                final BackpackItem backpackItem;
                if (newItemSlots > 0) {
                    //Check if there are new items or not
                    if (position < newItemSlots) {
                        //Item is a new item
                        backpackItem = position > mDataSetNew.size() ? new BackpackItem() : mDataSetNew.get(position - 1);
                    } else {
                        //Item is not a new item
                        backpackItem = mDataSet.get((position - newItemSlots) - ((position - newItemSlots) / 51) - 1);
                    }
                } else {
                    //There is no new item in the backpack
                    backpackItem = mDataSet.get(position - (position / 51) - 1);
                }

                Glide.clear(holder.icon);
                Glide.clear(holder.effect);
                Glide.clear(holder.paint);

                //Reset item slot to an empty slot
                holder.icon.setImageDrawable(null);
                holder.effect.setImageDrawable(null);
                holder.paint.setImageDrawable(null);
                holder.root.setCardBackgroundColor(Utility.getColor(mContext, R.color.card_color));
                holder.root.setOnClickListener(null);
                holder.quality.setVisibility(View.GONE);

                if (backpackItem.getDefindex() != 0) {

                    //Set the background to the color of the quality
                    holder.root.setCardBackgroundColor(backpackItem.getColor(mContext, true));

                    Glide.with(mContext)
                            .load(backpackItem.getIconUrl(mContext))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.icon);

                    if (backpackItem.getPriceIndex() != 0 && backpackItem.canHaveEffects()) {
                        Glide.with(mContext)
                                .load(backpackItem.getEffectUrl(mContext))
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(holder.effect);
                    }

                    if (!backpackItem.isTradable()) {
                        holder.quality.setVisibility(View.VISIBLE);
                        if (!backpackItem.isCraftable()) {
                            holder.quality.setImageResource(R.drawable.uncraft_untrad);
                        } else {
                            holder.quality.setImageResource(R.drawable.untrad);
                        }
                    } else if (!backpackItem.isCraftable()) {
                        holder.quality.setVisibility(View.VISIBLE);
                        holder.quality.setImageResource(R.drawable.uncraft);
                    }

                    if (BackpackItem.isPaint(backpackItem.getPaint())) {
                        Glide.with(mContext)
                                .load("file:///android_asset/paint/" + backpackItem.getPaint() + ".png")
                                .into(holder.paint);
                    }

                    //The on click listener for an item
                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null) {
                                mListener.OnItemClicked(holder, backpackItem);
                            }
                        }
                    });
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        //Magic. Not really.
        int count = mDataSet == null ? 0 : mDataSet.size() + mDataSet.size() / 50;
        if (newItemSlots > 0) {
            return count + newItemSlots;
        } else {
            return count;
        }
    }

    @Override
    public int getItemViewType(int position) {
        //More black magic
        //Check if there are new items
        if (newItemSlots > 0) {
            if (position < newItemSlots) {
                if (position == 0) {
                    return VIEW_TYPE_HEADER;
                } else {
                    return VIEW_TYPE_ITEM;
                }
            } else {
                if ((position - newItemSlots) % 51 == 0) {
                    return VIEW_TYPE_HEADER;
                } else {
                    return VIEW_TYPE_ITEM;
                }
            }
        } else if (position % 51 == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    public void setDataSet(List<BackpackItem> items, List<BackpackItem> newItems) {
        mDataSet = items;
        mDataSetNew = newItems;
        if (mDataSetNew != null && mDataSetNew.size() > 0) {
            newItemSlots = mDataSetNew.size() - mDataSetNew.size() % 5 + 6;
        }
    }

    public void setListener(OnItemClickedListener listener) {
        mListener = listener;
    }

    /**
     * View holder for the views.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Nullable @Bind(R.id.text_view_header) public TextView header = null;
        @Nullable @Bind(R.id.icon) public ImageView icon = null;
        @Nullable @Bind(R.id.effect) public ImageView effect = null;
        @Nullable @Bind(R.id.paint) public ImageView paint = null;
        @Nullable @Bind(R.id.quality) public ImageView quality;
        public CardView root = null;

        /**
         * Constructor
         *
         * @param view     the root view
         * @param viewType the type of the view in the list
         */
        public ViewHolder(View view, int viewType) {
            super(view);
            ButterKnife.bind(this, view);
            if (viewType == VIEW_TYPE_ITEM) {
                root = (CardView) view;
            }
        }
    }

    public interface OnItemClickedListener {
        void OnItemClicked(BackpackAdapter.ViewHolder holder, BackpackItem backpackItem);
    }
}
