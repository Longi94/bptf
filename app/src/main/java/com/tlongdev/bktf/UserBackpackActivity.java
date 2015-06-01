package com.tlongdev.bktf;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.tlongdev.bktf.adapter.BackpackSectionHeaderAdapter;
import com.tlongdev.bktf.data.UserBackpackContract.UserBackpackEntry;

/**
 * Activity for viewing user backpacks.
 */
public class UserBackpackActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = UserBackpackActivity.class.getSimpleName();

    //Loader types
    public static final int LOADER_NORMAL = 0;
    public static final int LOADER_NEW = 1;

    //Indexes for the columns above
    public static final int COL_BACKPACK_ID = 0;
    public static final int COL_BACKPACK_DEFI = 1;
    public static final int COL_BACKPACK_QUAL = 2;
    public static final int COL_BACKPACK_CRFN = 3; // TODO unused
    public static final int COL_BACKPACK_TRAD = 4;
    public static final int COL_BACKPACK_CRAF = 5;
    public static final int COL_BACKPACK_INDE = 6;
    public static final int COL_BACKPACK_PAIN = 7;
    public static final int COL_BACKPACK_AUS = 8;

    //Keys for extre data in the intent
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_GUEST = "guest";

    //Query columns for local user
    private static final String[] QUERY_COLUMNS = {
            UserBackpackEntry.TABLE_NAME + "." +
                    UserBackpackEntry._ID,
            UserBackpackEntry.COLUMN_DEFINDEX,
            UserBackpackEntry.COLUMN_QUALITY,
            UserBackpackEntry.COLUMN_CRAFT_NUMBER,
            UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE,
            UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT,
            UserBackpackEntry.COLUMN_ITEM_INDEX,
            UserBackpackEntry.COLUMN_PAINT,
            UserBackpackEntry.COLUMN_AUSTRALIUM
    };

    //Query columns for s guest user
    private static final String[] QUERY_COLUMNS_GUEST = {
            UserBackpackEntry.TABLE_NAME_GUEST + "." +
                    UserBackpackEntry._ID,
            UserBackpackEntry.COLUMN_DEFINDEX,
            UserBackpackEntry.COLUMN_QUALITY,
            UserBackpackEntry.COLUMN_CRAFT_NUMBER,
            UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE,
            UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT,
            UserBackpackEntry.COLUMN_ITEM_INDEX,
            UserBackpackEntry.COLUMN_PAINT,
            UserBackpackEntry.COLUMN_AUSTRALIUM
    };
    //Adapters used for the listview
    private BackpackSectionHeaderAdapter adapter;

    //Boolean to decide which database table to load from
    private boolean isGuest;

    //Cursors for the adapter (one for items in the backpack, one for new and unplaced items).
    private Cursor normalCursor;
    private Cursor newCursor;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_backpack);

        //Show the home button as back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set the actionbar title to xyz's backpack
        getSupportActionBar().setTitle(getString(R.string.title_custom_backpack,
                getIntent().getStringExtra(EXTRA_NAME)));

        //Decide which table to load data from according to the extra data from the intent
        isGuest = getIntent().getBooleanExtra(EXTRA_GUEST, false);

        //The listview that displays all the items from the backpack
        RecyclerView listView = (RecyclerView) findViewById(R.id.list_view_backpack);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        listView.setHasFixedSize(true);

        // use a linear layout manager
        listView.setLayoutManager(new LinearLayoutManager(this));

        //Initialise adn set the adapter
        adapter = new BackpackSectionHeaderAdapter(this, isGuest);
        listView.setAdapter(adapter);

        //Start loading data from the database
        getSupportLoaderManager().initLoader(LOADER_NORMAL, null, this);
        getSupportLoaderManager().initLoader(LOADER_NEW, null, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Return to the previous activity when the back button (home) is pressed.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //All the variables need for querying
        //Sort items by their position
        String sortOrder = UserBackpackEntry.COLUMN_POSITION + " ASC";
        Uri uri;
        String[] columns;
        String selection;

        if (isGuest) {
            //This user was searched for. Load from the second table
            uri = UserBackpackEntry.CONTENT_URI_GUEST;
            columns = QUERY_COLUMNS_GUEST;
        } else {
            //This is the current user. Load from the main table
            uri = UserBackpackEntry.CONTENT_URI;
            columns = QUERY_COLUMNS;
        }

        switch (id) {
            case LOADER_NORMAL:
                //Load the items, that have a plce in the backpack.
                if (isGuest) {
                    selection = UserBackpackEntry.TABLE_NAME_GUEST + "." +
                            UserBackpackEntry.COLUMN_POSITION + " >= 1";
                } else {
                    selection = UserBackpackEntry.TABLE_NAME + "." +
                            UserBackpackEntry.COLUMN_POSITION + " >= 1";
                }
                break;
            case LOADER_NEW:
                //Load the new items, position is -1
                if (isGuest) {
                    selection = UserBackpackEntry.TABLE_NAME_GUEST + "." +
                            UserBackpackEntry.COLUMN_POSITION + " = -1";
                } else {
                    selection = UserBackpackEntry.TABLE_NAME + "." +
                            UserBackpackEntry.COLUMN_POSITION + " = -1";
                }
                break;
            default:
                return null;
        }

        //Load
        return new CursorLoader(
                this,
                uri,
                columns,
                selection,
                null,
                sortOrder
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Pass the newly created cursor to the adapter to show the items.
        switch (loader.getId()) {
            case LOADER_NORMAL:
                normalCursor = data;
                if (newCursor != null) {
                    adapter.swapCursor(normalCursor, newCursor);
                }
                break;
            case LOADER_NEW:
                newCursor = data;
                if (normalCursor != null) {
                    adapter.swapCursor(normalCursor, newCursor);
                }
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //This is never reached, but it's here just in case
        //Remove all data from the adapter
        adapter.swapCursor(null, null);
    }
}
