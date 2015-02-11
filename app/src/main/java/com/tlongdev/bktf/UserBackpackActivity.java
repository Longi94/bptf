package com.tlongdev.bktf;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ListView;

import com.tlongdev.bktf.adapter.SimpleBackpackAdapter;
import com.tlongdev.bktf.data.UserBackpackContract;


public class UserBackpackActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //Query columns
    private static final String[] QUERY_COLUMNS = {
            UserBackpackContract.UserBackpackEntry.TABLE_NAME + "." + UserBackpackContract.UserBackpackEntry._ID,
            UserBackpackContract.UserBackpackEntry.COLUMN_DEFINDEX,
            UserBackpackContract.UserBackpackEntry.COLUMN_POSITION,
            UserBackpackContract.UserBackpackEntry.COLUMN_QUALITY,
            UserBackpackContract.UserBackpackEntry.COLUMN_CRAFT_NUMBER,
            UserBackpackContract.UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE,
            UserBackpackContract.UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT,
            UserBackpackContract.UserBackpackEntry.COLUMN_ITEM_INDEX,
            UserBackpackContract.UserBackpackEntry.COLUMN_PAINT
    };

    private static final String[] QUERY_COLUMNS_GUEST = {
            UserBackpackContract.UserBackpackEntry.TABLE_NAME_GUEST + "." + UserBackpackContract.UserBackpackEntry._ID,
            UserBackpackContract.UserBackpackEntry.COLUMN_DEFINDEX,
            UserBackpackContract.UserBackpackEntry.COLUMN_POSITION,
            UserBackpackContract.UserBackpackEntry.COLUMN_QUALITY,
            UserBackpackContract.UserBackpackEntry.COLUMN_CRAFT_NUMBER,
            UserBackpackContract.UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE,
            UserBackpackContract.UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT,
            UserBackpackContract.UserBackpackEntry.COLUMN_ITEM_INDEX,
            UserBackpackContract.UserBackpackEntry.COLUMN_PAINT
    };

    //Indexes for the columns above
    public static final int COL_BACKPACK_DEFI = 1;
    public static final int COL_BACKPACK_POS = 2;
    public static final int COL_BACKPACK_QUAL = 3;
    public static final int COL_BACKPACK_CRFN = 4;
    public static final int COL_BACKPACK_TRAD = 5;
    public static final int COL_BACKPACK_CRAF = 6;
    public static final int COL_BACKPACK_INDE = 7;
    public static final int COL_BACKPACK_PAIN = 8;

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_GUEST = "guest";

    private SimpleBackpackAdapter adapter;
    private ListView listView;

    private boolean isGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_backpack);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_NAME));

        isGuest = getIntent().getBooleanExtra(EXTRA_GUEST, false);

        listView = (ListView)findViewById(R.id.list_view);
        adapter = new SimpleBackpackAdapter(this, null, 0);
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

        if (isGuest){
            uri = UserBackpackContract.UserBackpackEntry.CONTENT_URI_GUEST;
            columns = QUERY_COLUMNS_GUEST;
        } else {
            uri = UserBackpackContract.UserBackpackEntry.CONTENT_URI;
            columns = QUERY_COLUMNS;
        }

        return new CursorLoader(
                this,
                uri,
                columns,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
