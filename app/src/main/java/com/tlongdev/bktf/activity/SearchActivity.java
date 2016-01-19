package com.tlongdev.bktf.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.SearchAdapter;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.util.Utility;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = SearchActivity.class.getSimpleName();

    private static final int PRICE_LIST_LOADER = 0;
    private static final String QUERY_KEY = "query";

    //Indexes of the columns above
    public static final int COLUMN_DEFINDEX = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_QUALITY = 2;
    public static final int COLUMN_TRADABLE = 3;
    public static final int COLUMN_CRAFTABLE = 4;
    public static final int COLUMN_PRICE_INDEX = 5;
    public static final int COLUMN_CURRENCY = 6;
    public static final int COLUMN_PRICE = 7;
    public static final int COLUMN_PRICE_HIGH = 8;
    public static final int COLUMN_AUSTRALIUM = 9;

    //Selection
    private static final String sNameSearch = ItemSchemaEntry.TABLE_NAME +
            "." + ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ? AND NOT(" +
            PriceEntry.COLUMN_ITEM_QUALITY + " = 5 AND " +
            PriceEntry.COLUMN_PRICE_INDEX + " != 0)";

    private static final String sFilterSearch = ItemSchemaEntry.TABLE_NAME +
            "." + ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ? AND " +
            PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
            PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
            PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
            PriceEntry.COLUMN_AUSTRALIUM + " = ?";

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    //The search query string
    private String searchQuery;

    //Store the task so it can be stopped when the user modifies the query
    // TODO: 2015. 10. 25. might need to change it to submission based query
    private SearchForUserTask searchTask;

    //The adapter of the recyclerview
    private SearchAdapter adapter;

    //Cursor of the adapter
    private Cursor data;

    private boolean filterEnabled = false;
    private boolean filterTradable = true;
    private boolean filterCraftable = true;
    private boolean filterAustralium = false;
    private int filterQuality = Quality.UNIQUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        int columnCount = 1;
        if (dpWidth >= 720) {
            columnCount = 3;
        } else if (dpWidth >= 600) {
            columnCount = 2;
        }

        //Initialize the list
        adapter = new SearchAdapter(this, null);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        super.onResume();
        getSupportLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        //Setup the search widget
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setQueryHint("Items and users...");

        //Restart the loader every time the query string is changed
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                restartLoader(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                restartLoader(s);
                return true;
            }
        });

        //Auto expand the search view
        mSearchView.setIconified(false);

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sql = "SELECT " +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                ItemSchemaEntry.TABLE_NAME + "." + DatabaseContract.ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                " FROM " + PriceEntry.TABLE_NAME +
                " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                " WHERE ";

        if (filterEnabled) {
            sql += sFilterSearch;
        } else {
            sql += sNameSearch;
        }

        String query;
        String[] selectionArgs;

        //Build the query selection argument
        if (args != null) {

            if (filterEnabled) {
                selectionArgs = new String[]{null, String.valueOf(filterQuality),
                        filterTradable ? "1" : "0", filterCraftable ? "1" : "0",
                        filterAustralium ? "1" : "0"};

                query = args.getString(QUERY_KEY);
                if (query != null && query.length() > 0)
                    selectionArgs[0] = "%" + query + "%";
                else
                    selectionArgs[0] = "there is no such itme like thisasd"; //stupid
            } else {
                query = args.getString(QUERY_KEY);
                if (query != null && query.length() > 0)
                    selectionArgs = new String[]{"%" + query + "%"};
                else
                    selectionArgs = new String[]{"there is no such itme like thisasd"}; //stupid
            }
        } else {
            selectionArgs = new String[]{"there is no such itme like thisasd"};
        }

        Log.d(LOG_TAG, "selection: " + sNameSearch + ", arguments: " + Arrays.toString(selectionArgs));

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

        adapter.setUserFound(false, null);
        adapter.swapCursor(data, false);
        this.data = data;

        //Cancel the user search task when query is updated and running
        if (searchTask != null) {
            searchTask.cancel(true);
        }
        if (Utility.isNetworkAvailable(this) && searchQuery != null && !searchQuery.equals("")) {
            //Search for user
            searchTask = new SearchForUserTask(this);
            searchTask.execute(searchQuery);

            MatrixCursor extras = new MatrixCursor(new String[]{
                    PriceEntry.COLUMN_DEFINDEX,
                    ItemSchemaEntry.COLUMN_ITEM_NAME,
                    PriceEntry.COLUMN_ITEM_QUALITY,
                    PriceEntry.COLUMN_ITEM_TRADABLE,
                    PriceEntry.COLUMN_ITEM_CRAFTABLE,
                    PriceEntry.COLUMN_PRICE_INDEX,
                    PriceEntry.COLUMN_CURRENCY,
                    PriceEntry.COLUMN_PRICE,
                    PriceEntry.COLUMN_PRICE_HIGH,});
            extras.addRow(new String[]{null, null, null, null, null, null, null, null, null});
            Cursor[] cursors = {extras, data};
            Cursor extendedCursor = new MergeCursor(cursors);
            adapter.setLoading(true);
            adapter.swapCursor(extendedCursor, false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            filterEnabled = data.getBooleanExtra(SearchFilterActivity.EXTRA_ENABLED, false);
            filterQuality = data.getIntExtra(SearchFilterActivity.EXTRA_QUALITY, Quality.UNIQUE);
            filterTradable = data.getBooleanExtra(SearchFilterActivity.EXTRA_TRADABLE, true);
            filterCraftable = data.getBooleanExtra(SearchFilterActivity.EXTRA_CRAFTABLE, true);
            filterAustralium = data.getBooleanExtra(SearchFilterActivity.EXTRA_AUSTRALIUM, false);

            restartLoader(searchQuery);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.fab)
    public void onClick(View v) {
        Intent intent = new Intent(this, SearchFilterActivity.class);
        intent.putExtra(SearchFilterActivity.EXTRA_ENABLED, filterEnabled);
        intent.putExtra(SearchFilterActivity.EXTRA_TRADABLE, filterTradable);
        intent.putExtra(SearchFilterActivity.EXTRA_CRAFTABLE, filterCraftable);
        intent.putExtra(SearchFilterActivity.EXTRA_QUALITY, filterQuality);
        intent.putExtra(SearchFilterActivity.EXTRA_AUSTRALIUM, filterAustralium);
        startActivityForResult(intent, 0);
    }

    /**
     * Task that searches for a user of the given query string
     */
    private class SearchForUserTask extends AsyncTask<String, Void, String[]> {

        private final String KEY_API = "key";
        private final String KEY_VANITY_URL = "vanityurl";

        private final String KEY_STEAM_ID = "steamids";

        /**
         * The context
         */
        private Context mContext;

        /**
         * Constructor
         *
         * @param context the context the task is launched in
         */
        private SearchForUserTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected String[] doInBackground(String... params) {
            String[] userData = new String[3];
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection;
            BufferedReader reader;
            Uri uri;
            URL url;
            InputStream inputStream;
            StringBuffer buffer;
            String jsonString;
            String steamId;

            try {

                if (!Utility.isSteamId(params[0])) {
                    uri = Uri.parse(mContext.getString(R.string.steam_resolve_vanity_url)).buildUpon()
                            .appendQueryParameter(KEY_API, mContext.getString(R.string.steam_web_api_key))
                            .appendQueryParameter(KEY_VANITY_URL, params[0]).build();
                    url = new URL(uri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    inputStream = urlConnection.getInputStream();
                    buffer = new StringBuffer();

                    // Nothing to do.
                    if (inputStream != null) {
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                            // But it does make debugging a *lot* easier if you print out the completed
                            // buffer for debugging.
                            buffer.append(line);
                        }

                        if (buffer.length() == 0) {
                            // Stream was empty.  No point in parsing.
                            return null;
                        }
                        jsonString = buffer.toString();

                        steamId = userData[1] = Utility.parseSteamIdFromVanityJson(jsonString);

                        if (!Utility.isSteamId(steamId)) {
                            return null;
                        }
                    } else {
                        steamId = userData[1] = params[0];
                    }

                    uri = Uri.parse(mContext.getString(R.string.steam_get_player_summaries_url)).buildUpon()
                            .appendQueryParameter(KEY_API, mContext.getString(R.string.steam_web_api_key))
                            .appendQueryParameter(KEY_STEAM_ID, steamId)
                            .build();

                    url = new URL(uri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    inputStream = urlConnection.getInputStream();
                    buffer = new StringBuffer();

                    String line;
                    if (inputStream != null) {

                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        while ((line = reader.readLine()) != null) {
                            // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                            // But it does make debugging a *lot* easier if you print out the completed
                            // buffer for debugging.
                            buffer.append(line);
                        }

                        if (buffer.length() > 0) {
                            jsonString = userData[2] = buffer.toString();
                            userData[0] = Utility.parseUserNameFromJson(jsonString);
                            PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                                    .putString(mContext.getString(R.string.pref_search_avatar_url),
                                            Utility.parseAvatarUrlFromJson(jsonString)).apply();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

                mTracker.send(new HitBuilders.ExceptionBuilder()
                        .setDescription("Network exception:SearchActivity, Message: " + e.getMessage())
                        .setFatal(false)
                        .build());
            } catch (JSONException e) {
                e.printStackTrace();

                mTracker.send(new HitBuilders.ExceptionBuilder()
                        .setDescription("JSON exception:SearchActivity, Message: " + e.getMessage())
                        .setFatal(true)
                        .build());
            }

            return userData;
        }

        @Override
        protected void onPostExecute(final String[] s) {
            if (s != null && s[0] != null) {

                //Insert an extra row to the cursor
                // TODO: 2015. 10. 25. there is really no need to do this, all we need is a proper adapter
                MatrixCursor extras = new MatrixCursor(new String[]{
                        PriceEntry.COLUMN_DEFINDEX,
                        ItemSchemaEntry.COLUMN_ITEM_NAME,
                        PriceEntry.COLUMN_ITEM_QUALITY,
                        PriceEntry.COLUMN_ITEM_TRADABLE,
                        PriceEntry.COLUMN_ITEM_CRAFTABLE,
                        PriceEntry.COLUMN_PRICE_INDEX,
                        PriceEntry.COLUMN_CURRENCY,
                        PriceEntry.COLUMN_PRICE,
                        PriceEntry.COLUMN_PRICE_HIGH,});
                extras.addRow(new String[]{null, s[0], null, null, null, null, null, null, null});
                Cursor[] cursors = {extras, data};
                Cursor extendedCursor = new MergeCursor(cursors);
                adapter.setUserFound(true, s);
                adapter.swapCursor(extendedCursor, false);
            } else {
                adapter.swapCursor(data, false);
                adapter.setLoading(false);
            }
        }
    }

    /**
     * Restarts the cursor loader with the given query string
     *
     * @param query the query string of the search view.
     */
    public void restartLoader(String query) {
        searchQuery = query;
        Bundle args = new Bundle();
        args.putString(QUERY_KEY, query);
        getSupportLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
    }
}
