package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.model.Tf2Item;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Adapter for the recycler view of the calculator.
 */
public class CalculatorAdapter extends RecyclerView.Adapter<CalculatorAdapter.ViewHolder> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = CalculatorAdapter.class.getSimpleName();

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
                Tf2Item item = new Tf2Item(
                        cursor.getInt(COL_PRICE_LIST_DEFI),
                        cursor.getString(COL_PRICE_LIST_NAME),
                        cursor.getInt(COL_PRICE_LIST_QUAL),
                        cursor.getInt(COL_PRICE_LIST_TRAD) == 1,
                        cursor.getInt(COL_PRICE_LIST_CRAF) == 1,
                        cursor.getInt(COL_AUSTRALIUM) == 1,
                        cursor.getInt(COL_PRICE_LIST_INDE),
                        new Price(
                                cursor.getDouble(COL_PRICE_LIST_PRIC),
                                cursor.getDouble(COL_PRICE_LIST_PMAX),
                                cursor.getDouble(COL_PRICE_LIST_PRAW),
                                0, 0,
                                cursor.getString(COL_PRICE_LIST_CURR)
                        )
                );

                int count = ids.get(position).getY();

                try {
                    //Get the icon of the item
                    holder.icon.setImageDrawable(item.getIconDrawable(mContext));
                } catch (IOException e) {
                    if (Utility.isDebugging(mContext)) {
                        e.printStackTrace();
                    }
                    holder.icon.setImageDrawable(null);
                }

                holder.name.setText(item.getFormattedName(mContext));

                //Put the number of items behind the name if it is higher than 1
                if (count > 1) {
                    holder.name.append(String.format(" %dx", count));
                }

                holder.price.setText(item.getPrice().getFormattedPrice(mContext));

                holder.effect.setBackgroundColor(item.getColor(mContext, true));

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
