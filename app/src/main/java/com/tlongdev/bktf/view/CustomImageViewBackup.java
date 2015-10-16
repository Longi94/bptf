package com.tlongdev.bktf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Used in grid view to achieve fixed item height.
 */
public class CustomImageViewBackup extends ImageView {

    /**
     * Required constructor
     *
     * @param context context
     */
    public CustomImageViewBackup(Context context) {
        super(context);
    }

    /**
     * Required constructor
     *
     * @param context context
     * @param attrs   attributes
     */
    public CustomImageViewBackup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Required constructor
     *
     * @param context      context
     * @param attrs        attributes
     * @param defStyleAttr default style attributes
     */
    public CustomImageViewBackup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //7:8 rectangle
        setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth() * 7.0 / 8.0));
    }
}
