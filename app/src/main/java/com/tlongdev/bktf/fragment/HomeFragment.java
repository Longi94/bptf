package com.tlongdev.bktf.fragment;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.PriceListCursorAdapter;
import com.tlongdev.bktf.data.PriceListContract.PriceEntry;
import com.tlongdev.bktf.quickreturn.QuickReturnAttacher;
import com.tlongdev.bktf.quickreturn.widget.QuickReturnAdapter;
import com.tlongdev.bktf.quickreturn.widget.QuickReturnTargetView;
import com.tlongdev.bktf.task.UpdatePriceList;

public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener{

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
    private QuickReturnAttacher quickReturnAttacher;
    private LinearLayout quickReturnTarget;
    private TextView metalPrice;
    private TextView keyPrice;
    private TextView budsPrice;


    private PriceListCursorAdapter cursorAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mPositionIndex = ListView.INVALID_POSITION;
    private int mPositionTop = 0;

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

        metalPrice = (TextView)rootView.findViewById(R.id.text_view_metal_price);
        keyPrice = (TextView)rootView.findViewById(R.id.text_view_key_price);
        budsPrice = (TextView)rootView.findViewById(R.id.text_view_buds_price);

        metalPrice.setText("$" + prefs.getFloat(getString(R.string.pref_metal_price), 0));
        keyPrice.setText("" + prefs.getFloat(getString(R.string.pref_key_price), 0) + " ref");
        budsPrice.setText("" + prefs.getFloat(getString(R.string.pref_buds_price), 0) + " keys");

        // The SimpleCursorAdapter will take data from the database through the
        // Loader and use it to populate the ListView it's attached to.
        cursorAdapter = new PriceListCursorAdapter(
                getActivity(), null, 0
        );

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_view_changes);

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeColors(0xff5787c5);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("debug_header_key", false)){
            mListView.setAdapter(cursorAdapter);
        } else {
            mListView.setAdapter(cursorAdapter);
            quickReturnTarget = (LinearLayout) rootView.findViewById(R.id.list_changes_header);

            mListView.setAdapter(new QuickReturnAdapter(cursorAdapter));

            quickReturnAttacher = QuickReturnAttacher.forView(mListView);
            int offset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, getResources().getDisplayMetrics());
            quickReturnAttacher.addTargetView(quickReturnTarget, QuickReturnTargetView.POSITION_TOP, offset);
            mSwipeRefreshLayout.setProgressViewOffset(false, (int)(offset * -1.0/2.0), (int) (offset * 3.0 / 2.0));
        }

        if(savedInstanceState != null && savedInstanceState.containsKey(LIST_VIEW_POSITION_KEY) &&
                savedInstanceState.containsKey(LIST_VIEW_TOP_POSITION_KEY)) {
            mPositionIndex = savedInstanceState.getInt(LIST_VIEW_POSITION_KEY);
            mPositionTop = savedInstanceState.getInt(LIST_VIEW_TOP_POSITION_KEY);
        }

        return rootView;
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

        String sortOrder = PriceEntry.COLUMN_LAST_UPDATE + " DESC";

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
        new UpdatePriceList(getActivity(), mSwipeRefreshLayout)
                .execute(getResources().getString(R.string.backpack_tf_api_key));
    }

}
