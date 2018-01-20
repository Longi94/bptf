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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.crashlytics.android.Crashlytics;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.dao.DecoratedWeaponDao;
import com.tlongdev.bktf.data.dao.ItemSchemaDao;
import com.tlongdev.bktf.data.dao.UnusualSchemaDao;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler view in the recents fragment.
 */
public class RecentsAdapter extends CursorRecyclerViewAdapter<RecentsAdapter.ViewHolder> {

    @Inject
    Context mContext;
    @Inject
    UnusualSchemaDao mUnusualSchemaDao;
    @Inject
    DecoratedWeaponDao mDecoratedWeaponDao;
    @Inject
    ItemSchemaDao mItemSchemaDao;

    private OnMoreListener mListener;

    public RecentsAdapter(BptfApplication application) {
        super(application, null);
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
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        Price price = new Price();
        price.setValue(cursor.getDouble(cursor.getColumnIndex("price")));
        price.setHighValue(cursor.getDouble(cursor.getColumnIndex("max")));
        price.setDifference(cursor.getDouble(cursor.getColumnIndex("difference")));
        price.setCurrency(cursor.getString(cursor.getColumnIndex("currency")));
        price.setRawValue(cursor.getDouble(cursor.getColumnIndex("raw_price")));

        //Get all the data from the cursor
        final Item item = new Item();
        item.setDefindex(cursor.getInt(cursor.getColumnIndex("defindex")));
        item.setName(cursor.getString(cursor.getColumnIndex("item_name")));
        item.setQuality(cursor.getInt(cursor.getColumnIndex("quality")));
        item.setTradable(cursor.getInt(cursor.getColumnIndex("tradable")) == 1);
        item.setCraftable(cursor.getInt(cursor.getColumnIndex("craftable")) == 1);
        item.setAustralium(cursor.getInt(cursor.getColumnIndex("australium")) == 1);
        item.setPriceIndex(cursor.getInt(cursor.getColumnIndex("price_index")));
        item.setPrice(price);

        holder.more.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onMoreClicked(v, item);
            }
        });

        holder.name.setText(item.getFormattedName(mContext, mUnusualSchemaDao, mItemSchemaDao, false));

        //Set the change indicator of the item
        holder.difference.setTextColor(item.getPrice().getDifferenceColor());
        holder.difference.setText(item.getPrice().getFormattedDifference(mContext));

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

        try {
            //Properly format the price
            holder.price.setText(item.getPrice().getFormattedPrice(mContext));
        } catch (Throwable t) {
            Crashlytics.logException(t);
            t.printStackTrace();
        }
    }

    public void setListener(OnMoreListener listener) {
        mListener = listener;
    }

    /**
     * The view holder.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View root;
        @BindView(R.id.more) View more;
        @BindView(R.id.icon) ImageView icon;
        @BindView(R.id.effect) ImageView effect;
        @BindView(R.id.quality) ImageView quality;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.price) TextView price;
        @BindView(R.id.difference) TextView difference;

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