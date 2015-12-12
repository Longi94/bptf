package com.tlongdev.bktf.adapter.spinner;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;

public class QualityAdapter extends ArrayAdapter<String> {

    public static final String[] QUALITIES = {
            "Collector's", "Decorated Weapon", "Genuine", "Haunted", "Normal", "Self-Made",
            "Strange", "Unique", "Unusual", "Vintage"
    };
    public static final int[] QUALITY_IDS = {
            Quality.COLLECTORS, Quality.PAINTKITWEAPON, Quality.GENUINE, Quality.HAUNTED, Quality.NORMAL,
            Quality.SELF_MADE, Quality.STRANGE, Quality.UNIQUE, Quality.UNUSUAL, Quality.VINTAGE
    };

    private Item quality;

    private Context mContext;

    public QualityAdapter(Context context) {
        super(context, R.layout.quality_spinner_item, QUALITIES);
        quality = new Item();
        quality.setDefindex(15059);
        mContext = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View rootView = super.getView(position, convertView, parent);
        setView(rootView, position);
        return rootView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = super.getDropDownView(position, convertView, parent);
        setView(rootView, position);
        return rootView;
    }

    private void setView(View view, int position) {
        TextView text = (TextView) view.findViewById(R.id.text1);
        text.setText(getItem(position));

        quality.setQuality(QUALITY_IDS[position]);

        text.getCompoundDrawables()[0].setColorFilter(quality.getColor(mContext, false), PorterDuff.Mode.MULTIPLY);
    }
}