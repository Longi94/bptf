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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.activity.MainActivity;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.adapter.UnusualAdapter;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * The unusual fragment, that shows a list of unusual item categories. Either categorized by
 * hats or effects.
 */
public class UnusualFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        MainActivity.OnDrawerOpenedListener, TextWatcher {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = UnusualFragment.class.getSimpleName();

    /**
     * Loader IDs
     */
    private static final int PRICE_LIST_LOADER = 0;
    private static final int EFFECT_LIST_LOADER = 1;

    /**
     * Extra key for the query string
     */
    private static final String QUERY_KEY = "query";

    /**
     * The IDs of the columns above
     */
    public static final int COLUMN_INDEX = 0;
    public static final int COLUMN_DEFINDEX = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_AVERAGE_PRICE = 2;

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    /**
     * The adapter of the recycler view
     */
    private UnusualAdapter adapter;

    /**
     * the menu item that switches between effects and hats
     */
    private MenuItem effectMenuItem;

    /**
     * Only needed for manually expanding the toolbar
     */
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;

    /**
     * the current sort type
     */
    private int currentSort = 0;

    /**
     * Whether to show effects or hats
     */
    private boolean showEffect = false;

    @Bind(R.id.search) EditText searchInput;

    /**
     * Constructor.
     */
    public UnusualFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) (getActivity()).getApplication();
        mTracker = application.getDefaultTracker();

        View rootView = inflater.inflate(R.layout.fragment_unusual, container, false);

        ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int columnCount = Math.max(3, (int)Math.floor(dpWidth / 120.0));

        //init the recycler view
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        adapter = new UnusualAdapter(getActivity(), null);
        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("Unusual Prices");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        //Init the loader
        Bundle args = new Bundle();
        args.putString(QUERY_KEY, "AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC");
        getLoaderManager().initLoader(PRICE_LIST_LOADER, args, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Query stuff
        String selection;
        String sql;
        String[] selectionArgs = {"5"};

        String filter = searchInput.getText().toString();


        switch (id) {
            case PRICE_LIST_LOADER:

                selection =  ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE '%" + filter + "%' AND " +
                        PriceEntry.TABLE_NAME +"." + PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        PriceEntry.COLUMN_PRICE_INDEX + " != 0 GROUP BY " +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX;

                sql = "SELECT " +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                        ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                        Utility.getRawPriceQueryString(getActivity()) +
                        " FROM " + PriceEntry.TABLE_NAME +
                        " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                        " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                        " WHERE " + selection +
                        " ORDER BY " + args.getString(QUERY_KEY);

                return new CursorLoader(
                        getActivity(),
                        DatabaseContract.RAW_QUERY_URI,
                        null,
                        sql,
                        selectionArgs,
                        null
                );
            case EFFECT_LIST_LOADER:

                selection = UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME + " LIKE '%" + filter + "%' AND " +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        PriceEntry.COLUMN_PRICE_INDEX + " != 0 GROUP BY " +
                        PriceEntry.COLUMN_PRICE_INDEX;

                sql = "SELECT " +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + "," +
                        UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME + "," +
                        Utility.getRawPriceQueryString(getActivity()) +
                        " FROM " + PriceEntry.TABLE_NAME +
                        " LEFT JOIN " + UnusualSchemaEntry.TABLE_NAME +
                        " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + " = " + UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_ID +
                        " WHERE " + selection +
                        " ORDER BY AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC";

                return new CursorLoader(
                        getActivity(),
                        DatabaseContract.RAW_QUERY_URI,
                        null,
                        sql,
                        selectionArgs,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == EFFECT_LIST_LOADER) {
            adapter.setType(UnusualAdapter.TYPE_EFFECTS);
        } else {
            adapter.setType(UnusualAdapter.TYPE_HATS);
        }
        adapter.swapCursor(data, false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.setType(UnusualAdapter.TYPE_HATS);
        adapter.swapCursor(null, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_unusual, menu);
        effectMenuItem = menu.findItem(R.id.action_effect);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (!showEffect && id == R.id.menu_sort_name && currentSort != 1) {
            //Show hats sorted by their name
            Bundle args = new Bundle();
            args.putString(QUERY_KEY, ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC");
            getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
            currentSort = 1;
        } else if (!showEffect && id == R.id.menu_sort_price && currentSort != 0) {
            //Show hats sorted by their average price
            Bundle args = new Bundle();
            args.putString(QUERY_KEY, "AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC");
            getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
            currentSort = 0;
        } else if (id == R.id.action_effect) {
            if (showEffect) {
                //Show hats sorted by their average price
                showEffect = false;
                effectMenuItem.setIcon(R.drawable.ic_star_outline_white_24dp);
                Bundle args = new Bundle();
                args.putString(QUERY_KEY, "AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC");
                getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
                getActivity().setTitle(getString(R.string.title_unusuals));

                searchInput.setHint("Name");
                searchInput.setText("");
            } else {
                //Show effects
                showEffect = true;
                effectMenuItem.setIcon(R.drawable.ic_star_white_24dp);
                getLoaderManager().restartLoader(EFFECT_LIST_LOADER, null, this);
                getActivity().setTitle(getString(R.string.title_effects));

                searchInput.setHint("Effect");
                searchInput.setText("");
            }
        } else if (id == R.id.action_search) {
            //Start the search activity
            startActivity(new Intent(getActivity(), SearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
        Utility.hideKeyboard(getActivity());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (showEffect) {
            getLoaderManager().restartLoader(EFFECT_LIST_LOADER, null, this);
        } else {
            if (currentSort != 1) {
                Bundle args = new Bundle();
                args.putString(QUERY_KEY, "AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC");
                getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
            } else {
                Bundle args = new Bundle();
                args.putString(QUERY_KEY, ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC");
                getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
            }
        }
    }
}
