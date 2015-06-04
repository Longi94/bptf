package com.tlongdev.bktf.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.SettingsActivity;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.adapter.PriceListAdapter;
import com.tlongdev.bktf.data.PriceListContract.PriceEntry;
import com.tlongdev.bktf.task.FetchPriceList;
import com.tlongdev.bktf.view.SpacesItemDecoration;

/**
 * Main fragment the shows the latest price changes.
 */
public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, FetchPriceList.OnPriceListFetchListener {

    private static final String LOG_TAG = HomeFragment.class.getSimpleName();

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
            PriceEntry.COLUMN_DIFFERENCE
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

    private ProgressBar progressBar;

    private PriceListAdapter cursorAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView metalPrice;
    private TextView keyPrice;
    private TextView budsPrice;
    private View metalPriceImage;
    private View keyPriceImage;
    private View budsPriceImage;

    public HomeFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        metalPrice = (TextView) rootView.findViewById(R.id.text_view_metal_price);
        keyPrice = (TextView) rootView.findViewById(R.id.text_view_key_price);
        budsPrice = (TextView) rootView.findViewById(R.id.text_view_buds_price);

        metalPriceImage = rootView.findViewById(R.id.image_view_metal_price);
        keyPriceImage = rootView.findViewById(R.id.image_view_key_price);
        budsPriceImage = rootView.findViewById(R.id.image_view_buds_price);

        metalPrice.setText(prefs.getString(getString(R.string.pref_metal_price), ""));
        keyPrice.setText(prefs.getString(getString(R.string.pref_key_price), ""));
        budsPrice.setText(prefs.getString(getString(R.string.pref_buds_price), ""));

        if (Utility.getDouble(prefs, getString(R.string.pref_metal_diff), 0) > 0) {
            metalPriceImage.setBackgroundColor(0xff008504);
        } else {
            metalPriceImage.setBackgroundColor(0xff850000);
        }
        if (Utility.getDouble(prefs, getString(R.string.pref_key_diff), 0) > 0) {
            keyPriceImage.setBackgroundColor(0xff008504);
        } else {
            keyPriceImage.setBackgroundColor(0xff850000);
        }
        if (Utility.getDouble(prefs, getString(R.string.pref_buds_diff), 0) > 0) {
            budsPriceImage.setBackgroundColor(0xff008504);
        } else {
            budsPriceImage.setBackgroundColor(0xff850000);
        }

        cursorAdapter = new PriceListAdapter(getActivity());

        // Get a reference to the ListView, and attach this adapter to it.
        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_changes);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new SpacesItemDecoration((int)Utility.convertDpToPx(getActivity(), 8)));

        //Set up the swipe refresh layout (color and listener)
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeColors(0xff5787c5);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //Set up quick return
        mRecyclerView.setAdapter(cursorAdapter);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

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

            if (prefs.getBoolean(getString(R.string.pref_dev_key_notification), true)) {
                showDeveloperKeyNotifications();
                prefs.edit().putBoolean(getString(R.string.pref_dev_key_notification), false).apply();
            }
        }
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
        cursorAdapter.swapCursor(data);
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
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

            //Update the header with currency prices
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            metalPrice.setText(prefs.getString(getActivity().getString(R.string.pref_metal_price), ""));
            keyPrice.setText(prefs.getString(getActivity().getString(R.string.pref_key_price), ""));
            budsPrice.setText(prefs.getString(getActivity().getString(R.string.pref_buds_price), ""));

            if (Utility.getDouble(prefs, getActivity().getString(R.string.pref_metal_diff), 0.0) > 0.0) {
                metalPriceImage.setBackgroundColor(0xff008504);
            } else {
                metalPriceImage.setBackgroundColor(0xff850000);
            }
            if (Utility.getDouble(prefs, getActivity().getString(R.string.pref_key_diff), 0.0) > 0.0) {
                keyPriceImage.setBackgroundColor(0xff008504);
            } else {
                keyPriceImage.setBackgroundColor(0xff850000);
            }
            if (Utility.getDouble(prefs, getActivity().getString(R.string.pref_buds_diff), 0.0) > 0) {
                budsPriceImage.setBackgroundColor(0xff008504);
            } else {
                budsPriceImage.setBackgroundColor(0xff850000);
            }

            if (prefs.getBoolean(getString(R.string.pref_dev_key_notification), true)) {
                showDeveloperKeyNotifications();
                prefs.edit().putBoolean(getString(R.string.pref_dev_key_notification), false).apply();
            }
        }
    }

    private void showDeveloperKeyNotifications() {
        //Quit the app if the download failed.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.message_developer_api)).setCancelable(false).
                setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getActivity(), SettingsActivity.class));
                    }
                }).
                setNegativeButton(getString(R.string.action_later), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
