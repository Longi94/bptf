package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
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
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.activity.PriceHistoryActivity;
import com.tlongdev.bktf.activity.UnusualActivity;
import com.tlongdev.bktf.fragment.UnusualFragment;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.util.Utility;

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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {

            //Get the raw key price
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            double rawKeyPrice = Utility.getDouble(prefs, mContext.getString(R.string.pref_key_raw), 1);

            switch (type) {
                //We are showing the hats, no effects
                case TYPE_HATS:
                    final Item item = new Item(
                            mDataSet.getInt(UnusualFragment.COLUMN_DEFINDEX),
                            mDataSet.getString(UnusualFragment.COLUMN_NAME),
                            Quality.UNIQUE, true, true, false, 0, null
                    );

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
                            Utility.formatDouble(mDataSet.getDouble(
                                    UnusualFragment.COLUMN_AVERAGE_PRICE) / rawKeyPrice)));

                    holder.name.setText(mDataSet.getString(UnusualFragment.COLUMN_NAME));

                    holder.more.setVisibility(View.GONE);
                    break;
                //We are showing the effects, no hats
                case TYPE_EFFECTS:

                    final Item effect = new Item(1, null, Quality.UNUSUAL, true, true, false,
                            mDataSet.getInt(UnusualFragment.COLUMN_INDEX), null);

                    Glide.with(mContext)
                            .load(effect.getEffectUrl(mContext))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.icon);

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
                            Utility.formatDouble(mDataSet.getDouble(
                                    UnusualFragment.COLUMN_AVERAGE_PRICE) / rawKeyPrice)));

                    holder.name.setText(mDataSet.getString(UnusualFragment.COLUMN_NAME));

                    holder.more.setVisibility(View.GONE);
                    break;
                //We are showing both that icon and the effect for a specific hat or effect
                case TYPE_SPECIFIC_HAT:
                    final Item hat = new Item(
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
                            .load(hat.getEffectUrl(mContext))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
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
        public final TextView name;

        public final View root;
        public final View more;

        /**
         * Constructor.
         *
         * @param view the root view
         */
        public ViewHolder(View view) {
            super(view);

            root = view;
            more = view.findViewById(R.id.more);

            icon = (ImageView) view.findViewById(R.id.icon);
            effect = (ImageView) view.findViewById(R.id.effect);
            price = (TextView) view.findViewById(R.id.price);
            name = (TextView) view.findViewById(R.id.name);
        }
    }
}
