package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.tlongdev.bktf.ItemChooserActivity;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;

public class ItemChooserAdapter extends CursorAdapter {

    private int selectedIndex = -1;
    private int itemId = -1;

    private Typeface normalTypeface;
    private int mainColor;

    public ItemChooserAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_chooser, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        normalTypeface = viewHolder.nameView.getTypeface();
        mainColor = viewHolder.nameView.getCurrentTextColor();

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        int quality = cursor.getInt(ItemChooserActivity.COL_PRICE_LIST_QUAL);
        int index = cursor.getInt(ItemChooserActivity.COL_PRICE_LIST_INDE);
        if (quality == 5 && index != 0) {
            viewHolder.nameView.setText(cursor.getString(ItemChooserActivity.COL_PRICE_LIST_NAME));
        } else {
            viewHolder.nameView.setText(Utility.formatItemName(context,
                    cursor.getInt(ItemChooserActivity.COL_PRICE_LIST_DEFINDEX),
                    cursor.getString(ItemChooserActivity.COL_PRICE_LIST_NAME),
                    cursor.getInt(ItemChooserActivity.COL_PRICE_LIST_TRAD),
                    cursor.getInt(ItemChooserActivity.COL_PRICE_LIST_CRAF),
                    quality, index));
        }

        if (selectedIndex == cursor.getPosition()) {
            viewHolder.nameView.setTypeface(normalTypeface, Typeface.BOLD);
            viewHolder.nameView.setTextColor(context.getResources().getColor(R.color.bptf_main_blue_dark));
        } else {
            viewHolder.nameView.setTypeface(normalTypeface, Typeface.NORMAL);
            viewHolder.nameView.setTextColor(mainColor);
        }
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
        Cursor cursor = getCursor();
        if (cursor != null && cursor.moveToPosition(index)) {
            itemId = cursor.getInt(ItemChooserActivity.COL_PRICE_LIST_ID);
        }
    }

    public int getItemId() {
        return itemId;
    }

    public static class ViewHolder {

        public final TextView nameView;

        public ViewHolder(View view) {
            nameView = (TextView) view.findViewById(R.id.text_view_item_name);
        }
    }
}
