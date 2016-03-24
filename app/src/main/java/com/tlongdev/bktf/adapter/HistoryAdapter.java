/**
 * Copyright 2015 Long Tran
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

package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.Utils;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter for the bar code read history list.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_NORMAL = 1;

    @Inject Context mContext;

    private List<Price> mDataSet;
    private Item mItem;

    private LineData mData;

    public HistoryAdapter(BptfApplication application, List<Price> prices, Item item) {
        application.getAdapterComponent().inject(this);
        this.mDataSet = prices;
        this.mItem = item;

        Utils.init(mContext);

        if (prices != null && prices.size() > 1) {
            buildDataSet();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout;
        //Different layout for the header
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                layout = R.layout.list_history_header;
                break;
            default:
                layout = R.layout.list_history;
                break;
        }
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_NORMAL;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                buildChart(holder.historyChart);

                holder.iconCard.setCardBackgroundColor(mItem.getColor(mContext, true));

                Glide.with(mContext)
                        .load(mItem.getIconUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.icon);
                Glide.with(mContext)
                        .load(mItem.getEffectUrl())
                        .into(holder.effect);

                holder.name.setText(mItem.getFormattedName(mContext));

                if (!mItem.isTradable()) {
                    holder.quality.setVisibility(View.VISIBLE);
                    if (!mItem.isCraftable()) {
                        holder.quality.setImageResource(R.drawable.uncraft_untrad);
                    } else {
                        holder.quality.setImageResource(R.drawable.untrad);
                    }
                } else if (!mItem.isCraftable()) {
                    holder.quality.setVisibility(View.VISIBLE);
                    holder.quality.setImageResource(R.drawable.uncraft);
                }
                break;
            case VIEW_TYPE_NORMAL:
                Price price = mDataSet.get(position - 1);
                holder.price.setText(price.getFormattedPrice(mContext));
                holder.date.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.US)
                        .format(new Date(price.getLastUpdate())));

                if (position == 1) {
                    holder.separator.setVisibility(View.GONE);
                } else {
                    holder.separator.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mDataSet == null) return 1;
        return mDataSet.size() + 1;
    }

    private static Comparator<Price> priceAgeComparator = new Comparator<Price>() {
        @Override
        public int compare(Price lhs, Price rhs) {
            if (lhs.getLastUpdate() > rhs.getLastUpdate()) {
                return 1;
            } else if (lhs.getLastUpdate() < rhs.getLastUpdate()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    private void buildDataSet() {
        long first = Collections.min(mDataSet, priceAgeComparator).getLastUpdate();
        long last = Collections.max(mDataSet, priceAgeComparator).getLastUpdate();

        long days = TimeUnit.MILLISECONDS.toDays(last - first);

        int textColor = Utility.getColor(mContext, R.color.text_primary);

        //Setup the X axis of the chart
        ArrayList<String> xValues = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.US);

        for (long day = 0; day < days * 1.1; day++) {
            xValues.add(dateFormat.format(new Date(first + (day * 86400000L))));
        }

        if (mDataSet.size() > 0) {
            ArrayList<Entry> entries = new ArrayList<>();

            for (Price price : mDataSet) {
                int day = (int) TimeUnit.MILLISECONDS.toDays(price.getLastUpdate() - first);
                entries.add(new Entry((float) price.getConvertedAveragePrice(mContext, mItem.getPrice().getCurrency()), day));
            }

            LineDataSet set = new LineDataSet(entries, mItem.getPrice().getCurrency());

            set.setColor(textColor);
            set.setCircleColor(textColor);
            set.setHighLightColor(textColor);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setDrawValues(false);

            //Add data to the chart
            mData = new LineData(xValues, set);
        }
    }

    private void buildChart(LineChart chart) {

        if (mDataSet != null && mDataSet.size() > 1) {

            chart.setData(mData);
            chart.setLogEnabled(true);
            chart.setDescription(null);
            chart.setDrawGridBackground(false);
            chart.setTouchEnabled(true);
            chart.setDragEnabled(true);
            chart.setScaleYEnabled(false);
            chart.setScaleXEnabled(true);
            chart.setPinchZoom(true);
            chart.getAxisRight().setEnabled(false);
            chart.getLegend().setEnabled(false);

            int textColor = Utility.getColor(mContext, R.color.text_primary);

            XAxis xAxis = chart.getXAxis();
            xAxis.setAvoidFirstLastClipping(true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(textColor);

            YAxis yAxis = chart.getAxisLeft();
            yAxis.setTextColor(textColor);
            yAxis.setStartAtZero(false);
            yAxis.setValueFormatter(new YAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, YAxis yAxis) {
                    return String.format("%s %s", Utility.formatDouble(value), mItem.getPrice().getCurrency());
                }
            });

            ((LineDataSet) chart.getLineData().getDataSetByIndex(0)).setLineWidth(2.0f);
            ((LineDataSet) chart.getLineData().getDataSetByIndex(0)).setCircleRadius(2.2f);

            chart.notifyDataSetChanged();
            chart.animateY(1000, Easing.EasingOption.EaseOutCubic);
        } else {
            chart.setNoDataTextDescription("The price is new.");
        }
    }

    /**
     * The view holder of this adapter
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * The views of the element
         */
        @Nullable @Bind(R.id.effect) ImageView effect;
        @Nullable @Bind(R.id.icon) ImageView icon;
        @Nullable @Bind(R.id.quality) ImageView quality;
        @Nullable @Bind(R.id.name) TextView name;
        @Nullable @Bind(R.id.history_chart) LineChart historyChart;
        @Nullable @Bind(R.id.icon_card) CardView iconCard;

        @Nullable @Bind(R.id.price) TextView price;
        @Nullable @Bind(R.id.date) TextView date;
        @Nullable @Bind(R.id.separator) View separator;

        /**
         * Constructor
         *
         * @param view the element
         */
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}