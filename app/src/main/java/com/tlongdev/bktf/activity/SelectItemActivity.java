package com.tlongdev.bktf.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.SelectItemAdapter;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SelectItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, TextWatcher, SelectItemAdapter.OnItemSelectedListener {

    public static String[] COLUMNS = {
            ItemSchemaEntry._ID,
            ItemSchemaEntry.COLUMN_DEFINDEX,
            ItemSchemaEntry.COLUMN_ITEM_NAME
    };

    public static final int COLUMN_DEFINDEX = 1;
    public static final int COLUMN_NAME = 2;

    private static final String QUERY_KEY = "query";
    public static final String EXTRA_DEFINDEX = "defindex";

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.item_name) EditText inputName;

    private SelectItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_item);
        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }

        setFinishOnTouchOutside(false);

        adapter = new SelectItemAdapter(this, null);
        adapter.setListener(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);

        inputName.addTextChangedListener(this);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("SelectItemActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String query;
        String[] selectionArgs;

        if (args != null) {
            query = args.getString(QUERY_KEY);
            if (query != null && query.length() > 0)
                selectionArgs = new String[]{"%" + query + "%"};
            else
                selectionArgs = new String[]{"there is no such itme like thisasd"}; //stupid
        } else {
            selectionArgs = new String[]{"there is no such itme like thisasd"};
        }

        return new CursorLoader(this,
                ItemSchemaEntry.CONTENT_URI,
                COLUMNS,
                ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ?",
                selectionArgs,
                ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data, false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
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
        restartLoader(s.toString());
    }

    /**
     * Restarts the cursor loader with the given query string
     *
     * @param query the query string of the search view.
     */
    public void restartLoader(String query) {
        Bundle args = new Bundle();
        args.putString(QUERY_KEY, query);
        getSupportLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public void onItemSelected(int defindex) {
        Intent result = new Intent();
        result.putExtra(EXTRA_DEFINDEX, defindex);
        setResult(RESULT_OK, result);
        finish();
    }
}
