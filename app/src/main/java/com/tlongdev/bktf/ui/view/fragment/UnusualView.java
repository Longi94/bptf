package com.tlongdev.bktf.ui.view.fragment;

import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.ui.view.BaseView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public interface UnusualView extends BaseView {

    void showUnusualHats(List<Item> unusuals);

    void showUnusualEffects(List<Item> unusuals);

}