package com.tlongdev.bktf.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.adapter.PriceListCursorAdapter;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.network.FetchPriceList;
import com.tlongdev.bktf.quickreturn.AbsListViewQuickReturnAttacher;
import com.tlongdev.bktf.quickreturn.QuickReturnAttacher;
import com.tlongdev.bktf.quickreturn.widget.QuickReturnAdapter;
import com.tlongdev.bktf.quickreturn.widget.QuickReturnTargetView;

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

    private Context mContext;

    private ProgressBar progressBar;

    private PriceListCursorAdapter cursorAdapter;

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

        mContext = getActivity();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

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

        cursorAdapter = new PriceListCursorAdapter(mContext, null, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView mListView = (ListView) rootView.findViewById(R.id.list_view_changes);

        //Set up the swipe refresh layout (color and listener)
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeColors(0xff5787c5);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //Offset, so the header isn't in the way
        mSwipeRefreshLayout.setProgressViewOffset(false,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -15, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics()));

        LinearLayout header = (LinearLayout) rootView.findViewById(R.id.list_changes_header);

        //Set up quick return
        mListView.setAdapter(new QuickReturnAdapter(cursorAdapter));
        AbsListViewQuickReturnAttacher quickReturnAttacher =
                (AbsListViewQuickReturnAttacher) QuickReturnAttacher.forView(mListView);
        quickReturnAttacher.addTargetView(header, QuickReturnTargetView.POSITION_TOP,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, getResources().getDisplayMetrics()));

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        //Download whole database when the app is first opened.
        if (prefs.getBoolean(getString(R.string.pref_initial_load), true)) {
            if (Utility.isNetworkAvailable(mContext)) {
                FetchPriceList task = new FetchPriceList(mContext, false, false);
                task.setOnPriceListFetchListener(this);
                task.execute();
            } else {
                //Quit the app if the download failed.
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(getString(R.string.message_database_fail_network)).setCancelable(false).
                        setPositiveButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity) mContext).finish();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        } else {

            //Update database if the last update happened more than an hour ago
            if (prefs.getBoolean(getString(R.string.pref_auto_sync), false) &&
                    System.currentTimeMillis() - prefs.getLong(getString(R.string.pref_last_price_list_update), 0) >= 3600000L
                    && Utility.isNetworkAvailable(mContext)) {
                FetchPriceList task = new FetchPriceList(mContext, true, false);
                task.setOnPriceListFetchListener(this);
                task.execute();
                mSwipeRefreshLayout.setRefreshing(true);
            }

            if (prefs.getBoolean(getString(R.string.pref_promo), true)) {
                prefs.edit().putBoolean(getString(R.string.pref_promo), false).apply();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Beta is now available!")
                        .setMessage("You can now participate in beta testing. Sign up to get the latest updates and features!")
                        .setPositiveButton("More info", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://steamcommunity.com/groups/bptfandroid")));
                            }
                        })
                        .setNegativeButton("Close", null)
                        .show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = PriceEntry.COLUMN_LAST_UPDATE + " DESC";

        PRICE_LIST_COLUMNS[COL_PRICE_LIST_PRAW] = Utility.getRawPriceQueryString(mContext);

        return new CursorLoader(
                mContext,
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
        if (Utility.isNetworkAvailable(mContext)) {
            FetchPriceList task = new FetchPriceList(mContext, true, true);
            task.setOnPriceListFetchListener(this);
            task.execute();
        } else {
            Toast.makeText(mContext, "bptf: " + getString(R.string.error_no_network),
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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

            metalPrice.setText(prefs.getString(mContext.getString(R.string.pref_metal_price), ""));
            keyPrice.setText(prefs.getString(mContext.getString(R.string.pref_key_price), ""));
            budsPrice.setText(prefs.getString(mContext.getString(R.string.pref_buds_price), ""));

            if (Utility.getDouble(prefs, mContext.getString(R.string.pref_metal_diff), 0.0) > 0.0) {
                metalPriceImage.setBackgroundColor(0xff008504);
            } else {
                metalPriceImage.setBackgroundColor(0xff850000);
            }
            if (Utility.getDouble(prefs, mContext.getString(R.string.pref_key_diff), 0.0) > 0.0) {
                keyPriceImage.setBackgroundColor(0xff008504);
            } else {
                keyPriceImage.setBackgroundColor(0xff850000);
            }
            if (Utility.getDouble(prefs, mContext.getString(R.string.pref_buds_diff), 0.0) > 0) {
                budsPriceImage.setBackgroundColor(0xff008504);
            } else {
                budsPriceImage.setBackgroundColor(0xff850000);
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (prefs.getBoolean(mContext.getString(R.string.pref_promo), true)) {
            prefs.edit().putBoolean(getString(R.string.pref_promo), false).apply();
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Beta is now available!")
                    .setMessage("You can now participate in beta testing. Sign up to get the latest updates and features!")
                    .setPositiveButton("More info", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://steamcommunity.com/groups/bptfandroid")));
                        }
                    })
                    .setNegativeButton("Close", null)
                    .show();
        }
    }
}
