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
import com.tlongdev.bktf.network.TlongdevInterface;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevDecoratedWeapon;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevItem;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevItemSchemaPayload;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevOrigin;
import com.tlongdev.bktf.network.model.tlongdev.TlongdevParticleName;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
        List<ItemSchema> schemas = new LinkedList<>();

        for (TlongdevItem item : items) {
            schemas.add(new ItemSchema(item.getDefindex(), item.getName(), item.getDescription(),
                    item.getTypeName(), item.getProperName() == 1));
        }

        if (schemas.size() > 0) {
            itemSchemaDao.insertSchemas(schemas);
            Log.v(LOG_TAG, "inserted " + schemas.size() + " rows into item_schema");
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
        List<DecoratedWeapon> decoratedWeapons = new LinkedList<>();

        for (TlongdevDecoratedWeapon weapon : weapons) {
            decoratedWeapons.add(new DecoratedWeapon(weapon.getDefindex(), weapon.getGrade()));
        }

        if (decoratedWeapons.size() > 0) {
            decoratedWeaponDao.insertDecoratedWeapons(decoratedWeapons);
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
