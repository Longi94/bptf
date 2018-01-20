package com.tlongdev.bktf.presenter;

import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 02. 26.
 */
public interface Presenter<V extends BaseView> {
    void attachView(V view);
    void detachView();
}
