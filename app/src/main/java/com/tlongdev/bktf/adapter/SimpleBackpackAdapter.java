package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.tlongdev.bktf.UserBackpackActivity;

public class SimpleBackpackAdapter extends CursorAdapter {

    public SimpleBackpackAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int defindex = cursor.getInt(UserBackpackActivity.COL_BACKPACK_DEFI);
        int position = cursor.getInt(UserBackpackActivity.COL_BACKPACK_POS);
        int quality = cursor.getInt(UserBackpackActivity.COL_BACKPACK_QUAL);
        int craftNumber = cursor.getInt(UserBackpackActivity.COL_BACKPACK_CRFN);
        int tradable = cursor.getInt(UserBackpackActivity.COL_BACKPACK_TRAD);
        int craftable = cursor.getInt(UserBackpackActivity.COL_BACKPACK_CRAF);
        int itemIndex = cursor.getInt(UserBackpackActivity.COL_BACKPACK_INDE);
        int paint = cursor.getInt(UserBackpackActivity.COL_BACKPACK_PAIN);

        ViewHolder holder = (ViewHolder)view.getTag();
        holder.textView.setText("" + defindex + "/" + position +"/" + quality + "/" + craftNumber + "/" + tradable + "/" + craftable + "/" + itemIndex + "/" + paint);
    }

    public static class ViewHolder {
        TextView textView;

        public ViewHolder(View v) {
            textView = (TextView)v.findViewById(android.R.id.text1);
        }
    }
}
