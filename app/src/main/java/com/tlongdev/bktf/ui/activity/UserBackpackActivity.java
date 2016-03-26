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

import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.BackpackAdapter;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.presenter.activity.UserBackpackPresenter;
import com.tlongdev.bktf.ui.view.activity.UserBackpackView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Activity for viewing user backpacks.
 */
public class UserBackpackActivity extends BptfActivity implements UserBackpackView, BackpackAdapter.OnItemClickedListener {

    //Keys for extra data in the intent
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_GUEST = "guest";

    @Bind(R.id.list_view_backpack) RecyclerView mRecyclerView;

    //Adapters used for the listview
    private BackpackAdapter mAdapter;

    //Boolean to decide which database table to load from
    private boolean isGuest;

    private UserBackpackPresenter mPresenter;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_backpack);
        ButterKnife.bind(this);

        mPresenter = new UserBackpackPresenter(mApplication);
        mPresenter.attachView(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Set the actionbar title to xyz's backpack
        getSupportActionBar().setTitle(getString(R.string.title_custom_backpack,
                getIntent().getStringExtra(EXTRA_NAME)));

        //Decide which table to load data from according to the extra data from the intent
        isGuest = getIntent().getBooleanExtra(EXTRA_GUEST, false);

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

        mPresenter.loadBackpackItems(isGuest);
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
    public void showItems(List<BackpackItem> items, List<BackpackItem> newItems) {
        mAdapter.setDataSet(items, newItems);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnItemClicked(BackpackAdapter.ViewHolder holder, BackpackItem backpackItem) {
        //Open the ItemDetailActivity and send some extra data to it
        Intent i = new Intent(this, ItemDetailActivity.class);
        i.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, backpackItem.getId());
        i.putExtra(ItemDetailActivity.EXTRA_GUEST, isGuest);

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
}
