/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
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
import com.tlongdev.bktf.BuildConfig;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.SearchAdapter;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.util.Utility;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends BptfActivity implements LoaderManager.LoaderCallbacks<Cursor> {

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

    //The search query string
    private String searchQuery;

    //Store the task so it can be stopped when the user modifies the query
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
            String jsonString;
            String steamId = params[0];

            try {

                if (!Utility.isSteamId(steamId)) {
                    Uri uri = Uri.parse(mContext.getString(R.string.steam_resolve_vanity_url)).buildUpon()
                            .appendQueryParameter(KEY_API, BuildConfig.STEAM_WEB_API_KEY)
                            .appendQueryParameter(KEY_VANITY_URL, params[0]).build();
                    URL url = new URL(uri.toString());

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();

                    int statusCode = response.code();

                    if (statusCode >= 500) {
                        return null;
                    } else if (statusCode >= 400) {
                        return null;
                    }

                    jsonString = response.body().string();
                    steamId = userData[1] = Utility.parseSteamIdFromVanityJson(jsonString);

                    if (!Utility.isSteamId(steamId)) {
                        return null;
                    }
                }

                Uri uri = Uri.parse(mContext.getString(R.string.steam_get_player_summaries_url)).buildUpon()
                        .appendQueryParameter(KEY_API, BuildConfig.STEAM_WEB_API_KEY)
                        .appendQueryParameter(KEY_STEAM_ID, steamId)
                        .build();
                URL url = new URL(uri.toString());

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                int statusCode = response.code();

                if (statusCode >= 500) {
                    return null;
                } else if (statusCode >= 400) {
                    return null;
                }

                jsonString = userData[2] = response.body().string();
                userData[0] = Utility.parseUserNameFromJson(jsonString);
                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                        .putString(mContext.getString(R.string.pref_search_avatar_url),
                                Utility.parseAvatarUrlFromJson(jsonString)).apply();

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
        protected void onPostExecute(final String[] userData) {
            if (userData != null && userData[0] != null) {

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
                extras.addRow(new String[]{null, userData[0], null, null, null, null, null, null, null});
                Cursor[] cursors = {extras, data};
                Cursor extendedCursor = new MergeCursor(cursors);
                adapter.setUserFound(true, userData);
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