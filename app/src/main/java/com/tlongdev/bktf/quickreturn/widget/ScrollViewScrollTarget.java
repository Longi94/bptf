package com.tlongdev.bktf.quickreturn.widget;

import android.view.View;
import android.view.ViewTreeObserver;

public class ScrollViewScrollTarget
        extends QuickReturnTargetView
        implements ObservableScrollView.OnScrollListener {

    private final ObservableScrollView scrollView;
    private int quickReturnHeight;
    private int maxScrollY;

    public ScrollViewScrollTarget(final ObservableScrollView scrollView, final View targetView, final int position, final int targetViewHeight) {
        super(targetView, position);
        this.scrollView = scrollView;
        scrollView.setOnScrollListener(this);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        onScrollChanged(scrollView.getScrollY());
                        maxScrollY = scrollView.computeVerticalScrollRange() - scrollView.getHeight();
                        quickReturnHeight = quickReturnView.getHeight();
                    }
                }
        );
    }

    @Override
    protected int getComputedScrollY() {
        return scrollView.getScrollY();
    }

    @Override
    public void onScrollChanged(int scrollY) {
        if (quickReturnView == null)
            return;

        scrollY = Math.min(maxScrollY, scrollY);

        int rawY = -scrollY;
        final int translationY = currentTransition.determineState(rawY, quickReturnView.getHeight());

        translateTo(translationY);
    }
}