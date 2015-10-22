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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.activity.MainActivity;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.adapter.UnusualAdapter;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;

public class UnusualFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        MainActivity.OnDrawerOpenedListener {

    public static final String LOG_TAG = UnusualFragment.class.getSimpleName();

    private static final int PRICE_LIST_LOADER = 0;
    private static final int EFFECT_LIST_LOADER = 1;
    private static final String QUERY_KEY = "query";

    private static final String[] PRICE_LIST_COLUMNS = {
            PriceEntry.TABLE_NAME + "." + PriceEntry._ID,
            PriceEntry.COLUMN_DEFINDEX,
            PriceEntry.COLUMN_ITEM_NAME,
            null
    };
    private static final String[] EFFECT_LIST_COLUMNS = {
            PriceEntry.TABLE_NAME + "." + PriceEntry._ID,
            PriceEntry.COLUMN_PRICE_INDEX,
            PriceEntry.COLUMN_ITEM_NAME,
            null
    };

    public static final int COL_PRICE_LIST_INDE = 1;
    public static final int COL_PRICE_LIST_DEFI = 1;
    public static final int COL_PRICE_LIST_NAME = 2;
    public static final int COL_PRICE_LIST_AVG_PRICE = 3;

    private UnusualAdapter adapter;

    private MenuItem effectMenuItem;

    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;

    private int currentSort = 0;
    private boolean showEffect = false;

    public UnusualFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_unusual, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        //Views used for toolbar behavior
        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar_layout);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        adapter = new UnusualAdapter(getActivity(), null);

        mRecyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        Bundle args = new Bundle();
        args.putString(QUERY_KEY, "AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC");
        getLoaderManager().initLoader(PRICE_LIST_LOADER, args, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection;
        String[] selectionArgs = {"5"};

        switch (id) {
            case PRICE_LIST_LOADER:
                PRICE_LIST_COLUMNS[COL_PRICE_LIST_AVG_PRICE] =
                        "AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC";

                selection = PriceEntry.TABLE_NAME +
                        "." + PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        PriceEntry.COLUMN_PRICE_INDEX + " != 0 GROUP BY " +
                        PriceEntry.COLUMN_DEFINDEX;

                return new CursorLoader(
                        getActivity(),
                        PriceEntry.CONTENT_URI,
                        PRICE_LIST_COLUMNS,
                        selection,
                        selectionArgs,
                        args.getString(QUERY_KEY)
                );
            case EFFECT_LIST_LOADER:
                EFFECT_LIST_COLUMNS[COL_PRICE_LIST_AVG_PRICE] =
                        "AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC";
                selection = PriceEntry.TABLE_NAME +
                        "." + PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        PriceEntry.COLUMN_PRICE_INDEX + " != 0 GROUP BY " +
                        PriceEntry.COLUMN_PRICE_INDEX;

                return new CursorLoader(
                        getActivity(),
                        PriceEntry.CONTENT_URI,
                        EFFECT_LIST_COLUMNS,
                        selection,
                        selectionArgs,
                        "AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC"
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
            Bundle args = new Bundle();
            args.putString(QUERY_KEY, PriceEntry.COLUMN_ITEM_NAME + " ASC");
            getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
            currentSort = 1;
        } else if (!showEffect && id == R.id.menu_sort_price && currentSort != 0) {
            Bundle args = new Bundle();
            args.putString(QUERY_KEY, "AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC");
            getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
            currentSort = 0;
        } else if (id == R.id.action_effect) {
            if (showEffect) {
                showEffect = false;
                effectMenuItem.setIcon(R.drawable.ic_star_outline_white_24dp);
                Bundle args = new Bundle();
                args.putString(QUERY_KEY, "AVG(" + Utility.getRawPriceQueryString(getActivity()) + ") DESC");
                getLoaderManager().initLoader(PRICE_LIST_LOADER, args, this);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_unusuals));
            } else {
                showEffect = true;
                effectMenuItem.setIcon(R.drawable.ic_star_white_24dp);
                getLoaderManager().initLoader(EFFECT_LIST_LOADER, null, this);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_effects));
            }
        } else if (id == R.id.action_search) {
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
    }
}
