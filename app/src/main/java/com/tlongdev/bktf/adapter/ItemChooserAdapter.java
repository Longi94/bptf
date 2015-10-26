package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.activity.ItemChooserActivity;

/**
 * Adapter for the item chooser.
 */
public class ItemChooserAdapter extends CursorAdapter {

    /**
     * The index of the selected item.
     */
    private int selectedIndex = -1;

    /**
     * The ID of the selected item.
     */
    private int itemId = -1;

    //The stored typeface to be reused when restyling the text
    private Typeface normalTypeface;

    /**
     * Recommended constructor.
     *
     * @param cursor  The cursor from which to get the data.
     * @param context The context
     * @param flags   Flags used to determine the behavior of the adapter; may
     *                be any combination of {@link #FLAG_AUTO_REQUERY} and
     *                {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public ItemChooserAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_chooser, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //Save the typeface of the text
        normalTypeface = viewHolder.nameView.getTypeface();

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        int quality = cursor.getInt(ItemChooserActivity.COLUMN_QUALITY);
        int index = cursor.getInt(ItemChooserActivity.COLUMN_PRICE_INDEX);

        //Set the name of the item
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

        //If there is a selected item, mark it
        if (selectedIndex == cursor.getPosition()) {
            viewHolder.nameView.setTypeface(normalTypeface, Typeface.BOLD);
            viewHolder.nameView.setTextColor(Utility.getColor(context, R.color.accent));
        } else {
            viewHolder.nameView.setTypeface(normalTypeface, Typeface.NORMAL);
            viewHolder.nameView.setTextColor(Utility.getColor(context, R.color.text_secondary));
        }
    }

    /**
     * Selects and marks an item in the list
     *
     * @param index the index of the item to be selected
     */
    public void setSelectedIndex(int index) {
        selectedIndex = index;
        Cursor cursor = getCursor();
        if (cursor != null && cursor.moveToPosition(index)) {
            itemId = cursor.getInt(ItemChooserActivity.COLUMN_ID);
        }
    }

    /**
     * Returns the ID of the selected item
     *
     * @return the ID
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * The view holder
     */
    public static class ViewHolder {

        public final TextView nameView;

        /**
         * Constructor of the view holder
         *
         * @param view the root view
         */
        public ViewHolder(View view) {
            nameView = (TextView) view.findViewById(R.id.text_view_item_name);
        }
    }
}
