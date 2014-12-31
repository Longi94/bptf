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
import com.tlongdev.bktf.fragment.HomeFragment;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class PriceListCursorAdapter extends CursorAdapter {

    boolean isCrate = false;

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

        viewHolder.icon.setImageDrawable(null);
        viewHolder.icon.setBackgroundDrawable(null);
        viewHolder.change.setImageDrawable(null);

        viewHolder.nameView.setText(Utility.formatItemName(cursor.getString(HomeFragment.COL_PRICE_LIST_NAME),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_TRAD),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_CRAF),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE)));

        isCrate = cursor.getString(HomeFragment.COL_PRICE_LIST_NAME).contains("Crate");

        viewHolder.icon.setTag(viewHolder.nameView.getText());

        new LoadImagesTask(context, viewHolder.icon, viewHolder.change)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (double)cursor.getInt(HomeFragment.COL_PRICE_LIST_DEFI),
                (double)cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE),
                cursor.getDouble(HomeFragment.COL_PRICE_LIST_DIFF),
                cursor.getDouble(HomeFragment.COL_PRICE_LIST_PRAW),
                (double)cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL),
                (double)cursor.getInt(HomeFragment.COL_PRICE_LIST_TRAD),
                (double)cursor.getInt(HomeFragment.COL_PRICE_LIST_CRAF)
        );

        try {
            viewHolder.priceView.setText(Utility.formatPrice(context,
                    cursor.getDouble(HomeFragment.COL_PRICE_LIST_PRIC),
                    cursor.getDouble(HomeFragment.COL_PRICE_LIST_PMAX),
                    cursor.getString(HomeFragment.COL_PRICE_LIST_CURR),
                    cursor.getString(HomeFragment.COL_PRICE_LIST_CURR)));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static class ViewHolder {
        public final ImageView change;
        public final ImageView icon;

        public final TextView nameView;
        public final TextView priceView;

        public ViewHolder(View view) {
            change = (ImageView) view.findViewById(R.id.image_view_change);
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            nameView = (TextView) view.findViewById(R.id.item_name);
            priceView = (TextView) view.findViewById(R.id.item_price);
        }
    }

    private class LoadImagesTask extends AsyncTask<Double, Void, Drawable[]>{
        private Context mContext;
        private ImageView icon;
        private ImageView change;
        private String path;

        private LoadImagesTask(Context context, ImageView icon, ImageView change) {
            mContext = context;
            this.icon = icon;
            this.change = change;
            path = icon.getTag().toString();
        }

        @Override
        protected Drawable[] doInBackground(Double... params) {
            try {
                Drawable[] returnVal = new Drawable[3];
                AssetManager assetManager = mContext.getAssets();
                InputStream ims = assetManager.open("items/" + (new DecimalFormat("#0")).format(params[0]) + ".png");
                Drawable iconDrawable = Drawable.createFromStream(ims, null);
                if (params[1] != 0 && !isCrate) {
                    ims = assetManager.open("effects/" + (new DecimalFormat("#0")).format(params[1]) + "_380x380.png");
                    Drawable effectDrawable = Drawable.createFromStream(ims, null);
                    returnVal[0] = new LayerDrawable(new Drawable[]{effectDrawable, iconDrawable});
                } else {
                    returnVal[0] = iconDrawable;
                }

                if (params[2].equals(params[3])) {
                    ims = mContext.getAssets().open("changes/new.png");
                }
                else if (params[2] == 0.0) {
                    ims = mContext.getAssets().open("changes/refresh.png");
                }
                else if (params[2] > 0.0) {
                    ims = mContext.getAssets().open("changes/up.png");
                }
                else {
                    ims = mContext.getAssets().open("changes/down.png");
                }

                returnVal[2] = Drawable.createFromStream(ims, null);


                returnVal[1] = Utility.getItemBackground(mContext, params[4].intValue(), params[5].intValue(), params[6].intValue());

                return returnVal;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable[] drawable) {
            if (icon.getTag().toString().equals(path)) {
                icon.setImageDrawable(drawable[0]);
                icon.setBackgroundDrawable(drawable[1]);
                change.setImageDrawable(drawable[2]);
            }
        }
    }
}
