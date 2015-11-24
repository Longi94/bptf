package com.tlongdev.bktf.adapter;

import android.content.Context;
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
import java.util.concurrent.TimeUnit;

/**
 * Adapter for the bar code read history list.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    /**
     * View types
     */
    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_NORMAL = 1;

    private Context mContext;

    /**
     * The data set
     */
    private List<Price> mDataSet;
    private Item mItem;

    private LineData mData;

    /**
     * Constructor
     *
     * @param context context
     * @param prices  the data set
     * @param item    the item
     */
    public HistoryAdapter(Context context, List<Price> prices, Item item) {
        this.mDataSet = prices;
        this.mContext = context;
        this.mItem = item;

        Utils.init(context);

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
        return new ViewHolder(v, viewType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                buildChart(holder.historyChart);

                holder.iconCard.setCardBackgroundColor(mItem.getColor(mContext, true));

                Glide.with(mContext)
                        .load(mItem.getIconUrl(mContext))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.icon);
                Glide.with(mContext)
                        .load(mItem.getEffectUrl(mContext))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.effect);

                holder.name.setText(mItem.getFormattedName(mContext));
                break;
            case VIEW_TYPE_NORMAL:
                Price price = mDataSet.get(position - 1);
                holder.price.setText(price.getFormattedPrice(mContext));
                holder.date.setText(new SimpleDateFormat("dd-MM-yyyy").format(new Date(price.getLastUpdate())));

                if (position == 1) {
                    holder.separator.setVisibility(View.GONE);
                } else {
                    holder.separator.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

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

            chart.getLineData().getDataSetByIndex(0).setLineWidth(2.0f);
            chart.getLineData().getDataSetByIndex(0).setCircleSize(2.2f);

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
        ImageView effect;
        ImageView icon;
        TextView name;
        LineChart historyChart;
        CardView iconCard;

        TextView price;
        TextView date;
        View separator;

        /**
         * Constructor
         *
         * @param view the element
         */
        public ViewHolder(View view, int type) {
            super(view);
            switch (type) {
                case HistoryAdapter.VIEW_TYPE_HEADER:
                    effect = (ImageView) view.findViewById(R.id.effect);
                    icon = (ImageView) view.findViewById(R.id.icon);
                    name = (TextView) view.findViewById(R.id.name);
                    historyChart = (LineChart) view.findViewById(R.id.history_chart);
                    iconCard = (CardView) view.findViewById(R.id.icon_card);
                    break;
                default:
                    separator = view.findViewById(R.id.separator);
                    price = (TextView) view.findViewById(R.id.price);
                    date = (TextView) view.findViewById(R.id.date);
                    break;
            }
        }
    }
}