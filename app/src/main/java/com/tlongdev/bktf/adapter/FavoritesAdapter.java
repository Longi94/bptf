package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.crashlytics.android.Crashlytics;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.dao.DecoratedWeaponDao;
import com.tlongdev.bktf.data.dao.ItemSchemaDao;
import com.tlongdev.bktf.data.dao.UnusualSchemaDao;
import com.tlongdev.bktf.model.Item;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    @Inject
    Context mContext;
    @Inject
    UnusualSchemaDao mUnusualSchemaDao;
    @Inject
    ItemSchemaDao mItemSchemaDao;
    @Inject
    DecoratedWeaponDao mDecoratedWeaponDao;

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

            holder.more.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onMoreClicked(v, item);
                }
            });

            holder.name.setText(item.getFormattedName(mContext, mUnusualSchemaDao, mItemSchemaDao, false));
            holder.icon.setImageDrawable(null);
            holder.quality.setBackgroundColor(item.getColor(mContext, mDecoratedWeaponDao, true));

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

            //Set the item icon
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

            if (item.getPrice() != null) {
                //Set the change indicator of the item
                holder.difference.setVisibility(View.VISIBLE);
                holder.difference.setTextColor(item.getPrice().getDifferenceColor());
                holder.difference.setText(item.getPrice().getFormattedDifference(mContext));

                try {
                    //Properly format the price
                    holder.price.setText(item.getPrice().getFormattedPrice(mContext));
                } catch (Throwable t) {
                    Crashlytics.logException(t);
                    t.printStackTrace();
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
        @BindView(R.id.more) View more;

        @BindView(R.id.icon) ImageView icon;
        @BindView(R.id.effect) ImageView effect;
        @BindView(R.id.quality) ImageView quality;

        @BindView(R.id.name) TextView name;
        @BindView(R.id.price) TextView price;
        @BindView(R.id.difference) TextView difference;

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
