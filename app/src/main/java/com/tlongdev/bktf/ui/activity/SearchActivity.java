package com.tlongdev.bktf.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.SearchAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.presenter.activity.SearchPresenter;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends BptfActivity implements com.tlongdev.bktf.ui.view.activity.SearchView, SearchAdapter.OnSearchClickListener {

    @Inject SearchPresenter mPresenter;

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    //The adapter of the recyclerview
    private SearchAdapter mAdapter;

    private boolean filterEnabled = false;
    private boolean filterTradable = true;
    private boolean filterCraftable = true;
    private boolean filterAustralium = false;
    private int filterQuality = Quality.UNIQUE;
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        mApplication.getActivityComponent().inject(this);

        mPresenter.attachView(this);

        setSupportActionBar(mToolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Initialize the list
        mAdapter = new SearchAdapter(mApplication);
        mAdapter.setListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();

        mAdapter.closeCursor();
    }

    @Override
    public void onBackPressed() {
        Utility.hideKeyboard(this);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        //Setup the search widget
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setQueryHint("Items and users...");

        //Restart the loader every time the query string is changed
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mQuery = s;
                mPresenter.search(mQuery, filterEnabled, filterQuality, filterTradable,
                        filterCraftable, filterAustralium);

                mAdapter.setLoading(true);
                mAdapter.setUser(null);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mQuery = s;
                mPresenter.search(mQuery, filterEnabled, filterQuality, filterTradable,
                        filterCraftable, filterAustralium);

                mAdapter.setLoading(true);
                mAdapter.setUser(null);
                return true;
            }
        });

        //Auto expand the search view
        mSearchView.setIconified(false);

        return true;
    }

    @Override
    public void showItems(Cursor items) {
        mAdapter.swapCursor(items);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void userFound(User user) {
        mAdapter.setUser(user);
        mAdapter.setLoading(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void userNotFound() {
        mAdapter.setUser(null);
        mAdapter.setLoading(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            filterEnabled = data.getBooleanExtra(SearchFilterActivity.EXTRA_ENABLED, false);
            filterQuality = data.getIntExtra(SearchFilterActivity.EXTRA_QUALITY, Quality.UNIQUE);
            filterTradable = data.getBooleanExtra(SearchFilterActivity.EXTRA_TRADABLE, true);
            filterCraftable = data.getBooleanExtra(SearchFilterActivity.EXTRA_CRAFTABLE, true);
            filterAustralium = data.getBooleanExtra(SearchFilterActivity.EXTRA_AUSTRALIUM, false);

            mPresenter.search(mQuery, filterEnabled, filterQuality, filterTradable,
                    filterCraftable, filterAustralium);

            mAdapter.setLoading(true);
            mAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.fab)
    public void onClick(View v) {
        Intent intent = new Intent(this, SearchFilterActivity.class);
        intent.putExtra(SearchFilterActivity.EXTRA_ENABLED, filterEnabled);
        intent.putExtra(SearchFilterActivity.EXTRA_TRADABLE, filterTradable);
        intent.putExtra(SearchFilterActivity.EXTRA_CRAFTABLE, filterCraftable);
        intent.putExtra(SearchFilterActivity.EXTRA_QUALITY, filterQuality);
        intent.putExtra(SearchFilterActivity.EXTRA_AUSTRALIUM, filterAustralium);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onMoreClicked(View view, final Item item) {
        Utility.createItemPopupMenu(this, view, item).show();
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra(UserActivity.STEAM_ID_KEY, user.getResolvedSteamId());
        startActivity(intent);
    }
}