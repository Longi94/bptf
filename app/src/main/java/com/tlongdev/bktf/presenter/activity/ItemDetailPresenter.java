package com.tlongdev.bktf.presenter.activity;

import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.interactor.LoadItemDetailsInteractor;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.activity.ItemDetailView;

/**
 * @author Long
 * @since 2016. 03. 24.
 */
public class ItemDetailPresenter implements Presenter<ItemDetailView>,LoadItemDetailsInteractor.Callback {

    private ItemDetailView mView;
    private final BptfApplication mApplication;

    public ItemDetailPresenter(BptfApplication application) {
        mApplication = application;
    }

    @Override
    public void attachView(ItemDetailView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void loadItemDetails(BackpackItem backpackItem) {
        LoadItemDetailsInteractor interactor = new LoadItemDetailsInteractor(
                mApplication, backpackItem, this
        );
        interactor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onItemDetailsLoaded(Price price) {
        if (mView != null) {
            mView.showItemDetails(price);
        }
    }
}
