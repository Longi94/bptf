package com.tlongdev.bktf.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.activity.MainActivity;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.adapter.RecentsAdapter;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.network.GetItemSchema;
import com.tlongdev.bktf.network.GetPriceList;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * recents fragment. Shows a list of all the prices orderd by the time of the price update.
 */
public class RecentsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, GetPriceList.OnPriceListListener,
        MainActivity.OnDrawerOpenedListener, GetItemSchema.OnItemSchemaListener {

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
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    /**
     * Loading indicator
     */
    @Bind(R.id.progress_bar) ProgressBar progressBar;

    /**
     * Adapter of the recycler view
     */
    private RecentsAdapter adapter;

    /**
     * the swipe refresh layout
     */
    @Bind(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * The recycler view
     */
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;

    /**
     * Only needed for manually expanding the toolbar
     */
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;

    /**
     * Views
     */
    @Bind(R.id.text_view_metal_price) TextView metalPrice;
    @Bind(R.id.text_view_key_price) TextView keyPrice;
    @Bind(R.id.text_view_buds_price) TextView budsPrice;
    @Bind(R.id.image_view_metal_price) View metalPriceImage;
    @Bind(R.id.image_view_key_price) View keyPriceImage;
    @Bind(R.id.image_view_buds_price) View budsPriceImage;

    //Dialog to indicate the download progress
    private ProgressDialog loadingDialog;

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

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) (getActivity()).getApplication();
        mTracker = application.getDefaultTracker();

        View rootView = inflater.inflate(R.layout.fragment_recents, container, false);

        ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        adapter = new RecentsAdapter(getActivity(), null);

        //Setup the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setVisibility(View.GONE);

        //Set up the swipe refresh layout (color and listener)
        mSwipeRefreshLayout.setColorSchemeColors(Utility.getColor(getActivity(), R.color.accent));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //Populate the toolbar header
        updateCurrencyHeader(PreferenceManager.getDefaultSharedPreferences(getActivity()));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("Latest Changes");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //Download whole database when the app is first opened.
        if (prefs.getBoolean(getString(R.string.pref_initial_load_v2), true)) {
            if (Utility.isNetworkAvailable(getActivity())) {
                GetPriceList task = new GetPriceList(getActivity(), false, true);
                task.setOnPriceListFetchListener(this);
                task.execute();

                //Show the progress dialog
                loadingDialog = ProgressDialog.show(getActivity(), null, "Downloading prices...", true);
                loadingDialog.setCancelable(false);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Request")
                        .setAction("Refresh")
                        .setLabel("Prices")
                        .build());
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
            if (System.currentTimeMillis() - prefs.getLong(getString(R.string.pref_last_price_list_update), 0) >= 3600000L
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

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Request")
                        .setAction("Refresh")
                        .setLabel("Prices")
                        .build());
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
                Utility.getRawPriceQueryString(getActivity()) + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DIFFERENCE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                " FROM " + PriceEntry.TABLE_NAME +
                " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
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

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("Refresh")
                    .setLabel("Prices")
                    .build());
        } else {
            Toast.makeText(getActivity(), "bptf: " + getString(R.string.error_no_network),
                    Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onPriceListFinished(int newItems) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (loadingDialog != null) {
            loadingDialog.dismiss();

            loadingDialog = ProgressDialog.show(getActivity(), null, "Downloading item schema...", true);
            loadingDialog.setCancelable(false);

            GetItemSchema task = new GetItemSchema(getActivity());
            task.setListener(this);
            task.execute();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("Refresh")
                    .setLabel("ItemSchema")
                    .build());
        } else {
            if (newItems > 0) {
                getLoaderManager().restartLoader(PRICE_LIST_LOADER, null, this);
            }


            if (System.currentTimeMillis() - prefs.getLong(getString(R.string.pref_last_item_schema_update), 0) >= 172800000L //2days
                    && Utility.isNetworkAvailable(getActivity())) {
                GetItemSchema task = new GetItemSchema(getActivity());
                task.setListener(this);
                task.execute();

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Request")
                        .setAction("Refresh")
                        .setLabel("ItemSchema")
                        .build());
            } else {
                if (isAdded()) {
                    //Stop animation
                    mSwipeRefreshLayout.setRefreshing(false);

                    updateCurrencyHeader(prefs);
                }
            }
        }

        //Get the shared preferences
        SharedPreferences.Editor editor = prefs.edit();

        //Save when the update finished
        editor.putLong(getActivity().getString(R.string.pref_last_price_list_update),
                System.currentTimeMillis());
        editor.putBoolean(getActivity().getString(R.string.pref_initial_load_v2), false);
        editor.apply();
    }

    @Override
    public void onPriceListUpdate(int max) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            if (loadingDialog.isIndeterminate()) {
                loadingDialog.dismiss();
                loadingDialog = new ProgressDialog(getActivity());
                loadingDialog.setIndeterminate(false);
                loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                loadingDialog.setMessage(getActivity().getString(R.string.message_database_create));
                loadingDialog.setMax(max);
                loadingDialog.setCancelable(false);
                loadingDialog.show();
            } else {
                loadingDialog.incrementProgressBy(1);
            }
        }
    }

    @Override
    public void onPriceListFailed(String errorMessage) {
        if (loadingDialog != null && loadingDialog.isShowing()) {

            AlertDialog.Builder builder;
            AlertDialog alertDialog;
            builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getActivity().getString(R.string.message_database_fail_network))
                    .setCancelable(false)
                    .setPositiveButton(getActivity().getString(R.string.action_close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Close app
                            (getActivity()).finish();
                        }
                    });
            alertDialog = builder.create();
            loadingDialog.dismiss();
            alertDialog.show();
        } else {
            Toast.makeText(getActivity(), "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSchemaFinished() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }

        getLoaderManager().restartLoader(PRICE_LIST_LOADER, null, this);

        //Update the header with currency prices
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //Get the shared preferences
        SharedPreferences.Editor editor = prefs.edit();

        //Save when the update finished
        editor.putLong(getActivity().getString(R.string.pref_last_item_schema_update),
                System.currentTimeMillis());
        editor.putBoolean(getActivity().getString(R.string.pref_initial_load_v2), false);
        editor.apply();

        if (isAdded()) {
            //Stop animation
            mSwipeRefreshLayout.setRefreshing(false);

            updateCurrencyHeader(prefs);
        }
    }

    @Override
    public void onItemSchemaUpdate(int max) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            if (loadingDialog.isIndeterminate()) {
                loadingDialog.dismiss();
                loadingDialog = new ProgressDialog(getActivity());
                loadingDialog.setIndeterminate(false);
                loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                loadingDialog.setMessage(getActivity().getString(R.string.message_item_schema_create));
                loadingDialog.setMax(max);
                loadingDialog.setCancelable(false);
                loadingDialog.show();
            } else {
                loadingDialog.incrementProgressBy(1);
            }
        }
    }

    @Override
    public void onItemSchemaFailed(String errorMessage) {
        if (loadingDialog != null && loadingDialog.isShowing()) {

            AlertDialog.Builder builder;
            AlertDialog alertDialog;
            builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getActivity().getString(R.string.message_database_fail_network))
                    .setCancelable(false)
                    .setPositiveButton(getActivity().getString(R.string.action_close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Close app
                            (getActivity()).finish();
                        }
                    });
            alertDialog = builder.create();
            loadingDialog.dismiss();
            alertDialog.show();
        } else {
            Toast.makeText(getActivity(), "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
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

    private void updateCurrencyHeader(SharedPreferences prefs) {
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
