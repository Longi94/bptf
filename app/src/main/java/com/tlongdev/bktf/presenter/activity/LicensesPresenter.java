/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.presenter.activity;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.License;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.LicensesView;
import com.tlongdev.bktf.util.ParseLicenseXml;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 23.
 */
public class LicensesPresenter implements Presenter<LicensesView> {

    @Inject Context mContext;

    private LicensesView mView;

    public LicensesPresenter(BptfApplication application) {
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(LicensesView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadLicenses() {
        List<License> licenses = new ArrayList<>();
        try {
            licenses = ParseLicenseXml.Parse(mContext.getResources().getXml(R.xml.licenses));
        } catch (XmlPullParserException | IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        mView.showLicenses(licenses);
    }
}
