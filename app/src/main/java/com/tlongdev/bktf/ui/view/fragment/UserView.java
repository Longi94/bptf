package com.tlongdev.bktf.ui.view.fragment;

import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 03. 15.
 */
public interface UserView extends BaseView {
    void showRefreshingAnimation();

    void hideRefreshingAnimation();

    void updateUserPage(User user);

    void backpack(boolean _private);
}