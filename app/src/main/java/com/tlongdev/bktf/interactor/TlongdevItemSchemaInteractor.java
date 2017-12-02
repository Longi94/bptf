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

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.DecoratedWeaponEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.dao.OriginDao;
import com.tlongdev.bktf.data.dao.UnusualSchemaDao;
import com.tlongdev.bktf.data.entity.Origin;
import com.tlongdev.bktf.data.entity.UnusualSchema;
import com.tlongdev.bktf.network.TlongdevInterface;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevDecoratedWeapon;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevItem;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevItemSchemaPayload;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevOrigin;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevParticleName;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

import retrofit2.Response;

public class TlongdevItemSchemaInteractor extends AsyncTask<Void, Void, Integer> {

    /**
     * Log tag for logging.
     */
    private static final String LOG_TAG = TlongdevItemSchemaInteractor.class.getSimpleName();

    @Inject
    TlongdevInterface mTlongdevInterface;

    @Inject
    Context mContext;

    @Inject
    UnusualSchemaDao unusualSchemaDao;

    @Inject
    OriginDao originDao;

    private final Callback mCallback;
    private String errorMessage;

    public TlongdevItemSchemaInteractor(BptfApplication application, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(Void... params) {

        try {
            Response<TlongdevItemSchemaPayload> response = mTlongdevInterface.getItemSchema().execute();

            if (response.body() != null) {
                TlongdevItemSchemaPayload payload = response.body();
                if (payload.getSuccess() == 1) {
                    insertItems(payload.getItems());
                    insertOrigins(payload.getOrigins());
                    insertParticles(payload.getParticleName());
                    insertDecoratedWeapons(payload.getDecoratedWeapons());
                    return 0;
                } else {
                    errorMessage = payload.getMessage();
                }
            } else if (response.raw().code() >= 500) {
                errorMessage = "Server error: " + response.raw().code();
            } else if (response.raw().code() >= 400) {
                errorMessage = "Client error: " + response.raw().code();
            }
            return -1;
        } catch (IOException e) {
            //There was a network error
            errorMessage = mContext.getString(R.string.error_network);
            e.printStackTrace();
        }
        return -1;
    }

    private void insertItems(List<TlongdevItem> items) {
        Vector<ContentValues> cVVectorItems = new Vector<>();

        for (TlongdevItem item : items) {
            //The DV that will contain all the data
            ContentValues itemValues = new ContentValues();
            itemValues.put(ItemSchemaEntry.COLUMN_DEFINDEX, item.getDefindex());
            itemValues.put(ItemSchemaEntry.COLUMN_ITEM_NAME, item.getName());
            itemValues.put(ItemSchemaEntry.COLUMN_TYPE_NAME, item.getTypeName());
            itemValues.put(ItemSchemaEntry.COLUMN_DESCRIPTION, item.getDescription());
            itemValues.put(ItemSchemaEntry.COLUMN_PROPER_NAME, item.getProperName());

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

    private void insertOrigins(List<TlongdevOrigin> origins) {
        List<Origin> originEntities = new LinkedList<>();

        for (TlongdevOrigin origin : origins) {
            originEntities.add(new Origin(origin.getId(), origin.getName()));
        }

        if (originEntities.size() > 0) {
            originDao.insertOrigins(originEntities);
            Log.v(LOG_TAG, "inserted " + originEntities.size() + " rows into origins");
        }

        publishProgress();
    }

    private void insertParticles(List<TlongdevParticleName> particles) {
        List<UnusualSchema> schemas = new LinkedList<>();

        for (TlongdevParticleName particle : particles) {
            schemas.add(new UnusualSchema(particle.getId(), particle.getName()));
        }

        if (schemas.size() > 0) {
            unusualSchemaDao.insertSchemas(schemas);
            Log.v(LOG_TAG, "inserted " + schemas.size() + " rows into unusual_schema");
        }

        publishProgress();
    }

    private void insertDecoratedWeapons(List<TlongdevDecoratedWeapon> weapons) {
        Vector<ContentValues> cVVectorWeapons = new Vector<>();

        for (TlongdevDecoratedWeapon weapon : weapons) {
            //The DV that will contain all the data
            ContentValues weaponValues = new ContentValues();
            weaponValues.put(DecoratedWeaponEntry.COLUMN_DEFINDEX, weapon.getDefindex());
            weaponValues.put(DecoratedWeaponEntry.COLUMN_GRADE, weapon.getGrade());

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
