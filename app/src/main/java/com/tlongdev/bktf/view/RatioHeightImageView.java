package com.tlongdev.bktf.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.tlongdev.bktf.R;

/**
 * An image view that allows to set that height of the view in ratio to its width.
 * Ratio is 1:1 by default.
 */
public class RatioHeightImageView extends ImageView {

    private double mRatioWidth = 1;
    private double mRatioHeight = 1;

    /**
     * Required constructor
     *
     * @param context context
     */
    public RatioHeightImageView(Context context) {
        super(context);
    }

    /**
     * Required constructor
     *
     * @param context context
     * @param attrs   attributes
     */
    public RatioHeightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Required constructor
     *
     * @param context      context
     * @param attrs        attributes
     * @param defStyleAttr default style attributes
     */
    public RatioHeightImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Ratio,
                0, 0);

        try {
            mRatioWidth = a.getFloat(R.styleable.Ratio_ratioWidth, 1f);
            mRatioHeight = a.getFloat(R.styleable.Ratio_ratioHeight, 1f);
        } finally {
            a.recycle();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth() * mRatioHeight / mRatioWidth));
    }
}
