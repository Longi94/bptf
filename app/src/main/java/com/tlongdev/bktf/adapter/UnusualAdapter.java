package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.activity.UnusualActivity;
import com.tlongdev.bktf.enums.Currency;
import com.tlongdev.bktf.fragment.UnusualFragment;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class UnusualAdapter extends RecyclerView.Adapter<UnusualAdapter.ViewHolder> {

    public static final int TYPE_HATS = 0;
    public static final int TYPE_EFFECTS = 1;
    public static final int TYPE_SPECIFIC_HAT = 2;

    private Cursor mDataSet;
    private Context mContext;

    private int type = 0;

    public UnusualAdapter(Context context, Cursor dataSet) {
        this.mContext = context;
        this.mDataSet = dataSet;
    }

    public UnusualAdapter(Context context, Cursor dataSet, int type) {
        this.mContext = context;
        this.mDataSet = dataSet;
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_unusual, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataSet.moveToPosition(position)) {

            String iconPath = "";

            AssetManager assets = mContext.getAssets();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            double rawKeyPrice = Utility.getDouble(prefs, mContext.getString(R.string.pref_key_raw), 1);

            switch (type) {
                case TYPE_HATS:
                    final int defindex = mDataSet.getInt(UnusualFragment.COL_PRICE_LIST_DEFI);
                    final String name = mDataSet.getString(UnusualFragment.COL_PRICE_LIST_NAME);
                    iconPath = "items/" + Utility.getIconIndex(defindex) + ".png";

                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, UnusualActivity.class);
                            i.putExtra(UnusualActivity.DEFINDEX_KEY, defindex);
                            i.putExtra(UnusualActivity.NAME_KEY, name);
                            mContext.startActivity(i);
                        }
                    });
                    holder.price.setText(mContext.getString(R.string.currency_key_plural,
                            new DecimalFormat("#0.00").format(mDataSet.getDouble(
                                    UnusualFragment.COL_PRICE_LIST_AVG_PRICE) / rawKeyPrice)));
                    break;
                case TYPE_EFFECTS:
                    final int index = mDataSet.getInt(UnusualFragment.COL_PRICE_LIST_INDE);
                    iconPath = "effects/" + index + "_188x188.png";

                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, UnusualActivity.class);
                            i.putExtra(UnusualActivity.PRICE_INDEX_KEY, index);
                            i.putExtra(UnusualActivity.NAME_KEY, Utility.getUnusualEffectName(mContext, index));
                            mContext.startActivity(i);
                        }
                    });

                    holder.price.setText(mContext.getString(R.string.currency_key_plural,
                            new DecimalFormat("#0.00").format(mDataSet.getDouble(
                                    UnusualFragment.COL_PRICE_LIST_AVG_PRICE) / rawKeyPrice)));
                    break;
                case TYPE_SPECIFIC_HAT:
                    try {
                        holder.price.setText(Utility.formatPrice(mContext,
                                mDataSet.getDouble(UnusualActivity.COL_PRICE_LIST_PRIC),
                                mDataSet.getDouble(UnusualActivity.COL_PRICE_LIST_PMAX),
                                mDataSet.getString(UnusualActivity.COL_PRICE_LIST_CURR),
                                Currency.KEY, false));
                    } catch (IllegalArgumentException e) {
                        if (Utility.isDebugging(mContext))
                            e.printStackTrace();
                        holder.price.setText(null);
                    }

                    iconPath = "items/" + Utility.getIconIndex(mDataSet.getInt(UnusualActivity.COL_PRICE_LIST_DEFI)) + ".png";

                    try {
                        InputStream ims = assets.open("effects/" + mDataSet.getInt(UnusualActivity.COL_PRICE_LIST_INDE) + "_188x188.png");
                        holder.effect.setImageDrawable(Drawable.createFromStream(ims, null));
                    } catch (IOException e) {
                        if (Utility.isDebugging(mContext))
                            e.printStackTrace();
                        holder.effect.setImageDrawable(null);
                    }
                    break;
            }

            try {
                InputStream ims = assets.open(iconPath);
                holder.icon.setImageDrawable(Drawable.createFromStream(ims, null));
            } catch (IOException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                holder.icon.setImageDrawable(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.getCount();
    }

    public void swapCursor(Cursor data, boolean closePrevious) {
        if (closePrevious && mDataSet != null) mDataSet.close();
        mDataSet = data;
        notifyDataSetChanged();
    }

    public void setType(int type) {
        this.type = type;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView icon;
        public final ImageView effect;
        public final TextView price;

        public final View root;

        public ViewHolder(View view) {
            super(view);

            root = view;

            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            effect = (ImageView) view.findViewById(R.id.image_view_item_effect);
            price = (TextView) view.findViewById(R.id.grid_item_price);
        }
    }
}
