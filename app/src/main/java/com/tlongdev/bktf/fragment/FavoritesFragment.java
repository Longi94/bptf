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

package com.tlongdev.bktf.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.activity.ItemChooserActivity;
import com.tlongdev.bktf.activity.MainActivity;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.adapter.FavoritesAdapter;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.FavoritesEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.util.Utility;

import java.util.ArrayList;
import java.util.Vector;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends Fragment implements MainActivity.OnDrawerOpenedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = FavoritesFragment.class.getSimpleName();

    /**
     * The ID of the loader
     */
    private static final int FAVORITES_LOADER = 100;

    /**
     * Indexes for the columns
     */
    public static final int COLUMN_DEFINDEX = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_QUALITY = 2;
    public static final int COLUMN_TRADABLE = 3;
    public static final int COLUMN_CRAFTABLE = 4;
    public static final int COLUMN_PRICE_INDEX = 5;
    public static final int COLUMN_CURRENCY = 6;
    public static final int COLUMN_PRICE = 7;
    public static final int COLUMN_PRICE_MAX = 8;
    public static final int COLUMN_PRICE_RAW = 9;
    public static final int COLUMN_DIFFERENCE = 10;
    public static final int COLUMN_AUSTRALIUM = 11;

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    private FavoritesAdapter mAdapter;

    /**
     * Only needed for manually expanding the toolbar
     */
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;

    /**
     * Constructor
     */
    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(FAVORITES_LOADER, null, this);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) (getActivity()).getApplication();
        mTracker = application.getDefaultTracker();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        mAdapter = new FavoritesAdapter(getActivity(), null);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        int columnCount = 1;
        if (dpWidth >= 720) {
            columnCount = 3;
        } else if (dpWidth >= 600) {
            columnCount = 2;
        }

        //Setup the recycler view
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("Favorites");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MainActivity.REQUEST_NEW_ITEM:
                if (resultCode == Activity.RESULT_OK) {
                    Utility.addToFavorites(getActivity(), (Item) data.getSerializableExtra(ItemChooserActivity.EXTRA_ITEM));
                    getLoaderManager().restartLoader(FAVORITES_LOADER, null, this);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorites, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //Start the search activity
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.action_add_currencies:

                //Iterator that will iterate through the items
                Vector<ContentValues> cVVector = new Vector<>();

                int[] defindexes = new int[]{143, 5002, 5021};

                for (int defindex : defindexes) {

                    ContentValues cv = new ContentValues();

                    cv.put(FavoritesEntry.COLUMN_DEFINDEX, defindex);
                    cv.put(FavoritesEntry.COLUMN_ITEM_QUALITY, Quality.UNIQUE);
                    cv.put(FavoritesEntry.COLUMN_ITEM_TRADABLE, 1);
                    cv.put(FavoritesEntry.COLUMN_ITEM_CRAFTABLE, 1);
                    cv.put(FavoritesEntry.COLUMN_PRICE_INDEX, 0);
                    cv.put(FavoritesEntry.COLUMN_AUSTRALIUM, 0);
                    cv.put(FavoritesEntry.COLUMN_WEAPON_WEAR, 0);

                    cVVector.add(cv);
                }

                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                //Insert all the data into the database
                int rowsInserted = getActivity().getContentResolver()
                        .bulkInsert(FavoritesEntry.CONTENT_URI, cvArray);
                Log.v(LOG_TAG, "inserted " + rowsInserted + " rows");

                getLoaderManager().restartLoader(FAVORITES_LOADER, null, this);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sql = "SELECT " +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + "," +
                ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_QUALITY + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_TRADABLE + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_CRAFTABLE + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_PRICE_INDEX + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                Utility.getRawPriceQueryString(getActivity()) + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DIFFERENCE + "," +
                FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_AUSTRALIUM +
                " FROM " + FavoritesEntry.TABLE_NAME +
                " LEFT JOIN " + PriceEntry.TABLE_NAME +
                " ON " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_TRADABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_CRAFTABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_PRICE_INDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_ITEM_QUALITY + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY +
                " AND " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_AUSTRALIUM + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                " ON " + FavoritesEntry.TABLE_NAME + "." + FavoritesEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                " ORDER BY " + ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC";

        return new CursorLoader(
                getActivity(),
                DatabaseContract.RAW_QUERY_URI,
                null,
                sql,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        ArrayList<Item> items = new ArrayList<>();

        if (data != null) {
            while (data.moveToNext()) {
                Item item = new Item(data.getInt(COLUMN_DEFINDEX),
                        data.getString(COLUMN_NAME),
                        data.getInt(COLUMN_QUALITY),
                        data.getInt(COLUMN_TRADABLE) == 1,
                        data.getInt(COLUMN_CRAFTABLE) == 1,
                        data.getInt(COLUMN_AUSTRALIUM) == 1,
                        data.getInt(COLUMN_PRICE_INDEX),
                        null
                        );

                if (data.getString(COLUMN_CURRENCY) != null) {
                    item.setPrice(new Price(data.getDouble(COLUMN_PRICE),
                            data.getDouble(COLUMN_PRICE_MAX),
                            data.getDouble(COLUMN_PRICE_RAW),
                            0,
                            data.getDouble(COLUMN_DIFFERENCE),
                            data.getString(COLUMN_CURRENCY)));
                }

                items.add(item);
            }
        }

        mAdapter.setDataSet(items);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setDataSet(null);
        mAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.fab)
    public void addItem() {
        startActivityForResult(new Intent(getActivity(), ItemChooserActivity.class), MainActivity.REQUEST_NEW_ITEM);
    }

    /**
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }
}
