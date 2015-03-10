package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.data.PriceListContract;
import com.tlongdev.bktf.enums.Quality;

import java.io.IOException;
import java.util.ArrayList;

public class AdvancedCalculatorAdapter extends RecyclerView.Adapter<AdvancedCalculatorAdapter.ViewHolder>{

    private ArrayList<Utility.IntegerPair> ids;
    private Context mContext;

    private static final String[] PRICE_LIST_COLUMNS = {
            PriceListContract.PriceEntry.TABLE_NAME + "." + PriceListContract.PriceEntry._ID,
            PriceListContract.PriceEntry.COLUMN_DEFINDEX,
            PriceListContract.PriceEntry.COLUMN_ITEM_NAME,
            PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY,
            PriceListContract.PriceEntry.COLUMN_ITEM_TRADABLE,
            PriceListContract.PriceEntry.COLUMN_ITEM_CRAFTABLE,
            PriceListContract.PriceEntry.COLUMN_PRICE_INDEX,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_MAX,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_RAW,
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

    public static final String mSelection = PriceListContract.PriceEntry.TABLE_NAME+
                    "." + PriceListContract.PriceEntry._ID + " = ?";

    private OnItemEditListener listener;

    public AdvancedCalculatorAdapter(Context context, ArrayList<Utility.IntegerPair> ids) {
        mContext = context;
        this.ids = ids;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_calculator, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Cursor cursor = mContext.getContentResolver().query(
                PriceListContract.PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                mSelection,
                new String[]{"" + ids.get(position).getX()},
                null
        );

        if (cursor.moveToFirst()){
            try {
                holder.icon.setImageDrawable(Drawable.createFromStream(
                        mContext.getAssets().open("items/" + Utility.getIconIndex(cursor.getInt(COL_PRICE_LIST_DEFI))
                                + ".png"), null));
            } catch (IOException e) {
                if (Utility.isDebugging(mContext)) {
                    e.printStackTrace();
                }
            }

            holder.name.setText(Utility.formatItemName(cursor.getString(COL_PRICE_LIST_NAME),
                    cursor.getInt(COL_PRICE_LIST_TRAD),
                    cursor.getInt(COL_PRICE_LIST_CRAF),
                    cursor.getInt(COL_PRICE_LIST_QUAL),
                    cursor.getInt(COL_PRICE_LIST_INDE)));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            try {
                String currency = cursor.getString(COL_PRICE_LIST_CURR);
                double priceRaw = cursor.getDouble(COL_PRICE_LIST_PRAW);
                if (currency.equals(Utility.CURRENCY_USD) && cursor.getInt(COL_PRICE_LIST_DEFI) != 5002) {
                    if (priceRaw >= Utility.getDouble(prefs, mContext.getString(R.string.pref_buds_raw), 0)) {
                        holder.price.setText(Utility.formatPrice(mContext,
                                cursor.getDouble(COL_PRICE_LIST_PRIC),
                                cursor.getDouble(COL_PRICE_LIST_PMAX),
                                currency,
                                Utility.CURRENCY_BUD, false));
                    } else if (priceRaw >= Utility.getDouble(prefs, mContext.getString(R.string.pref_key_raw), 0)){
                        holder.price.setText(Utility.formatPrice(mContext,
                                cursor.getDouble(COL_PRICE_LIST_PRIC),
                                cursor.getDouble(COL_PRICE_LIST_PMAX),
                                currency,
                                Utility.CURRENCY_KEY, false));
                    } else {
                        holder.price.setText(Utility.formatPrice(mContext,
                                cursor.getDouble(COL_PRICE_LIST_PRIC),
                                cursor.getDouble(COL_PRICE_LIST_PMAX),
                                currency,
                                Utility.CURRENCY_METAL, false));
                    }
                } else {
                    holder.price.setText(Utility.formatPrice(mContext,
                            cursor.getDouble(COL_PRICE_LIST_PRIC),
                            cursor.getDouble(COL_PRICE_LIST_PMAX),
                            currency,
                            currency, false));
                }
            } catch (Throwable throwable) {
                Toast.makeText(mContext, "bptf: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                if (Utility.isDebugging(mContext))
                    throwable.printStackTrace();
            }

            holder.count.setText(String.valueOf(ids.get(position).getY()));
            setNameColor(holder.name, cursor.getInt(COL_PRICE_LIST_QUAL));

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onItemDeleted(ids.get(position).getX(), ids.get(position).getY());
                    }
                    ids.remove(position);
                    notifyDataSetChanged();
                }
            });
        }
        cursor.close();
    }

    private void setNameColor(TextView name, int quality) {
        Quality q = Quality.values()[quality];
        int color;
        switch (q) {
            case NORMAL:
                color = mContext.getResources().getColor(R.color.tf2_normal_color_dark);
                break;
            case GENUINE:
                color = mContext.getResources().getColor(R.color.tf2_genuine_color_dark);
                break;
            case VINTAGE:
                color = mContext.getResources().getColor(R.color.tf2_vintage_color_dark);
                break;
            case UNUSUAL:
                color = mContext.getResources().getColor(R.color.tf2_unusual_color_dark);
                break;
            case UNIQUE:
                color = mContext.getResources().getColor(R.color.tf2_unique_color_dark);
                break;
            case COMMUNITY:
                color = mContext.getResources().getColor(R.color.tf2_community_color_dark);
                break;
            case VALVE:
                color = mContext.getResources().getColor(R.color.tf2_valve_color_dark);
                break;
            case SELF_MADE:
                color = mContext.getResources().getColor(R.color.tf2_community_color_dark);
                break;
            case STRANGE:
                color = mContext.getResources().getColor(R.color.tf2_strange_color_dark);
                break;
            case HAUNTED:
                color = mContext.getResources().getColor(R.color.tf2_haunted_color_dark);
                break;
            case COLLECTORS:
                color = mContext.getResources().getColor(R.color.tf2_collectors_color_dark);
                break;
            default:
                color = Color.BLACK;
                break;
        }
        name.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

    public void setOnItemDeletedListener(OnItemEditListener listener){
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView icon;
        TextView name;
        TextView price;
        TextView count;
        ImageView delete;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView)itemView.findViewById(R.id.image_view_item_icon);
            name = (TextView)itemView.findViewById(R.id.text_view_item_name);
            price = (TextView)itemView.findViewById(R.id.text_view_item_price);
            count = (TextView)itemView.findViewById(R.id.text_view_item_count);
            delete = (ImageView)itemView.findViewById(R.id.image_view_delete);
        }
    }

    public interface OnItemEditListener {
        public void onItemDeleted(int itemId, int count);
    }
}
