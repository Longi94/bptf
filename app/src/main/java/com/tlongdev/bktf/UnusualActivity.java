package com.tlongdev.bktf;

import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.tlongdev.bktf.adapter.UnusualPricesCursorAdapter;
import com.tlongdev.bktf.data.PriceListContract;


public class UnusualActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String DEFINDEX_KEY = "defindex";
    public static final String ITEM_NAME_KEY = "item_name";

    public static final String LOG_TAG = UnusualActivity.class.getSimpleName();

    private static final int PRICE_LIST_LOADER = 0;

    private static final String[] PRICE_LIST_COLUMNS = {
            PriceListContract.PriceEntry.TABLE_NAME + "." + PriceListContract.PriceEntry._ID,
            PriceListContract.PriceEntry.COLUMN_ITEM_NAME,
            PriceListContract.PriceEntry.COLUMN_PRICE_INDEX,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_MAX,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_RAW,
            PriceListContract.PriceEntry.COLUMN_LAST_UPDATE,
            PriceListContract.PriceEntry.COLUMN_DIFFERENCE
    };

    public static final int COL_PRICE_LIST_ID = 0;
    public static final int COL_PRICE_LIST_NAME = 1;
    public static final int COL_PRICE_LIST_INDE = 2;
    public static final int COL_PRICE_LIST_CURR = 3;
    public static final int COL_PRICE_LIST_PRIC = 4;
    public static final int COL_PRICE_LIST_PMAX = 5;
    public static final int COL_PRICE_LIST_PRAW = 6;
    public static final int COL_PRICE_LIST_UPDA = 7;
    public static final int COL_PRICE_LIST_DIFF = 8;

    private GridView mGridView;
    private UnusualPricesCursorAdapter cursorAdapter;

    private int defindex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unusual);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff5787c5));



        defindex = getIntent().getIntExtra(DEFINDEX_KEY, -1);
        cursorAdapter = new UnusualPricesCursorAdapter(this, null, 0, defindex);

        getSupportActionBar().setTitle(getIntent().getStringExtra(ITEM_NAME_KEY));

        mGridView = (GridView) getWindow().getDecorView().findViewById(R.id.grid_view);
        mGridView.setAdapter(cursorAdapter);

        getSupportLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_unusual, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = PriceListContract.PriceEntry.TABLE_NAME+
                "." + PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                PriceListContract.PriceEntry.COLUMN_DEFINDEX + " = ?";

        String[] selectionArgs = {"5", "" + defindex};

        return new CursorLoader(
                this,
                PriceListContract.PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                selection,
                selectionArgs,
                PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_RAW + " DESC"
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
