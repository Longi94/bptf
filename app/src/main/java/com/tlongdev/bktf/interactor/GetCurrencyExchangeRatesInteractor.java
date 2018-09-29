package com.tlongdev.bktf.interactor;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.network.FixerIoInterface;
import com.tlongdev.bktf.network.model.fixerio.CurrencyRates;
import com.tlongdev.bktf.util.HttpUtil;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Response;

/**
 * @author lngtr
 * @since 2016. 05. 09.
 */
public class GetCurrencyExchangeRatesInteractor extends AsyncTask<Void, Void, Integer> {

    private static final String TAG = GetCurrencyExchangeRatesInteractor.class.getSimpleName();

    @Inject FixerIoInterface mFixerIoInterface;
    @Inject Context mContext;

    private Callback mCallback;

    private Map<String, Double> mRates;
    private String mErrorMessage;

    public GetCurrencyExchangeRatesInteractor(BptfApplication application, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            Response<CurrencyRates> response = mFixerIoInterface.getCurrencyRates("USD").execute();

            if (response.body() != null) {
                mRates = response.body().getRates();
                return 0;
            } else if (response.raw().code() >= 400) {
                mErrorMessage = HttpUtil.buildErrorMessage(response);
                return -1;
            }
        } catch (IOException e) {
            mErrorMessage = e.getMessage();
            Log.e(TAG, "network error", e);
        }
        return -1;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (mCallback != null) {
            if (integer == 0) {
                mCallback.onGetCurrencyExchangeRatesFinished(mRates);
            } else {
                mCallback.onGetCurrencyExchangeRatesFailed(mErrorMessage);
            }
        }
    }

    public interface Callback {
        void onGetCurrencyExchangeRatesFinished(Map<String, Double> rates);

        void onGetCurrencyExchangeRatesFailed(String errorMessage);
    }
}
