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

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.RecentsFragment;

import java.io.IOException;
import java.io.InputStream;

public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.ViewHolder> {

    private Context mContext;
    private Cursor mDataSet;

    public RecentsAdapter(Context context, Cursor dataSet) {
        this.mContext = context;
        this.mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_changes, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {
            holder.view.setVisibility(View.INVISIBLE);
            holder.nameView.setVisibility(View.INVISIBLE);
            holder.priceView.setVisibility(View.INVISIBLE);

            String itemTag = Utility.formatItemName(mContext,
                    mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_DEFI),
                    mDataSet.getString(RecentsFragment.COL_PRICE_LIST_NAME),
                    mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_TRAD),
                    mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_CRAF),
                    mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_QUAL),
                    mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_INDE));
            holder.nameView.setText(itemTag);

            holder.background.setTag(itemTag);

            holder.icon.setImageDrawable(null);
            holder.background.setBackgroundDrawable(null);
            holder.change.setImageDrawable(null);

            LoadImagesTask task = (LoadImagesTask) holder.icon.getTag();
            if (task != null) {
                task.cancel(true);
            }
            task = new LoadImagesTask(mContext, holder.view, holder);
            holder.icon.setTag(task);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    (double) mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_DEFI),
                    (double) mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_INDE),
                    mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_DIFF),
                    mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_PRAW),
                    (double) mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_QUAL),
                    (double) mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_TRAD),
                    (double) mDataSet.getInt(RecentsFragment.COL_PRICE_LIST_CRAF),
                    (double) mDataSet.getInt(RecentsFragment.COL_AUSTRALIUM)
            );

            try {
                holder.priceView.setText(Utility.formatPrice(mContext,
                        mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_PRIC),
                        mDataSet.getDouble(RecentsFragment.COL_PRICE_LIST_PMAX),
                        mDataSet.getString(RecentsFragment.COL_PRICE_LIST_CURR),
                        mDataSet.getString(RecentsFragment.COL_PRICE_LIST_CURR), false));
            } catch (Throwable throwable) {
                if (Utility.isDebugging(mContext))
                    throwable.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.getCount();
    }

    public void swapCursor(Cursor data) {
        if (mDataSet != null) mDataSet.close();
        mDataSet = data;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public final View view;

        public final ImageView change;
        public final ImageView icon;
        public final ImageView background;

        public final TextView nameView;
        public final TextView priceView;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            change = (ImageView) view.findViewById(R.id.image_view_change);
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            background = (ImageView) view.findViewById(R.id.image_view_item_background);
            nameView = (TextView) view.findViewById(R.id.item_name);
            priceView = (TextView) view.findViewById(R.id.item_price);
        }
    }


    private class LoadImagesTask extends AsyncTask<Double, Void, Drawable[]> {
        private Context mContext;
        private View rootView;
        private ViewHolder viewHolder;
        private String name;

        private LoadImagesTask(Context context, View rootView, ViewHolder viewHolder) {
            mContext = context;
            this.rootView = rootView;
            this.viewHolder = viewHolder;
            name = (String) viewHolder.background.getTag();
        }

        @Override
        protected Drawable[] doInBackground(Double... params) {
            try {
                Drawable[] returnVal = new Drawable[3];
                AssetManager assetManager = mContext.getAssets();

                InputStream ims;
                if (params[7] == 1 && params[0] != 5037) {
                    ims = assetManager.open("items/" + Utility.getIconIndex(params[0].intValue()) + "aus.png");
                } else {
                    ims = assetManager.open("items/" + Utility.getIconIndex(params[0].intValue()) + ".png");
                }

                Drawable iconDrawable = Drawable.createFromStream(ims, null);
                if (params[1] != 0 && Utility.canHaveEffects(params[0].intValue(), params[4].intValue())) {
                    ims = assetManager.open("effects/" + params[1].intValue() + "_188x188.png");
                    Drawable effectDrawable = Drawable.createFromStream(ims, null);
                    returnVal[0] = new LayerDrawable(new Drawable[]{effectDrawable, iconDrawable});
                } else {
                    returnVal[0] = iconDrawable;
                }

                if (params[2].equals(params[3])) {
                    ims = mContext.getAssets().open("changes/new.png");
                } else if (params[2] == 0.0) {
                    ims = mContext.getAssets().open("changes/refresh.png");
                } else if (params[2] > 0.0) {
                    ims = mContext.getAssets().open("changes/up.png");
                } else {
                    ims = mContext.getAssets().open("changes/down.png");
                }

                returnVal[2] = Drawable.createFromStream(ims, null);

                returnVal[1] = Utility.getItemBackground(mContext, params[0].intValue(),
                        params[4].intValue(), params[5].intValue(), params[6].intValue());

                return returnVal;
            } catch (IOException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable[] drawable) {
            if (drawable != null) {
                viewHolder.icon.setImageDrawable(drawable[0]);
                viewHolder.background.setBackgroundDrawable(drawable[1]);
                viewHolder.change.setImageDrawable(drawable[2]);
            } else {
                viewHolder.icon.setImageDrawable(null);
                viewHolder.background.setBackgroundDrawable(null);
                viewHolder.change.setImageDrawable(null);
            }
            Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.simple_fade_in);
            fadeIn.setDuration(100);
            rootView.startAnimation(fadeIn);
            rootView.setVisibility(View.VISIBLE);
            viewHolder.nameView.setVisibility(View.VISIBLE);
            viewHolder.priceView.setVisibility(View.VISIBLE);
        }
    }
}
