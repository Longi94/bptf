package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;
import com.tlongdev.bktf.activity.UnusualActivity;
import com.tlongdev.bktf.fragment.UnusualFragment;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.model.Item;

import java.io.IOException;
import java.text.DecimalFormat;

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
    private Cursor mDataSet;

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
     * @param dataSet the data set
     */
    public UnusualAdapter(Context context, Cursor dataSet) {
        this.mContext = context;
        this.mDataSet = dataSet;
    }

    /**
     * Constructor.
     *
     * @param context the context
     * @param dataSet the data set
     * @param type    the type of the adapter
     */
    public UnusualAdapter(Context context, Cursor dataSet, int type) {
        this.mContext = context;
        this.mDataSet = dataSet;
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_unusual, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {

            //Get the raw key price
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            double rawKeyPrice = Utility.getDouble(prefs, mContext.getString(R.string.pref_key_raw), 1);

            Picasso picasso = Picasso.with(mContext);
            picasso.setLoggingEnabled(true);
            picasso.setIndicatorsEnabled(true);

            switch (type) {
                //We are showing the hats, no effects
                case TYPE_HATS:
                    final Item item = new Item(
                            mDataSet.getInt(UnusualFragment.COL_PRICE_LIST_DEFI),
                            mDataSet.getString(UnusualFragment.COL_PRICE_LIST_NAME),
                            Quality.UNUSUAL, false, false, false, 0, null
                    );

                    picasso.load(item.getIconUrl(mContext)).into(holder.icon);

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
                            new DecimalFormat("#0.00").format(mDataSet.getDouble(
                                    UnusualFragment.COL_PRICE_LIST_AVG_PRICE) / rawKeyPrice)));
                    break;
                //We are showing the effects, no hats
                case TYPE_EFFECTS:

                    final Item effect = new Item(1, null, Quality.UNUSUAL, false, false, false,
                            mDataSet.getInt(UnusualFragment.COL_PRICE_LIST_INDE), null);

                    try {
                        holder.icon.setImageDrawable(effect.getEffectDrawable(mContext));
                    } catch (IOException e) {
                        e.printStackTrace();
                        holder.icon.setImageDrawable(null);
                    }

                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, UnusualActivity.class);
                            i.putExtra(UnusualActivity.PRICE_INDEX_KEY, effect.getPriceIndex());
                            i.putExtra(UnusualActivity.NAME_KEY, Utility.getUnusualEffectName(mContext, effect.getPriceIndex()));
                            mContext.startActivity(i);
                        }
                    });

                    holder.price.setText(mContext.getString(R.string.currency_key_plural,
                            new DecimalFormat("#0.00").format(mDataSet.getDouble(
                                    UnusualFragment.COL_PRICE_LIST_AVG_PRICE) / rawKeyPrice)));
                    break;
                //We are showing both that icon and the effect for a specific hat or effect
                case TYPE_SPECIFIC_HAT:
                    Item hat = new Item(
                            mDataSet.getInt(UnusualActivity.COL_PRICE_LIST_DEFI),
                            null,
                            Quality.UNUSUAL, false, false, false,
                            mDataSet.getInt(UnusualActivity.COL_PRICE_LIST_INDE),
                            new Price(
                                    mDataSet.getDouble(UnusualActivity.COL_PRICE_LIST_PRIC),
                                    mDataSet.getDouble(UnusualActivity.COL_PRICE_LIST_PMAX),
                                    0, 0, 0,
                                    mDataSet.getString(UnusualActivity.COL_PRICE_LIST_CURR)
                            )
                    );

                    holder.price.setText(hat.getPrice().getFormattedPrice(mContext, Currency.KEY));

                    picasso.load(hat.getIconUrl(mContext)).into(holder.icon);

                    try {
                        //Load the effect icon
                        holder.effect.setImageDrawable(hat.getEffectDrawable(mContext));
                    } catch (IOException e) {
                        e.printStackTrace();
                        holder.effect.setImageDrawable(null);
                    }
                    break;
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
     * Set the type of the adapter
     *
     * @param type the type of the adapter
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * The view holder.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView icon;
        public final ImageView effect;
        public final TextView price;

        public final View root;

        /**
         * Constructor.
         *
         * @param view the root view
         */
        public ViewHolder(View view) {
            super(view);

            root = view;

            icon = (ImageView) view.findViewById(R.id.icon);
            effect = (ImageView) view.findViewById(R.id.image_view_item_effect);
            price = (TextView) view.findViewById(R.id.grid_item_price);
        }
    }
}
