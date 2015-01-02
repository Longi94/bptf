package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.SearchFragment;

import java.io.IOException;
import java.io.InputStream;

public class SearchCursorAdapter extends CursorAdapter {

    public SearchCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_search, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.icon.setBackgroundDrawable(Utility.getItemBackground(context,
                cursor.getInt(SearchFragment.COL_PRICE_LIST_QUAL),
                cursor.getInt(SearchFragment.COL_PRICE_LIST_TRAD),
                cursor.getInt(SearchFragment.COL_PRICE_LIST_CRAF)));

        setIconImage(context, viewHolder.icon, cursor.getInt(SearchFragment.COL_PRICE_LIST_DEFI));

        viewHolder.nameView.setText(Utility.formatItemName(cursor.getString(SearchFragment.COL_PRICE_LIST_NAME),
                cursor.getInt(SearchFragment.COL_PRICE_LIST_TRAD),
                cursor.getInt(SearchFragment.COL_PRICE_LIST_CRAF),
                cursor.getInt(SearchFragment.COL_PRICE_LIST_QUAL),
                cursor.getInt(SearchFragment.COL_PRICE_LIST_INDE)));
        viewHolder.priceView.setText("" + cursor.getDouble(SearchFragment.COL_PRICE_LIST_PRIC));

        if (cursor.getDouble(SearchFragment.COL_PRICE_LIST_PMAX) > 0.0){
            viewHolder.priceView.append(" - " + cursor.getDouble(SearchFragment.COL_PRICE_LIST_PMAX));
        }

        viewHolder.priceView.append("\n" + cursor.getString(SearchFragment.COL_PRICE_LIST_CURR));
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    private void setIconImage(Context context, ImageView icon, int defindex) {
        try {
            // get input stream
            InputStream ims = context.getAssets().open("items/" + defindex + ".png");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            icon.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ViewHolder {

        public final ImageView icon;

        public final TextView nameView;
        public final TextView priceView;

        public ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            nameView = (TextView) view.findViewById(R.id.item_name);
            priceView = (TextView) view.findViewById(R.id.item_price);
        }
    }
}
