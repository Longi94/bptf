package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.UnusualActivity;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.UnusualPriceListFragment;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class UnusualEffectListCursorAdapter extends CursorAdapter {

    public UnusualEffectListCursorAdapter(Context context, Cursor c, int flags) {
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

        final int index = cursor.getInt(UnusualPriceListFragment.COL_PRICE_LIST_INDE);

        LoadImagesTask task = (LoadImagesTask)viewHolder.icon.getTag();
        if (task != null){
            task.cancel(true);
        }
        task = new LoadImagesTask(context, viewHolder.icon);
        viewHolder.icon.setTag(task);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, index);

        viewHolder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UnusualActivity.class);
                i.putExtra(UnusualActivity.PRICE_INDEX_KEY, index);
                i.putExtra(UnusualActivity.NAME_KEY, Utility.getUnusualEffectName(index));
                context.startActivity(i);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        double rawKeyPrice = Utility.getDouble(prefs, context.getString(R.string.pref_key_raw), 1);
        viewHolder.priceView.setText("" +
                new DecimalFormat("#0.00").format(cursor.getDouble(UnusualPriceListFragment.COL_PRICE_LIST_AVG_PRICE) / rawKeyPrice)
                + " keys");
    }

    public static class ViewHolder {

        public final ImageView icon;
        public final TextView priceView;

        public ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            priceView = (TextView) view.findViewById(R.id.grid_item_price);
        }
    }

    private class LoadImagesTask extends AsyncTask<Integer, Void, Drawable> {
        private Context mContext;
        private ImageView icon;

        private LoadImagesTask(Context context, ImageView icon) {
            mContext = context;
            this.icon = icon;
        }

        @Override
        protected Drawable doInBackground(Integer... params) {
            try {
                InputStream ims = mContext.getAssets().open("effects/" + params[0] + "_188x188.png");
                return Drawable.createFromStream(ims, null);
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
