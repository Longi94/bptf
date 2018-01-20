package com.tlongdev.bktf.interactor;

import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.dao.PriceDao;
import com.tlongdev.bktf.model.Price;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 15.
 */
public class LoadCurrencyPricesInteractor extends AsyncTask<Void, Void, Void> {

    @Inject
    PriceDao mPriceDao;

    private Price mMetalPrice;
    private Price mKeyPrice;
    private Price mBudPrice;

    private final Callback mCallback;

    public LoadCurrencyPricesInteractor(BptfApplication application, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected Void doInBackground(Void... params) {

        com.tlongdev.bktf.data.entity.Price[] prices = mPriceDao.getCurrencyPrices();

        mBudPrice = new Price();
        mMetalPrice = new Price();
        mKeyPrice = new Price();

        for (com.tlongdev.bktf.data.entity.Price price : prices) {
            switch (price.getDefindex()) {
                case 143:
                    mBudPrice.setValue(price.getValue());
                    mBudPrice.setHighValue(price.getHighValue());
                    mBudPrice.setDifference(price.getDifference());
                    mBudPrice.setCurrency(price.getCurrency());
                    mBudPrice.setRawValue(price.getRawValue());
                    break;
                case 5002:
                    mMetalPrice.setValue(price.getValue());
                    mMetalPrice.setHighValue(price.getHighValue());
                    mMetalPrice.setDifference(price.getDifference());
                    mMetalPrice.setCurrency(price.getCurrency());
                    mMetalPrice.setRawValue(price.getRawValue());
                    break;
                case 5021:
                    mKeyPrice.setValue(price.getValue());
                    mKeyPrice.setHighValue(price.getHighValue());
                    mKeyPrice.setDifference(price.getDifference());
                    mKeyPrice.setCurrency(price.getCurrency());
                    mKeyPrice.setRawValue(price.getRawValue());
                    break;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mCallback != null) {
            mCallback.onLoadCurrencyPricesFinished(mMetalPrice, mKeyPrice, mBudPrice);
        }

    }

    public interface Callback {
        void onLoadCurrencyPricesFinished(Price metalPrice, Price keyPrice, Price budPrice);
    }
}
