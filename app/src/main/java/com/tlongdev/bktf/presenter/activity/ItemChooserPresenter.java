package com.tlongdev.bktf.presenter.activity;

import android.os.AsyncTask;
import android.widget.Toast;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.dao.CalculatorDao;
import com.tlongdev.bktf.interactor.LoadUnusualEffectsInteractor;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.presenter.fragment.UnusualPresenter;
import com.tlongdev.bktf.ui.view.activity.ItemChooserView;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 24.
 */
public class ItemChooserPresenter implements Presenter<ItemChooserView>,LoadUnusualEffectsInteractor.Callback {

    @Inject
    CalculatorDao mCalculatorDao;

    private ItemChooserView mView;
    private final BptfApplication mApplication;

    public ItemChooserPresenter(BptfApplication application) {
        application.getPresenterComponent().inject(this);
        mApplication = application;
    }

    @Override
    public void attachView(ItemChooserView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadEffects() {
        LoadUnusualEffectsInteractor interactor = new LoadUnusualEffectsInteractor(
                mApplication, "", UnusualPresenter.ORDER_BY_NAME, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onUnusualEffectsLoadFinished(List<Item> items) {
        if (mView != null) {
            mView.showEffects(items);
        }
    }

    public boolean checkCalculator(Item item) {
        if (Utility.isInCalculator(mCalculatorDao, item)) {
            mView.showToast("You have already added this item", Toast.LENGTH_SHORT);
            return false;
        }
        return true;
    }
}
