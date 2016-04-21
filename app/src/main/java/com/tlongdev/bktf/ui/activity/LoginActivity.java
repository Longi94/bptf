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

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.customtabs.CustomTabActivityHelper;
import com.tlongdev.bktf.customtabs.WebViewFallback;
import com.tlongdev.bktf.presenter.activity.LoginPresenter;
import com.tlongdev.bktf.ui.view.activity.LoginView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BptfActivity implements LoginView {

    @Bind(R.id.steam_id) EditText steamIdInput;
    @Bind(R.id.toolbar) Toolbar mToolbar;

    private ProgressDialog loadingDialog;
    private LoginPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mPresenter = new LoginPresenter(mApplication);
        mPresenter.attachView(this);

        setSupportActionBar(mToolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            // Required because this activity doesn't have a parent activity
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.what_is_id)
    public void showSteamIdInstructions() {
        Uri uri = Uri.parse("http://tlongdev.com/steamid.html");
        CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
        CustomTabActivityHelper.openCustomTab(this, intent, uri, new WebViewFallback());
    }

    @OnClick(R.id.enter)
    public void submit() {
        if (steamIdInput.getText().toString().isEmpty()) {
            steamIdInput.setError("You didn't enter anything!");
        } else {
            mPresenter.login(steamIdInput.getText().toString());
            loadingDialog = ProgressDialog.show(LoginActivity.this, null, "Please wait...", true, false);
        }
    }

    @Override
    public void dismissDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }
}
