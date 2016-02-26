package com.tlongdev.bktf.presenter;

/**
 * @author Long
 * @since 2016. 02. 26.
 */
public interface Presenter<V> {
    void attachView(V view);
    void detachView();
}
