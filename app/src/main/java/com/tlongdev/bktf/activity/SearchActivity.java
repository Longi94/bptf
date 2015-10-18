package com.tlongdev.bktf.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.adapter.SearchAdapter;
import com.tlongdev.bktf.data.DatabaseContract;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = SearchActivity.class.getSimpleName();

    private static final int PRICE_LIST_LOADER = 0;
    private static final String QUERY_KEY = "query";

    //The columns we need
    private static final String[] PRICE_LIST_COLUMNS = {
            DatabaseContract.PriceEntry.TABLE_NAME + "." + DatabaseContract.PriceEntry._ID,
            DatabaseContract.PriceEntry.COLUMN_DEFINDEX,
            DatabaseContract.PriceEntry.COLUMN_ITEM_NAME,
            DatabaseContract.PriceEntry.COLUMN_ITEM_QUALITY,
            DatabaseContract.PriceEntry.COLUMN_ITEM_TRADABLE,
            DatabaseContract.PriceEntry.COLUMN_ITEM_CRAFTABLE,
            DatabaseContract.PriceEntry.COLUMN_PRICE_INDEX,
            DatabaseContract.PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
            DatabaseContract.PriceEntry.COLUMN_ITEM_PRICE,
            DatabaseContract.PriceEntry.COLUMN_ITEM_PRICE_MAX,
            DatabaseContract.PriceEntry.COLUMN_AUSTRALIUM
    };

    //Indexes of the columns above
    public static final int COL_PRICE_LIST_DEFI = 1;
    public static final int COL_PRICE_LIST_NAME = 2;
    public static final int COL_PRICE_LIST_QUAL = 3;
    public static final int COL_PRICE_LIST_TRAD = 4;
    public static final int COL_PRICE_LIST_CRAF = 5;
    public static final int COL_PRICE_LIST_INDE = 6;
    public static final int COL_PRICE_LIST_CURR = 7;
    public static final int COL_PRICE_LIST_PRIC = 8;
    public static final int COL_PRICE_LIST_PMAX = 9;
    public static final int COL_AUSTRALIUM = 10;

    //Selection
    private static final String sNameSearch =
            DatabaseContract.PriceEntry.TABLE_NAME +
                    "." + DatabaseContract.PriceEntry.COLUMN_ITEM_NAME + " LIKE ? AND NOT(" +
                    DatabaseContract.PriceEntry.COLUMN_ITEM_QUALITY + " = 5 AND " +
                    DatabaseContract.PriceEntry.COLUMN_PRICE_INDEX + " != 0)";

    private String searchQuery;
    private SearchForUserTask searchTask;

    private RecyclerView mRecyclerView;
    private SearchAdapter adapter;
    private Cursor data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new SearchAdapter(this, null);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        //Setup the search widget
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setQueryHint("Items and users...");

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

        mSearchView.setIconified(false);

        return true;
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
                selectionArgs = new String[]{"there is no such itme like thisasd"};
        } else {
            selectionArgs = new String[]{"there is no such itme like thisasd"};
        }
        if (Utility.isDebugging(this)) {
            Log.d(LOG_TAG, "selection: " + sNameSearch + ", arguments: " + Arrays.toString(selectionArgs));
        }
        return new CursorLoader(
                this,
                DatabaseContract.PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                sNameSearch,
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
                    DatabaseContract.PriceEntry._ID,
                    DatabaseContract.PriceEntry.COLUMN_DEFINDEX,
                    DatabaseContract.PriceEntry.COLUMN_ITEM_NAME,
                    DatabaseContract.PriceEntry.COLUMN_ITEM_QUALITY,
                    DatabaseContract.PriceEntry.COLUMN_ITEM_TRADABLE,
                    DatabaseContract.PriceEntry.COLUMN_ITEM_CRAFTABLE,
                    DatabaseContract.PriceEntry.COLUMN_PRICE_INDEX,
                    DatabaseContract.PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
                    DatabaseContract.PriceEntry.COLUMN_ITEM_PRICE,
                    DatabaseContract.PriceEntry.COLUMN_ITEM_PRICE_MAX,});
            extras.addRow(new String[]{"-1", null, null, null, null, null, null, null, null, null});
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

    private class SearchForUserTask extends AsyncTask<String, Void, String[]> {

        private final String VANITY_BASE_URL = "http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/";
        private final String KEY_API = "key";
        private final String KEY_VANITY_URL = "vanityurl";

        private final String KEY_STEAM_ID = "steamids";

        private final String USER_SUMMARIES_BASE_URL = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/";

        private Bitmap bmp;
        private Context mContext;

        private SearchForUserTask(Context mContext) {
            this.mContext = mContext;
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
                    uri = Uri.parse(VANITY_BASE_URL).buildUpon()
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

                    uri = Uri.parse(USER_SUMMARIES_BASE_URL).buildUpon()
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
                            bmp = getBitmapFromURL(Utility.parseAvatarUrlFromJson(jsonString));
                        }

                        if (bmp != null) {
                            FileOutputStream fos = mContext.openFileOutput("avatar_search.png", Context.MODE_PRIVATE);
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                        }

                    }
                }
            } catch (IOException | JSONException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
            }

            return userData;
        }

        @Override
        protected void onPostExecute(final String[] s) {
            if (s != null && s[0] != null) {
                //Insert an extra row to the cursor
                MatrixCursor extras = new MatrixCursor(new String[]{
                        DatabaseContract.PriceEntry._ID,
                        DatabaseContract.PriceEntry.COLUMN_DEFINDEX,
                        DatabaseContract.PriceEntry.COLUMN_ITEM_NAME,
                        DatabaseContract.PriceEntry.COLUMN_ITEM_QUALITY,
                        DatabaseContract.PriceEntry.COLUMN_ITEM_TRADABLE,
                        DatabaseContract.PriceEntry.COLUMN_ITEM_CRAFTABLE,
                        DatabaseContract.PriceEntry.COLUMN_PRICE_INDEX,
                        DatabaseContract.PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
                        DatabaseContract.PriceEntry.COLUMN_ITEM_PRICE,
                        DatabaseContract.PriceEntry.COLUMN_ITEM_PRICE_MAX,});
                extras.addRow(new String[]{"-1", null, s[0], null, null, null, null, null, null, null});
                Cursor[] cursors = {extras, data};
                Cursor extendedCursor = new MergeCursor(cursors);
                adapter.setUserFound(true, s);
                adapter.swapCursor(extendedCursor, false);
            } else {
                adapter.swapCursor(data, false);
                adapter.setLoading(false);
            }
        }

        //Method for downloading image
        public Bitmap getBitmapFromURL(String link) {

            try {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                return BitmapFactory.decodeStream(input);

            } catch (IOException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                return null;
            }
        }
    }

    public void restartLoader(String query) {
        searchQuery = query;
        Bundle args = new Bundle();
        args.putString(QUERY_KEY, query);
        getSupportLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
    }
}
