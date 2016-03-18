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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.ui.activity.UnusualActivity;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler view in the unusual fragment and activity.
 */
public class UnusualAdapter extends RecyclerView.Adapter<UnusualAdapter.ViewHolder> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = UnusualAdapter.class.getSimpleName();

    /**
     * Type IDs
     */
    public static final int TYPE_HATS = 0;
    public static final int TYPE_EFFECTS = 1;
    public static final int TYPE_SPECIFIC_HAT = 2;

    /**
     * The data set
     */
    private List<Item> mDataSet;

    /**
     * The context
     */
    private Context mContext;

    /**
     * This variable will determine how the items will look like in the list
     */
    private int type = 0;

    /**
     * Main constructor.
     *
     * @param context the context
     */
    public UnusualAdapter(Context context) {
        this.mContext = context;
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

            switch (type) {
                //We are showing the hats, no effects
                case TYPE_HATS:

                    Glide.with(mContext)
                            .load(item.getIconUrl(mContext))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.icon);

                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, UnusualActivity.class);
                            i.putExtra(UnusualActivity.DEFINDEX_KEY, item.getDefindex());
                            i.putExtra(UnusualActivity.NAME_KEY, item.getName());
                            mContext.startActivity(i);
                        }
                    });
                    holder.price.setText(mContext.getString(R.string.currency_key_plural,
                            Utility.formatDouble(item.getPrice().getValue())));

                    holder.name.setText(item.getName());

                    holder.more.setVisibility(View.GONE);
                    break;
                //We are showing the effects, no hats
                case TYPE_EFFECTS:

                    Glide.with(mContext)
                            .load(item.getEffectUrl())
                            .into(holder.icon);

                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, UnusualActivity.class);
                            i.putExtra(UnusualActivity.PRICE_INDEX_KEY, item.getPriceIndex());
                            i.putExtra(UnusualActivity.NAME_KEY, Utility.getUnusualEffectName(mContext, item.getPriceIndex()));
                            mContext.startActivity(i);
                        }
                    });

                    holder.price.setText(mContext.getString(R.string.currency_key_plural,
                            Utility.formatDouble(item.getPrice().getValue())));

                    holder.name.setText(item.getName());

                    holder.more.setVisibility(View.GONE);
                    break;
                //We are showing both that icon and the effect for a specific hat or effect
                case TYPE_SPECIFIC_HAT:
                    /*final Item hat = new Item(
                            mDataSet.getInt(UnusualActivity.COLUMN_DEFINDEX),
                            null,
                            Quality.UNUSUAL, true, true, false,
                            mDataSet.getInt(UnusualActivity.COLUMN_PRICE_INDEX),
                            new Price(
                                    mDataSet.getDouble(UnusualActivity.COLUMN_PRICE),
                                    mDataSet.getDouble(UnusualActivity.COLUMN_PRICE_MAX),
                                    0, 0, 0,
                                    mDataSet.getString(UnusualActivity.COLUMN_CURRENCY)
                            )
                    );

                    holder.price.setText(hat.getPrice().getFormattedPrice(mContext, Currency.KEY));

                    holder.name.setText(mDataSet.getString(UnusualActivity.COLUMN_NAME));

                    Glide.with(mContext)
                            .load(hat.getIconUrl(mContext))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.icon);
                    Glide.with(mContext)
                            .load(hat.getEffectUrl())
                            .into(holder.effect);

                    holder.more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PopupMenu menu = new PopupMenu(mContext, holder.more);

                            menu.getMenuInflater().inflate(R.menu.popup_item, menu.getMenu());

                            menu.getMenu().getItem(0).setTitle(
                                    Utility.isFavorite(mContext, hat) ? "Remove from favorites" : "Add to favorites");

                            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    switch (menuItem.getItemId()) {
                                        case R.id.history:

                                            Intent i = new Intent(mContext, PriceHistoryActivity.class);

                                            i.putExtra(PriceHistoryActivity.EXTRA_ITEM, hat);

                                            mContext.startActivity(i);
                                            break;
                                        case R.id.favorite:
                                            if (Utility.isFavorite(mContext, hat)) {
                                                Utility.removeFromFavorites(mContext, hat);
                                            } else {
                                                Utility.addToFavorites(mContext, hat);
                                            }
                                            break;
                                        case R.id.backpack_tf:
                                            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                                    hat.getBackpackTfUrl())));
                                            break;
                                    }
                                    return true;
                                }
                            });

                            menu.show();
                        }
                    });
*/
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
    public void setType(int type) {
        this.type = type;
    }

    public void setDataSet(List<Item> dataSet) {
        mDataSet = dataSet;
    }

    /**
     * The view holder.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        View root;

        @Bind(R.id.icon) ImageView icon;
        @Bind(R.id.effect) ImageView effect;
        @Bind(R.id.price) TextView price;
        @Bind(R.id.name) TextView name;
        @Bind(R.id.more) View more;

        public ViewHolder(View view) {
            super(view);
            root = view;
            ButterKnife.bind(this, view);
        }
    }
}
