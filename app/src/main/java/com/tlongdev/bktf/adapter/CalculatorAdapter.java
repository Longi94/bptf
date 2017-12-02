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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.dao.UnusualSchemaDao;
import com.tlongdev.bktf.model.Item;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler view of the calculator.
 */
public class CalculatorAdapter extends RecyclerView.Adapter<CalculatorAdapter.ViewHolder> {

    @Inject
    Context mContext;
    @Inject
    UnusualSchemaDao mUnusualSchemaDao;

    private List<Item> mDataSet;
    private List<Integer> mCountSet;

    /**
     * The listener that will be notified when an items is edited.
     */
    private OnItemEditListener mListener;

    public CalculatorAdapter(BptfApplication application) {
        application.getAdapterComponent().inject(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_calculator, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (mDataSet != null && mDataSet.size() > position) {

            final Item item = mDataSet.get(position);

            Glide.with(mContext)
                    .load(item.getIconUrl(mContext))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.icon);

            if (item.getPriceIndex() != 0 && item.canHaveEffects()) {
                Glide.with(mContext)
                        .load(item.getEffectUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(holder.effect);
            } else {
                Glide.with(mContext).clear(holder.effect);
                holder.effect.setImageDrawable(null);
            }

            holder.name.setText(item.getFormattedName(mContext, mUnusualSchemaDao));

            holder.quality.setBackgroundColor(item.getColor(mContext, true));

            if (!item.isTradable()) {
                if (!item.isCraftable()) {
                    holder.quality.setImageResource(R.drawable.uncraft_untrad);
                } else {
                    holder.quality.setImageResource(R.drawable.untrad);
                }
            } else if (!item.isCraftable()) {
                holder.quality.setImageResource(R.drawable.uncraft);
            } else {
                holder.quality.setImageResource(0);
            }

            if (item.getPrice() != null) {
                holder.price.setText(item.getPrice().getFormattedPrice(mContext));
            } else {
                holder.price.setText("Price unknown");
            }

            holder.count.setText(String.valueOf(mCountSet.get(position)));

            holder.delete.setOnClickListener(v -> {
                if (mDataSet.contains(item)){
                    if (mListener != null) {
                        //Notify listener that an item was deleted
                        mListener.onItemDeleted(item, mCountSet.get(holder.getAdapterPosition()));
                    }
                    int removedPos = holder.getAdapterPosition();
                    mDataSet.remove(removedPos);
                    mCountSet.remove(removedPos);
                    notifyItemRemoved(removedPos);
                }
            });

            holder.count.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && mDataSet.contains(item)) {
                    String countStr = holder.count.getText().toString();

                    int count = 1;
                    try {
                        count = Integer.parseInt(countStr);

                        if (count < 1) {
                            count = 1;
                        }

                        holder.count.setText(String.valueOf(count));
                    } catch (NumberFormatException e) {
                        holder.count.setText("1");
                    }

                    if (mListener != null && mCountSet.get(holder.getAdapterPosition()) != count) {
                        mListener.onItemEdited(item, mCountSet.get(holder.getAdapterPosition()), count);
                    }

                    mCountSet.set(holder.getAdapterPosition(), count);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    public void setDataSet(List<Item> dataSet, List<Integer> count) {
        mDataSet = dataSet;
        mCountSet = count;
    }

    /**
     * Set the listener that will be notified when an items is edited/deleted
     *
     * @param listener the listener to be set
     */
    public void setListener(OnItemEditListener listener) {
        this.mListener = listener;
    }

    public void clearDataSet() {
        mDataSet.clear();
        mCountSet.clear();
    }

    /**
     * The view holder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon) ImageView icon;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.price) TextView price;
        @BindView(R.id.effect) ImageView effect;
        @BindView(R.id.delete) ImageView delete;
        @BindView(R.id.quality) ImageView quality;
        @BindView(R.id.count) EditText count;

        /**
         * Constructor
         *
         * @param view the root view
         */
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * Interface that will be notified when an item is edited/deleted.
     */
    public interface OnItemEditListener {

        void onItemDeleted(Item item, int count);

        void onItemEdited(Item item, int oldCount, int newCount);
    }
}
