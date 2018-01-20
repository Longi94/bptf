package com.tlongdev.bktf.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.UnusualAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.activity.UnusualPresenter;
import com.tlongdev.bktf.ui.view.activity.UnusualView;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity for showing unusual prices for specific effects or hats.
 */
public class UnusualActivity extends BptfActivity implements UnusualView, TextWatcher, UnusualAdapter.OnItemClickListener {

    public static final String EXTRA_DEFINDEX = "defindex";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_PRICE_INDEX = "index";

    @Inject UnusualPresenter mPresenter;

    @SuppressWarnings("NullableProblems")
    @Nullable
    @InjectExtra(EXTRA_DEFINDEX) int mDefindex = -1;
    @SuppressWarnings("NullableProblems")
    @Nullable
    @InjectExtra(EXTRA_PRICE_INDEX) int mIndex = -1;
    @InjectExtra(EXTRA_NAME) String mName;

    @BindView(R.id.search) EditText mSearchInput;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;

    private UnusualAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unusual);
        ButterKnife.bind(this);
        Dart.inject(this);

        mApplication.getActivityComponent().inject(this);

        mPresenter.attachView(this);

        setSupportActionBar(mToolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Set the action bar title to the current hat/effect name
        setTitle(mName);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int columnCount = Math.max(3, (int) Math.floor(dpWidth / 120.0));

        //Initialize adapter
        mAdapter = new UnusualAdapter(mApplication);
        mAdapter.setType(UnusualAdapter.TYPE_SPECIFIC_HAT);
        mAdapter.setListener(this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));

        mSearchInput.addTextChangedListener(this);
        mSearchInput.setHint(mDefindex != -1 ? "Effect" : "Name");

        mPresenter.loadUnusuals(mDefindex, mIndex, "");
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
        mPresenter.loadUnusuals(mDefindex, mIndex, s.toString());
    }

    @Override
    public void showUnusuals(List<Item> unusuals) {
        mAdapter.setDataSet(unusuals);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMoreClicked(View view, final Item item) {
        Utility.createItemPopupMenu(this, view, item).show();
    }

    @Override
    public void onItemClicked(int index, String name, boolean effect) {
    }
}
