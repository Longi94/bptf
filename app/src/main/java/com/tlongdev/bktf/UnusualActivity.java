package com.tlongdev.bktf;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.widget.GridView;

import com.tlongdev.bktf.adapter.UnusualPricesCursorAdapter;
import com.tlongdev.bktf.data.PriceListContract;

/**
 * Activity for showing unusual prices for specific effects or hats.
 */
public class UnusualActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = UnusualActivity.class.getSimpleName();

    //Intent extra keys
    public static final String DEFINDEX_KEY = "defindex";
    public static final String NAME_KEY = "name";
    public static final String PRICE_INDEX_KEY = "index";

    //Index of the columns below
    public static final int COL_PRICE_LIST_DEFI = 1;
    public static final int COL_PRICE_LIST_INDE = 2;
    public static final int COL_PRICE_LIST_CURR = 3;
    public static final int COL_PRICE_LIST_PRIC = 4;
    public static final int COL_PRICE_LIST_PMAX = 5;
    public static final int COL_PRICE_LIST_PRAW = 6;
    public static final int COL_PRICE_LIST_UPDA = 7;
    public static final int COL_PRICE_LIST_DIFF = 8;

    //Default loader id
    private static final int PRICE_LIST_LOADER = 0;

    //Columns to query from database
    private static final String[] PRICE_LIST_COLUMNS = {
            PriceListContract.PriceEntry.TABLE_NAME + "." + PriceListContract.PriceEntry._ID,
            PriceListContract.PriceEntry.COLUMN_DEFINDEX,
            PriceListContract.PriceEntry.COLUMN_PRICE_INDEX,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_MAX,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_RAW,
            PriceListContract.PriceEntry.COLUMN_LAST_UPDATE,
            PriceListContract.PriceEntry.COLUMN_DIFFERENCE
    };

    //Adapter for the gridView
    private UnusualPricesCursorAdapter cursorAdapter;

    //The defindex and index of the item to be viewed
    private int defindex;
    private int index;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unusual);

        //Get necessary data from intent
        Intent i = getIntent();
        defindex = i.getIntExtra(DEFINDEX_KEY, -1);
        index = i.getIntExtra(PRICE_INDEX_KEY, -1);

        //Set the action bar title to the current hat/effect name
        getSupportActionBar().setTitle(i.getStringExtra(NAME_KEY));

        //The rootview is the gridview
        GridView mGridView = (GridView) getWindow().getDecorView().findViewById(R.id.grid_view);

        //Initialize adapter
        cursorAdapter = new UnusualPricesCursorAdapter(this, null, 0, defindex);
        mGridView.setAdapter(cursorAdapter);

        //Start loading the data for the cursor
        getSupportLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Select only items with unusual quelity
        String selection = PriceListContract.PriceEntry.TABLE_NAME +
                "." + PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY + " = ?";
        String[] selectionArgs;

        //If defindex is -1, user is browsing by effects
        if (defindex != -1) {
            selection = selection + " AND " + PriceListContract.PriceEntry.COLUMN_DEFINDEX + " = ?";
            selectionArgs = new String[]{"5", "" + defindex};
        } else {
            selection = selection + " AND " + PriceListContract.PriceEntry.COLUMN_PRICE_INDEX + " = ?";
            selectionArgs = new String[]{"5", "" + index};
        }

        //Load
        return new CursorLoader(
                this,
                PriceListContract.PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                selection,
                selectionArgs,
                PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_RAW + " DESC" //order by raw price
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //pass the cursor to the adapter to show data
        cursorAdapter.swapCursor(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //This is never reached, but it's here just in case
        //Remove all data from the adapter
        cursorAdapter.swapCursor(null);
    }
}
