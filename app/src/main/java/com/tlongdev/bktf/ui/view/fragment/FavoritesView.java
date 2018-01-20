package com.tlongdev.bktf.ui.view.fragment;

import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.ui.view.BaseView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 12.
 */
public interface FavoritesView extends BaseView {

    void showFavorites(List<Item> items);
}