package com.tlongdev.bktf.presenter.activity;

import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.UserView;

/**
 * @author Long
 * @since 2016. 03. 19.
 */
public class UserPresenter implements Presenter<UserView> {

    private UserView mView;

    @Override
    public void attachView(UserView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }
}