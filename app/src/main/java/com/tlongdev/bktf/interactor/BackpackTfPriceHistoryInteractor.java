/**
 * Copyright 2016 Long Tran
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

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.BuildConfig;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.network.BackpackTfInterface;
import com.tlongdev.bktf.network.model.bptf.BackpackTfPriceHistory;
import com.tlongdev.bktf.network.model.bptf.BackpackTfPriceHistoryPayload;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class BackpackTfPriceHistoryInteractor extends AsyncTask<Void, Void, Integer> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = BackpackTfPriceHistoryInteractor.class.getSimpleName();

    @Inject BackpackTfInterface mBackpackTfInterface;
    @Inject Tracker mTracker;

    private final Item mItem;

    private String errorMessage;

    private final List<Price> mResult = new LinkedList<>();

    private final Callback mCallback;

    public BackpackTfPriceHistoryInteractor(BptfApplication application, Item item, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
        mItem = item;
    }

    @Override
    protected Integer doInBackground(Void... params) {

        try {
            Response<BackpackTfPriceHistoryPayload> response =
                    mBackpackTfInterface.getPriceHistory(BuildConfig.BACKPACK_TF_API_KEY,
                            String.valueOf(mItem.isAustralium() ? "Australium " + mItem.getName() : mItem.getDefindex()),
                            mItem.getQuality(),
                            mItem.isTradable() ? 1 : 0,
                            mItem.isCraftable() ? 1 : 0,
                            mItem.getPriceIndex())
                            .execute();

            if (response.body() != null) {
                BackpackTfPriceHistoryPayload payload = response.body();

                if (payload.getResponse().getSuccess() == 0) {
                    errorMessage = payload.getResponse().getMessage();
                    Log.e(LOG_TAG, errorMessage);
                    return -1;
                }

                for (BackpackTfPriceHistory history : payload.getResponse().getHistory()) {
                    mResult.add(mapToPrice(history));
                }

                return 0;
            } else if (response.raw().code() >= 500) {
                errorMessage = "Server error: " + response.raw().code();
            } else if (response.raw().code() >= 400) {
                errorMessage = "Client error: " + response.raw().code();
            }

            return -1;

        } catch (IOException e) {
            e.printStackTrace();
            mTracker.send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Network exception:GetPriceHistory, Message: " + e.getMessage())
                    .setFatal(false)
                    .build());
        }

        return -1;
    }

    @SuppressWarnings("WrongConstant")
    private Price mapToPrice(BackpackTfPriceHistory history) {
        Price price = new Price();

        price.setValue(history.getValue());
        price.setCurrency(history.getCurrency());
        price.setHighValue(history.getValueHigh());
        price.setLastUpdate(history.getTimestamp());

        return price;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (mCallback != null) {
            if (integer >= 0) {
                //Notify the listener that the update finished
                mCallback.onPriceHistoryFinished(mResult);
            } else {
                mCallback.onPriceHistoryFailed(errorMessage);
            }
        }
    }

    /**
     * Listener interface
     */
    public interface Callback {

        /**
         * Notify the listener, that the fetching has stopped.
         */
        void onPriceHistoryFinished(List<Price> prices);

        void onPriceHistoryFailed(String errorMessage);
    }
}
