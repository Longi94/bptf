package com.tlongdev.bktf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

//Used in grid view to achieve fixed item height
public class CustomImageView extends ImageView{
    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //7:8 rectangle
        setMeasuredDimension(getMeasuredWidth(), (int)(getMeasuredWidth() * 7.0/8.0));
    }
}
