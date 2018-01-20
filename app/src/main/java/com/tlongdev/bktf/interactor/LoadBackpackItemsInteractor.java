package com.tlongdev.bktf.interactor;

import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.dao.BackpackDao;
import com.tlongdev.bktf.model.BackpackItem;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 18.
 */
public class LoadBackpackItemsInteractor extends AsyncTask<Void, Void, Void> {

    @Inject
    BackpackDao mBackpackDao;

    private final Callback mCallback;
    private final boolean mGuest;

    private List<BackpackItem> mItems;
    private List<BackpackItem> mNewItems;

    public LoadBackpackItemsInteractor(BptfApplication application, boolean guest, Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
        mGuest = guest;
    }

    @Override
    protected Void doInBackground(Void... params) {
        mItems = new LinkedList<>();
        mNewItems = new LinkedList<>();

        List<com.tlongdev.bktf.data.entity.BackpackItem> items = mBackpackDao.findAll(mGuest);

        for (com.tlongdev.bktf.data.entity.BackpackItem item : items) {
            if (item.getPosition() == -1) {
                mNewItems.add(mapItem(item));
            } else {
                mItems.add(mapItem(item));
            }
        }

        return null;
    }

    private BackpackItem mapItem(com.tlongdev.bktf.data.entity.BackpackItem item) {
        BackpackItem backpackItem = new BackpackItem();
        backpackItem.setId(item.getId().intValue());
        backpackItem.setDefindex(item.getDefindex());
        backpackItem.setQuality(item.getQuality());
        if (item.getCraftNumber() != null) {
            backpackItem.setCraftNumber(item.getCraftNumber());
        }
        backpackItem.setTradable(!item.getFlagCannotTrade());
        backpackItem.setCraftable(!item.getFlagCannotCraft());
        backpackItem.setPriceIndex(item.getItemIndex());
        if (item.getPaint() != null) {
            backpackItem.setPaint(item.getPaint());
        }
        backpackItem.setAustralium(item.getAustralium());
        if (item.getWeaponWear() != null) {
            backpackItem.setWeaponWear(item.getWeaponWear());
        }
        return backpackItem;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mCallback != null) {
            mCallback.onLoadBackpackItemFinished(mItems, mNewItems);
        }
    }

    public interface Callback {
        void onLoadBackpackItemFinished(List<BackpackItem> items, List<BackpackItem> newItems);
    }
}
