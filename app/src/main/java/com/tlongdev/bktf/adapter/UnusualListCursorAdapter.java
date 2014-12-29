package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.UnusualActivity;
import com.tlongdev.bktf.data.PriceListContract;
import com.tlongdev.bktf.fragment.UnusualPriceListFragment;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class UnusualListCursorAdapter extends CursorAdapter {

    private double rawBudsPrice;

    public UnusualListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        String[] columns = {PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_RAW};
        Cursor cursor = context.getContentResolver().query(
                PriceListContract.PriceEntry.buildPriceListUriWithNameSpecific(
                        "Earbuds",
                        6,
                        1,
                        1,
                        0
                ),
                columns,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            rawBudsPrice = cursor.getDouble(0);
        }
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

        final int defindex = cursor.getInt(UnusualPriceListFragment.COL_PRICE_LIST_DEFI);
        viewHolder.icon.setTag("" + defindex);

        new LoadImagesTask(context, viewHolder.icon).
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, defindex);

        viewHolder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UnusualActivity.class);
                i.putExtra(UnusualActivity.DEFINDEX_KEY, defindex);
                context.startActivity(i);
            }
        });

        viewHolder.priceView.setText("" +
                new DecimalFormat("#0.00").format(cursor.getDouble(UnusualPriceListFragment.COL_PRICE_LIST_AVG_PRICE) / rawBudsPrice)
                + " buds");
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
        private String path;

        private LoadImagesTask(Context context, ImageView icon) {
            mContext = context;
            this.icon = icon;
            path = icon.getTag().toString();
        }

        @Override
        protected Drawable doInBackground(Integer... params) {
            try {
                InputStream ims = mContext.getAssets().open("items/" + params[0] + ".png");
                return Drawable.createFromStream(ims, null);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (icon.getTag().toString().equals(path)) {
                icon.setImageDrawable(drawable);
            }
        }
    }
}
