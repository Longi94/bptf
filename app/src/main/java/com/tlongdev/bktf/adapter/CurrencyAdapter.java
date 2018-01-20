package com.tlongdev.bktf.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author lngtr
 * @since 2016. 05. 09.
 */
public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.ViewHolder> {

    private Map<String, Double> mDataSet;
    private List<String> mKeys = new ArrayList<>();
    private double mBase;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_currency, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataSet != null) {
            String currency = mKeys.get(position);
            double multiplier = mDataSet.get(currency);

            holder.text.setText(String.format("%s %s", Utility.formatDouble(mBase * multiplier), currency));
        }
    }

    @Override
    public int getItemCount() {
        return mKeys == null ? 0 : mKeys.size();
    }

    public void setDataSet(Map<String, Double> dataSet, double base) {
        mDataSet = dataSet;
        mBase = base;
        mKeys.clear();
        mKeys.addAll(dataSet.keySet());
        Collections.sort(mKeys);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View root;

        @BindView(R.id.text) TextView text;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            root = view;
        }
    }
}