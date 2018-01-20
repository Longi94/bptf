package com.tlongdev.bktf.ui.view.activity;

import com.tlongdev.bktf.model.License;
import com.tlongdev.bktf.ui.view.BaseView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 23.
 */
public interface LicensesView extends BaseView {
    void showLicenses(List<License> licenses);
}
