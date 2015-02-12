package com.tlongdev.bktf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SquareSmallFrameLayout extends FrameLayout {

    public SquareSmallFrameLayout(Context context) {
        super(context);
    }

    public SquareSmallFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareSmallFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth() / 3, getMeasuredWidth() / 3);
    }
}
