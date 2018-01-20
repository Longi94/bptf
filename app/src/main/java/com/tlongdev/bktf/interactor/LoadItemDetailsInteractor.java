package com.tlongdev.bktf.interactor;

import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.data.dao.BackpackDao;
import com.tlongdev.bktf.data.dao.PriceDao;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.model.Price;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 24.
 */
public class LoadItemDetailsInteractor extends AsyncTask<Void, Void, BackpackItem> {

    @Inject
    PriceDao mPriceDao;

    @Inject
    BackpackDao mBackpackDao;

    private final int mId;
    private final boolean mGuest;
    private final Callback mCallback;

    public LoadItemDetailsInteractor(BptfApplication application, int id, boolean guest,
                                     Callback callback) {
        application.getInteractorComponent().inject(this);
        mId = id;
        mGuest = guest;
        mCallback = callback;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected BackpackItem doInBackground(Void... params) {

        com.tlongdev.bktf.data.entity.BackpackItem entity = mBackpackDao.find(mId, mGuest);

        BackpackItem item = new BackpackItem();

        if (entity != null) {
            item.setDefindex(entity.getDefindex());
            item.setQuality(entity.getQuality());
            item.setCraftNumber(entity.getCraftNumber());
            item.setTradable(!entity.getFlagCannotTrade());
            item.setCraftable(!entity.getFlagCannotCraft());
            item.setPriceIndex(entity.getItemIndex());
            item.setPaint(entity.getPaint());
            item.setAustralium(entity.getAustralium());
            item.setCreatorName(entity.getCreatorName());
            item.setGifterName(entity.getGifterName());
            item.setCustomName(entity.getCustomName());
            item.setCustomDescription(entity.getCustomName());
            item.setLevel(entity.getLevel());
            item.setEquipped(entity.getEquipped());
            item.setOrigin(entity.getOrigin());
            item.setWeaponWear(entity.getWeaponWear());
        }

        com.tlongdev.bktf.data.entity.Price price = mPriceDao.getNewestPrice();

        if (price != null) {
            Price itemPrice = new Price();
            itemPrice.setValue(price.getValue());
            itemPrice.setHighValue(price.getHighValue());
            itemPrice.setCurrency(price.getCurrency());

            item.setPrice(itemPrice);
        }
        return item;
    }

    @Override
    protected void onPostExecute(BackpackItem item) {
        if (mCallback != null) {
            mCallback.onItemDetailsLoaded(item);
        }
    }

    public interface Callback {
        void onItemDetailsLoaded(BackpackItem item);
    }
}
