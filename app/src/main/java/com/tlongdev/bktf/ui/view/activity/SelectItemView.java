package com.tlongdev.bktf.ui.view.activity;

import android.database.Cursor;

import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public interface SelectItemView extends BaseView {

    void showItems(Cursor items);
}