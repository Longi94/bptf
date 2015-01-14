package com.tlongdev.bktf.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.adapter.PriceListCursorAdapter;
import com.tlongdev.bktf.data.PriceListContract.PriceEntry;
import com.tlongdev.bktf.quickreturn.AbsListViewQuickReturnAttacher;
import com.tlongdev.bktf.quickreturn.QuickReturnAttacher;
import com.tlongdev.bktf.quickreturn.widget.QuickReturnAdapter;
import com.tlongdev.bktf.quickreturn.widget.QuickReturnTargetView;
import com.tlongdev.bktf.task.FetchPriceList;

public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {

    private static final String LOG_TAG = HomeFragment.class.getSimpleName();

    private static final int PRICE_LIST_LOADER = 0;

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
            PriceEntry.COLUMN_ITEM_PRICE_RAW,
            PriceEntry.COLUMN_LAST_UPDATE,
            PriceEntry.COLUMN_DIFFERENCE
    };

    public static final int COL_PRICE_LIST_ID = 0;
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
    public static final int COL_PRICE_LIST_UPDA = 11;
    public static final int COL_PRICE_LIST_DIFF = 12;
    private static final String LIST_VIEW_POSITION_KEY = "position_index";
    private static final String LIST_VIEW_TOP_POSITION_KEY = "position_top";

    private ListView mListView;
    private LinearLayout header;

    private ProgressBar progressBar;

    private PriceListCursorAdapter cursorAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mPositionIndex = ListView.INVALID_POSITION;
    private int mPositionTop = 0;
    private int currentPage = 1;
    private int threshold = 1;
    private View footerView;

    public HomeFragment() {
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

        TextView metalPrice = (TextView) rootView.findViewById(R.id.text_view_metal_price);
        TextView keyPrice = (TextView) rootView.findViewById(R.id.text_view_key_price);
        TextView budsPrice = (TextView) rootView.findViewById(R.id.text_view_buds_price);

        ImageView metalPriceImage = (ImageView) rootView.findViewById(R.id.image_view_metal_price);
        ImageView keyPriceImage = (ImageView) rootView.findViewById(R.id.image_view_key_price);
        ImageView budsPriceImage = (ImageView) rootView.findViewById(R.id.image_view_buds_price);

        metalPrice.setText(prefs.getString(getString(R.string.pref_metal_price), ""));
        keyPrice.setText(prefs.getString(getString(R.string.pref_key_price), ""));
        budsPrice.setText(prefs.getString(getString(R.string.pref_buds_price), ""));

        if (prefs.getFloat(getString(R.string.pref_metal_diff), 0) > 0) {
            metalPriceImage.setBackgroundColor(0xff008504);
        } else {
            metalPriceImage.setBackgroundColor(0xff850000);
        }
        if (prefs.getFloat(getString(R.string.pref_key_diff), 0) > 0) {
            keyPriceImage.setBackgroundColor(0xff008504);
        } else {
            keyPriceImage.setBackgroundColor(0xff850000);
        }
        if (prefs.getFloat(getString(R.string.pref_buds_diff), 0) > 0) {
            budsPriceImage.setBackgroundColor(0xff008504);
        } else {
            budsPriceImage.setBackgroundColor(0xff850000);
        }

        cursorAdapter = new PriceListCursorAdapter(getActivity(), null, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_view_changes);
        footerView =  ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.list_view_footer, null, false);
        footerView.setVisibility(View.INVISIBLE);
        mListView.addFooterView(footerView);

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeColors(0xff5787c5);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.setProgressViewOffset(false,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -15, getResources().getDisplayMetrics()),
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics()));

        header = (LinearLayout) rootView.findViewById(R.id.list_changes_header);

        mListView.setAdapter(new QuickReturnAdapter(cursorAdapter));

        AbsListViewQuickReturnAttacher quickReturnAttacher =
                (AbsListViewQuickReturnAttacher)QuickReturnAttacher.forView(mListView);
        quickReturnAttacher.addTargetView(header, QuickReturnTargetView.POSITION_TOP,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, getResources().getDisplayMetrics()));
        quickReturnAttacher.addOnScrollListener(this);

        if(savedInstanceState != null && savedInstanceState.containsKey(LIST_VIEW_POSITION_KEY) &&
                savedInstanceState.containsKey(LIST_VIEW_TOP_POSITION_KEY)) {
            mPositionIndex = savedInstanceState.getInt(LIST_VIEW_POSITION_KEY);
            mPositionTop = savedInstanceState.getInt(LIST_VIEW_TOP_POSITION_KEY);
        }

        progressBar = (ProgressBar)rootView.findViewById(R.id.progress_bar);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs.getBoolean(getString(R.string.pref_initial_load), true)){
            if (Utility.isNetworkAvailable(getActivity())) {
                new FetchPriceList(getActivity(), false, false, null, header).execute(getResources().getString(R.string.backpack_tf_api_key));
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Failed to download database. Check your internet connection and try again.").setCancelable(false).
                        setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
        else if (prefs.getBoolean(getString(R.string.pref_auto_sync), false) &&
                System.currentTimeMillis() - prefs.getLong(getString(R.string.pref_last_price_list_update), 0) >= 3600000L
                && Utility.isNetworkAvailable(getActivity())) {
            new FetchPriceList(getActivity(), true, false, mSwipeRefreshLayout, header).execute(getResources()
                    .getString(R.string.backpack_tf_api_key));
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPositionIndex != ListView.INVALID_POSITION) {
            // save index and top position
            int index = mListView.getFirstVisiblePosition();
            View v = mListView.getChildAt(0);
            int top = (v == null) ? 0 : v.getTop();

            outState.putInt(LIST_VIEW_POSITION_KEY, index);
            outState.putInt(LIST_VIEW_TOP_POSITION_KEY, top);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = PriceEntry.COLUMN_LAST_UPDATE + " DESC LIMIT " + currentPage * 10;

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
        footerView.setVisibility(View.INVISIBLE);
        /*if (mPositionIndex != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.setSelectionFromTop(mPositionIndex, mPositionTop);
        }*/
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onRefresh() {
        if (Utility.isNetworkAvailable(getActivity())) {
            new FetchPriceList(getActivity(), true, true, mSwipeRefreshLayout, header)
                    .execute(getResources().getString(R.string.backpack_tf_api_key));
        } else {
            Toast.makeText(getActivity(), "bptf: no connection", Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if (listView.getLastVisiblePosition() >= listView.getCount() - 1 - threshold) {
                currentPage++;
                //load more list items:
                footerView.setVisibility(View.VISIBLE);
                getLoaderManager().restartLoader(PRICE_LIST_LOADER, null, this);
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
