package com.tlongdev.bktf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Square shaped image view.
 */
public class SquareImageView extends ImageView {

    /**
     * Required constructor
     *
     * @param context context
     */
    public SquareImageView(Context context) {
        super(context);
    }

    /**
     * Required constructor
     *
     * @param context context
     * @param attrs   attributes
     */
    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Required constructor
     *
     * @param context      context
     * @param attrs        attributes
     * @param defStyleAttr default style attributes
     */
    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Square shape, the side of the sides are the size of the width
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
