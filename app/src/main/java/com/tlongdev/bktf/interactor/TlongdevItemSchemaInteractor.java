package com.tlongdev.bktf.interactor;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.DecoratedWeaponEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.OriginEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;
import com.tlongdev.bktf.flatbuffers.itemschema.DecoratedWeapon;
import com.tlongdev.bktf.flatbuffers.itemschema.Item;
import com.tlongdev.bktf.flatbuffers.itemschema.ItemSchema;
import com.tlongdev.bktf.flatbuffers.itemschema.Origin;
import com.tlongdev.bktf.flatbuffers.itemschema.Particle;
import com.tlongdev.bktf.util.HttpUtil;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Vector;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TlongdevItemSchemaInteractor extends AsyncTask<Void, Void, Integer> {

    /**
     * Log tag for logging.
     */
    private static final String LOG_TAG = TlongdevItemSchemaInteractor.class.getSimpleName();

    @Inject
    Context mContext;

    private final Callback mCallback;
    private String errorMessage;

    public TlongdevItemSchemaInteractor(BptfApplication application, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(Void... params) {

        try {

            Uri uri = Uri.parse(mContext.getString(R.string.main_host) + "/fbs/item_schema").buildUpon()
                    .build();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(uri.toString())
                    .build();

            Response response = client.newCall(request).execute();

            if (response.body() != null) {
                return parseFlatBuffers(response.body().byteStream());
            } else if (response.code() >= 400) {
                errorMessage = HttpUtil.buildErrorMessage(response);
            }
            return -1;
        } catch (IOException e) {
            //There was a network error
            errorMessage = e.getMessage();
            Log.e(LOG_TAG, "network error", e);
        }
        return -1;
    }

    private int parseFlatBuffers(InputStream inputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));

        ItemSchema schema = ItemSchema.getRootAsItemSchema(buffer);

        insertItems(schema);
        insertOrigins(schema);
        insertParticles(schema);
        insertDecoratedWeapons(schema);

        return 0;
    }

    private void insertItems(ItemSchema schema) {
        Vector<ContentValues> cVVectorItems = new Vector<>();

        for (int i = 0; i < schema.itemsLength(); i++) {
            Item item = schema.items(i);

            //The DV that will contain all the data
            ContentValues itemValues = new ContentValues();
            itemValues.put(ItemSchemaEntry.COLUMN_DEFINDEX, item.defindex());
            itemValues.put(ItemSchemaEntry.COLUMN_ITEM_NAME, item.name());
            itemValues.put(ItemSchemaEntry.COLUMN_TYPE_NAME, item.type());
            itemValues.put(ItemSchemaEntry.COLUMN_DESCRIPTION, item.description());
            itemValues.put(ItemSchemaEntry.COLUMN_PROPER_NAME, item.proper());
            itemValues.put(ItemSchemaEntry.COLUMN_IMAGE, item.image());
            itemValues.put(ItemSchemaEntry.COLUMN_IMAGE_LARGE, item.imageLarge());

            //Add the price to the CV vector
            cVVectorItems.add(itemValues);
        }

        if (cVVectorItems.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVectorItems.size()];
            cVVectorItems.toArray(cvArray);
            //Insert all the data into the database
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(ItemSchemaEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows into item_schema");
        }

        publishProgress();
    }

    private void insertOrigins(ItemSchema schema) {
        Vector<ContentValues> cVVectorOrigins = new Vector<>();

        for (int i = 0; i < schema.originsLength(); i++) {
            Origin origin = schema.origins(i);

            //The DV that will contain all the data
            ContentValues itemValues = new ContentValues();
            itemValues.put(OriginEntry.COLUMN_ID, origin.id());
            itemValues.put(OriginEntry.COLUMN_NAME, origin.name());

            //Add the price to the CV vector
            cVVectorOrigins.add(itemValues);
        }

        if (cVVectorOrigins.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVectorOrigins.size()];
            cVVectorOrigins.toArray(cvArray);
            //Insert all the data into the database
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(OriginEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows into origins");
        }

        publishProgress();
    }

    private void insertParticles(ItemSchema schema) {
        Vector<ContentValues> cVVectorParticles = new Vector<>();

        for (int i = 0; i < schema.particleLength(); i++) {
            Particle particle = schema.particle(i);

            //The DV that will contain all the data
            ContentValues itemValues = new ContentValues();
            itemValues.put(UnusualSchemaEntry.COLUMN_ID, particle.id());
            itemValues.put(UnusualSchemaEntry.COLUMN_NAME, particle.name());

            //Add the price to the CV vector
            cVVectorParticles.add(itemValues);
        }

        if (cVVectorParticles.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVectorParticles.size()];
            cVVectorParticles.toArray(cvArray);
            //Insert all the data into the database
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(UnusualSchemaEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows into unusual_schema");
        }

        publishProgress();
    }

    private void insertDecoratedWeapons(ItemSchema schema) {
        Vector<ContentValues> cVVectorWeapons = new Vector<>();

        for (int i = 0; i < schema.decoratedWeaponLength(); i++) {
            DecoratedWeapon weapon = schema.decoratedWeapon(i);
            //The DV that will contain all the data
            ContentValues weaponValues = new ContentValues();
            weaponValues.put(DecoratedWeaponEntry.COLUMN_DEFINDEX, weapon.defindex());
            weaponValues.put(DecoratedWeaponEntry.COLUMN_GRADE, weapon.grade());

            //Add the price to the CV vector
            cVVectorWeapons.add(weaponValues);
        }

        if (cVVectorWeapons.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVectorWeapons.size()];
            cVVectorWeapons.toArray(cvArray);
            //Insert all the data into the database
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(DecoratedWeaponEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows into decorated_weapons");
        }

        publishProgress();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if (mCallback != null) {
            switch (integer) {
                case 0:
                    mCallback.onItemSchemaFinished();
                    mContext.getContentResolver().notifyChange(PriceEntry.ALL_PRICES_URI, null);
                    break;
                case -1:
                    mCallback.onItemSchemaFailed(errorMessage);
                    break;
                default:
                    mCallback.onItemSchemaFailed(errorMessage);
                    break;
            }
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        if (mCallback != null) {
            mCallback.onItemSchemaUpdate(4);
        }
    }

    /**
     * Listener interface
     */
    public interface Callback {

        void onItemSchemaFinished();

        void onItemSchemaUpdate(int max);

        void onItemSchemaFailed(String errorMessage);
    }
}
