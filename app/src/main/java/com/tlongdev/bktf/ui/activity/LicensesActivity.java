/**
 * Copyright 2015 Long Tran
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

package com.tlongdev.bktf.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.LicensesAdapter;
import com.tlongdev.bktf.model.License;
import com.tlongdev.bktf.presenter.activity.LicensesPresenter;
import com.tlongdev.bktf.ui.view.activity.LicensesView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LicensesActivity extends BptfActivity implements LicensesView, LicensesAdapter.OnClickListener {

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.toolbar) Toolbar mToolbar;

    private LicensesPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);
        ButterKnife.bind(this);

        mPresenter = new LicensesPresenter(mApplication);
        mPresenter.attachView(this);

        setSupportActionBar(mToolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPresenter.loadLicenses();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void showLicenses(List<License> licenses) {
        LicensesAdapter adapter = new LicensesAdapter(licenses);
        adapter.setListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLicenseClicked(String url) {
        Uri webPage = Uri.parse(url);

        //Open link in the device default web browser
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
