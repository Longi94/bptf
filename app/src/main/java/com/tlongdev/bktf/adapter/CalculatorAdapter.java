package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

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
     * The context
     */
    private Context mContext;

    /**
     * The columns to load
     * TODO there is really no need to query for every single item...
     */
    private String sql;

    /**
     * Indexes for the columns above
     */
    public static final int COLUMN_DEFINDEX = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_QUALITY = 2;
    public static final int COLUMN_TRADABLE = 3;
    public static final int COLUMN_CRAFTABLE = 4;
    public static final int COLUMN_PRICE_INDEX = 5;
    public static final int COLUMN_CURRENCY = 6;
    public static final int COLUMN_PRICE = 7;
    public static final int COLUMN_PRICE_MAX = 8;
    public static final int COLUMN_PRICE_RAW = 9;
    public static final int COLUMN_AUSTRALIUM = 10;

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

        sql = "SELECT " +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                DatabaseContract.ItemSchemaEntry.TABLE_NAME + "." + DatabaseContract.ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                Utility.getRawPriceQueryString(context) + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                " FROM " + PriceEntry.TABLE_NAME +
                " LEFT JOIN " + DatabaseContract.ItemSchemaEntry.TABLE_NAME +
                " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + DatabaseContract.ItemSchemaEntry.TABLE_NAME + "." + DatabaseContract.ItemSchemaEntry.COLUMN_DEFINDEX +
                " WHERE " + mSelection;
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

        //Query data of the item
        Cursor cursor = mContext.getContentResolver().query(
                DatabaseContract.RAW_QUERY_URI,
                null,
                sql,
                new String[]{"" + ids.get(position).getX()},
                null
        );

        if (cursor != null) {

            if (cursor.moveToFirst()) {

                //Get the data from the cursor
                Item item = new Item(
                        cursor.getInt(COLUMN_DEFINDEX),
                        cursor.getString(COLUMN_NAME),
                        cursor.getInt(COLUMN_QUALITY),
                        cursor.getInt(COLUMN_TRADABLE) == 1,
                        cursor.getInt(COLUMN_CRAFTABLE) == 1,
                        cursor.getInt(COLUMN_AUSTRALIUM) == 1,
                        cursor.getInt(COLUMN_PRICE_INDEX),
                        new Price(
                                cursor.getDouble(COLUMN_PRICE),
                                cursor.getDouble(COLUMN_PRICE_MAX),
                                cursor.getDouble(COLUMN_PRICE_RAW),
                                0, 0,
                                cursor.getString(COLUMN_CURRENCY)
                        )
                );

                int count = ids.get(position).getY();

                Glide.with(mContext)
                        .load(item.getIconUrl(mContext))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.icon);

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

        @Bind(R.id.icon) ImageView icon;
        @Bind(R.id.text_view_item_name) TextView name;
        @Bind(R.id.text_view_item_price) TextView price;
        @Bind(R.id.effect) ImageView effect;
        @Bind(R.id.image_view_delete) ImageView delete;

        /**
         * Constructor
         *
         * @param view the root view
         */
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
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
