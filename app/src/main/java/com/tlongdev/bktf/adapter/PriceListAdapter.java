package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.HomeFragment;

import java.io.IOException;
import java.io.InputStream;

public class PriceListAdapter extends RecyclerView.Adapter<PriceListAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mDataSet;

    public PriceListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_changes, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {

            holder.icon.setVisibility(View.INVISIBLE);
            holder.background.setBackgroundColor(Utility.getBackgroundColor(mContext,
                    mDataSet.getInt(HomeFragment.COL_PRICE_LIST_QUAL)));

            String itemTag = Utility.formatItemName(mContext,
                    mDataSet.getInt(HomeFragment.COL_PRICE_LIST_DEFI),
                    mDataSet.getString(HomeFragment.COL_PRICE_LIST_NAME),
                    mDataSet.getInt(HomeFragment.COL_PRICE_LIST_TRAD),
                    mDataSet.getInt(HomeFragment.COL_PRICE_LIST_CRAF),
                    mDataSet.getInt(HomeFragment.COL_PRICE_LIST_QUAL),
                    mDataSet.getInt(HomeFragment.COL_PRICE_LIST_INDE));
            holder.nameView.setText(itemTag);

            holder.nameView.setTag(itemTag);

            holder.icon.setImageDrawable(null);

            LoadImagesTask task = (LoadImagesTask) holder.icon.getTag();
            if (task != null) {
                task.cancel(true);
            }
            task = new LoadImagesTask(mContext, holder);
            holder.icon.setTag(task);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    (double) mDataSet.getInt(HomeFragment.COL_PRICE_LIST_DEFI),
                    (double) mDataSet.getInt(HomeFragment.COL_PRICE_LIST_INDE),
                    (double) mDataSet.getInt(HomeFragment.COL_PRICE_LIST_QUAL)
            );

            try {
                holder.priceView.setText(Utility.formatPrice(mContext,
                        mDataSet.getDouble(HomeFragment.COL_PRICE_LIST_PRIC),
                        mDataSet.getDouble(HomeFragment.COL_PRICE_LIST_PMAX),
                        mDataSet.getString(HomeFragment.COL_PRICE_LIST_CURR),
                        mDataSet.getString(HomeFragment.COL_PRICE_LIST_CURR), false));
            } catch (Throwable throwable) {
                Toast.makeText(mContext, "bptf: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                if (Utility.isDebugging(mContext))
                    throwable.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mDataSet == null) return 0;
        return mDataSet.getCount();
    }

    public void swapCursor(Cursor data) {
        mDataSet = data;
        notifyDataSetChanged();
    }

    public static class ViewHolder  extends RecyclerView.ViewHolder{
        public final View background;
        public final ImageView icon;

        public final TextView nameView;
        public final TextView priceView;

        public ViewHolder(View view) {
            super(view);
            background = view.findViewById(R.id.view_background);
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            nameView = (TextView) view.findViewById(R.id.item_name);
            priceView = (TextView) view.findViewById(R.id.item_price);
        }
    }

    private class LoadImagesTask extends AsyncTask<Double, Void, Drawable> {
        private ViewHolder viewHolder;
        private String name;

        private LoadImagesTask(Context context, ViewHolder viewHolder) {
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
                if (params[1] != 0 && Utility.canHaveEffects(params[0].intValue(), params[2].intValue())) {
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
