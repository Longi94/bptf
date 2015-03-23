package com.tlongdev.bktf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * A square shaped frame layout that will always be third of the given size.
 */
public class SquareSmallFrameLayout extends FrameLayout {

    /**
     * Required constructor
     *
     * @param context context
     */
    public SquareSmallFrameLayout(Context context) {
        super(context);
    }

    /**
     * Required constructor
     *
     * @param context context
     * @param attrs   attributes
     */
    public SquareSmallFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Required constructor
     *
     * @param context      context
     * @param attrs        attributes
     * @param defStyleAttr default style attributes
     */
    public SquareSmallFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Square shape, third of the given size
        setMeasuredDimension(getMeasuredWidth() / 3, getMeasuredWidth() / 3);
    }
}
