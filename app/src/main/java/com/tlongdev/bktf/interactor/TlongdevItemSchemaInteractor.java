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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.DecoratedWeaponEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.OriginEntry;
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;
import com.tlongdev.bktf.network.TlongdevInterface;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevDecoratedWeapon;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevItem;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevItemSchemaPayload;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevOrigin;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevParticleName;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

import retrofit2.Response;

public class TlongdevItemSchemaInteractor extends AsyncTask<Void, Void, Integer> {

    /**
     * Log tag for logging.
     */
    private static final String LOG_TAG = TlongdevItemSchemaInteractor.class.getSimpleName();

    @Inject TlongdevInterface mTlongdevInterface;
    @Inject Tracker mTracker;
    @Inject Context mContext;

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

            mTracker.send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Network exception:GetItemSchema, Message: " + e.getMessage())
                    .setFatal(false)
                    .build());
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
        Vector<ContentValues> cVVectorOrigins = new Vector<>();

        for (TlongdevOrigin origin : origins) {
            //The DV that will contain all the data
            ContentValues itemValues = new ContentValues();
            itemValues.put(OriginEntry.COLUMN_ID, origin.getId());
            itemValues.put(OriginEntry.COLUMN_NAME, origin.getName());

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

    private void insertParticles(List<TlongdevParticleName> particles) {
        Vector<ContentValues> cVVectorParticles = new Vector<>();

        for (TlongdevParticleName particle : particles) {
            //The DV that will contain all the data
            ContentValues itemValues = new ContentValues();
            itemValues.put(UnusualSchemaEntry.COLUMN_ID, particle.getId());
            itemValues.put(UnusualSchemaEntry.COLUMN_NAME, particle.getName());

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
