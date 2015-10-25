package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.tlongdev.bktf.activity.ItemChooserActivity;
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
        int quality = cursor.getInt(ItemChooserActivity.COLUMN_QUALITY);
        int index = cursor.getInt(ItemChooserActivity.COLUMN_PRICE_INDEX);
        if (quality == 5 && index != 0) {
            viewHolder.nameView.setText(cursor.getString(ItemChooserActivity.COLUMN_NAME));
        } else {
            viewHolder.nameView.setText(Utility.formatItemName(context,
                    cursor.getInt(ItemChooserActivity.COLUMN_DEFINDEX),
                    cursor.getString(ItemChooserActivity.COLUMN_NAME),
                    cursor.getInt(ItemChooserActivity.COLUMN_TRADABLE),
                    cursor.getInt(ItemChooserActivity.COLUMN_CRAFTABLE),
                    quality, index));
        }

        if (selectedIndex == cursor.getPosition()) {
            viewHolder.nameView.setTypeface(normalTypeface, Typeface.BOLD);
            viewHolder.nameView.setTextColor(context.getResources().getColor(R.color.accent));
        } else {
            viewHolder.nameView.setTypeface(normalTypeface, Typeface.NORMAL);
            viewHolder.nameView.setTextColor(mainColor);
        }
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
        Cursor cursor = getCursor();
        if (cursor != null && cursor.moveToPosition(index)) {
            itemId = cursor.getInt(ItemChooserActivity.COLUMN_ID);
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
