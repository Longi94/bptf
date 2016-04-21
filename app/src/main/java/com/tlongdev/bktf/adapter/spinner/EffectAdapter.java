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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;

import java.util.List;

public class EffectAdapter extends ArrayAdapter<Item> {

    private final Context mContext;
    private final List<Item> mDataSet;

    public EffectAdapter(Context context, List<Item> items) {
        super(context, R.layout.effect_spinner_item);
        mContext = context;
        mDataSet = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = super.getView(position, convertView, parent);
        setView(rootView, position);
        return rootView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View rootView = super.getDropDownView(position, convertView, parent);
        setView(rootView, position);
        return rootView;
    }

    private void setView(View view, int position) {
        Item effect = mDataSet.get(position);
        TextView text = (TextView) view.findViewById(R.id.text1);
        text.setText(effect.getName());
        Glide.with(mContext)
                .load(effect.getEffectUrl())
                .into((ImageView) view.findViewById(R.id.effect));
    }

    public int getEffectId(int selectedItemPosition) {
        return mDataSet.get(selectedItemPosition).getPriceIndex();
    }
}