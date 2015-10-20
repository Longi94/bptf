package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.activity.UserInfoActivity;

import java.io.IOException;
import java.io.InputStream;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private static final int VIEW_TYPE_PRICE = 0;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private Cursor mDataSet;
    private Context mContext;

    private boolean userFound = false;
    private boolean loading;
    private String[] userInfo;

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
                case VIEW_TYPE_USER:
                    holder.more.setVisibility(View.GONE);
                    if (userFound) {
                        holder.more.setVisibility(View.GONE);
                        holder.loading.setVisibility(View.GONE);
                        holder.priceLayout.setVisibility(View.VISIBLE);
                        holder.root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, UserInfoActivity.class);
                                intent.putExtra(UserInfoActivity.STEAM_ID_KEY, userInfo[1]);
                                intent.putExtra(UserInfoActivity.JSON_USER_SUMMARIES_KEY, userInfo[2]);
                                mContext.startActivity(intent);
                            }
                        });

                        holder.name.setText(mDataSet.getString(SearchActivity.COL_PRICE_LIST_NAME));
                        holder.icon.setImageDrawable(Drawable.
                                createFromPath(mContext.getFilesDir().toString() + "/avatar_search.png"));
                    }
                    break;
                case VIEW_TYPE_LOADING:
                    holder.loading.setVisibility(View.VISIBLE);
                    holder.priceLayout.setVisibility(View.GONE);

                    holder.name.setText(null);
                    holder.icon.setImageDrawable(null);
                    break;
                case VIEW_TYPE_PRICE:

                    holder.priceLayout.setVisibility(View.VISIBLE);
                    holder.more.setVisibility(View.VISIBLE);
                    holder.loading.setVisibility(View.GONE);

                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });

                    int defindex = mDataSet.getInt(SearchActivity.COL_PRICE_LIST_DEFI);
                    int quality = mDataSet.getInt(SearchActivity.COL_PRICE_LIST_QUAL);
                    int tradable = mDataSet.getInt(SearchActivity.COL_PRICE_LIST_TRAD);
                    int craftable = mDataSet.getInt(SearchActivity.COL_PRICE_LIST_CRAF);
                    int priceIndex = mDataSet.getInt(SearchActivity.COL_PRICE_LIST_INDE);
                    int australium = mDataSet.getInt(SearchActivity.COL_AUSTRALIUM);

                    double price = mDataSet.getDouble(SearchActivity.COL_PRICE_LIST_PRIC);
                    double priceHigh = mDataSet.getDouble(SearchActivity.COL_PRICE_LIST_PMAX);

                    String name = mDataSet.getString(SearchActivity.COL_PRICE_LIST_NAME);
                    String currency = mDataSet.getString(SearchActivity.COL_PRICE_LIST_CURR);

                    holder.name.setText(Utility.formatItemName(mContext, defindex, name,
                            tradable, craftable, quality, priceIndex));

                    holder.icon.setImageDrawable(null);
                    holder.icon.setBackgroundColor(Utility.getQualityColor(mContext, quality, defindex, true));

                    if (priceIndex == 0) {
                        setIconImage(mContext, holder.icon, defindex, australium == 1);
                    } else {
                        setIconImageWithIndex(mContext, holder.icon, defindex, priceIndex,
                                australium == 1, quality);
                    }

                    try {
                        holder.price.setText(Utility.formatPrice(mContext, price, priceHigh,
                                currency, currency, false));
                    } catch (Throwable throwable) {
                        if (Utility.isDebugging(mContext))
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

    public void swapCursor(Cursor data, boolean closePrevious) {
        if (closePrevious && mDataSet != null) mDataSet.close();
        mDataSet = data;
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            userFound = false;
        }
    }

    public void setUserFound(boolean userFound, String[] userInfo) {
        this.userFound = userFound;
        this.userInfo = userInfo;
        if (userFound) {
            loading = false;
        }
    }

    private void setIconImage(Context context, ImageView icon, int defindex, boolean isAustralium) {
        try {
            InputStream ims;
            AssetManager assetManager = context.getAssets();
            if (isAustralium && defindex != 5037) {
                ims = assetManager.open("items/" + Utility.getIconIndex(defindex) + "aus.png");
            } else {
                ims = assetManager.open("items/" + Utility.getIconIndex(defindex) + ".png");
            }

            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            icon.setImageDrawable(d);
        } catch (IOException e) {
            if (Utility.isDebugging(context))
                e.printStackTrace();
        }
    }

    private void setIconImageWithIndex(Context context, ImageView icon, int defindex, int priceIndex, boolean isAustralium, int quality) {
        try {
            InputStream ims;
            AssetManager assetManager = context.getAssets();
            if (isAustralium && defindex != 5037) {
                ims = assetManager.open("items/" + Utility.getIconIndex(defindex) + "aus.png");
            } else {
                ims = assetManager.open("items/" + Utility.getIconIndex(defindex) + ".png");
            }

            // load image as Drawable
            Drawable iconDrawable = Drawable.createFromStream(ims, null);

            if (Utility.canHaveEffects(defindex, quality)) {
                ims = assetManager.open("effects/" + priceIndex + "_188x188.png");
                Drawable effectDrawable = Drawable.createFromStream(ims, null);
                // set image to ImageView
                icon.setImageDrawable(new LayerDrawable(new Drawable[]{effectDrawable, iconDrawable}));
            } else {
                icon.setImageDrawable(iconDrawable);
            }
        } catch (IOException e) {
            if (Utility.isDebugging(context))
                e.printStackTrace();
            icon.setImageDrawable(null);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public final View root;

        public final View loading;
        public final View priceLayout;

        public final ImageView icon;
        public final ImageView more;

        public final TextView name;
        public final TextView price;

        public ViewHolder(View view) {
            super(view);
            root = view;
            loading = view.findViewById(R.id.loading_layout);
            priceLayout = view.findViewById(R.id.price_layout);
            icon = (ImageView) view.findViewById(R.id.icon);
            more = (ImageView) view.findViewById(R.id.more);
            name = (TextView) view.findViewById(R.id.name);
            price = (TextView) view.findViewById(R.id.price);
        }
    }
}
