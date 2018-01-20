package com.tlongdev.bktf.ui.view.activity;

import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.ui.view.BaseView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 24.
 */
public interface ItemChooserView extends BaseView {
    void showEffects(List<Item> items);
}
