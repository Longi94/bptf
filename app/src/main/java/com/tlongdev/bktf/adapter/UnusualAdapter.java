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
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.util.Utility;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler view in the unusual fragment and activity.
 */
public class UnusualAdapter extends RecyclerView.Adapter<UnusualAdapter.ViewHolder> {

    @IntDef({TYPE_HATS, TYPE_EFFECTS, TYPE_SPECIFIC_HAT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UnusualAdapterType{}

    public static final int TYPE_HATS = 0;
    public static final int TYPE_EFFECTS = 1;
    public static final int TYPE_SPECIFIC_HAT = 2;

    @Inject Context mContext;

    private List<Item> mDataSet;

    private OnItemClickListener mListener;

    /**
     * This variable will determine how the items will look like in the list
     */
    @UnusualAdapterType
    private int mType;

    public UnusualAdapter(BptfApplication application) {
        application.getAdapterComponent().inject(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_unusual, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mDataSet != null) {

            final Item item = mDataSet.get(position);

            switch (mType) {
                //We are showing the hats, no effects
                case TYPE_HATS:

                    Glide.with(mContext).load(item.getIconUrl()).into(holder.icon);

                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null) {
                                mListener.onItemClicked(item.getDefindex(),
                                        item.getName(),
                                        false);
                            }
                        }
                    });
                    holder.price.setText(mContext.getString(R.string.currency_key_plural,
                            Utility.formatDouble(item.getPrice().getValue())));

                    holder.name.setText(item.getName());

                    holder.more.setVisibility(View.GONE);
                    break;
                //We are showing the effects, no hats
                case TYPE_EFFECTS:

                    Glide.with(mContext).load(item.getEffectUrl()).into(holder.icon);

                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null) {
                                mListener.onItemClicked(item.getPriceIndex(),
                                        Utility.getUnusualEffectName(mContext, item.getPriceIndex()),
                                        true);
                            }
                        }
                    });

                    holder.price.setText(mContext.getString(R.string.currency_key_plural,
                            Utility.formatDouble(item.getPrice().getValue())));

                    holder.name.setText(item.getName());

                    holder.more.setVisibility(View.GONE);
                    break;
                //We are showing both that icon and the effect for a specific hat or effect
                case TYPE_SPECIFIC_HAT:
                    holder.price.setText(item.getPrice().getFormattedPrice(mContext, Currency.KEY));

                    holder.name.setText(item.getName());

                    Glide.with(mContext).load(item.getIconUrl()).into(holder.icon);
                    Glide.with(mContext).load(item.getEffectUrl()).into(holder.effect);

                    holder.more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null) {
                                mListener.onMoreClicked(v, item);
                            }
                        }
                    });
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    /**
     * Set the type of the adapter
     *
     * @param type the type of the adapter
     */
    public void setType(@UnusualAdapterType int type) {
        this.mType = type;
    }

    public void setDataSet(List<Item> dataSet) {
        mDataSet = dataSet;
    }

    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    /**
     * The view holder.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        final View root;

        @BindView(R.id.icon) ImageView icon;
        @BindView(R.id.effect) ImageView effect;
        @BindView(R.id.price) TextView price;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.more) View more;

        public ViewHolder(View view) {
            super(view);
            root = view;
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemClickListener {
        void onMoreClicked(View view, Item item);
        void onItemClicked(int index, String name, boolean effect);
    }
}
