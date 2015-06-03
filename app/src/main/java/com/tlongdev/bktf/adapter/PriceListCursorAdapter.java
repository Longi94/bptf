package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.HomeFragment;

import java.io.IOException;
import java.io.InputStream;

public class PriceListCursorAdapter extends CursorAdapter {

    public PriceListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_changes, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.icon.setVisibility(View.INVISIBLE);
        viewHolder.background.setBackgroundColor(Utility.getBackgroundColor(context,
                cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL)));

        String itemTag = Utility.formatItemName(context,
                cursor.getInt(HomeFragment.COL_PRICE_LIST_DEFI),
                cursor.getString(HomeFragment.COL_PRICE_LIST_NAME),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_TRAD),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_CRAF),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE));
        viewHolder.nameView.setText(itemTag);

        viewHolder.nameView.setTag(itemTag);

        viewHolder.icon.setImageDrawable(null);

        LoadImagesTask task = (LoadImagesTask) viewHolder.icon.getTag();
        if (task != null) {
            task.cancel(true);
        }
        task = new LoadImagesTask(context, viewHolder);
        viewHolder.icon.setTag(task);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (double) cursor.getInt(HomeFragment.COL_PRICE_LIST_DEFI),
                (double) cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE),
                cursor.getDouble(HomeFragment.COL_PRICE_LIST_DIFF),
                cursor.getDouble(HomeFragment.COL_PRICE_LIST_PRAW),
                (double) cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL),
                (double) cursor.getInt(HomeFragment.COL_PRICE_LIST_TRAD),
                (double) cursor.getInt(HomeFragment.COL_PRICE_LIST_CRAF)
        );

        try {
            viewHolder.priceView.setText(Utility.formatPrice(context,
                    cursor.getDouble(HomeFragment.COL_PRICE_LIST_PRIC),
                    cursor.getDouble(HomeFragment.COL_PRICE_LIST_PMAX),
                    cursor.getString(HomeFragment.COL_PRICE_LIST_CURR),
                    cursor.getString(HomeFragment.COL_PRICE_LIST_CURR), false));
        } catch (Throwable throwable) {
            Toast.makeText(context, "bptf: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            if (Utility.isDebugging(context))
                throwable.printStackTrace();
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public static class ViewHolder {
        public final View background;
        public final ImageView icon;

        public final TextView nameView;
        public final TextView priceView;

        public ViewHolder(View view) {
            background = view.findViewById(R.id.view_background);
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            nameView = (TextView) view.findViewById(R.id.item_name);
            priceView = (TextView) view.findViewById(R.id.item_price);
        }
    }

    private class LoadImagesTask extends AsyncTask<Double, Void, Drawable> {
        private Context mContext;
        private ViewHolder viewHolder;
        private String name;

        private LoadImagesTask(Context context, ViewHolder viewHolder) {
            mContext = context;
            this.viewHolder = viewHolder;
            name = (String) viewHolder.nameView.getTag();
        }

        @Override
        protected Drawable doInBackground(Double... params) {
            try {
                Drawable returnVal;
                AssetManager assetManager = mContext.getAssets();

                InputStream ims;
                if (name.contains("Australium") && params[0] != 5037) {
                    ims = assetManager.open("items/" + Utility.getIconIndex(params[0].intValue()) + "aus.png");
                } else {
                    ims = assetManager.open("items/" + Utility.getIconIndex(params[0].intValue()) + ".png");
                }

                Drawable iconDrawable = Drawable.createFromStream(ims, null);
                if (params[1] != 0 && Utility.canHaveEffects(params[0].intValue(), params[4].intValue())) {
                    ims = assetManager.open("effects/" + params[1].intValue() + "_188x188.png");
                    Drawable effectDrawable = Drawable.createFromStream(ims, null);
                    returnVal = new LayerDrawable(new Drawable[]{effectDrawable, iconDrawable});
                } else {
                    returnVal = iconDrawable;
                }

                return returnVal;
            } catch (IOException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (drawable != null) {
                viewHolder.icon.setImageDrawable(drawable);
            } else {
                viewHolder.icon.setImageDrawable(null);
            }
            Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.simple_fade_in);
            fadeIn.setDuration(100);
            viewHolder.icon.startAnimation(fadeIn);
            viewHolder.icon.setVisibility(View.VISIBLE);
        }
    }
}
