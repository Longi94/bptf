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
import android.widget.ListView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.SearchCursorAdapter;
import com.tlongdev.bktf.data.PriceListContract;

public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PRICE_LIST_LOADER = 0;
    private static final String QUERY_KEY = "query";

    private static final String[] PRICE_LIST_COLUMNS = {
            PriceListContract.PriceEntry.TABLE_NAME + "." + PriceListContract.PriceEntry._ID,
            PriceListContract.PriceEntry.COLUMN_DEFINDEX,
            PriceListContract.PriceEntry.COLUMN_ITEM_NAME,
            PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY,
            PriceListContract.PriceEntry.COLUMN_ITEM_TRADABLE,
            PriceListContract.PriceEntry.COLUMN_ITEM_CRAFTABLE,
            PriceListContract.PriceEntry.COLUMN_PRICE_INDEX,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_MAX,
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

    private static final String sNameSearch =
            PriceListContract.PriceEntry.TABLE_NAME+
                    "." + PriceListContract.PriceEntry.COLUMN_ITEM_NAME + " LIKE ? AND " +
                    PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY + " != 5";

    ListView mListView;
    private SearchCursorAdapter cursorAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mListView = (ListView)inflater.inflate(R.layout.fragment_search, container, false);

        cursorAdapter = new SearchCursorAdapter(
                getActivity(), null, 0
        );
        mListView.setAdapter(cursorAdapter);

        return mListView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String query;
        String[] selectionArgs;
        String selection = sNameSearch;

        if (args != null){
            query = args.getString(QUERY_KEY);
            if (query.length() > 0)
                selectionArgs = new String[] {"%" + query + "%"};
            else
                selectionArgs = new String[] {"%" + "there is no such itme like thisasd" + "%"};
        }
        else {
            selectionArgs = new String[] {"%" + "there is no such itme like thisasd" + "%"};
        }
        return new CursorLoader(
                getActivity(),
                PriceListContract.PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                selection,
                selectionArgs,
                null
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

    public void restartLoader(String s) {
        Bundle args = new Bundle();
        args.putString(QUERY_KEY, s);
        getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
    }
}
