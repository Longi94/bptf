package com.tlongdev.bktf.interactor;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.network.BackpackTfInterface;
import com.tlongdev.bktf.network.model.bptf.BackpackTfPriceHistory;
import com.tlongdev.bktf.network.model.bptf.BackpackTfPriceHistoryPayload;
import com.tlongdev.bktf.util.HttpUtil;

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
    private final Application mContext;

    @Inject BackpackTfInterface mBackpackTfInterface;

    private final Item mItem;

    private String errorMessage;

    private final List<Price> mResult = new LinkedList<>();

    private final Callback mCallback;

    public BackpackTfPriceHistoryInteractor(BptfApplication application, Item item, Callback callback) {
        mContext = application;
        application.getInteractorComponent().inject(this);
        mCallback = callback;
        mItem = item;
    }

    @Override
    protected Integer doInBackground(Void... params) {

        try {
            Response<BackpackTfPriceHistoryPayload> response =
                    mBackpackTfInterface.getPriceHistory(mContext.getString(R.string.api_key_backpack_tf),
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
            } else if (response.raw().code() >= 400) {
                errorMessage = HttpUtil.buildErrorMessage(response);
            }

            return -1;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @SuppressWarnings("WrongConstant")
    private Price mapToPrice(BackpackTfPriceHistory history) {
        Price price = new Price();

        if (history.getCurrency().equals(Currency.HAT)) { //OMG
            price.setValue(history.getValue() * 1.33);
            price.setCurrency(Currency.METAL);
            price.setHighValue(history.getValueHigh() * 1.33);
        } else {
            price.setValue(history.getValue());
            price.setCurrency(history.getCurrency());
            price.setHighValue(history.getValueHigh());
        }

        price.setLastUpdate(history.getTimestamp() * 1000L);

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
