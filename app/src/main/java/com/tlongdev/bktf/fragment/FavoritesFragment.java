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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.tlongdev.bktf.activity.MainActivity;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.adapter.FavoritesAdapter;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.FavoritesEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import java.util.ArrayList;

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

    /**
     * The recycler view
     */
    private RecyclerView mRecyclerView;

    private FavoritesAdapter mAdapter;

    /**
     * Only needed for manually expanding the toolbar
     */
    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;

    /**
     * Constructor
     */
    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FAVORITES_LOADER, null, this);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) (getActivity()).getApplication();
        mTracker = application.getDefaultTracker();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        ;

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        //Views used for toolbar behavior
        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar_layout);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);

        mAdapter = new FavoritesAdapter(getActivity(), null);

        //Setup the recycler view
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("Favorites");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
    }

    /**
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
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
                items.add(new Item(
                        data.getInt(COLUMN_DEFINDEX),
                        data.getString(COLUMN_NAME),
                        data.getInt(COLUMN_QUALITY),
                        data.getInt(COLUMN_TRADABLE) == 1,
                        data.getInt(COLUMN_CRAFTABLE) == 1,
                        data.getInt(COLUMN_AUSTRALIUM) == 1,
                        data.getInt(COLUMN_PRICE_INDEX),
                        new Price(
                            data.getDouble(COLUMN_PRICE),
                                data.getDouble(COLUMN_PRICE_MAX),
                                data.getDouble(COLUMN_PRICE_RAW),
                                0,
                                data.getDouble(COLUMN_DIFFERENCE),
                                data.getString(COLUMN_CURRENCY)
                        )
                ));
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
}
