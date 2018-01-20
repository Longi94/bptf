package com.tlongdev.bktf.adapter.spinner;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.dao.DecoratedWeaponDao;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;

import javax.inject.Inject;

public class QualityAdapter extends BaseAdapter {

    private static final String[] QUALITIES = {
            "Collector's", "Decorated Weapon", "Genuine", "Haunted", "Normal", "Self-Made",
            "Strange", "Unique", "Unusual", "Vintage"
    };
    public static final Integer[] QUALITY_IDS = {
            Quality.COLLECTORS, Quality.PAINTKITWEAPON, Quality.GENUINE, Quality.HAUNTED, Quality.NORMAL,
            Quality.SELF_MADE, Quality.STRANGE, Quality.UNIQUE, Quality.UNUSUAL, Quality.VINTAGE
    };

    @Inject
    DecoratedWeaponDao mDecoratedWeaponDao;

    private final Item quality;

    private final Context mContext;

    public QualityAdapter(BptfApplication application) {
        application.getAdapterComponent().inject(this);
        quality = new Item();
        quality.setDefindex(15059);
        mContext = application;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater lInflater = (LayoutInflater) mContext.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            convertView = lInflater.inflate(R.layout.quality_spinner_item, parent, false);
        }
        setView(convertView, position);
        return convertView;
    }

    @Override
    public int getCount() {
        return QUALITY_IDS.length;
    }

    @Override
    public Object getItem(int position) {
        return QUALITY_IDS[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater lInflater = (LayoutInflater) mContext.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            convertView = lInflater.inflate(R.layout.quality_spinner_item, parent, false);
        }
        setView(convertView, position);
        return convertView;
    }

    private void setView(View view, int position) {
        TextView text = view.findViewById(R.id.text1);
        text.setText(QUALITIES[position]);

        quality.setQuality(QUALITY_IDS[position]);

        int color = quality.getColor(mContext, mDecoratedWeaponDao, false);
        ImageView image = view.findViewById(R.id.quality);
        image.getDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public int getQualityId(int selectedItemPosition) {
        return QUALITY_IDS[selectedItemPosition];
    }
}