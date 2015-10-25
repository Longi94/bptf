package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.enums.Currency;

import java.io.IOException;
import java.util.ArrayList;

public class CalculatorAdapter extends RecyclerView.Adapter<CalculatorAdapter.ViewHolder> {

    private ArrayList<Utility.IntegerPair> ids;
    private Context mContext;

    private static final String[] PRICE_LIST_COLUMNS = {
            PriceEntry.TABLE_NAME + "." + PriceEntry._ID,
            PriceEntry.COLUMN_DEFINDEX,
            PriceEntry.COLUMN_ITEM_NAME,
            PriceEntry.COLUMN_ITEM_QUALITY,
            PriceEntry.COLUMN_ITEM_TRADABLE,
            PriceEntry.COLUMN_ITEM_CRAFTABLE,
            PriceEntry.COLUMN_PRICE_INDEX,
            PriceEntry.COLUMN_CURRENCY,
            PriceEntry.COLUMN_PRICE,
            PriceEntry.COLUMN_PRICE_HIGH,
            null,
            PriceEntry.COLUMN_AUSTRALIUM
    };

    //Indexes for the columns above
    public static final int COL_PRICE_LIST_DEFI = 1;
    public static final int COL_PRICE_LIST_NAME = 2;
    public static final int COL_PRICE_LIST_QUAL = 3;
    public static final int COL_PRICE_LIST_TRAD = 4;
    public static final int COL_PRICE_LIST_CRAF = 5;
    public static final int COL_PRICE_LIST_INDE = 6;
    public static final int COL_PRICE_LIST_CURR = 7;
    public static final int COL_PRICE_LIST_PRIC = 8;
    public static final int COL_PRICE_LIST_PMAX = 9;
    public static final int COL_PRICE_LIST_PRAW = 10;
    public static final int COL_AUSTRALIUM = 11;

    public static final String mSelection = PriceEntry.TABLE_NAME +
            "." + PriceEntry._ID + " = ?";

    private OnItemEditListener listener;

    public CalculatorAdapter(Context context, ArrayList<Utility.IntegerPair> ids) {
        mContext = context;
        this.ids = ids;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_calculator, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        PRICE_LIST_COLUMNS[COL_PRICE_LIST_PRAW] = Utility.getRawPriceQueryString(mContext);

        Cursor cursor = mContext.getContentResolver().query(
                PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                mSelection,
                new String[]{"" + ids.get(position).getX()},
                null
        );

        if (cursor.moveToFirst()) {

            int defindex = cursor.getInt(COL_PRICE_LIST_DEFI);
            int quality = cursor.getInt(COL_PRICE_LIST_QUAL);
            int australium = cursor.getInt(COL_AUSTRALIUM);
            int tradable = cursor.getInt(COL_PRICE_LIST_TRAD);
            int craftable = cursor.getInt(COL_PRICE_LIST_CRAF);
            int priceIndex = cursor.getInt(COL_PRICE_LIST_INDE);

            double raw = cursor.getDouble(COL_PRICE_LIST_PRAW);
            double price = cursor.getDouble(COL_PRICE_LIST_PRIC);
            double high = cursor.getDouble(COL_PRICE_LIST_PMAX);

            String currency = cursor.getString(COL_PRICE_LIST_CURR);
            String name = cursor.getString(COL_PRICE_LIST_NAME);

            int count = ids.get(position).getY();

            try {
                if (australium == 1) {
                    holder.icon.setImageDrawable(Drawable.createFromStream(
                            mContext.getAssets().open("items/" + Utility
                                    .getIconIndex(defindex) + "aus.png"), null));
                } else {
                    holder.icon.setImageDrawable(Drawable.createFromStream(
                            mContext.getAssets().open("items/" + Utility
                                    .getIconIndex(defindex) + ".png"), null));
                }
            } catch (IOException e) {
                if (Utility.isDebugging(mContext)) {
                    e.printStackTrace();
                }
                holder.icon.setImageDrawable(null);
            }

            holder.name.setText(Utility.formatItemName(mContext, defindex, name, tradable, craftable,
                    quality, priceIndex));

            if (count > 1) {
                holder.name.append(" " + count + "x");
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            try {
                if (currency.equals(Currency.USD) && defindex != 5002) {
                    if (raw >= Utility.getDouble(prefs, mContext.getString(R.string.pref_buds_raw), 0)) {
                        holder.price.setText(Utility.formatPrice(mContext, price, high, currency,
                                Currency.BUD, false));
                    } else if (raw >= Utility.getDouble(prefs, mContext.getString(R.string.pref_key_raw), 0)) {
                        holder.price.setText(Utility.formatPrice(mContext, price, high, currency,
                                Currency.KEY, false));
                    } else {
                        holder.price.setText(Utility.formatPrice(mContext, price, high, currency,
                                Currency.METAL, false));
                    }
                } else {
                    holder.price.setText(Utility.formatPrice(mContext,
                            price,
                            high,
                            currency,
                            currency, false));
                }
            } catch (Throwable throwable) {
                if (Utility.isDebugging(mContext))
                    throwable.printStackTrace();
            }

            holder.effect.setBackgroundColor(Utility.getQualityColor(mContext, quality, defindex, true));

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemDeleted(ids.get(position).getX(), ids.get(position).getY());
                    }
                    ids.remove(position);
                    notifyDataSetChanged();
                }
            });
        }
        cursor.close();
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

    public void setOnItemDeletedListener(OnItemEditListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView icon;
        public final TextView name;
        public final TextView price;
        public final ImageView effect;
        public final ImageView delete;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            effect = (ImageView) itemView.findViewById(R.id.effect);
            name = (TextView) itemView.findViewById(R.id.text_view_item_name);
            price = (TextView) itemView.findViewById(R.id.text_view_item_price);
            delete = (ImageView) itemView.findViewById(R.id.image_view_delete);
        }
    }

    public interface OnItemEditListener {
        void onItemDeleted(int itemId, int count);
    }
}
