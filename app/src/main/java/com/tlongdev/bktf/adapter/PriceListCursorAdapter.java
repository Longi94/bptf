package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.fragment.HomeFragment;

/**
 * Created by ThanhLong on 2014.12.25..
 */
public class PriceListCursorAdapter extends CursorAdapter {

    public PriceListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_changes, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        if (cursor.getDouble(HomeFragment.COL_PRICE_LIST_DIFF) > 0)
            view.setBackgroundColor(Color.GREEN);
        else if (cursor.getDouble(HomeFragment.COL_PRICE_LIST_DIFF) < 0)
            view.setBackgroundColor(Color.RED);
        else
            view.setBackgroundColor(Color.GRAY);

        viewHolder.nameView.setText(cursor.getString(HomeFragment.COL_PRICE_LIST_NAME));
        viewHolder.tradableView.setText("" + cursor.getInt(HomeFragment.COL_PRICE_LIST_TRAD));
        viewHolder.craftableView.setText("" + cursor.getInt(HomeFragment.COL_PRICE_LIST_CRAF));
        viewHolder.qualityView.setText("" + cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL));
        viewHolder.indexView.setText("" + cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE));
        viewHolder.currencyView.setText(cursor.getString(HomeFragment.COL_PRICE_LIST_CURR));
        viewHolder.priceView.setText("" + cursor.getDouble(HomeFragment.COL_PRICE_LIST_PRIC));
        viewHolder.priceMaxView.setText("" + cursor.getDouble(HomeFragment.COL_PRICE_LIST_PMAX));
        viewHolder.lastUpdateView.setText("" + cursor.getInt(HomeFragment.COL_PRICE_LIST_UPDA));
        viewHolder.differenceView.setText("" + cursor.getDouble(HomeFragment.COL_PRICE_LIST_DIFF));
    }

    public static class ViewHolder {
        public final TextView nameView;
        public final TextView tradableView;
        public final TextView craftableView;
        public final TextView qualityView;
        public final TextView indexView;
        public final TextView currencyView;
        public final TextView priceView;
        public final TextView priceMaxView;
        public final TextView lastUpdateView;
        public final TextView differenceView;

        public ViewHolder(View view) {
            nameView = (TextView) view.findViewById(R.id.item_name);
            tradableView = (TextView) view.findViewById(R.id.item_tradable);
            craftableView = (TextView) view.findViewById(R.id.item_craftable);
            qualityView = (TextView) view.findViewById(R.id.item_quality);
            indexView = (TextView) view.findViewById(R.id.item_price_index);
            currencyView = (TextView) view.findViewById(R.id.item_currency);
            priceView = (TextView) view.findViewById(R.id.item_price);
            priceMaxView = (TextView) view.findViewById(R.id.item_price_max);
            lastUpdateView = (TextView) view.findViewById(R.id.item_last_update);
            differenceView = (TextView) view.findViewById(R.id.item_difference);
        }
    }
}
