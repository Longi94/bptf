package com.tlongdev.bktf.view;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;

/**
 * Putting a chart view into a view that will resize itself after inflating won't make the chart
 * fill the parent view.
 */
public class SquareLineChart extends LineChart {

    public SquareLineChart(Context context) {
        super(context);
    }

    public SquareLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
