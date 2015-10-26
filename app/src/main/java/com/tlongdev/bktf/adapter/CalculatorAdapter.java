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

/**
 * Adapter for the recycler view of the calculator.
 */
public class CalculatorAdapter extends RecyclerView.Adapter<CalculatorAdapter.ViewHolder> {

    /**
     * Pairs of prices table IDs and count numbers.
     */
    private ArrayList<Utility.IntegerPair> ids;

    /**
     * The context
     */
    private Context mContext;

    /**
     * The columns to load
     * TODO there is really no need to query for every single item...
     */
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

    /**
     * Indexes for the columns above
     */
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

    /**
     * The selection
     */
    public static final String mSelection = PriceEntry.TABLE_NAME +
            "." + PriceEntry._ID + " = ?";

    /**
     * The listener that will be notified when an items is edited.
     */
    private OnItemEditListener listener;

    /**
     * Constructor.
     *
     * @param context the context
     * @param ids     the ids and counts if the items
     */
    public CalculatorAdapter(Context context, ArrayList<Utility.IntegerPair> ids) {
        mContext = context;
        this.ids = ids;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_calculator, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        PRICE_LIST_COLUMNS[COL_PRICE_LIST_PRAW] = Utility.getRawPriceQueryString(mContext);

        //Query data of the item
        Cursor cursor = mContext.getContentResolver().query(
                PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                mSelection,
                new String[]{"" + ids.get(position).getX()},
                null
        );

        if (cursor != null) {

            if (cursor.moveToFirst()) {

                //Get the data from the cursor
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
                    //Get the icon of the item
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

                holder.name.setText(Utility.formatItemName(mContext, defindex, name, tradable,
                        craftable, quality, priceIndex));

                //Put the number of items behind the name if it is higher than 1
                if (count > 1) {
                    holder.name.append(String.format(" %dx", count));
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                try {
                    //If the currency is in USD convert it to keys or refs
                    if (currency.equals(Currency.USD) && defindex != 5002) {
                        if (raw >= Utility.getDouble(prefs, mContext.getString(R.string.pref_key_raw), 0)) {
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
                            //Notify listener that an item was deleted
                            listener.onItemDeleted(ids.get(position).getX(), ids.get(position).getY());
                        }
                        ids.remove(position);
                        notifyDataSetChanged();
                    }
                });
            }
            cursor.close();
        }
    }

    @Override
    public int getItemCount() {
        return ids == null ? 0 : ids.size();
    }

    /**
     * Set the listener that will be notified when an items is edited/deleted
     *
     * @param listener the listener to be set
     */
    public void setOnItemDeletedListener(OnItemEditListener listener) {
        this.listener = listener;
    }

    /**
     * The view holder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView icon;
        public final TextView name;
        public final TextView price;
        public final ImageView effect;
        public final ImageView delete;

        /**
         * Constructor
         *
         * @param view the root view
         */
        public ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.icon);
            effect = (ImageView) view.findViewById(R.id.effect);
            name = (TextView) view.findViewById(R.id.text_view_item_name);
            price = (TextView) view.findViewById(R.id.text_view_item_price);
            delete = (ImageView) view.findViewById(R.id.image_view_delete);
        }
    }

    /**
     * Interface that will be notified when an item is edited/deleted.
     */
    public interface OnItemEditListener {
        /**
         * Called when an item is deleted.
         *
         * @param itemId the id of the item
         * @param count  the number of the item(s)
         */
        void onItemDeleted(int itemId, int count);
    }
}
