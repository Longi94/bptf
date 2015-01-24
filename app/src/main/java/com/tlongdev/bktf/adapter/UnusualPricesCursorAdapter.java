package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.UnusualActivity;
import com.tlongdev.bktf.Utility;

import java.io.IOException;
import java.io.InputStream;

/**
 * Adapter to be used in the UnusualActivity.
 */
public class UnusualPricesCursorAdapter extends CursorAdapter{

    int defindex;

    public UnusualPricesCursorAdapter(Context context, Cursor c, int flags, int defindex) {
        super(context, c, flags);
        this.defindex = defindex;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_items, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.icon.setImageDrawable(null);
        viewHolder.icon.setTag(cursor.getInt(UnusualActivity.COL_PRICE_LIST_INDE));

        new LoadImagesTask(context, viewHolder.icon).
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        (double)cursor.getInt(UnusualActivity.COL_PRICE_LIST_DEFI),
                        (double)cursor.getInt(UnusualActivity.COL_PRICE_LIST_INDE),
                        cursor.getDouble(UnusualActivity.COL_PRICE_LIST_DIFF),
                        cursor.getDouble(UnusualActivity.COL_PRICE_LIST_PRAW));

        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (cursor.getDouble(UnusualActivity.COL_PRICE_LIST_PRAW) >= Utility.getDouble(prefs, context.getString(R.string.pref_buds_raw), 0)) {
                viewHolder.priceView.setText(Utility.formatPrice(context,
                        cursor.getDouble(UnusualActivity.COL_PRICE_LIST_PRIC),
                        cursor.getDouble(UnusualActivity.COL_PRICE_LIST_PMAX),
                        cursor.getString(UnusualActivity.COL_PRICE_LIST_CURR),
                        Utility.CURRENCY_BUD, false));
            } else {
                viewHolder.priceView.setText(Utility.formatPrice(context,
                        cursor.getDouble(UnusualActivity.COL_PRICE_LIST_PRIC),
                        cursor.getDouble(UnusualActivity.COL_PRICE_LIST_PMAX),
                        cursor.getString(UnusualActivity.COL_PRICE_LIST_CURR),
                        Utility.CURRENCY_KEY, false));
            }
        } catch (Throwable throwable) {
            if (Utility.isDebugging(context))
                throwable.printStackTrace();
        }

        if (!Utility.isPriceOld(cursor.getInt(UnusualActivity.COL_PRICE_LIST_UPDA))){
            int difference = cursor.getInt(UnusualActivity.COL_PRICE_LIST_DIFF);
            if (difference > 0){
                viewHolder.priceView.setBackgroundColor(0x44008504);
            } else if (difference < 0){
                viewHolder.priceView.setBackgroundColor(0x44850000);
            } else {
                viewHolder.priceView.setBackgroundColor(0x44f2ee11);
            }
        } else {
            viewHolder.priceView.setBackgroundColor(0x44000000);
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public static class ViewHolder {

        public final ImageView icon;
        public final TextView priceView;

        public ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            priceView = (TextView) view.findViewById(R.id.grid_item_price);
        }
    }

    /**
     * Task for loading images in the background
     */
    private class LoadImagesTask extends AsyncTask<Double, Void, Drawable> {
        private Context mContext;
        private ImageView icon;
        private String path;
        private String errorMessage;

        private LoadImagesTask(Context context, ImageView icon) {
            mContext = context;
            this.icon = icon;
            path = icon.getTag().toString();
        }

        @Override
        protected Drawable doInBackground(Double... params) {
            try {
                Drawable d;
                AssetManager assetManager = mContext.getAssets();
                InputStream ims = assetManager.open("items/" + params[0].intValue() + ".png");
                Drawable iconDrawable = Drawable.createFromStream(ims, null);
                ims = assetManager.open("effects/" + params[1].intValue() + "_188x188.png");
                Drawable effectDrawable = Drawable.createFromStream(ims, null);
                d = new LayerDrawable(new Drawable[]{effectDrawable, iconDrawable});

                return d;
            } catch (IOException e) {
                errorMessage = e.getMessage();
                publishProgress();
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (icon.getTag().toString().equals(path)) {
                icon.setImageDrawable(drawable);
            }
        }
    }
}
