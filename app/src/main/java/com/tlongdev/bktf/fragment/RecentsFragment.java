package com.tlongdev.bktf.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.activity.MainActivity;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.adapter.RecentsAdapter;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.network.GetPriceList;

/**
 * recents fragment. Shows a list of all the prices orderd by the time of the price update.
 */
public class RecentsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, GetPriceList.OnPriceListFetchListener,
        AppBarLayout.OnOffsetChangedListener, MainActivity.OnDrawerOpenedListener {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = RecentsFragment.class.getSimpleName();

    /**
     * The ID of the loader
     */
    private static final int PRICE_LIST_LOADER = 0;

    /**
     * Indexes for the columns
     */
    public static final int COL_PRICE_LIST_DEFI = 0;
    public static final int COL_PRICE_LIST_NAME = 1;
    public static final int COL_PRICE_LIST_QUAL = 2;
    public static final int COL_PRICE_LIST_TRAD = 3;
    public static final int COL_PRICE_LIST_CRAF = 4;
    public static final int COL_PRICE_LIST_INDE = 5;
    public static final int COL_PRICE_LIST_CURR = 6;
    public static final int COL_PRICE_LIST_PRIC = 7;
    public static final int COL_PRICE_LIST_PMAX = 8;
    public static final int COL_PRICE_LIST_PRAW = 9;
    public static final int COL_PRICE_LIST_DIFF = 10;
    public static final int COL_AUSTRALIUM = 11;

    /**
     * Loading indicator
     */
    private ProgressBar progressBar;

    /**
     * Adapter of the recycler view
     */
    private RecentsAdapter adapter;

    /**
     * the swipe refresh layout
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * The recycler view
     */
    private RecyclerView mRecyclerView;

    /**
     * Only needed for manually expanding the toolbar
     */
    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;

    /**
     * Views
     */
    private TextView metalPrice;
    private TextView keyPrice;
    private TextView budsPrice;
    private View metalPriceImage;
    private View keyPriceImage;
    private View budsPriceImage;

    /**
     * Constructor
     */
    public RecentsFragment() {
        //Required empty constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recents, container, false);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        //Views used for toolbar behavior
        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar_layout);
        mAppBarLayout.addOnOffsetChangedListener(this);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);

        adapter = new RecentsAdapter(getActivity(), null);

        //Setup the recycler view
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setVisibility(View.GONE);

        //Set up the swipe refresh layout (color and listener)
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeColors(Utility.getColor(getActivity(), R.color.accent));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

        //Views of in the toolbar
        metalPrice = (TextView) rootView.findViewById(R.id.text_view_metal_price);
        keyPrice = (TextView) rootView.findViewById(R.id.text_view_key_price);
        budsPrice = (TextView) rootView.findViewById(R.id.text_view_buds_price);

        metalPriceImage = rootView.findViewById(R.id.image_view_metal_price);
        keyPriceImage = rootView.findViewById(R.id.image_view_key_price);
        budsPriceImage = rootView.findViewById(R.id.image_view_buds_price);

        //Populate the toolbar header
        onPriceListFetchFinished();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //Download whole database when the app is first opened.
        if (prefs.getBoolean(getString(R.string.pref_initial_load), true)) {
            if (Utility.isNetworkAvailable(getActivity())) {
                GetPriceList task = new GetPriceList(getActivity(), false, false);
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
                GetPriceList task = new GetPriceList(getActivity(), true, false);
                task.setOnPriceListFetchListener(this);
                task.execute();
                //Workaround for the circle not appearing
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_recents, menu);
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sql = "SELECT " +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                ItemSchemaEntry.TABLE_NAME + "." + DatabaseContract.ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                Utility.getRawPriceQueryString(getActivity()) +  "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DIFFERENCE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                " FROM " + PriceEntry.TABLE_NAME +
                " JOIN " + ItemSchemaEntry.TABLE_NAME +
                " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                " ORDER BY " + PriceEntry.COLUMN_LAST_UPDATE + " DESC";

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
        adapter.swapCursor(data, false);

        //Animate in the recycler view, so it's not that abrupt
        Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.simple_fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.simple_fade_in);

        fadeIn.setDuration(500);
        fadeOut.setDuration(500);

        mRecyclerView.startAnimation(fadeIn);
        mRecyclerView.setVisibility(View.VISIBLE);

        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.startAnimation(fadeOut);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null, false);
    }

    @Override
    public void onRefresh() {
        //Manual update
        if (Utility.isNetworkAvailable(getActivity())) {
            GetPriceList task = new GetPriceList(getActivity(), true, true);
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

            //Update the header with currency prices
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            metalPrice.setText(prefs.getString(getString(R.string.pref_metal_price), ""));
            keyPrice.setText(prefs.getString(getString(R.string.pref_key_price), ""));
            budsPrice.setText(prefs.getString(getString(R.string.pref_buds_price), ""));

            if (Utility.getDouble(prefs, getString(R.string.pref_metal_diff), 0.0) > 0.0) {
                metalPriceImage.setBackgroundColor(0xff008504);
            } else {
                metalPriceImage.setBackgroundColor(0xff850000);
            }
            if (Utility.getDouble(prefs, getString(R.string.pref_key_diff), 0.0) > 0.0) {
                keyPriceImage.setBackgroundColor(0xff008504);
            } else {
                keyPriceImage.setBackgroundColor(0xff850000);
            }
            if (Utility.getDouble(prefs, getString(R.string.pref_buds_diff), 0.0) > 0) {
                budsPriceImage.setBackgroundColor(0xff008504);
            } else {
                budsPriceImage.setBackgroundColor(0xff850000);
            }
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        //Disable the swipe refresh layout when the toolbar is not fully visible
        if (i == 0) {
            mSwipeRefreshLayout.setEnabled(true);
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }
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
