package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.tlongdev.bktf.activity.PriceHistoryActivity;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.util.Utility;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Long on 2015. 12. 08..
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Item> mDataSet;

    public FavoritesAdapter(Context mContext, ArrayList<Item> mDataSet) {
        this.mContext = mContext;
        this.mDataSet = mDataSet;
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
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 2015. 10. 26. does nothing, this is for the fancy ripples for now
                }
            });

            final Item item = mDataSet.get(position);

            holder.more.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    PopupMenu menu = new PopupMenu(mContext, holder.more);

                    menu.getMenuInflater().inflate(R.menu.popup_item, menu.getMenu());

                    menu.getMenu().getItem(0).setTitle("Remove from favorites");

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
                                    Utility.removeFromFavorites(mContext, item);
                                    notifyItemRemoved(mDataSet.indexOf(item));
                                    mDataSet.remove(item);
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

                ((BptfApplication) mContext.getApplicationContext()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                        .setDescription("Formatter exception:RecentsAdapter, Message: " + t.getMessage())
                        .setFatal(false)
                        .build());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    public void setDataSet(ArrayList<Item> dataSet) {
        this.mDataSet = dataSet;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View root;
        @Bind(R.id.more) View more;
        @Bind(R.id.icon_background) View background;

        @Bind(R.id.icon) ImageView icon;
        @Bind(R.id.effect) ImageView effect;

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
}
