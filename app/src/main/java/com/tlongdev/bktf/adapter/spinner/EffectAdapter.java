package com.tlongdev.bktf.adapter.spinner;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;

import java.util.List;

public class EffectAdapter extends BaseAdapter {

    private final Context mContext;
    private List<Item> mDataSet;

    public EffectAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSet != null ? mDataSet.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater lInflater = (LayoutInflater)mContext.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            convertView = lInflater.inflate(R.layout.effect_spinner_item, parent, false);
        }

        setView(convertView, position);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater lInflater = (LayoutInflater)mContext.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            convertView = lInflater.inflate(R.layout.effect_spinner_item, parent, false);
        }
        setView(convertView, position);
        return convertView;
    }

    private void setView(View view, int position) {
        if (view == null) {
            return;
        }

        Item effect = mDataSet.get(position);
        TextView text = view.findViewById(R.id.text1);
        text.setText(effect.getName());
        Glide.with(mContext)
                .load(effect.getEffectUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into((ImageView) view.findViewById(R.id.effect));
    }

    public int getEffectId(int selectedItemPosition) {
        return mDataSet.get(selectedItemPosition).getPriceIndex();
    }

    public void setDataSet(List<Item> dataSet) {
        mDataSet = dataSet;
    }
}