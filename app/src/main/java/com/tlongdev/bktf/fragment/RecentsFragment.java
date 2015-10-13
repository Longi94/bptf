package com.tlongdev.bktf.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tlongdev.bktf.MainActivity;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.adapter.RecentsAdapter;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.network.FetchPriceList;

/**
 * Main fragment the shows the latest price changes.
 */
public class RecentsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, FetchPriceList.OnPriceListFetchListener, AppBarLayout.OnOffsetChangedListener {

    private static final String LOG_TAG = RecentsFragment.class.getSimpleName();

    private static final int PRICE_LIST_LOADER = 0;

    //Query columns
    private static final String[] PRICE_LIST_COLUMNS = {
            PriceEntry.TABLE_NAME + "." + PriceEntry._ID,
            PriceEntry.COLUMN_DEFINDEX,
            PriceEntry.COLUMN_ITEM_NAME,
            PriceEntry.COLUMN_ITEM_QUALITY,
            PriceEntry.COLUMN_ITEM_TRADABLE,
            PriceEntry.COLUMN_ITEM_CRAFTABLE,
            PriceEntry.COLUMN_PRICE_INDEX,
            PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
            PriceEntry.COLUMN_ITEM_PRICE,
            PriceEntry.COLUMN_ITEM_PRICE_MAX,
            null,
            PriceEntry.COLUMN_DIFFERENCE,
            PriceEntry.COLUMN_AUSTRALIUM
    };

    //Indexes for the columns above
    public static final int COL_PRICE_LIST_DEFI = 1;
    public static final int COL_PRICE_LIST_NAME = 2;
    public static final int COL_PRICE_LIST_QUAL = 3;
    public static final int COL_PRICE_LIST_TRAD = 4;
    public static final int COL_PRICE_LIST_CRAF = 5;
    public static final int COL_PRICE_LIST_INDE = 6;
    public static final int COL_PRICE_LIST_CURR = 7;
    public static final int COL_PRICE_LIST_PRIC = 8;
    public static final int COL_PRICE_LIST_PMAX = 9;
    public static final int COL_PRICE_LIST_PRAW = 10;
    public static final int COL_PRICE_LIST_DIFF = 11;
    public static final int COL_AUSTRALIUM = 12;

    private ProgressBar progressBar;

    private RecentsAdapter adapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private FetchPriceList.OnPriceListFetchListener listener;

    private AppBarLayout appBarLayout;
    private MainActivity parentActivity;

    public RecentsFragment() {
        //Required empty constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recents, container, false);

        setHasOptionsMenu(true);

        adapter = new RecentsAdapter(getActivity(), null);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        //Set up the swipe refresh layout (color and listener)
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accent));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

        if (appBarLayout != null) {
            appBarLayout.addOnOffsetChangedListener(this);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //Download whole database when the app is first opened.
        if (prefs.getBoolean(getString(R.string.pref_initial_load), true)) {
            if (Utility.isNetworkAvailable(getActivity())) {
                FetchPriceList task = new FetchPriceList(getActivity(), false, false);
                task.setOnPriceListFetchListener(this);
                task.execute();
            } else {
                //Quit the app if the download failed.
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.message_database_fail_network)).setCancelable(false).
                        setPositiveButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        } else {

            //Update database if the last update happened more than an hour ago
            if (prefs.getBoolean(getString(R.string.pref_auto_sync), false) &&
                    System.currentTimeMillis() - prefs.getLong(getString(R.string.pref_last_price_list_update), 0) >= 3600000L
                    && Utility.isNetworkAvailable(getActivity())) {
                FetchPriceList task = new FetchPriceList(getActivity(), true, false);
                task.setOnPriceListFetchListener(this);
                task.execute();
                mSwipeRefreshLayout.setRefreshing(true);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_recents, menu);
        super.onCreateOptionsMenu(menu, inflater);

        //Setup the search widget
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setQueryHint("Search for items or users...");

        //Start a new query everytime the text is edited and when the player taps on submit.
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                parentActivity.expandToolbar();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                parentActivity.expandToolbar();
                return true;
            }
        });

        //Switch to the search fragment when the searchview is expanded and switch back when
        //collapsed.
        MenuItemCompat.setOnActionExpandListener(menuItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        parentActivity.expandToolbar();
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        parentActivity.expandToolbar();
                        return true;
                    }
                });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = PriceEntry.COLUMN_LAST_UPDATE + " DESC";

        PRICE_LIST_COLUMNS[COL_PRICE_LIST_PRAW] = Utility.getRawPriceQueryString(getActivity());

        return new CursorLoader(
                getActivity(),
                PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onRefresh() {
        //Manual update
        if (Utility.isNetworkAvailable(getActivity())) {
            FetchPriceList task = new FetchPriceList(getActivity(), true, true);
            task.setOnPriceListFetchListener(this);
            task.execute();
        } else {
            Toast.makeText(getActivity(), "bptf: " + getString(R.string.error_no_network),
                    Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onPriceListFetchFinished() {
        if (isAdded()) {
            //Stop animation
            mSwipeRefreshLayout.setRefreshing(false);

            if (listener != null) {
                listener.onPriceListFetchFinished();
            }
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            mSwipeRefreshLayout.setEnabled(true);
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }
    }

    public void setAppBarLayout(AppBarLayout appBarLayout) {
        this.appBarLayout = appBarLayout;
    }

    public void setListener(FetchPriceList.OnPriceListFetchListener listener) {
        this.listener = listener;
    }

    public void setCallBack(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }
}
