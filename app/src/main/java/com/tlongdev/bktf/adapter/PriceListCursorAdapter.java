package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.enums.Quality;
import com.tlongdev.bktf.fragment.HomeFragment;

import java.io.IOException;
import java.io.InputStream;

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

        setItemBackground(viewHolder.itemFrame, cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL));

        setEffectImage(context, viewHolder.effect, cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE));
        setChangeImage(context, viewHolder.change, cursor.getDouble(HomeFragment.COL_PRICE_LIST_DIFF),
                cursor.getDouble(HomeFragment.COL_PRICE_LIST_PRAW));

        viewHolder.nameView.setText(Utility.formatItemName(cursor.getString(HomeFragment.COL_PRICE_LIST_NAME),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_TRAD),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_CRAF),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_QUAL),
                cursor.getInt(HomeFragment.COL_PRICE_LIST_INDE)));
        viewHolder.priceView.setText("" + cursor.getDouble(HomeFragment.COL_PRICE_LIST_PRIC));
        viewHolder.differenceView.setText("" + cursor.getDouble(HomeFragment.COL_PRICE_LIST_DIFF));
    }

    private void setChangeImage(Context context, ImageView change, double difference, double raw) {
        try {
            InputStream ims;
            if (difference == raw) {
                ims = context.getAssets().open("changes/new.png");
            }
            else if (difference == 0.0) {
                ims = context.getAssets().open("changes/refresh.png");
            }
            else if (difference > 0.0) {
                ims = context.getAssets().open("changes/up.png");
            }
            else {
                ims = context.getAssets().open("changes/down.png");
            }


            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            change.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void setEffectImage(Context context, ImageView effect, int index) {
        if (index != 0) {
            try {
                // get input stream
                InputStream ims = context.getAssets().open("effects/" + index + "_380x380.png");
                // load image as Drawable
                Drawable d = Drawable.createFromStream(ims, null);
                // set image to ImageView
                effect.setImageDrawable(d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            effect.setImageDrawable(null);
    }

    public static class ViewHolder {
        public final RelativeLayout itemFrame;

        public final ImageView effect;
        public final ImageView change;

        public final TextView nameView;
        public final TextView priceView;
        public final TextView differenceView;

        public ViewHolder(View view) {
            itemFrame = (RelativeLayout) view.findViewById(R.id.item_frame);
            effect = (ImageView) view.findViewById(R.id.image_view_effect);
            change = (ImageView) view.findViewById(R.id.image_view_change);
            nameView = (TextView) view.findViewById(R.id.item_name);
            priceView = (TextView) view.findViewById(R.id.item_price);
            differenceView = (TextView) view.findViewById(R.id.item_difference);
        }
    }
}
