package com.tlongdev.bktf.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.SelectItemAdapter;
import com.tlongdev.bktf.presenter.activity.SelectItemPresenter;
import com.tlongdev.bktf.ui.view.activity.SelectItemView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectItemActivity extends BptfActivity implements SelectItemView, TextWatcher,
        SelectItemAdapter.OnItemSelectedListener {

    public static final String EXTRA_DEFINDEX = "defindex";
    public static final String EXTRA_NAME = "name";

    @Inject SelectItemPresenter mPresenter;

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.item_name) EditText inputName;

    private SelectItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_item);
        ButterKnife.bind(this);

        mApplication.getActivityComponent().inject(this);

        mPresenter.attachView(this);

        setFinishOnTouchOutside(false);

        mAdapter = new SelectItemAdapter(mApplication);
        mAdapter.setListener(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        inputName.addTextChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();

        mAdapter.closeCursor();
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
        mAdapter.swapCursor(items);
    }
}
