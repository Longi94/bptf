/**
 * Copyright 2015 Long Tran
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

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.BuildConfig;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.UserBackpackEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.network.Tf2Interface;
import com.tlongdev.bktf.network.model.tf2.PlayerItem;
import com.tlongdev.bktf.network.model.tf2.PlayerItemAttribute;
import com.tlongdev.bktf.network.model.tf2.PlayerItemsPayload;
import com.tlongdev.bktf.util.ProfileManager;
import com.tlongdev.bktf.util.Utility;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

import retrofit2.Response;

/**
 * Task for fetching the user's backpack in the background
 */
public class Tf2UserBackpackInteractor extends AsyncTask<Void, Void, Integer> {

    /**
     * Log tag for logging.
     */
    private static final String LOG_TAG = Tf2UserBackpackInteractor.class.getSimpleName();

    //A list containing all the possible positions. This is needed to fill in the empty item slots
    //with empty items so the backpack is correctly shown.
    private List<Integer> slotNumbers;

    @Inject Tf2Interface mTf2Interface;
    @Inject Tracker mTracker;
    @Inject Context mContext;
    @Inject ProfileManager mProfileManager;

    //Indicates which table to insert data into
    private final boolean mIsGuest;

    //Error message to be displayed to the user
    private String errorMessage;

    //Number of raw currencies in the user's backpack
    private int rawKeys = 0;
    private int rawRef = 0;
    private int rawRec = 0;
    private int rawScraps = 0;

    //The listener that will be notified when the fetching finishes
    private final Callback mCallback;
    private final User mUser;

    public Tf2UserBackpackInteractor(BptfApplication application, User user, boolean isGuest,
                                     Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
        mUser = user;
        mIsGuest = isGuest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer doInBackground(Void... params) {
        try {
            Response<PlayerItemsPayload> response = mTf2Interface.getUserBackpack(
                    BuildConfig.STEAM_WEB_API_KEY, mUser.getResolvedSteamId()).execute();

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
            mTracker.send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Network exception:GetUserBackpack, Message: " + e.getMessage())
                    .setFatal(false)
                    .build());

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
                //Notify the listener that the backpack was private
                mCallback.onPrivateBackpack();
            } else if (integer >= 1) {
                //Notify the user that the fetching finished and pass on the data
                mCallback.onUserBackpackFinished(mUser);
            } else {
                mCallback.onUserBackpackFailed(errorMessage);
            }
        }
    }

    private int saveItems(PlayerItemsPayload payload) {
        switch (payload.getResult().getStatus()) {
            case 1:
                Vector<ContentValues> cVVector = new Vector<>();

                List<PlayerItem> items = payload.getResult().getItems();

                int backpackSlots = payload.getResult().getNumBackpackSlots();
                int itemNumber = items.size();

                //Create a list containing all the possible position
                slotNumbers = new LinkedList<>();
                for (int i = 1; i <= backpackSlots; i++) {
                    slotNumbers.add(i);
                }

                for (PlayerItem item : items) {
                    ContentValues values = buildContentValues(item);
                    if (values != null) {
                        cVVector.add(values);
                    }
                }

                //Fill in the empty slots with empty items
                fillInEmptySlots(cVVector);

                //Add the items to the database
                if (cVVector.size() > 0) {
                    //Create an array
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);

                    //Content uri based on which talbe to insert into
                    Uri contentUri;
                    if (!mIsGuest) {
                        contentUri = UserBackpackEntry.CONTENT_URI;
                    } else {
                        contentUri = UserBackpackEntry.CONTENT_URI_GUEST;
                    }

                    //Clear the database first
                    int rowsDeleted = mContext.getContentResolver().delete(contentUri, null, null);

                    Log.v(LOG_TAG, "deleted " + rowsDeleted + " rows");

                    //Insert all the data into the database
                    int rowsInserted = mContext.getContentResolver()
                            .bulkInsert(contentUri, cvArray);

                    Log.v(LOG_TAG, "inserted " + rowsInserted + " rows");

                    mUser.setRawMetal(Utility.getRawMetal(rawRef, rawRec, rawScraps));
                    mUser.setRawKeys(rawKeys);
                    mUser.setBackpackSlots(backpackSlots);
                    mUser.setItemCount(itemNumber);

                    if (!mIsGuest) {
                        mProfileManager.saveUser(mUser);
                    }
                }
                return 1;
            case 8: //Invalid ID, shouldn't reach
                throw new IllegalStateException(
                        "Steam ID provided for backpack fetching was invalid: " + mUser.getResolvedSteamId());
            case 15: //Backpack is private
                return 2;
            case 18: //ID doesn't exist, shouldn't reach
                throw new IllegalStateException(
                        "Steam ID provided for backpack fetching doesn't exist: " + mUser.getResolvedSteamId());
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
    private ContentValues buildContentValues(PlayerItem playerItem) {

        //Get the inventory token
        long inventoryToken = playerItem.getInventory();
        if (inventoryToken == 0) {
            //Item hasn't been found yet
            return null;
        }

        //The CV object that will contain the data
        ContentValues values = new ContentValues();

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

        //Save the unique ID
        values.put(UserBackpackEntry.COLUMN_UNIQUE_ID, playerItem.getId());

        //Save the original ID
        values.put(UserBackpackEntry.COLUMN_ORIGINAL_ID, playerItem.getOriginalId());

        //Save the defindex
        values.put(UserBackpackEntry.COLUMN_DEFINDEX, defindex);

        //Save the level
        values.put(UserBackpackEntry.COLUMN_LEVEL, playerItem.getLevel());

        //Save the origin type
        values.put(UserBackpackEntry.COLUMN_ORIGIN, playerItem.getOrigin());

        //Save the tradability
        values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE, playerItem.isFlagCannotTrade() ? 1 : 0);

        //Save the craftability
        values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT, playerItem.isFlagCannotCraft() ? 1 : 0);

        if (inventoryToken >= 3221225472L /*11000000000000000000000000000000*/) {
            //The jsonItem doesn't have a designated place i the backpack yet. It's a new jsonItem.
            values.put(UserBackpackEntry.COLUMN_POSITION, -1);
        } else {
            //Save the position of the jsonItem
            int position = (int) (inventoryToken % ((Double) Math.pow(2, 16)).intValue());
            values.put(UserBackpackEntry.COLUMN_POSITION, position);

            //The position doesn't need to be filled with an empty jsonItem.
            slotNumbers.remove(Integer.valueOf(position));
        }

        //Save the quality of the jsonItem
        values.put(UserBackpackEntry.COLUMN_QUALITY, playerItem.getQuality());

        //Save the custom name of the jsonItem
        values.put(UserBackpackEntry.COLUMN_CUSTOM_NAME, playerItem.getCustomName());

        //Save the custom description of the jsonItem
        values.put(UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION, playerItem.getCustomDesc());

        //Save the content of the jsonItem TODO show the content of a gift
        // TODO: 2016. 03. 14. values.put(UserBackpackEntry.COLUMN_CONTAINED_ITEM, );

        //Get the other attributes from the attributes JSON object
        values = addAttributes(values, playerItem);

        if (!values.containsKey(UserBackpackEntry.COLUMN_ITEM_INDEX)) {
            values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, 0);
        }

        if (!values.containsKey(UserBackpackEntry.COLUMN_AUSTRALIUM)) {
            values.put(UserBackpackEntry.COLUMN_AUSTRALIUM, 0);
        }

        //Save the equipped property of the jsonItem
        if (playerItem.getEquipped() != null)
            values.put(UserBackpackEntry.COLUMN_EQUIPPED, 1);
        else
            values.put(UserBackpackEntry.COLUMN_EQUIPPED, 0);

        return values;
    }

    /**
     * Fill in the given vector with empty item slots
     *
     * @param cVVector the vector to be filled
     */
    private void fillInEmptySlots(Vector<ContentValues> cVVector) {
        //Add an empty item to each empty slot
        for (int i : slotNumbers) {
            ContentValues values = new ContentValues();

            values.put(UserBackpackEntry.COLUMN_UNIQUE_ID, 0);
            values.put(UserBackpackEntry.COLUMN_ORIGINAL_ID, 0);
            values.put(UserBackpackEntry.COLUMN_DEFINDEX, 0);
            values.put(UserBackpackEntry.COLUMN_LEVEL, 0);
            values.put(UserBackpackEntry.COLUMN_ORIGIN, 0);
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE, 0);
            values.put(UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT, 0);
            values.put(UserBackpackEntry.COLUMN_POSITION, i);
            values.put(UserBackpackEntry.COLUMN_QUALITY, 0);
            values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, 0);
            values.put(UserBackpackEntry.COLUMN_CRAFT_NUMBER, 0);
            values.put(UserBackpackEntry.COLUMN_AUSTRALIUM, 0);
            values.put(UserBackpackEntry.COLUMN_EQUIPPED, 0);

            cVVector.add(values);
        }
    }

    /**
     * Add all the attributes of the item to the contentvalues
     *
     * @param values the ContentValues the attributes will be added to
     * @param item   json string containing the attributes
     * @return the extended contentvalues
     */
    private ContentValues addAttributes(ContentValues values, PlayerItem item) {
        if (item.getAttributes() != null) {
            //Get the attributes from the json
            List<PlayerItemAttribute> attributes = item.getAttributes();

            //iterate through them and add them to the cv
            for (PlayerItemAttribute attribute : attributes) {

                switch (attribute.getDefindex()) {
                    case 133://Medal number
                        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, attribute.getFloatValue());
                        break;
                    case 134://Particle effect
                        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, attribute.getFloatValue());
                        break;
                    case 142://Painted
                        values.put(UserBackpackEntry.COLUMN_PAINT, attribute.getFloatValue());
                        break;
                    case 186://Gifted by
                        values.put(UserBackpackEntry.COLUMN_GIFTER_NAME,
                                attribute.getAccountInfo().getPersonaName());
                        break;
                    case 187://Crate series
                        values.put(UserBackpackEntry.COLUMN_ITEM_INDEX, attribute.getFloatValue());
                        break;
                    case 228://Crafted by
                        values.put(UserBackpackEntry.COLUMN_CREATOR_NAME,
                                attribute.getAccountInfo().getPersonaName());
                        break;
                    case 229://Craft number
                        values.put(UserBackpackEntry.COLUMN_CRAFT_NUMBER, Integer.parseInt(attribute.getValue()));
                        break;
                    case 725://Decorated weapon wear
                        /*
                        1045220557 - Factory New
                        1053609165 - Minimal Wear
                        1058642330 - Field-Tested
                        1061997773 - Well Worn
                        1065353216 - Battle Scarred
                        */
                        values.put(UserBackpackEntry.COLUMN_DECORATED_WEAPON_WEAR, Long.parseLong(attribute.getValue()));
                        break;
                    case 2013://TODO Killstreaker
                        break;
                    case 2014://TODO Killstreak sheen
                        break;
                    case 2025://TODO Killstreak tier
                        break;
                    case 2027://Is australium
                        values.put(UserBackpackEntry.COLUMN_AUSTRALIUM, attribute.getFloatValue());
                        break;
                    default:
                        //Unused attribute
                        break;
                }
            }
        }
        return values;
    }

    /**
     * Listener interface for listening for the end of the fetch.
     */
    public interface Callback {

        /**
         * Notify the mCallback, that the fetching has finished. The backpack is public.
         */
        void onUserBackpackFinished(User user);

        /**
         * Notify the mCallback that the backpack was private.
         */
        void onPrivateBackpack();

        void onUserBackpackFailed(String errorMessage);
    }
}
