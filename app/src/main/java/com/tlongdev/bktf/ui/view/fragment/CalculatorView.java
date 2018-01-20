package com.tlongdev.bktf.ui.view.fragment;

import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.ui.view.BaseView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 11.
 */
public interface CalculatorView extends BaseView {
    void showItems(List<Item> items, List<Integer> count, Price totalPrice);

    void updatePrices(Price totalPrice);

    void clearItems();
}
