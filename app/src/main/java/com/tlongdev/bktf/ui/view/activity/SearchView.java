package com.tlongdev.bktf.ui.view.activity;

import android.database.Cursor;

import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 03. 22.
 */
public interface SearchView extends BaseView {

    void showItems(Cursor items);

    void userFound(User user);

    void userNotFound();
}