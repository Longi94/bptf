package com.tlongdev.bktf.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.UnusualListCursorAdapter;
import com.tlongdev.bktf.data.PriceListContract;

public class UnusualPriceListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = UnusualPriceListFragment.class.getSimpleName();

    private static final int PRICE_LIST_LOADER = 0;

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

    public UnusualPriceListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        getLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);

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
                "AVG(" + PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_RAW + ") DESC"
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
}
