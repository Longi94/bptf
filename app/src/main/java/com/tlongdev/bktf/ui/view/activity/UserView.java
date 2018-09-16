package com.tlongdev.bktf.ui.view.activity;

import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 03. 19.
 */
public interface UserView extends BaseView {

    void showData(User user);

    void showError();

    void showPartial(User user);
}