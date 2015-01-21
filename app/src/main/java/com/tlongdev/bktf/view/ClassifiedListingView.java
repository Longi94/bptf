package com.tlongdev.bktf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tlongdev.bktf.R;

// TODO implement classifieds
public class ClassifiedListingView extends FrameLayout {

    public ClassifiedListingView(Context context) {
        super(context);
        init();
    }

    public ClassifiedListingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClassifiedListingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.classified_listing, this);
    }
}
