/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.adapter.spinner;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;

public class QualityAdapter extends BaseAdapter {

    private static final String[] QUALITIES = {
            "Collector's", "Decorated Weapon", "Genuine", "Haunted", "Normal", "Self-Made",
            "Strange", "Unique", "Unusual", "Vintage"
    };
    public static final Integer[] QUALITY_IDS = {
            Quality.COLLECTORS, Quality.PAINTKITWEAPON, Quality.GENUINE, Quality.HAUNTED, Quality.NORMAL,
            Quality.SELF_MADE, Quality.STRANGE, Quality.UNIQUE, Quality.UNUSUAL, Quality.VINTAGE
    };

    private final Item quality;

    private final Context mContext;

    public QualityAdapter(Context context) {
        quality = new Item();
        quality.setDefindex(15059);
        mContext = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater lInflater = (LayoutInflater) mContext.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            convertView = lInflater.inflate(R.layout.quality_spinner_item, null);
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

            convertView = lInflater.inflate(R.layout.quality_spinner_item, null);
        }
        setView(convertView, position);
        return convertView;
    }

    private void setView(View view, int position) {
        TextView text = (TextView) view.findViewById(R.id.text1);
        text.setText(QUALITIES[position]);

        quality.setQuality(QUALITY_IDS[position]);

        int color = quality.getColor(mContext, false);
        text.getCompoundDrawables()[0].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public int getQualityId(int selectedItemPosition) {
        return QUALITY_IDS[selectedItemPosition];
    }
}