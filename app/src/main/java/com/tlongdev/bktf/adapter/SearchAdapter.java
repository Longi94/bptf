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
        View v;

        switch (viewType) {
            case VIEW_TYPE_USER: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_search_user, parent, false);
                break;
            }
            case VIEW_TYPE_LOADING: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_search_loading, parent, false);
                break;
            }
            default: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_search, parent, false);
                break;
            }
        }
        
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {
            switch (getItemViewType(position)) {
                case VIEW_TYPE_USER:
                    ((TextView) holder.root.findViewById(R.id.text_view_user_name)).setText(mDataSet.getString(SearchActivity.COL_PRICE_LIST_NAME));
                    ((ImageView) holder.root.findViewById(R.id.image_view_avatar)).setImageDrawable(Drawable.
                            createFromPath(mContext.getFilesDir().toString() + "/avatar_search.png"));

                    if (userFound) {
                        holder.root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, UserInfoActivity.class);
                                intent.putExtra(UserInfoActivity.STEAM_ID_KEY, userInfo[1]);
                                intent.putExtra(UserInfoActivity.JSON_USER_SUMMARIES_KEY, userInfo[2]);
                                mContext.startActivity(intent);
                            }
                        });
                    }
                    break;
                case VIEW_TYPE_PRICE:

                    int quality = mDataSet.getInt(SearchActivity.COL_PRICE_LIST_QUAL);
                    int defindex = mDataSet.getInt(SearchActivity.COL_PRICE_LIST_DEFI);

                    holder.background.setBackgroundDrawable(Utility.getItemBackground(mContext,
                            defindex, quality,
                            mDataSet.getInt(SearchActivity.COL_PRICE_LIST_TRAD),
                            mDataSet.getInt(SearchActivity.COL_PRICE_LIST_CRAF)));

                    holder.nameView.setText(Utility.formatItemName(mContext,
                            defindex,
                            mDataSet.getString(SearchActivity.COL_PRICE_LIST_NAME),
                            mDataSet.getInt(SearchActivity.COL_PRICE_LIST_TRAD),
                            mDataSet.getInt(SearchActivity.COL_PRICE_LIST_CRAF),
                            quality,
                            mDataSet.getInt(SearchActivity.COL_PRICE_LIST_INDE)));

                    if (mDataSet.getInt(SearchActivity.COL_PRICE_LIST_INDE) == 0) {
                        setIconImage(mContext, holder.icon, defindex,
                                mDataSet.getInt(SearchActivity.COL_AUSTRALIUM) == 1);
                    } else {
                        setIconImageWithIndex(mContext, holder.icon, defindex,
                                mDataSet.getInt(SearchActivity.COL_PRICE_LIST_INDE),
                                mDataSet.getInt(SearchActivity.COL_AUSTRALIUM) == 1, quality);
                    }

                    try {
                        holder.priceView.setText(Utility.formatPrice(
                                mContext, mDataSet.getDouble(SearchActivity.COL_PRICE_LIST_PRIC),
                                mDataSet.getDouble(SearchActivity.COL_PRICE_LIST_PMAX),
                                mDataSet.getString(SearchActivity.COL_PRICE_LIST_CURR),
                                mDataSet.getString(SearchActivity.COL_PRICE_LIST_CURR),
                                true
                        ));
                    } catch (Throwable throwable) {
                        if (Utility.isDebugging(mContext))
                            throwable.printStackTrace();
                    }
                    break;
                case VIEW_TYPE_LOADING:
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

        public final ImageView icon;
        public final ImageView background;

        public final TextView nameView;
        public final TextView priceView;

        public ViewHolder(View view) {
            super(view);
            root = view;
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            background = (ImageView) view.findViewById(R.id.image_view_item_background);
            nameView = (TextView) view.findViewById(R.id.name);
            priceView = (TextView) view.findViewById(R.id.price);
        }
    }
}
