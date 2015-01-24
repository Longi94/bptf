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
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.SearchFragment;

import java.io.IOException;
import java.io.InputStream;

public class SearchCursorAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_PRICE = 0;
    private static final int VIEW_TYPE_USER = 1;

    private boolean userFound = false;

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
        switch (viewType){
            case VIEW_TYPE_USER:
                ((TextView)view.findViewById(R.id.text_view_user_name)).setText(cursor.getString(SearchFragment.COL_PRICE_LIST_NAME));
                ((ImageView)view.findViewById(R.id.image_view_avatar)).setImageDrawable(Drawable.
                        createFromPath(context.getFilesDir().toString() + "/avatar_search.png"));
                break;
            case VIEW_TYPE_PRICE:
                ViewHolder viewHolder = (ViewHolder) view.getTag();

                viewHolder.background.setBackgroundDrawable(Utility.getItemBackground(context,
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_QUAL),
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_TRAD),
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_CRAF)));

                viewHolder.nameView.setText(Utility.formatItemName(
                        cursor.getString(SearchFragment.COL_PRICE_LIST_NAME),
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_TRAD),
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_CRAF),
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_QUAL),
                        cursor.getInt(SearchFragment.COL_PRICE_LIST_INDE)));

                if (cursor.getInt(SearchFragment.COL_PRICE_LIST_INDE) == 0) {
                    setIconImage(context, viewHolder.icon, cursor.getInt(SearchFragment.COL_PRICE_LIST_DEFI),
                            viewHolder.nameView.getText().toString().contains("Australium"));
                } else if (!cursor.getString(SearchFragment.COL_PRICE_LIST_NAME).contains("Crate")) {
                    setIconImageWithIndex(context, viewHolder.icon, cursor.getInt(SearchFragment.COL_PRICE_LIST_DEFI),
                            cursor.getInt(SearchFragment.COL_PRICE_LIST_INDE),
                            viewHolder.nameView.getText().toString().contains("Australium"));
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
                    Toast.makeText(context, "bptf: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    if (Utility.isDebugging(context))
                        throwable.printStackTrace();
                }
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
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

    public void setUserFound(boolean userFound) {
        this.userFound = userFound;
    }

    private void setIconImage(Context context, ImageView icon, int defindex, boolean isAustralium) {
        try {
            InputStream ims;
            AssetManager assetManager = context.getAssets();
            if (isAustralium && defindex != 5037) {
                ims = assetManager.open("items/" + defindex + "aus.png");
            } else {
                ims = assetManager.open("items/" + defindex + ".png");
            }

            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            icon.setImageDrawable(d);
        } catch (IOException e) {
            Toast.makeText(context, "bptf: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (Utility.isDebugging(context))
                e.printStackTrace();
        }
    }

    private void setIconImageWithIndex(Context context, ImageView icon, int defindex, int priceIndex, boolean isAustralium) {
        try {
            InputStream ims;
            AssetManager assetManager = context.getAssets();
            if (isAustralium && defindex != 5037) {
                ims = assetManager.open("items/" + defindex + "aus.png");
            } else {
                ims = assetManager.open("items/" + defindex + ".png");
            }

            // load image as Drawable
            Drawable iconDrawable = Drawable.createFromStream(ims, null);

            ims = assetManager.open("effects/" + priceIndex + "_188x188.png");
            Drawable effectDrawable = Drawable.createFromStream(ims, null);
            // set image to ImageView
            icon.setImageDrawable(new LayerDrawable(new Drawable[]{effectDrawable, iconDrawable}));
        } catch (IOException e) {
            Toast.makeText(context, "bptf: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (Utility.isDebugging(context))
                e.printStackTrace();
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
            nameView = (TextView) view.findViewById(R.id.item_name);
            priceView = (TextView) view.findViewById(R.id.item_price);
        }
    }
}
