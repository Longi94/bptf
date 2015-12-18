package com.tlongdev.bktf.network;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.android.gms.analytics.HitBuilders;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.OriginEntry;
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class GetItemSchema extends AsyncTask<Void, Void, Integer> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = GetItemSchema.class.getSimpleName();

    private Context mContext;

    private OnItemSchemaListener listener;
    private String errorMessage;

    public GetItemSchema(Context context) {
        this.mContext = context;
    }

    @Override
    protected Integer doInBackground(Void... params) {

        HttpURLConnection urlConnection = null;

        try {
            //The prices api and input keys
            final String BASE_URL = mContext.getString(R.string.tlongdev_item_schema);

            //Initialize the URL
            URL url = new URL(BASE_URL);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            //Get the input stream
            InputStream inputStream = response.body().byteStream();

            if (inputStream == null) {
                // Stream was empty. Nothing to do.
                return null;
            }

            return parseJson(inputStream);
        } catch (JsonParseException e) {
            //There was a network error
            errorMessage = mContext.getString(R.string.error_data_parse);
            e.printStackTrace();

            ((BptfApplication) mContext.getApplicationContext()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("JSON exception:GetItemSchema, Message: " + e.getMessage())
                    .setFatal(true)
                    .build());

        } catch (IOException e) {
            //There was a network error
            errorMessage = mContext.getString(R.string.error_network);
            e.printStackTrace();

            ((BptfApplication) mContext.getApplicationContext()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Network exception:GetItemSchema, Message: " + e.getMessage())
                    .setFatal(false)
                    .build());
        } finally {
            //Close the connection
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    private int parseJson(InputStream inputStream) throws IOException {

        //All the JSON keys needed to parse
        final String KEY_SUCCESS = "success";
        final String KEY_MESSAGE = "message";
        final String KEY_ITEMS = "items";
        final String KEY_ORIGINS = "origins";
        final String KEY_PARTICLES = "particle_names";
        final String KEY_DEFINDEX = "defindex";
        final String KEY_NAME = "name";
        final String KEY_DESCRIPTION = "description";
        final String KEY_TYPE_NAME = "type_name";
        final String KEY_PROPER_NAME = "proper_name";
        final String KEY_ID = "id";

        //Create a parser from the input stream for fast parsing and low impact on memory
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(inputStream);

        //Not a JSON if it doesn't start with START OBJECT
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            return -1;
        }

        //Iterator that will iterate through the items
        Vector<ContentValues> cVVectorItems = new Vector<>();
        Vector<ContentValues> cVVectorOrigins = new Vector<>();
        Vector<ContentValues> cVVectorParticles = new Vector<>();

        JsonToken token;
        while ((token = parser.nextToken()) != JsonToken.END_OBJECT) {

            //success object
            if (parser.getCurrentName().equals(KEY_SUCCESS)) {
                parser.nextToken();
                if (parser.getIntValue() == 0) {

                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        if (parser.getCurrentName().equals(KEY_MESSAGE)) {
                            errorMessage = parser.getText();
                                Log.e(LOG_TAG, errorMessage);
                        }
                    }
                    parser.close();
                    return -1;
                }
            }

            //success object
            if (parser.getCurrentName().equals(KEY_ITEMS)) {

                parser.nextToken();

                //Keep iterating while the array hasn't ended
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    //Initial values
                    int defindex = 0;
                    String name = null;
                    String description = null;
                    String typeName = null;
                    int properName = 0;

                    //Parse an attribute and get the value of it
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        switch (parser.getCurrentName()) {
                            case KEY_DEFINDEX:
                                parser.nextToken();
                                defindex = parser.getIntValue();
                                break;
                            case KEY_NAME:
                                parser.nextToken();
                                name = parser.getText();
                                break;
                            case KEY_DESCRIPTION:
                                parser.nextToken();
                                description = parser.getText();
                                if (description != null && description.equals("null")) {
                                    description = null;
                                }
                                break;
                            case KEY_TYPE_NAME:
                                parser.nextToken();
                                typeName = parser.getText();
                                break;
                            case KEY_PROPER_NAME:
                                parser.nextToken();
                                properName = parser.getIntValue();
                                break;
                        }
                    }

                    //The DV that will contain all the data
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(ItemSchemaEntry.COLUMN_DEFINDEX, defindex);
                    itemValues.put(ItemSchemaEntry.COLUMN_ITEM_NAME, name);
                    itemValues.put(ItemSchemaEntry.COLUMN_TYPE_NAME, typeName);
                    itemValues.put(ItemSchemaEntry.COLUMN_DESCRIPTION, description);
                    itemValues.put(ItemSchemaEntry.COLUMN_PROPER_NAME, properName);

                    //Add the price to the CV vector
                    cVVectorItems.add(itemValues);
                }

                publishProgress();
            }

            //success object
            if (parser.getCurrentName().equals(KEY_ORIGINS)) {

                parser.nextToken();

                //Keep iterating while the array hasn't ended
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    //Initial values
                    int id = 0;
                    String name = null;

                    //Parse an attribute and get the value of it
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        switch (parser.getCurrentName()) {
                            case KEY_ID:
                                parser.nextToken();
                                id = parser.getIntValue();
                                break;
                            case KEY_NAME:
                                parser.nextToken();
                                name = parser.getText();
                                break;
                        }
                    }

                    //The DV that will contain all the data
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(OriginEntry.COLUMN_ID, id);
                    itemValues.put(OriginEntry.COLUMN_NAME, name);

                    //Add the price to the CV vector
                    cVVectorOrigins.add(itemValues);
                }

                publishProgress();
            }

            //success object
            if (parser.getCurrentName().equals(KEY_PARTICLES)) {

                parser.nextToken();

                //Keep iterating while the array hasn't ended
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    //Initial values
                    int id = 0;
                    String name = null;

                    //Parse an attribute and get the value of it
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        switch (parser.getCurrentName()) {
                            case KEY_ID:
                                parser.nextToken();
                                id = parser.getIntValue();
                                break;
                            case KEY_NAME:
                                parser.nextToken();
                                name = parser.getText();
                                break;
                        }
                    }

                    //The DV that will contain all the data
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(UnusualSchemaEntry.COLUMN_ID, id);
                    itemValues.put(UnusualSchemaEntry.COLUMN_NAME, name);

                    //Add the price to the CV vector
                    cVVectorParticles.add(itemValues);
                }

                publishProgress();
            }
        }

        parser.close();

        if (cVVectorItems.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVectorItems.size()];
            cVVectorItems.toArray(cvArray);
            //Insert all the data into the database
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(ItemSchemaEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows into item_schema");
        }

        if (cVVectorOrigins.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVectorOrigins.size()];
            cVVectorOrigins.toArray(cvArray);
            //Insert all the data into the database
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(OriginEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows into origins");
        }

        if (cVVectorParticles.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVectorParticles.size()];
            cVVectorParticles.toArray(cvArray);
            //Insert all the data into the database
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(UnusualSchemaEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows into unusual_schema");
        }

        return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if (listener != null) {
            switch (integer) {
                case 0:
                    listener.onItemSchemaFinished();
                    break;
                case -1:
                    listener.onItemSchemaFailed(errorMessage);
                    break;
                default:
                    listener.onItemSchemaFailed(errorMessage);
                    break;
            }
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        if (listener != null) {
            listener.onItemSchemaUpdate(3);
        }
    }

    /**
     * Sets the listener, which will be notified of the results
     *
     * @param listener the listener
     */
    public void setListener(OnItemSchemaListener listener) {
        this.listener = listener;
    }

    /**
     * Listener interface
     */
    public interface OnItemSchemaListener {

        void onItemSchemaFinished();

        void onItemSchemaUpdate(int max);

        void onItemSchemaFailed(String errorMessage);
    }
}
