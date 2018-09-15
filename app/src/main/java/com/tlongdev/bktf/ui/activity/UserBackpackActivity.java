package com.tlongdev.bktf.ui.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.BackpackAdapter;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
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

    @Inject UserBackpackPresenter mPresenter;

    @InjectExtra(EXTRA_NAME) String mUserName;
    //Boolean to decide which database table to load from
    @InjectExtra(EXTRA_GUEST) boolean mIsGuest;
    @InjectExtra(EXTRA_STEAM_ID) String mSteamId;

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.privateBackpackIcon) ImageView privateBackpackIcon;
    @BindView(R.id.privateBackpackText) TextView privateBackpackText;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
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
        // TODO: 2018-09-14
        mSwipeRefreshLayout.setEnabled(false);

        mPresenter.getBackpackItems(mSteamId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
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
    public void showItems(SparseArray<BackpackItem> items, List<BackpackItem> newItems, int backpackSlots) {
        mAdapter.setDataSet(items, newItems, backpackSlots);
        mAdapter.notifyDataSetChanged();

        mProgressBar.setVisibility(View.GONE);
        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void privateBackpack() {
        mProgressBar.setVisibility(View.GONE);
        privateBackpackIcon.setVisibility(View.VISIBLE);
        privateBackpackText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String errorMessage) {
        mProgressBar.setVisibility(View.GONE);
        privateBackpackIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_emoticon_sad));
        privateBackpackText.setText("Something went wrong!\n" + errorMessage);
        privateBackpackIcon.setVisibility(View.VISIBLE);
        privateBackpackText.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnItemClicked(BackpackAdapter.ViewHolder holder, BackpackItem backpackItem) {
        //Open the ItemDetailActivity and send some extra data to it
        Intent i = new Intent(this, ItemDetailActivity.class);
        i.putExtra(ItemDetailActivity.EXTRA_ITEM, backpackItem);

        Cursor itemCursor = getContentResolver().query(
                ItemSchemaEntry.CONTENT_URI,
                new String[]{
                        ItemSchemaEntry.COLUMN_ITEM_NAME,
                        ItemSchemaEntry.COLUMN_TYPE_NAME,
                        ItemSchemaEntry.COLUMN_PROPER_NAME
                },
                ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX + " = ?",
                new String[]{String.valueOf(backpackItem.getDefindex())},
                null
        );

        if (itemCursor != null) {
            if (itemCursor.moveToFirst()) {
                i.putExtra(ItemDetailActivity.EXTRA_ITEM_NAME,
                        itemCursor.getString(0));
                i.putExtra(ItemDetailActivity.EXTRA_ITEM_TYPE,
                        itemCursor.getString(1));
                i.putExtra(ItemDetailActivity.EXTRA_PROPER_NAME,
                        itemCursor.getInt(2));
            }
            itemCursor.close();
        }

        if (Build.VERSION.SDK_INT >= 23) {
            //Fancy shared elements transition if above 20
            ActivityOptions options;
            if (holder.quality.getVisibility() == View.VISIBLE) {
                options = ActivityOptions
                        .makeSceneTransitionAnimation(this,
                                Pair.create(holder.icon, "icon_transition"),
                                Pair.create(holder.effect, "effect_transition"),
                                Pair.create(holder.paint, "paint_transition"),
                                Pair.create(holder.quality, "quality_transition"),
                                Pair.create(holder.root, "background_transition"));
            } else {
                options = ActivityOptions
                        .makeSceneTransitionAnimation(this,
                                Pair.create(holder.icon, "icon_transition"),
                                Pair.create(holder.effect, "effect_transition"),
                                Pair.create(holder.paint, "paint_transition"),
                                Pair.create(holder.root, "background_transition"));
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
