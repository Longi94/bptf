package com.tlongdev.bktf;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.tlongdev.bktf.adapter.BackpackSectionHeaderAdapter;
import com.tlongdev.bktf.data.ItemSchemaDbHelper;
import com.tlongdev.bktf.data.UserBackpackContract;


public class UserBackpackActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //Query columns
    private static final String[] QUERY_COLUMNS = {
            UserBackpackContract.UserBackpackEntry.TABLE_NAME + "." + UserBackpackContract.UserBackpackEntry._ID,
            UserBackpackContract.UserBackpackEntry.COLUMN_DEFINDEX,
            UserBackpackContract.UserBackpackEntry.COLUMN_QUALITY,
            UserBackpackContract.UserBackpackEntry.COLUMN_CRAFT_NUMBER,
            UserBackpackContract.UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE,
            UserBackpackContract.UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT,
            UserBackpackContract.UserBackpackEntry.COLUMN_ITEM_INDEX,
            UserBackpackContract.UserBackpackEntry.COLUMN_PAINT,
            UserBackpackContract.UserBackpackEntry.COLUMN_AUSTRALIUM
    };

    private static final String[] QUERY_COLUMNS_GUEST = {
            UserBackpackContract.UserBackpackEntry.TABLE_NAME_GUEST + "." + UserBackpackContract.UserBackpackEntry._ID,
            UserBackpackContract.UserBackpackEntry.COLUMN_DEFINDEX,
            UserBackpackContract.UserBackpackEntry.COLUMN_QUALITY,
            UserBackpackContract.UserBackpackEntry.COLUMN_CRAFT_NUMBER,
            UserBackpackContract.UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE,
            UserBackpackContract.UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT,
            UserBackpackContract.UserBackpackEntry.COLUMN_ITEM_INDEX,
            UserBackpackContract.UserBackpackEntry.COLUMN_PAINT,
            UserBackpackContract.UserBackpackEntry.COLUMN_AUSTRALIUM
    };

    //Indexes for the columns above
    public static final int COL_BACKPACK_ID = 0;
    public static final int COL_BACKPACK_DEFI = 1;
    public static final int COL_BACKPACK_QUAL = 2;
    public static final int COL_BACKPACK_CRFN = 3;
    public static final int COL_BACKPACK_TRAD = 4;
    public static final int COL_BACKPACK_CRAF = 5;
    public static final int COL_BACKPACK_INDE = 6;
    public static final int COL_BACKPACK_PAIN = 7;
    public static final int COL_BACKPACK_AUS = 8;

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_GUEST = "guest";

    private BackpackSectionHeaderAdapter adapter;

    private boolean isGuest;

    private ItemSchemaDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_backpack);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_NAME) + "'s backpack");

        isGuest = getIntent().getBooleanExtra(EXTRA_GUEST, false);

        RecyclerView listView = (RecyclerView) findViewById(R.id.list_view_backpack);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        listView.setHasFixedSize(true);

        // use a linear layout manager
        listView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BackpackSectionHeaderAdapter(this, isGuest);
        listView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = UserBackpackContract.UserBackpackEntry.COLUMN_POSITION + " ASC";
        Uri uri;
        String[] columns;
        String selection;

        if (isGuest){
            uri = UserBackpackContract.UserBackpackEntry.CONTENT_URI_GUEST;
            columns = QUERY_COLUMNS_GUEST;
            selection = UserBackpackContract.UserBackpackEntry.TABLE_NAME_GUEST + "." +
                    UserBackpackContract.UserBackpackEntry.COLUMN_POSITION + " >= 1";
        } else {
            uri = UserBackpackContract.UserBackpackEntry.CONTENT_URI;
            columns = QUERY_COLUMNS;
            selection = UserBackpackContract.UserBackpackEntry.TABLE_NAME + "." +
                    UserBackpackContract.UserBackpackEntry.COLUMN_POSITION + " >= 1";
        }

        return new CursorLoader(
                this,
                uri,
                columns,
                selection,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        adapter.notifyDataSetChanged();
    }
}
