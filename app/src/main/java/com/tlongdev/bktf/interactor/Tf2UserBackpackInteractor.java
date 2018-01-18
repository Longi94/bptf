/**
 * Copyright 2015 Long Tran
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import com.tlongdev.bktf.data.dao.BackpackDao;
import com.tlongdev.bktf.data.entity.BackpackItem;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.network.Tf2Interface;
import com.tlongdev.bktf.network.model.tf2.PlayerItem;
import com.tlongdev.bktf.network.model.tf2.PlayerItemAttribute;
import com.tlongdev.bktf.network.model.tf2.PlayerItemsPayload;
import com.tlongdev.bktf.util.ProfileManager;
import com.tlongdev.bktf.util.Utility;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

/**
 * Task for fetching the user's backpack in the background
 */
public class Tf2UserBackpackInteractor extends AsyncTask<Void, Void, Integer> {

    private static final String LOG_TAG = Tf2UserBackpackInteractor.class.getSimpleName();

    //A list containing all the possible positions. This is needed to fill in the empty item slots
    //with empty items so the backpack is correctly shown.
    private List<Integer> slotNumbers;

    @Inject
    Tf2Interface mTf2Interface;
    @Inject
    Context mContext;
    @Inject
    ProfileManager mProfileManager;
    @Inject
    BackpackDao mBackpackDao;

    //Indicates which table to insert data into
    private final boolean mIsGuest;

    //Error message to be displayed to the user
    private String errorMessage;

    //Number of raw currencies in the user's backpack
    private int rawKeys = 0;
    private int rawRef = 0;
    private int rawRec = 0;
    private int rawScraps = 0;

    private double rawMetal;
    private int backpackSlots;
    private int itemCount;

    //The listener that will be notified when the fetching finishes
    private final Callback mCallback;
    private String mResolvedSteamId;

    public Tf2UserBackpackInteractor(BptfApplication application, String resolvedSteamId, boolean isGuest,
                                     Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
        mResolvedSteamId = resolvedSteamId;
        mIsGuest = isGuest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer doInBackground(Void... params) {
        try {
            Response<PlayerItemsPayload> response = mTf2Interface.getUserBackpack(
                    mContext.getString(R.string.api_key_steam_web), mResolvedSteamId).execute();

            if (response.body() != null) {
                return saveItems(response.body());
            } else if (response.raw().code() >= 500) {
                errorMessage = "Server error: " + response.raw().code();
                return -1;
            } else if (response.raw().code() >= 400) {
                errorMessage = "Client error: " + response.raw().code();
                return -1;
            }
            return -1;

        } catch (IOException e) {
            errorMessage = mContext.getString(R.string.error_network);
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(Integer integer) {
        if (mCallback != null) {
            if (integer == 0) {
                //Notify the user that the fetching finished and pass on the data
                mCallback.onUserBackpackFinished(rawMetal, rawKeys, backpackSlots, itemCount);
            } else if (integer >= 1) {
                //Notify the listener that the backpack was private
                mCallback.onPrivateBackpack();
            } else {
                mCallback.onUserBackpackFailed(errorMessage);
            }
        }
    }

    private int saveItems(PlayerItemsPayload payload) {
        switch (payload.getResult().getStatus()) {
            case 1:
                List<BackpackItem> backpackItems = new LinkedList<>();

                List<PlayerItem> items = payload.getResult().getItems();

                backpackSlots = payload.getResult().getNumBackpackSlots();
                itemCount = items.size();

                //Create a list containing all the possible position
                slotNumbers = new LinkedList<>();
                for (int i = 1; i <= backpackSlots; i++) {
                    slotNumbers.add(i);
                }

                for (PlayerItem item : items) {
                    BackpackItem backpackItem = buildContentValues(item);
                    if (backpackItem != null) {
                        backpackItems.add(backpackItem);
                    }
                }

                //Fill in the empty slots with empty items
                fillInEmptySlots(backpackItems);

                //Add the items to the database
                if (backpackItems.size() > 0) {
                    mBackpackDao.deleteAll(mIsGuest);
                    Log.v(LOG_TAG, "nuked backpack table, guest:" + mIsGuest);

                    mBackpackDao.insert(backpackItems);
                    Log.v(LOG_TAG, "inserted " + backpackItems.size() + " rows into backpack");

                    rawMetal = Utility.getRawMetal(rawRef, rawRec, rawScraps);
                }
                return 0;
            case 8: //Invalid ID, shouldn't reach
                throw new IllegalStateException(
                        "Steam ID provided for backpack fetching was invalid: " + mResolvedSteamId);
            case 15: //Backpack is private
                return 1;
            case 18: //ID doesn't exist, shouldn't reach
                throw new IllegalStateException(
                        "Steam ID provided for backpack fetching doesn't exist: " + mResolvedSteamId);
            default: //Shouldn't reach
                throw new IllegalStateException("Unknown status returned by GetPlayerItems api: " +
                        payload.getResult().getStatus());
        }
    }

    /**
     * Create a ContentValues object from the PlayerItem
     *
     * @param playerItem the object containing the data
     * @return the ContentValues object containing the data from the PlayerItem
     */
    private BackpackItem buildContentValues(PlayerItem playerItem) {

        //Get the inventory token
        long inventoryToken = playerItem.getInventory();
        if (inventoryToken == 0) {
            //Item hasn't been found yet
            return null;
        }

        //Get the defindex
        int defindex = playerItem.getDefindex();

        //Raw currency values
        switch (defindex) {
            case 5021:
                rawKeys++;
                break;
            case 5000:
                rawScraps++;
                break;
            case 5001:
                rawRec++;
                break;
            case 5002:
                rawRef++;
                break;
        }

        //Fix the defindex for pricing purposes
        Item item = new Item();
        item.setDefindex(defindex);
        defindex = item.getFixedDefindex();

        BackpackItem backpackItem = new BackpackItem();
        backpackItem.setGuest(mIsGuest);
        backpackItem.setDefindex(defindex);
        backpackItem.setUniqueId(playerItem.getId());
        backpackItem.setOriginalId(playerItem.getOriginalId());
        backpackItem.setDefindex(playerItem.getDefindex());
        backpackItem.setLevel(playerItem.getLevel());
        backpackItem.setOrigin(playerItem.getOrigin());
        backpackItem.setFlagCannotTrade(playerItem.isFlagCannotTrade());
        backpackItem.setFlagCannotCraft(playerItem.isFlagCannotCraft());

        if (inventoryToken >= 3221225472L /*11000000000000000000000000000000*/) {
            backpackItem.setPosition(-1);
        } else {
            int position = (int) (inventoryToken % ((Double) Math.pow(2, 16)).intValue());
            backpackItem.setPosition(position);

            //The position doesn't need to be filled with an empty jsonItem.
            slotNumbers.remove(Integer.valueOf(position));
        }

        backpackItem.setQuality(playerItem.getQuality());
        backpackItem.setCustomName(playerItem.getCustomName());
        backpackItem.setCustomDescription(playerItem.getCustomDesc());

        //Save the content of the jsonItem TODO show the content of a gift
        // TODO: 2016. 03. 14. values.put(UserBackpackEntry.COLUMN_CONTAINED_ITEM, );

        //Get the other attributes from the attributes JSON object
        addAttributes(backpackItem, playerItem);

        //Save the equipped property of the jsonItem
        backpackItem.setEquipped(playerItem.getEquipped() != null);

        return backpackItem;
    }

    /**
     * Fill in the given vector with empty item slots
     *
     * @param backpackItems the vector to be filled
     */
    private void fillInEmptySlots(List<BackpackItem> backpackItems) {
        //Add an empty item to each empty slot
        for (int i : slotNumbers) {
            BackpackItem item = new BackpackItem();
            item.setPosition(i);
            item.setGuest(mIsGuest);
            backpackItems.add(item);
        }
    }

    /**
     * Add all the attributes of the item to the contentvalues
     *
     * @param backpackItem the ContentValues the attributes will be added to
     * @param item         json string containing the attributes
     */
    private void addAttributes(BackpackItem backpackItem, PlayerItem item) {
        if (item.getAttributes() != null) {
            //Get the attributes from the json
            List<PlayerItemAttribute> attributes = item.getAttributes();

            //iterate through them and add them to the cv
            for (PlayerItemAttribute attribute : attributes) {

                switch (attribute.getDefindex()) {
                    case 133://Medal number
                        backpackItem.setItemIndex((int) attribute.getFloatValue());
                        break;
                    case 134://Particle effect
                        backpackItem.setItemIndex((int) attribute.getFloatValue());
                        break;
                    case 2041://Taunt particle effect
                        backpackItem.setItemIndex(Integer.parseInt(attribute.getValue()));
                        break;
                    case 142://Painted
                        backpackItem.setPosition((int) attribute.getFloatValue());
                        break;
                    case 186://Gifted by
                        backpackItem.setGifterName(attribute.getAccountInfo().getPersonaName());
                        break;
                    case 187://Crate series
                        backpackItem.setItemIndex((int) attribute.getFloatValue());
                        break;
                    case 228://Crafted by
                        backpackItem.setCreatorName(attribute.getAccountInfo().getPersonaName());
                        break;
                    case 229://Craft number
                        backpackItem.setCraftNumber(Integer.parseInt(attribute.getValue()));
                        break;
                    case 725://Decorated weapon wear
                        /*
                        1045220557 - Factory New
                        1053609165 - Minimal Wear
                        1058642330 - Field-Tested
                        1061997773 - Well Worn
                        1065353216 - Battle Scarred
                        */
                        backpackItem.setWeaponWear(Integer.parseInt(attribute.getValue()));
                        break;
                    case 2013://TODO Killstreaker
                        break;
                    case 2014://TODO Killstreak sheen
                        break;
                    case 2025://TODO Killstreak tier
                        break;
                    case 2027://Is australium
                        backpackItem.setAustralium(attribute.getFloatValue() == 1);
                        break;
                    default:
                        //Unused attribute
                        break;
                }
            }
        }
    }

    /**
     * Listener interface for listening for the end of the fetch.
     */
    public interface Callback {

        /**
         * Notify the mCallback, that the fetching has finished. The backpack is public.
         */
        void onUserBackpackFinished(double rawMetal, int rawKeys, int backpackSlots, int itemCount);

        /**
         * Notify the mCallback that the backpack was private.
         */
        void onPrivateBackpack();

        void onUserBackpackFailed(String errorMessage);
    }
}
