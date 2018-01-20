package com.tlongdev.bktf.ui.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.google.android.gms.ads.AdView;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.BackpackAdapter;
import com.tlongdev.bktf.data.dao.ItemSchemaDao;
import com.tlongdev.bktf.data.entity.ItemSchema;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.presenter.activity.UserBackpackPresenter;
import com.tlongdev.bktf.ui.view.activity.UserBackpackView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity for viewing user backpacks.
 */
public class UserBackpackActivity extends BptfActivity implements UserBackpackView, BackpackAdapter.OnItemClickedListener, SwipeRefreshLayout.OnRefreshListener {

    //Keys for extra data in the intent
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_GUEST = "guest";
    public static final String EXTRA_STEAM_ID = "steam_id";

    @Inject
    UserBackpackPresenter mPresenter;
    @Inject
    ItemSchemaDao itemSchemaDao;

    @InjectExtra(EXTRA_NAME) String mUserName;
    //Boolean to decide which database table to load from
    @InjectExtra(EXTRA_GUEST) boolean mIsGuest;
    @InjectExtra(EXTRA_STEAM_ID) String mSteamId;

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.ad_view) AdView mAdView;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    //Adapters used for the listview
    private BackpackAdapter mAdapter;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_backpack);
        ButterKnife.bind(this);
        Dart.inject(this);

        mApplication.getActivityComponent().inject(this);

        mPresenter.attachView(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            //Set the actionbar title to xyz's backpack
            actionBar.setTitle(getString(R.string.title_custom_backpack, mUserName));
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new BackpackAdapter(mApplication);
        mAdapter.setListener(this);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        final int columnCount = dpWidth >= 600 ? 10 : 5;

        GridLayoutManager layoutManager = new GridLayoutManager(this, columnCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mAdapter.getItemViewType(position) == BackpackAdapter.VIEW_TYPE_HEADER) {
                    return columnCount;
                } else {
                    return 1;
                }
            }
        });

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        mAdManager.addAdView(mAdView);

        mPresenter.getBackpackItems(mSteamId, mIsGuest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
        mAdManager.removeAdView(mAdView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Return to the previous activity when the back button (home) is pressed.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showItems(List<BackpackItem> items, List<BackpackItem> newItems) {
        mAdapter.setDataSet(items, newItems);
        mAdapter.notifyDataSetChanged();

        mProgressBar.setVisibility(View.GONE);
        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void privateBackpack() {
        // TODO: 2017-10-10  
    }

    @Override
    public void OnItemClicked(BackpackAdapter.ViewHolder holder, BackpackItem backpackItem) {
        //Open the ItemDetailActivity and send some extra data to it
        Intent i = new Intent(this, ItemDetailActivity.class);
        i.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, backpackItem.getId());
        i.putExtra(ItemDetailActivity.EXTRA_GUEST, mIsGuest);

        ItemSchema itemSchema = itemSchemaDao.find(backpackItem.getDefindex());

        if (itemSchema != null) {
            i.putExtra(ItemDetailActivity.EXTRA_ITEM_NAME, itemSchema.getItemName());
            i.putExtra(ItemDetailActivity.EXTRA_ITEM_TYPE, itemSchema.getTypeName());
            i.putExtra(ItemDetailActivity.EXTRA_PROPER_NAME, itemSchema.getProperName());
        }

        if (Build.VERSION.SDK_INT >= 23) {
            //Fancy shared elements transition if above 20
            ActivityOptions options;
            if (holder.quality.getVisibility() == View.VISIBLE) {
                options = ActivityOptions
                        .makeSceneTransitionAnimation(this,
                                Pair.create((View) holder.icon, "icon_transition"),
                                Pair.create((View) holder.effect, "effect_transition"),
                                Pair.create((View) holder.paint, "paint_transition"),
                                Pair.create((View) holder.quality, "quality_transition"),
                                Pair.create((View) holder.root, "background_transition"));
            } else {
                options = ActivityOptions
                        .makeSceneTransitionAnimation(this,
                                Pair.create((View) holder.icon, "icon_transition"),
                                Pair.create((View) holder.effect, "effect_transition"),
                                Pair.create((View) holder.paint, "paint_transition"),
                                Pair.create((View) holder.root, "background_transition"));
            }
            startActivity(i, options.toBundle());
        } else if (Build.VERSION.SDK_INT >= 21) {
            //Fancy shared elements transition if above 20
            @SuppressWarnings("unchecked")
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(this, holder.root, "background_transition");
            startActivity(i, options.toBundle());
        } else {
            startActivity(i);
        }
    }

    @Override
    public void onRefresh() {

    }
}
