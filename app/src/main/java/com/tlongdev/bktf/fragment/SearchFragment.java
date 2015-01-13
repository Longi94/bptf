package com.tlongdev.bktf.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.UserInfoActivity;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.adapter.SearchCursorAdapter;
import com.tlongdev.bktf.data.PriceListContract;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PRICE_LIST_LOADER = 0;
    private static final String QUERY_KEY = "query";

    private static final String[] PRICE_LIST_COLUMNS = {
            PriceListContract.PriceEntry.TABLE_NAME + "." + PriceListContract.PriceEntry._ID,
            PriceListContract.PriceEntry.COLUMN_DEFINDEX,
            PriceListContract.PriceEntry.COLUMN_ITEM_NAME,
            PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY,
            PriceListContract.PriceEntry.COLUMN_ITEM_TRADABLE,
            PriceListContract.PriceEntry.COLUMN_ITEM_CRAFTABLE,
            PriceListContract.PriceEntry.COLUMN_PRICE_INDEX,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE,
            PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_MAX,
    };

    public static final int COL_PRICE_LIST_ID = 0;
    public static final int COL_PRICE_LIST_DEFI = 1;
    public static final int COL_PRICE_LIST_NAME = 2;
    public static final int COL_PRICE_LIST_QUAL = 3;
    public static final int COL_PRICE_LIST_TRAD = 4;
    public static final int COL_PRICE_LIST_CRAF = 5;
    public static final int COL_PRICE_LIST_INDE = 6;
    public static final int COL_PRICE_LIST_CURR = 7;
    public static final int COL_PRICE_LIST_PRIC = 8;
    public static final int COL_PRICE_LIST_PMAX = 9;

    private static final String sNameSearch =
            PriceListContract.PriceEntry.TABLE_NAME+
                    "." + PriceListContract.PriceEntry.COLUMN_ITEM_NAME + " LIKE ? AND " +
                    PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY + " != 5";

    private ListView mListView;
    private SearchCursorAdapter cursorAdapter;

    private String searchQuery;
    private SearchForUserTask searchTask;

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
        mListView = (ListView)inflater.inflate(R.layout.fragment_search, container, false);

        cursorAdapter = new SearchCursorAdapter(
                getActivity(), null, 0
        );
        mListView.setAdapter(cursorAdapter);

        return mListView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String query;
        String[] selectionArgs;
        String selection = sNameSearch;

        if (args != null){
            query = args.getString(QUERY_KEY);
            if (query.length() > 0)
                selectionArgs = new String[] {"%" + query + "%"};
            else
                selectionArgs = new String[] {"%" + "there is no such itme like thisasd" + "%"};
        }
        else {
            selectionArgs = new String[] {"%" + "there is no such itme like thisasd" + "%"};
        }
        return new CursorLoader(
                getActivity(),
                PriceListContract.PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                selection,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.setUserFound(false);
        cursorAdapter.swapCursor(data);
        if (searchTask  != null){
            searchTask.cancel(true);
        }
        if (searchQuery != null)
        {
            searchTask = new SearchForUserTask(getActivity());
            searchTask.execute(searchQuery);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    public void restartLoader(String s) {
        searchQuery = s;
        Bundle args = new Bundle();
        args.putString(QUERY_KEY, s);
        getLoaderManager().restartLoader(PRICE_LIST_LOADER, args, this);
    }

    private class SearchForUserTask extends AsyncTask<String, Void, String[]>{

        private final String VANITY_BASE_URL = "http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/";
        private final String KEY_API = "key";
        private final String KEY_VANITY_URL = "vanityurl";

        private final String KEY_STEAM_ID = "steamids";

        private final String USER_SUMMARIES_BASE_URL = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/";

        private Bitmap bmp;
        private Drawable d;
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

                        if (bmp != null){
                            FileOutputStream fos = mContext.openFileOutput("avatar_search.png", Context.MODE_PRIVATE);
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                        }

                    }
                }
            } catch (IOException | JSONException e) {
                if (Utility.isDebugging())
                    e.printStackTrace();
            }

            return userData;
        }

        @Override
        protected void onPostExecute(final String[] s) {
            if (isAdded() && s != null && s[0] != null) {
                Cursor cursor = cursorAdapter.getCursor();
                MatrixCursor extras = new MatrixCursor(new String[] {
                        PriceListContract.PriceEntry._ID,
                        PriceListContract.PriceEntry.COLUMN_DEFINDEX,
                        PriceListContract.PriceEntry.COLUMN_ITEM_NAME,
                        PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY,
                        PriceListContract.PriceEntry.COLUMN_ITEM_TRADABLE,
                        PriceListContract.PriceEntry.COLUMN_ITEM_CRAFTABLE,
                        PriceListContract.PriceEntry.COLUMN_PRICE_INDEX,
                        PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_CURRENCY,
                        PriceListContract.PriceEntry.COLUMN_ITEM_PRICE,
                        PriceListContract.PriceEntry.COLUMN_ITEM_PRICE_MAX, });
                extras.addRow(new String[] { "-1", null, s[0], null, null, null, null, null, null, null});
                Cursor[] cursors = { extras, cursor };
                Cursor extendedCursor = new MergeCursor(cursors);
                cursorAdapter.setUserFound(true);
                cursorAdapter.swapCursor(extendedCursor);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0){
                            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                            intent.putExtra(UserInfoActivity.STEAM_ID_KEY, s[1]);
                            intent.putExtra(UserInfoActivity.JSON_USER_SUMMARIES_KEY, s[2]);
                            startActivity(intent);
                        }
                    }
                });
            }
        }

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
                if (Utility.isDebugging())
                    e.printStackTrace();
                return null;
            }
        }
    }
}
