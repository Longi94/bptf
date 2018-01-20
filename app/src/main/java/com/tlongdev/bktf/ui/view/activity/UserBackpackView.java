package com.tlongdev.bktf.ui.view.activity;

import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.ui.view.BaseView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 18.
 */
public interface UserBackpackView extends BaseView {

    void showItems(List<BackpackItem> items, List<BackpackItem> newItems);

    void privateBackpack();
}