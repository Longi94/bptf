package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.activity.UserActivity;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;

/**
 * Adapter for the recycler view in the search activity
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = SearchAdapter.class.getSimpleName();

    /**
     * View type IDs
     */
    private static final int VIEW_TYPE_PRICE = 0;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    /**
     * The data set
     */
    private Cursor mDataSet;

    /**
     * The context
     */
    private Context mContext;

    /**
     * Whether we found a user for the string query or not
     */
    private boolean userFound = false;

    /**
     * Whether we are searching for the user or not
     */
    private boolean loading;

    /**
     * Some info on the user we found
     */
    private String[] userInfo;

    /**
     * Constructor.
     *
     * @param context the context
     * @param dataSet the data set
     */
    public SearchAdapter(Context context, Cursor dataSet) {
        this.mContext = context;
        this.mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_search, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {
            switch (getItemViewType(position)) {
                //We found a user, show some info
                case VIEW_TYPE_USER:
                    holder.more.setVisibility(View.GONE);
                    if (userFound) {
                        holder.loading.setVisibility(View.GONE);
                        holder.priceLayout.setVisibility(View.VISIBLE);

                        //open the user activity when the user clicks on the view
                        holder.root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, UserActivity.class);
                                intent.putExtra(UserActivity.STEAM_ID_KEY, userInfo[1]);
                                intent.putExtra(UserActivity.JSON_USER_SUMMARIES_KEY, userInfo[2]);
                                mContext.startActivity(intent);
                            }
                        });

                        //The name
                        holder.name.setText(mDataSet.getString(SearchActivity.COLUMN_NAME));
                        //The avatar of the user
                        String path = PreferenceManager.getDefaultSharedPreferences(mContext).
                                getString(mContext.getString(R.string.pref_search_avatar_url), "");

                        if (!path.equals("")) {
                            Glide.with(mContext)
                                    .load(path)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(holder.icon);
                        }
                    }
                    break;
                //Lading view, searching for the user
                case VIEW_TYPE_LOADING:
                    holder.loading.setVisibility(View.VISIBLE);
                    holder.priceLayout.setVisibility(View.GONE);

                    holder.name.setText(null);
                    holder.icon.setImageDrawable(null);
                    break;
                //Simple price view
                case VIEW_TYPE_PRICE:
                    holder.priceLayout.setVisibility(View.VISIBLE);
                    holder.more.setVisibility(View.VISIBLE);
                    holder.loading.setVisibility(View.GONE);

                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // TODO: 2015. 10. 26. Does nothing, fancy ripples for now
                        }
                    });

                    //Get all the data from the cursor
                    Item item = new Item(
                            mDataSet.getInt(SearchActivity.COLUMN_DEFINDEX),
                            mDataSet.getString(SearchActivity.COLUMN_NAME),
                            mDataSet.getInt(SearchActivity.COLUMN_QUALITY),
                            mDataSet.getInt(SearchActivity.COLUMN_TRADABLE) == 1,
                            mDataSet.getInt(SearchActivity.COLUMN_CRAFTABLE) == 1,
                            mDataSet.getInt(SearchActivity.COLUMN_AUSTRALIUM) == 1,
                            mDataSet.getInt(SearchActivity.COLUMN_PRICE_INDEX),
                            new Price(
                                    mDataSet.getDouble(SearchActivity.COLUMN_PRICE),
                                    mDataSet.getDouble(SearchActivity.COLUMN_PRICE_HIGH),
                                    0,
                                    0,
                                    0,
                                    mDataSet.getString(SearchActivity.COLUMN_CURRENCY)
                            )
                    );

                    holder.name.setText(item.getFormattedName(mContext));

                    holder.icon.setImageDrawable(null);
                    holder.icon.setBackgroundColor(item.getColor(mContext, true));

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
                        holder.effect.setImageDrawable(null);
                    }

                    try {
                        holder.price.setText(item.getPrice().getFormattedPrice(mContext));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (loading) {
            return position == 0 ? VIEW_TYPE_LOADING : VIEW_TYPE_PRICE;
        }
        return userFound && position == 0 ? VIEW_TYPE_USER : VIEW_TYPE_PRICE;
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
     * Tells whether the list should show a loading animation or not
     *
     * @param loading whether it is loading or not
     */
    public void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            userFound = false;
        }
    }

    /**
     * Tells the list that we found a user and it should show it
     *
     * @param userFound whether we found a user or not
     * @param userInfo  some info about the user
     */
    public void setUserFound(boolean userFound, String[] userInfo) {
        this.userFound = userFound;
        this.userInfo = userInfo;
        if (userFound) {
            loading = false;
        }
    }

    /**
     * The view holder.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        public final View root;

        public final View loading;
        public final View priceLayout;

        public final ImageView icon;
        public final ImageView effect;
        public final ImageView more;

        public final TextView name;
        public final TextView price;

        /**
         * Constructor.
         *
         * @param view the root view
         */
        public ViewHolder(View view) {
            super(view);
            root = view;
            loading = view.findViewById(R.id.loading_layout);
            priceLayout = view.findViewById(R.id.price_layout);
            icon = (ImageView) view.findViewById(R.id.icon);
            effect = (ImageView) view.findViewById(R.id.effect);
            more = (ImageView) view.findViewById(R.id.more);
            name = (TextView) view.findViewById(R.id.name);
            price = (TextView) view.findViewById(R.id.price);
        }
    }
}
