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

package com.tlongdev.bktf.interactor;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.dao.DecoratedWeaponDao;
import com.tlongdev.bktf.data.dao.ItemSchemaDao;
import com.tlongdev.bktf.data.dao.OriginDao;
import com.tlongdev.bktf.data.dao.UnusualSchemaDao;
import com.tlongdev.bktf.data.entity.DecoratedWeapon;
import com.tlongdev.bktf.data.entity.ItemSchema;
import com.tlongdev.bktf.data.entity.Origin;
import com.tlongdev.bktf.data.entity.UnusualSchema;
import com.tlongdev.bktf.flatbuffers.itemschema.Item;
import com.tlongdev.bktf.flatbuffers.itemschema.Particle;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

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

    @Inject
    UnusualSchemaDao unusualSchemaDao;

    @Inject
    OriginDao originDao;

    @Inject
    DecoratedWeaponDao decoratedWeaponDao;

    @Inject
    ItemSchemaDao itemSchemaDao;

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
            } else if (response.code() >= 500) {
                errorMessage = "Server error: " + response.code();
            } else if (response.code() >= 400) {
                errorMessage = "Client error: " + response.code();
            }
            return -1;
        } catch (IOException e) {
            //There was a network error
            errorMessage = mContext.getString(R.string.error_network);
            e.printStackTrace();
        }
        return -1;
    }

    private int parseFlatBuffers(InputStream inputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));

        com.tlongdev.bktf.flatbuffers.itemschema.ItemSchema schema =
                com.tlongdev.bktf.flatbuffers.itemschema.ItemSchema.getRootAsItemSchema(buffer);

        insertItems(schema);
        insertOrigins(schema);
        insertParticles(schema);
        insertDecoratedWeapons(schema);

        return 0;
    }

    private void insertItems(com.tlongdev.bktf.flatbuffers.itemschema.ItemSchema schema) {
        List<ItemSchema> schemas = new LinkedList<>();

        for (int i = 0; i < schema.itemsLength(); i++) {
            Item item = schema.items(i);
            schemas.add(new ItemSchema(item.defindex(), item.name(), item.description(),
                    item.type(), item.proper()));
        }

        if (schemas.size() > 0) {
            itemSchemaDao.insert(schemas);
            Log.v(LOG_TAG, "inserted " + schemas.size() + " rows into item_schema");
        }

        publishProgress();
    }

    private void insertOrigins(com.tlongdev.bktf.flatbuffers.itemschema.ItemSchema schema) {
        List<Origin> originEntities = new LinkedList<>();

        for (int i = 0; i < schema.originsLength(); i++) {
            com.tlongdev.bktf.flatbuffers.itemschema.Origin origin = schema.origins(i);
            originEntities.add(new Origin(origin.id(), origin.name()));
        }

        if (originEntities.size() > 0) {
            originDao.insert(originEntities);
            Log.v(LOG_TAG, "inserted " + originEntities.size() + " rows into origins");
        }

        publishProgress();
    }

    private void insertParticles(com.tlongdev.bktf.flatbuffers.itemschema.ItemSchema schema) {
        List<UnusualSchema> schemas = new LinkedList<>();

        for (int i = 0; i < schema.particleLength(); i++) {
            Particle particle = schema.particle(i);
            schemas.add(new UnusualSchema(particle.id(), particle.name()));
        }

        if (schemas.size() > 0) {
            unusualSchemaDao.insert(schemas);
            Log.v(LOG_TAG, "inserted " + schemas.size() + " rows into unusual_schema");
        }

        publishProgress();
    }

    private void insertDecoratedWeapons(com.tlongdev.bktf.flatbuffers.itemschema.ItemSchema schema) {
        List<DecoratedWeapon> decoratedWeapons = new LinkedList<>();

        for (int i = 0; i < schema.decoratedWeaponLength(); i++) {
            com.tlongdev.bktf.flatbuffers.itemschema.DecoratedWeapon weapon = schema.decoratedWeapon(i);
            decoratedWeapons.add(new DecoratedWeapon(weapon.defindex(), weapon.grade()));
        }

        if (decoratedWeapons.size() > 0) {
            decoratedWeaponDao.insert(decoratedWeapons);
            Log.v(LOG_TAG, "inserted " + decoratedWeapons.size() + " rows into decorated_weapons");
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
