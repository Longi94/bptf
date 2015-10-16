package com.tlongdev.bktf.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tlongdev.bktf.R;

public class CustomFrameLayout extends FrameLayout {
    private double mRatioWidth = 1;
    private double mRatioHeight = 1;

    /**
     * Required constructor
     *
     * @param context context
     */
    public CustomFrameLayout(Context context) {
        super(context);
    }

    /**
     * Required constructor
     *
     * @param context context
     * @param attrs   attributes
     */
    public CustomFrameLayout(Context context, AttributeSet attrs) {
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
    public CustomFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomLayout,
                0, 0);

        try {
            mRatioWidth = a.getFloat(R.styleable.CustomLayout_ratioWidth, 1f);
            mRatioHeight = a.getFloat(R.styleable.CustomLayout_ratioHeight, 1f);
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
