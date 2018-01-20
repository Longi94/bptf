package com.tlongdev.bktf.ui.view.activity;

import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 03. 23.
 */
public interface SettingsView extends BaseView {
    void dismissDialog();

    void finish();

    void userInfoDownloaded();
}
