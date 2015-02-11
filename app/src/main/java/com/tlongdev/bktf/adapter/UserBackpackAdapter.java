package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.UserBackpackActivity;
import com.tlongdev.bktf.Utility;

import java.io.IOException;
import java.io.InputStream;

public class UserBackpackAdapter extends CursorAdapter {

    public UserBackpackAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_backpack, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int defindex = cursor.getInt(UserBackpackActivity.COL_BACKPACK_DEFI);
        int quality = cursor.getInt(UserBackpackActivity.COL_BACKPACK_QUAL);
        int tradable = Math.abs(cursor.getInt(UserBackpackActivity.COL_BACKPACK_TRAD) - 1);
        int craftable = Math.abs(cursor.getInt(UserBackpackActivity.COL_BACKPACK_CRAF) - 1);
        int itemIndex = cursor.getInt(UserBackpackActivity.COL_BACKPACK_INDE);
        int paint = cursor.getInt(UserBackpackActivity.COL_BACKPACK_PAIN);
        int australium = cursor.getInt(UserBackpackActivity.COL_BACKPACK_AUS);

        ViewHolder holder = (ViewHolder)view.getTag();

        if (defindex == 0){
            holder.icon.setImageDrawable(null);
            holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.item_background_blank));
        } else {
            setIconImage(context, holder.icon, defindex, australium == 1);
            holder.background.setBackgroundDrawable(Utility.getItemBackground(context,
                    quality, tradable, craftable));
        }
    }

    private void setIconImage(Context context, ImageView icon, int defindex, boolean isAustralium) {
        try {
            InputStream ims;
            AssetManager assetManager = context.getAssets();
            if (isAustralium) {
                ims = assetManager.open("items/" + defindex + "aus.png");
            } else {
                ims = assetManager.open("items/" + defindex + ".png");
            }

            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            icon.setImageDrawable(d);
        } catch (IOException e) {
            Toast.makeText(context, "bptf: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (Utility.isDebugging(context))
                e.printStackTrace();
        }
    }

    public static class ViewHolder {
        ImageView background;
        ImageView icon;

        public ViewHolder(View v) {
            background = (ImageView)v.findViewById(R.id.image_view_item_background);
            icon = (ImageView)v.findViewById(R.id.image_view_item_icon);
        }
    }
}
