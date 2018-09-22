package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.model.User;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler view in the search activity
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private static final int VIEW_TYPE_PRICE = 0;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    @Inject Context mContext;

    private Cursor mDataSet;
    private boolean mLoading;
    private User mUser;

    private OnSearchClickListener mListener;

    public SearchAdapter(BptfApplication application) {
        application.getAdapterComponent().inject(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;

        switch (viewType) {
            case VIEW_TYPE_LOADING:
                layout = R.layout.list_search_loading;
                break;
            default:
                layout = R.layout.list_search;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @SuppressWarnings({"WrongConstant", "ConstantConditions"})
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            //We found a user, show some info
            case VIEW_TYPE_USER:
                holder.more.setVisibility(View.GONE);
                if (mUser != null) {

                    //open the user activity when the user clicks on the view
                    holder.root.setOnClickListener(v -> {
                        if (mListener != null) {
                            mListener.onUserClicked(mUser);
                        }
                    });

                    holder.name.setSelected(false);
                    holder.root.setOnClickListener(v -> {
                        holder.name.setSelected(false);
                        holder.name.setSelected(true);
                    });

                    holder.name.setText(mUser.getName());

                    RequestOptions options = new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL);
                    Glide.with(mContext)
                            .load(mUser.getAvatarUrl())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .apply(options)
                            .into(holder.icon);
                }
                break;
            //Simple price view
            case VIEW_TYPE_PRICE:
                if (mDataSet != null && mDataSet.moveToPosition(position - (getItemCount() - mDataSet.getCount()))) {

                    Price price = new Price();
                    price.setValue(mDataSet.getDouble(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE)));
                    price.setHighValue(mDataSet.getDouble(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE_HIGH)));
                    price.setCurrency(mDataSet.getString(mDataSet.getColumnIndex(PriceEntry.COLUMN_CURRENCY)));

                    //Get all the data from the cursor
                    final Item item = new Item();
                    item.setDefindex(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_DEFINDEX)));
                    item.setName(mDataSet.getString(mDataSet.getColumnIndex(ItemSchemaEntry.COLUMN_ITEM_NAME)));
                    item.setImage(mDataSet.getString(mDataSet.getColumnIndex(ItemSchemaEntry.COLUMN_IMAGE)));
                    item.setQuality(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_ITEM_QUALITY)));
                    item.setTradable(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_ITEM_TRADABLE)) == 1);
                    item.setCraftable(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_ITEM_CRAFTABLE)) == 1);
                    item.setAustralium(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_AUSTRALIUM)) == 1);
                    item.setPriceIndex(mDataSet.getInt(mDataSet.getColumnIndex(PriceEntry.COLUMN_PRICE_INDEX)));
                    item.setPrice(price);

                    holder.more.setOnClickListener(v -> {
                        if (mListener != null) {
                            mListener.onMoreClicked(v, item);
                        }
                    });

                    holder.name.setSelected(false);
                    holder.root.setOnClickListener(v -> {
                        holder.name.setSelected(false);
                        holder.name.setSelected(true);
                    });

                    holder.name.setText(item.getFormattedName(mContext));

                    holder.icon.setImageDrawable(null);
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
                        holder.price.setText(item.getPrice().getFormattedPrice(mContext));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        int count = mDataSet == null ? 0 : mDataSet.getCount();
        count += mUser != null || mLoading ? 1 : 0;
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (mLoading) {
            return position == 0 ? VIEW_TYPE_LOADING : VIEW_TYPE_PRICE;
        }
        return mUser != null && position == 0 ? VIEW_TYPE_USER : VIEW_TYPE_PRICE;
    }

    /**
     * Replaces the cursor of the adapter
     *
     * @param data          the cursor that will replace the current one
     */
    public void swapCursor(Cursor data) {
        if (mDataSet != null) mDataSet.close();
        mDataSet = data;
    }

    /**
     * Tells whether the list should show a loading animation or not
     *
     * @param loading whether it is loading or not
     */
    public void setLoading(boolean loading) {
        this.mLoading = loading;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public void setListener(OnSearchClickListener listener) {
        mListener = listener;
    }

    public void closeCursor() {
        if (mDataSet != null) {
            mDataSet.close();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final View root;
        @Nullable @BindView(R.id.icon) ImageView icon;
        @Nullable @BindView(R.id.effect) ImageView effect;
        @Nullable @BindView(R.id.more) ImageView more;
        @Nullable @BindView(R.id.quality) ImageView quality;
        @Nullable @BindView(R.id.name) TextView name;
        @Nullable @BindView(R.id.price) TextView price;

        public ViewHolder(View view) {
            super(view);
            root = view;
            ButterKnife.bind(this, view);
        }
    }

    public interface OnSearchClickListener {
        void onMoreClicked(View view, Item item);
        void onUserClicked(User user);
    }
}
