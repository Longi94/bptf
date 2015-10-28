package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.RecentsFragment;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.model.Item;

import java.io.IOException;

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
            Item item = new Item(
                    mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_DEFI),
                    mDataSet.getString(RecentsFragment.COL_PRICE_LIST_NAME),
                    mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_QUAL),
                    mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_TRAD) == 1,
                    mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_CRAF) == 1,
                    mDataSet.getInt(RecentsFragment.COL_AUSTRALIUM) == 1,
                    mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_INDE),
                    new Price(
                            mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_PRIC),
                            mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_PMAX),
                            mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_PRAW),
                            0 /* TODO last update */,
                            mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_DIFF),
                            mDataSet.getString(RecentsFragment.COL_PRICE_LIST_CURR)
                    )
            );

            holder.name.setText(item.getFormattedName(mContext, false));

            //Set the change indicator of the item
            holder.difference.setTextColor(item.getPrice().getDifferenceColor());
            holder.difference.setText(item.getPrice().getFormattedDifference(mContext));

            holder.icon.setImageDrawable(null);
            holder.effect.setBackgroundColor(item.getColor(mContext, true));

            //Set the item icon
            try {
                holder.icon.setImageDrawable(item.getIconDrawable(mContext));
            } catch (IOException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                holder.icon.setImageDrawable(null);
            }

            //Set the effect icon
            try {
                holder.effect.setImageDrawable(item.getEffectDrawable(mContext));
            } catch (IOException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                holder.effect.setImageDrawable(null);
            }

            try {
                //Properly format the price
                holder.price.setText(item.getPrice().getFormattedPrice(mContext));
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