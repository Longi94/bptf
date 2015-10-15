package com.tlongdev.bktf.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.activity.UserInfoActivity;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.adapter.SearchCursorAdapter;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * Fragment for searching for prices.
 */
public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SearchFragment.class.getSimpleName();

    private static final int PRICE_LIST_LOADER = 0;
    private static final String QUERY_KEY = "query";

    //The columns we need
    private static final String[] PRICE_LIST_COLUMNS = {
            PriceEntry.TABLE_NAME + "." + PriceEntry._ID,
            PriceEntry.COLUMN_DEFINDEX,
            PriceEntry.COLUMN_ITEM_NAME,
            PriceEntry.COLUMN_ITEM_QUALITY,
            PriceEntry.COLUMN_ITEM_TRADABLE,
            PriceEntry.COLUMN_ITEM_CRAFTABLE,
            PriceEntry.COLUMN_PRICE_INDEX,
            PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
            PriceEntry.COLUMN_ITEM_PRICE,
            PriceEntry.COLUMN_ITEM_PRICE_MAX,
            PriceEntry.COLUMN_AUSTRALIUM
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
            PriceEntry.TABLE_NAME +
                    "." + PriceEntry.COLUMN_ITEM_NAME + " LIKE ? AND NOT(" +
                    PriceEntry.COLUMN_ITEM_QUALITY + " = 5 AND " +
                    PriceEntry.COLUMN_PRICE_INDEX + " != 0)";

    private ListView mListView;
    private SearchCursorAdapter cursorAdapter;

    private String searchQuery;
    private SearchForUserTask searchTask;

    private Cursor adapterCursor;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mListView = (ListView) inflater.inflate(R.layout.fragment_search, container, false);

        cursorAdapter = new SearchCursorAdapter(getActivity(), null, 0);
        mListView.setAdapter(cursorAdapter);

        return mListView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String query;
        String[] selectionArgs;

        if (args != null) {
            query = args.getString(QUERY_KEY);
            if (query.length() > 0)
                selectionArgs = new String[]{"%" + query + "%"};
            else
                selectionArgs = new String[]{"there is no such itme like thisasd"};
        } else {
            selectionArgs = new String[]{"there is no such itme like thisasd"};
        }
        if (Utility.isDebugging(getActivity())) {
            Log.d(LOG_TAG, "selection: " + sNameSearch + ", arguments: " + Arrays.toString(selectionArgs));
        }
        return new CursorLoader(
                getActivity(),
                PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                sNameSearch,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.setUserFound(false);
        cursorAdapter.swapCursor(data);
        adapterCursor = data;
        //Cancel the user search task when query is updated and running
        if (searchTask != null) {
            searchTask.cancel(true);
        }
        if (Utility.isNetworkAvailable(getActivity()) && searchQuery != null && !searchQuery.equals("")) {
            //Search for user
            searchTask = new SearchForUserTask(getActivity());
            searchTask.execute(searchQuery);

            MatrixCursor extras = new MatrixCursor(new String[]{
                    PriceEntry._ID,
                    PriceEntry.COLUMN_DEFINDEX,
                    PriceEntry.COLUMN_ITEM_NAME,
                    PriceEntry.COLUMN_ITEM_QUALITY,
                    PriceEntry.COLUMN_ITEM_TRADABLE,
                    PriceEntry.COLUMN_ITEM_CRAFTABLE,
                    PriceEntry.COLUMN_PRICE_INDEX,
                    PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
                    PriceEntry.COLUMN_ITEM_PRICE,
                    PriceEntry.COLUMN_ITEM_PRICE_MAX,});
            extras.addRow(new String[]{"-1", null, null, null, null, null, null, null, null, null});
            Cursor[] cursors = {extras, adapterCursor};
            Cursor extendedCursor = new MergeCursor(cursors);
            cursorAdapter.setLoading(true);
            cursorAdapter.swapCursor(extendedCursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
        adapterCursor = null;
    }

    public void restartLoader(String query) {
        searchQuery = query;
        Bundle args = new Bundle();
        args.putString(QUERY_KEY, query);
        getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
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
                if (Utility.isDebugging(getActivity()))
                    e.printStackTrace();
            }

            return userData;
        }

        @Override
        protected void onPostExecute(final String[] s) {
            if (isAdded()) {
                if (s != null && s[0] != null) {
                    //Insert an extra row to the cursor
                    MatrixCursor extras = new MatrixCursor(new String[]{
                            PriceEntry._ID,
                            PriceEntry.COLUMN_DEFINDEX,
                            PriceEntry.COLUMN_ITEM_NAME,
                            PriceEntry.COLUMN_ITEM_QUALITY,
                            PriceEntry.COLUMN_ITEM_TRADABLE,
                            PriceEntry.COLUMN_ITEM_CRAFTABLE,
                            PriceEntry.COLUMN_PRICE_INDEX,
                            PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
                            PriceEntry.COLUMN_ITEM_PRICE,
                            PriceEntry.COLUMN_ITEM_PRICE_MAX,});
                    extras.addRow(new String[]{"-1", null, s[0], null, null, null, null, null, null, null});
                    Cursor[] cursors = {extras, adapterCursor};
                    Cursor extendedCursor = new MergeCursor(cursors);
                    cursorAdapter.setUserFound(true);
                    cursorAdapter.swapCursor(extendedCursor);
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //Open user page if user clicks on the user name
                            if (position == 0) {
                                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                                intent.putExtra(UserInfoActivity.STEAM_ID_KEY, s[1]);
                                intent.putExtra(UserInfoActivity.JSON_USER_SUMMARIES_KEY, s[2]);
                                startActivity(intent);
                            }
                        }
                    });
                } else {
                    cursorAdapter.swapCursor(adapterCursor);
                    cursorAdapter.setLoading(false);
                }
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
                if (Utility.isDebugging(getActivity()))
                    e.printStackTrace();
                return null;
            }
        }
    }
}
