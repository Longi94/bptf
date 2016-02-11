/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tlongdev.bktf.R;

/**
 * An frame layout that allows to set that width of the view in ratio to its height.
 * Ratio is 1:1 by default.
 */
public class RatioWidthFrameLayout extends FrameLayout {

    private double mRatioWidth = 1;
    private double mRatioHeight = 1;

    /**
     * Required constructor
     *
     * @param context context
     */
    public RatioWidthFrameLayout(Context context) {
        super(context);
    }

    /**
     * Required constructor
     *
     * @param context context
     * @param attrs   attributes
     */
    public RatioWidthFrameLayout(Context context, AttributeSet attrs) {
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
    public RatioWidthFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
        setMeasuredDimension((int) (getMeasuredHeight() * mRatioWidth / mRatioHeight), getMeasuredHeight());
    }
}
