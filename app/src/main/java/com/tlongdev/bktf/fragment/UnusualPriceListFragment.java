package com.tlongdev.bktf.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.UnusualListCursorAdapter;
import com.tlongdev.bktf.data.PriceListContract;

public class UnusualPriceListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = UnusualPriceListFragment.class.getSimpleName();

    private static final int PRICE_LIST_LOADER = 0;
    private static final String QUERY_KEY = "query";

    private static final String[] PRICE_LIST_COLUMNS = {
            PriceListContract.PriceEntry.TABLE_NAME + "." + PriceListContract.PriceEntry._ID,
            PriceListContract.PriceEntry.COLUMN_DEFINDEX,
            PriceListContract.PriceEntry.COLUMN_ITEM_NAME,
            "AVG(" + PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_RAW + ")"
    };

    public static final int COL_PRICE_LIST_ID = 0;
    public static final int COL_PRICE_LIST_DEFI = 1;
    public static final int COL_PRICE_LIST_NAME = 2;
    public static final int COL_PRICE_LIST_AVG_PRICE = 3;

    private GridView mGridView;
    private UnusualListCursorAdapter cursorAdapter;

    private int currentSort = 0;

    public UnusualPriceListFragment() {
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
        mGridView = (GridView) inflater.inflate(R.layout.fragment_unusual_price_list, container, false);

        cursorAdapter = new UnusualListCursorAdapter(getActivity(), null, 0);

        mGridView.setAdapter(cursorAdapter);
        return mGridView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        Bundle args = new Bundle();
        args.putString(QUERY_KEY, "AVG(" + PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_RAW + ") DESC");
        getLoaderManager().initLoader(PRICE_LIST_LOADER, args, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = PriceListContract.PriceEntry.TABLE_NAME+
                "." + PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                PriceListContract.PriceEntry.COLUMN_PRICE_INDEX + " != 0 GROUP BY " +
                PriceListContract.PriceEntry.COLUMN_DEFINDEX;

        String[] selectionArgs = {"5"};

        return new CursorLoader(
                getActivity(),
                PriceListContract.PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                selection,
                selectionArgs,
                args.getString(QUERY_KEY)
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_unusual, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sort_name && currentSort != 1){
            Bundle args = new Bundle();
            args.putString(QUERY_KEY, PriceListContract.PriceEntry.COLUMN_ITEM_NAME + " ASC");
            getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
            currentSort = 1;
        }
        else if (id == R.id.menu_sort_price && currentSort != 0){
            Bundle args = new Bundle();
            args.putString(QUERY_KEY, "AVG(" + PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_RAW + ") DESC");
            getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
            currentSort = 0;
        }
        return super.onOptionsItemSelected(item);
    }
}
