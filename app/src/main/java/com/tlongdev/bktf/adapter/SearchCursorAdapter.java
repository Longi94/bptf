package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.SearchFragment;

import java.io.IOException;
import java.io.InputStream;

public class SearchCursorAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 3;
    private static final int VIEW_TYPE_PRICE = 0;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private boolean userFound = false;
    private boolean loading;

    public SearchCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        View view;
        switch (viewType) {
            case VIEW_TYPE_USER: {
                view = LayoutInflater.from(context).inflate(R.layout.list_search_user, parent, false);
                break;
            }
            case VIEW_TYPE_LOADING: {
                view = LayoutInflater.from(context).inflate(R.layout.list_search_loading, parent, false);
                break;
            }
            default: {
                view = LayoutInflater.from(context).inflate(R.layout.list_search, parent, false);

                ViewHolder viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
                break;
            }
        }
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_USER:
                ((TextView) view.findViewById(R.id.text_view_user_name)).setText(cursor.getString(SearchFragment.COL_PRICE_LIST_NAME));
                ((ImageView) view.findViewById(R.id.image_view_avatar)).setImageDrawable(Drawable.
                        createFromPath(context.getFilesDir().toString() + "/avatar_search.png"));
                break;
            case VIEW_TYPE_PRICE:
                ViewHolder viewHolder = (ViewHolder) view.getTag();

                int quality = cursor.getInt(SearchFragment.COL_PRICE_LIST_QUAL);
                int defindex = cursor.getInt(SearchFragment.COL_PRICE_LIST_DEFI);

                viewHolder.background.setBackgroundDrawable(Utility.getItemBackground(context,
                        defindex, quality,
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_TRAD),
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_CRAF)));

                viewHolder.nameView.setText(Utility.formatItemName(context,
                        defindex,
                        cursor.getString(SearchFragment.COL_PRICE_LIST_NAME),
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_TRAD),
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_CRAF),
                        quality,
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_INDE)));

                if (cursor.getInt(SearchFragment.COL_PRICE_LIST_INDE) == 0) {
                    setIconImage(context, viewHolder.icon, defindex,
                            cursor.getInt(SearchFragment.COL_AUSTRALIUM) == 1);
                } else {
                    setIconImageWithIndex(context, viewHolder.icon, defindex,
                            cursor.getInt(SearchFragment.COL_PRICE_LIST_INDE),
                            cursor.getInt(SearchFragment.COL_AUSTRALIUM) == 1, quality);
                }

                try {
                    viewHolder.priceView.setText(Utility.formatPrice(
                            context, cursor.getDouble(SearchFragment.COL_PRICE_LIST_PRIC),
                            cursor.getDouble(SearchFragment.COL_PRICE_LIST_PMAX),
                            cursor.getString(SearchFragment.COL_PRICE_LIST_CURR),
                            cursor.getString(SearchFragment.COL_PRICE_LIST_CURR),
                            true
                    ));
                } catch (Throwable throwable) {
                    if (Utility.isDebugging(context))
                        throwable.printStackTrace();
                }
                break;
            case VIEW_TYPE_LOADING:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (loading) {
            return position == 0 ? VIEW_TYPE_LOADING : VIEW_TYPE_PRICE;
        }
        return userFound && position == 0 ? VIEW_TYPE_USER : VIEW_TYPE_PRICE;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public boolean isEnabled(int position) {
        return userFound && position == 0;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            userFound = false;
        }
    }

    public void setUserFound(boolean userFound) {
        this.userFound = userFound;
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

    public static class ViewHolder {

        public final ImageView icon;
        public final ImageView background;

        public final TextView nameView;
        public final TextView priceView;

        public ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            background = (ImageView) view.findViewById(R.id.image_view_item_background);
            nameView = (TextView) view.findViewById(R.id.name);
            priceView = (TextView) view.findViewById(R.id.price);
        }
    }
}
