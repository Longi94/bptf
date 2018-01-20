package com.tlongdev.bktf.interactor;

import android.content.Context;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.network.FixerIoInterface;
import com.tlongdev.bktf.network.model.fixerio.CurrencyRates;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Response;

/**
 * @author lngtr
 * @since 2016. 05. 09.
 */
public class GetCurrencyExchangeRatesInteractor extends AsyncTask<Void, Void, Integer> {

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
            } else if (response.raw().code() >= 500) {
                mErrorMessage = "Server error: " + response.raw().code();
                return -1;
            } else if (response.raw().code() >= 400) {
                mErrorMessage = "Client error: " + response.raw().code();
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            mErrorMessage = mContext.getString(R.string.error_network);
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
