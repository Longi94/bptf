package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.enums.Quality;
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

        setItemBackground(viewHolder.itemFrame, cursor.getInt(SearchFragment.COL_PRICE_LIST_QUAL));

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

    private void setItemBackground(View frame, int quality) {
        Quality q = Quality.values()[quality];

        switch (q) {
            case GENUINE:
                frame.setBackgroundColor(0xFF4D7455);
                break;
            case VINTAGE:
                frame.setBackgroundColor(0xFF476291);
                break;
            case UNUSUAL:
                frame.setBackgroundColor(0xFF8650AC);
                break;
            case UNIQUE:
                frame.setBackgroundColor(0xFFFFD700);
                break;
            case COMMUNITY:
                frame.setBackgroundColor(0xFF70B04A);
                break;
            case VALVE:
                frame.setBackgroundColor(0xFFA50F79);
                break;
            case SELF_MADE:
                frame.setBackgroundColor(0xFF70B04A);
                break;
            case STRANGE:
                frame.setBackgroundColor(0xFFCF6A32);
                break;
            case HAUNTED:
                frame.setBackgroundColor(0xFF38F3AB);
                break;
            case COLLECTORS:
                frame.setBackgroundColor(0xFFAA0000);
                break;
            default:
                frame.setBackgroundColor(0xFFB2B2B2);
                break;
        }
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
        public final FrameLayout itemFrame;

        public final ImageView icon;

        public final TextView nameView;
        public final TextView priceView;

        public ViewHolder(View view) {
            itemFrame = (FrameLayout) view.findViewById(R.id.item_frame);
            icon = (ImageView) view.findViewById(R.id.image_view_item_icon);
            nameView = (TextView) view.findViewById(R.id.item_name);
            priceView = (TextView) view.findViewById(R.id.item_price);
        }
    }
}
