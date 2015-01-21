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
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.HomeFragment;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class PriceListCursorAdapter extends CursorAdapter {

    private final Map<String, Drawable[]> drawableManager;

    public PriceListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        drawableManager = new LinkedHashMap<String, Drawable[]>(15, 0.75f, true){
            @Override
            protected boolean removeEldestEntry(Entry<String, Drawable[]> eldest) {
                return size() > 15;
            }
        };
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

        String itemTag = Utility.formatItemName(cursor.getString(HomeFragment.COL_PRICE_LIST_NAME),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_TRAD),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_CRAF),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE));
        viewHolder.nameView.setText(itemTag);

        viewHolder.icon.setTag(itemTag);

        if (!drawableManager.containsKey(itemTag)) {
            viewHolder.icon.setImageDrawable(null);
            viewHolder.background.setBackgroundDrawable(null);
            viewHolder.change.setImageDrawable(null);
            new LoadImagesTask(context, viewHolder.icon, viewHolder.background, viewHolder.change)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            (double) cursor.getInt(HomeFragment.COL_PRICE_LIST_DEFI),
                            (double) cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE),
                            cursor.getDouble(HomeFragment.COL_PRICE_LIST_DIFF),
                            cursor.getDouble(HomeFragment.COL_PRICE_LIST_PRAW),
                            (double) cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL),
                            (double) cursor.getInt(HomeFragment.COL_PRICE_LIST_TRAD),
                            (double) cursor.getInt(HomeFragment.COL_PRICE_LIST_CRAF)
                    );
        } else {
            Drawable[] drawable = drawableManager.get(itemTag);
            viewHolder.icon.setImageDrawable(drawable[0]);
            viewHolder.background.setBackgroundDrawable(drawable[1]);
            viewHolder.change.setImageDrawable(drawable[2]);
        }

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
        public final ImageView change;
        public final ImageView icon;
        public final ImageView background;

        public final TextView nameView;
        public final TextView priceView;

        public ViewHolder(View view) {
            change = (ImageView) view.findViewById(R.id.image_view_change);
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            background = (ImageView) view.findViewById(R.id.image_view_item_background);
            nameView = (TextView) view.findViewById(R.id.item_name);
            priceView = (TextView) view.findViewById(R.id.item_price);
        }
    }

    private class LoadImagesTask extends AsyncTask<Double, Void, Drawable[]>{
        private Context mContext;
        private ImageView icon;
        private ImageView background;
        private ImageView change;
        private String path;
        private String errorMessage;

        private LoadImagesTask(Context context, ImageView icon, ImageView background, ImageView change) {
            mContext = context;
            this.icon = icon;
            this.change = change;
            this.background = background;
            path = icon.getTag().toString();
        }

        @Override
        protected Drawable[] doInBackground(Double... params) {
            if (drawableManager.containsKey(path)){
                return drawableManager.get(path);
            }
            try {
                Drawable[] returnVal = new Drawable[3];
                AssetManager assetManager = mContext.getAssets();

                InputStream ims;
                if (path.contains("Australium") && params[0] != 5037) {
                    ims = assetManager.open("items/" + (new DecimalFormat("#0")).format(params[0]) + "aus.png");
                } else {
                    ims = assetManager.open("items/" + (new DecimalFormat("#0")).format(params[0]) + ".png");
                }

                Drawable iconDrawable = Drawable.createFromStream(ims, null);
                if (params[1] != 0 && !path.contains("Crate")) {
                    ims = assetManager.open("effects/" + (new DecimalFormat("#0")).format(params[1]) + "_188x188.png");
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

                drawableManager.put(path, returnVal);
                return returnVal;
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
        protected void onPostExecute(Drawable[] drawable) {
            if (icon.getTag().toString().equals(path) && drawable != null) {
                icon.setImageDrawable(drawable[0]);
                background.setBackgroundDrawable(drawable[1]);
                change.setImageDrawable(drawable[2]);
            }
        }
    }
}
