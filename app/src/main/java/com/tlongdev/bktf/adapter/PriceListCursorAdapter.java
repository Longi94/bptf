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
import com.tlongdev.bktf.enums.Quality;
import com.tlongdev.bktf.fragment.HomeFragment;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

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

        setItemBackground(viewHolder.icon, cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL));
        viewHolder.icon.setImageDrawable(null);
        viewHolder.change.setImageDrawable(null);

        viewHolder.nameView.setText(Utility.formatItemName(cursor.getString(HomeFragment.COL_PRICE_LIST_NAME),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_TRAD),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_CRAF),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE)));

        viewHolder.icon.setTag(viewHolder.nameView.getText());

        new LoadImagesTask(context, viewHolder.icon, viewHolder.change)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (double)cursor.getInt(HomeFragment.COL_PRICE_LIST_DEFI),
                (double)cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE),
                cursor.getDouble(HomeFragment.COL_PRICE_LIST_DIFF),
                cursor.getDouble(HomeFragment.COL_PRICE_LIST_PRAW)
        );


        double price = cursor.getDouble(HomeFragment.COL_PRICE_LIST_PRIC);
        if ((int)price == price)
            viewHolder.priceView.setText("" + (int)price);
        else
            viewHolder.priceView.setText("" + price);

        price = cursor.getDouble(HomeFragment.COL_PRICE_LIST_PMAX);

        if (price > 0.0){
            if ((int)price == price)
                viewHolder.priceView.append(" - " + (int)price);
            else
                viewHolder.priceView.append(" - " + price);
        }

        viewHolder.priceView.append(" " + cursor.getString(HomeFragment.COL_PRICE_LIST_CURR));
    }

    private void setItemBackground(View frame, int quality) {
        Quality q = Quality.values()[quality];

        switch (q) {
            case GENUINE:
                frame.setBackgroundColor(0xFF4D7455);
                break;
            case VINTAGE:
                frame.setBackgroundColor(0xFF476291);
                break;
            case UNUSUAL:
                frame.setBackgroundColor(0xFF8650AC);
                break;
            case UNIQUE:
                frame.setBackgroundColor(0xFFFFD700);
                break;
            case COMMUNITY:
                frame.setBackgroundColor(0xFF70B04A);
                break;
            case VALVE:
                frame.setBackgroundColor(0xFFA50F79);
                break;
            case SELF_MADE:
                frame.setBackgroundColor(0xFF70B04A);
                break;
            case STRANGE:
                frame.setBackgroundColor(0xFFCF6A32);
                break;
            case HAUNTED:
                frame.setBackgroundColor(0xFF38F3AB);
                break;
            case COLLECTORS:
                frame.setBackgroundColor(0xFFAA0000);
                break;
            default:
                frame.setBackgroundColor(0xFFB2B2B2);
                break;
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
                Drawable[] returnVal = new Drawable[2];
                AssetManager assetManager = mContext.getAssets();
                InputStream ims = assetManager.open("items/" + (new DecimalFormat("#0")).format(params[0]) + ".png");
                Drawable iconDrawable = Drawable.createFromStream(ims, null);
                if (params[1] != 0) {
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

                returnVal[1] = Drawable.createFromStream(ims, null);

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
                change.setImageDrawable(drawable[1]);
            }
        }
    }
}
