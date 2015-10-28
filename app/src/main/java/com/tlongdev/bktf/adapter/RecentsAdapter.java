package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.fragment.RecentsFragment;

import java.io.IOException;
import java.io.InputStream;

/**
 * Adapter for the recycler view in the recents fragment.
 */
public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.ViewHolder> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = RecentsAdapter.class.getSimpleName();

    /**
     * The context
     */
    private Context mContext;

    /**
     * The data set
     */
    private Cursor mDataSet;

    /**
     * Constructor.
     *
     * @param context the context
     * @param dataSet the data set
     */
    public RecentsAdapter(Context context, Cursor dataSet) {
        this.mContext = context;
        this.mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_recents, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {

            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 2015. 10. 26. does nothing, this is for the fancy ripples for now
                }
            });

            //Get all the data from the cursor
            int defindex = mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_DEFI);
            int quality = mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_QUAL);
            int tradable = mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_TRAD);
            int craftable = mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_CRAF);
            int priceIndex = mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_INDE);
            int australium = mDataSet.getInt(RecentsFragment.COL_AUSTRALIUM);

            double price = mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_PRIC);
            double priceHigh = mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_PMAX);
            double difference = mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_DIFF);
            double raw = mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_PRAW);

            String name = mDataSet.getString(RecentsFragment.COL_PRICE_LIST_NAME);
            String currency = mDataSet.getString(RecentsFragment.COL_PRICE_LIST_CURR);

            String itemName = Utility.formatItemName(mContext, defindex, name, tradable, craftable,
                    quality, priceIndex);
            holder.name.setText(itemName);

            //Set the change indicator of the item
            if (Math.abs(difference - raw) < Utility.EPSILON) {
                // TODO: 2015. 10. 26. There might be inaccuracies resulting in the difference not being equal to the raw price
                holder.difference.setText("new");
                holder.difference.setTextColor(0xFFFFFF00);
            } else if (difference == 0.0) {
                holder.difference.setText("refresh");
                holder.difference.setTextColor(0xFFFFFFFF);
            } else if (difference > 0.0) {
                holder.difference.setText(String.format("+ %s", Utility.formatPrice(mContext, difference, 0, Currency.METAL, currency, false)));
                holder.difference.setTextColor(0xFF00FF00);
            } else {
                holder.difference.setText(String.format("- %s", Utility.formatPrice(mContext, -difference, 0, Currency.METAL, currency, false)));
                holder.difference.setTextColor(0xFFFF0000);
            }

            holder.icon.setImageDrawable(null);
            holder.effect.setBackgroundColor(Utility.getQualityColor(mContext, quality, defindex, true));

            try {
                AssetManager assetManager = mContext.getAssets();
                InputStream ims;

                //Get the icon of the item
                if (australium == 1 && defindex != 5037) {
                    ims = assetManager.open("items/" + Utility.getIconIndex(defindex) + "aus.png");
                } else {
                    ims = assetManager.open("items/" + Utility.getIconIndex(defindex) + ".png");
                }
                holder.icon.setImageDrawable(Drawable.createFromStream(ims, null));

                //Get the icon if the effect if needed
                if (priceIndex != 0 && Utility.canHaveEffects(defindex, quality)) {
                    ims = assetManager.open("effects/" + priceIndex + "_188x188.png");
                    holder.effect.setImageDrawable(Drawable.createFromStream(ims, null));
                } else {
                    holder.effect.setImageDrawable(null);
                }

            } catch (IOException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                holder.icon.setImageDrawable(null);
                holder.effect.setImageDrawable(null);
            }

            try {
                //Properly format the price
                holder.price.setText(Utility.formatPrice(mContext, price, priceHigh,
                        currency, currency, false));
            } catch (Throwable throwable) {
                if (Utility.isDebugging(mContext))
                    throwable.printStackTrace();
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
     * @param closePrevious whether to close the previous cursor
     */
    public void swapCursor(Cursor data, boolean closePrevious) {
        if (closePrevious && mDataSet != null) mDataSet.close();
        mDataSet = data;
        notifyDataSetChanged();
    }

    /**
     * The view holder.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View root;

        public final ImageView icon;
        public final ImageView effect;

        public final TextView name;
        public final TextView price;
        public final TextView difference;

        /**
         * Constructor.
         *
         * @param view the root view
         */
        public ViewHolder(View view) {
            super(view);
            root = view;
            icon = (ImageView) view.findViewById(R.id.icon);
            effect = (ImageView) view.findViewById(R.id.effect);
            name = (TextView) view.findViewById(R.id.name);
            price = (TextView) view.findViewById(R.id.price);
            difference = (TextView) view.findViewById(R.id.difference);
        }
    }
}