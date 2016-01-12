package com.tlongdev.bktf.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.UnusualAdapter;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Activity for showing unusual prices for specific effects or hats.
 */
public class UnusualActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, TextWatcher {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = UnusualActivity.class.getSimpleName();

    //Intent extra keys
    public static final String DEFINDEX_KEY = "defindex";
    public static final String NAME_KEY = "name";
    public static final String PRICE_INDEX_KEY = "index";

    //Index of the columns below
    public static final int COLUMN_DEFINDEX = 1;
    public static final int COLUMN_PRICE_INDEX = 2;
    public static final int COLUMN_CURRENCY = 3;
    public static final int COLUMN_PRICE = 4;
    public static final int COLUMN_PRICE_MAX = 5;
    public static final int COLUMN_LAST_UDPATE = 6; // TODO: 2015. 10. 25.
    public static final int COLUMN_DIFFRENCE = 7;
    public static final int COLUMN_NAME = 8;

    //Default loader id
    private static final int PRICE_LIST_LOADER = 0;

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    //Adapter for the gridView
    private UnusualAdapter adapter;

    //The defindex and index of the item to be viewed
    private int defindex;
    private int index;

    @Bind(R.id.search) EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unusual);
        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Get necessary data from intent
        Intent i = getIntent();
        defindex = i.getIntExtra(DEFINDEX_KEY, -1);
        index = i.getIntExtra(PRICE_INDEX_KEY, -1);

        //Set the action bar title to the current hat/effect name
        setTitle(i.getStringExtra(NAME_KEY));

        //The rootview is the gridview
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        //Initialize adapter
        adapter = new UnusualAdapter(this, null, UnusualAdapter.TYPE_SPECIFIC_HAT);
        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(this);
        searchInput.setHint(defindex != -1 ? "Effect" : "Name");

        //Start loading the data for the cursor
        getSupportLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Select only items with unusual quelity
        String selection = PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + " = ?";
        String[] selectionArgs;
        String filter = searchInput.getText().toString();

        String sql;

        //If defindex is -1, user is browsing by effects
        if (defindex != -1) {
            selection = selection + " AND " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = ? AND " +
                    UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME + " LIKE '%" + filter + "%'";
            selectionArgs = new String[]{"5", String.valueOf(defindex)};

            sql = "SELECT " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry._ID + "," +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                    PriceEntry.COLUMN_PRICE_INDEX + "," +
                    PriceEntry.COLUMN_CURRENCY + "," +
                    PriceEntry.COLUMN_PRICE + "," +
                    PriceEntry.COLUMN_PRICE_HIGH + "," +
                    PriceEntry.COLUMN_LAST_UPDATE + "," +
                    PriceEntry.COLUMN_DIFFERENCE + "," +
                    UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_NAME +
                    " FROM " + PriceEntry.TABLE_NAME +
                    " LEFT JOIN " + UnusualSchemaEntry.TABLE_NAME +
                    " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + " = " + UnusualSchemaEntry.TABLE_NAME + "." + UnusualSchemaEntry.COLUMN_ID +
                    " WHERE " + selection +
                    " ORDER BY " + Utility.getRawPriceQueryString(this) + " DESC";
        } else {
            selection = selection + " AND " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                    ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE '%" + filter + "%'";
            selectionArgs = new String[]{"5", String.valueOf(index)};

            sql = "SELECT " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry._ID + "," +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                    PriceEntry.COLUMN_PRICE_INDEX + "," +
                    PriceEntry.COLUMN_CURRENCY + "," +
                    PriceEntry.COLUMN_PRICE + "," +
                    PriceEntry.COLUMN_PRICE_HIGH + "," +
                    PriceEntry.COLUMN_LAST_UPDATE + "," +
                    PriceEntry.COLUMN_DIFFERENCE + "," +
                    ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME +
                    " FROM " + PriceEntry.TABLE_NAME +
                    " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                    " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                    " WHERE " + selection +
                    " ORDER BY " + Utility.getRawPriceQueryString(this) + " DESC";
        }

        //Load
        return new CursorLoader(
                this,
                DatabaseContract.RAW_QUERY_URI,
                null,
                sql,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //pass the cursor to the adapter to show data
        adapter.swapCursor(data, false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //This is never reached, but it's here just in case
        //Remove all data from the adapter
        adapter.swapCursor(null, false);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        getSupportLoaderManager().restartLoader(PRICE_LIST_LOADER, null, this);
    }
}
