package com.tlongdev.bktf.ui.view.activity;

import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 03. 24.
 */
public interface ItemDetailView extends BaseView {
    void showItemDetails(Price price);
}
