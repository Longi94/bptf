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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.SelectItemAdapter;
import com.tlongdev.bktf.presenter.activity.SelectItemPresenter;
import com.tlongdev.bktf.ui.view.activity.SelectItemView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SelectItemActivity extends BptfActivity implements SelectItemView, TextWatcher,
        SelectItemAdapter.OnItemSelectedListener {

    public static final String EXTRA_DEFINDEX = "defindex";
    public static final String EXTRA_NAME = "name";

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.item_name) EditText inputName;

    private SelectItemPresenter mPresenter;
    private SelectItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_item);
        ButterKnife.bind(this);

        mPresenter = new SelectItemPresenter(mApplication);
        mPresenter.attachView(this);

        setFinishOnTouchOutside(false);

        mAdapter = new SelectItemAdapter(this, null);
        mAdapter.setListener(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        inputName.addTextChangedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("SelectItemActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mPresenter.loadItems(s.toString());
    }

    @Override
    public void onItemSelected(int defindex, String name) {
        Intent result = new Intent();
        result.putExtra(EXTRA_DEFINDEX, defindex);
        result.putExtra(EXTRA_NAME, name);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void showItems(Cursor items) {
        mAdapter.swapCursor(items, true);
    }
}
