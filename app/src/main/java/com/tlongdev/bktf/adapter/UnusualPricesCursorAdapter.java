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
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.activity.UnusualActivity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Adapter to be used in the UnusualActivity.
 */
public class UnusualPricesCursorAdapter extends CursorAdapter {


    public UnusualPricesCursorAdapter(Context context, Cursor c, int flags, int defindex) {
        super(context, c, flags);
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

        LoadImagesTask task = (LoadImagesTask) viewHolder.icon.getTag();
        if (task != null) {
            task.cancel(true);
        }
        task = new LoadImagesTask(context, viewHolder.icon);
        viewHolder.icon.setTag(task);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (double) cursor.getInt(UnusualActivity.COL_PRICE_LIST_DEFI),
                (double) cursor.getInt(UnusualActivity.COL_PRICE_LIST_INDE));



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

        private LoadImagesTask(Context context, ImageView icon) {
            mContext = context;
            this.icon = icon;
        }

        @Override
        protected Drawable doInBackground(Double... params) {
            try {
                Drawable d;
                AssetManager assetManager = mContext.getAssets();
                InputStream ims = assetManager.open("items/" + Utility.getIconIndex(params[0].intValue()) + ".png");
                Drawable iconDrawable = Drawable.createFromStream(ims, null);
                ims = assetManager.open("effects/" + params[1].intValue() + "_188x188.png");
                Drawable effectDrawable = Drawable.createFromStream(ims, null);
                d = new LayerDrawable(new Drawable[]{effectDrawable, iconDrawable});

                return d;
            } catch (IOException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            icon.setImageDrawable(drawable);
        }
    }
}
