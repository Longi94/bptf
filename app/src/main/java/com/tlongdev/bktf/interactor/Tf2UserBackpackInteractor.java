package com.tlongdev.bktf.interactor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.network.Tf2Interface;
import com.tlongdev.bktf.network.model.tf2.PlayerItem;
import com.tlongdev.bktf.network.model.tf2.PlayerItemAttribute;
import com.tlongdev.bktf.network.model.tf2.PlayerItemsPayload;
import com.tlongdev.bktf.util.HttpUtil;
import com.tlongdev.bktf.util.ProfileManager;
import com.tlongdev.bktf.util.Utility;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Response;

/**
 * Task for fetching the user's backpack in the background
 */
public class Tf2UserBackpackInteractor extends AsyncTask<Void, Void, Integer> {

    private static final String TAG = Tf2UserBackpackInteractor.class.getSimpleName();

    @Inject
    Tf2Interface mTf2Interface;
    @Inject
    Context mContext;
    @Inject
    ProfileManager mProfileManager;

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

    private List<BackpackItem> mNewItems;
    private SparseArray<BackpackItem> mItems;

    public Tf2UserBackpackInteractor(BptfApplication application, String resolvedSteamId,
                                     Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
        mResolvedSteamId = resolvedSteamId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer doInBackground(Void... params) {
        Cursor cursor = mContext.getContentResolver().query(
                ItemSchemaEntry.CONTENT_URI,
                new String[]{
                        ItemSchemaEntry.COLUMN_IMAGE,
                        ItemSchemaEntry.COLUMN_DEFINDEX
                },
                null, null, null
        );

        @SuppressLint("UseSparseArrays")
        Map<Integer, String> images = new HashMap<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                images.put(cursor.getInt(1), cursor.getString(0));
            }
            cursor.close();
        }

        try {
            Log.i(TAG, "Fetching backpack of " + mResolvedSteamId);

            Response<PlayerItemsPayload> response = mTf2Interface.getUserBackpack(
                    mContext.getString(R.string.api_key_steam_web), mResolvedSteamId).execute();

            if (response.body() != null) {
                Log.i(TAG, "Parsing backpack data...");
                return saveItems(response.body(), images);
            } else if (response.raw().code() >= 400) {
                errorMessage = HttpUtil.buildErrorMessage(response);
                return -1;
            }
            return -1;

        } catch (IOException e) {
            errorMessage = mContext.getString(R.string.error_network);
            Log.e(TAG, "Failed to fetch backpack", e);
            return -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(Integer integer) {
        Log.i(TAG, "Done fetching backpack of " + mResolvedSteamId);

        if (mCallback != null) {
            if (integer == 0) {
                //Notify the user that the fetching finished and pass on the data
                mCallback.onUserBackpackFinished(mNewItems, mItems, rawMetal, rawKeys,
                        backpackSlots, itemCount);
            } else if (integer >= 1) {
                //Notify the listener that the backpack was private
                mCallback.onPrivateBackpack();
            } else {
                mCallback.onUserBackpackFailed(errorMessage);
            }
        }
    }

    private int saveItems(PlayerItemsPayload payload, Map<Integer, String> images) {
        switch (payload.getResult().getStatus()) {
            case 1:
                List<PlayerItem> items = payload.getResult().getItems();

                backpackSlots = payload.getResult().getNumBackpackSlots();
                itemCount = items.size();
                mNewItems = new LinkedList<>();
                mItems = new SparseArray<>();

                for (PlayerItem item : items) {
                    mapItem(item, images);
                }

                rawMetal = Utility.getRawMetal(rawRef, rawRec, rawScraps);
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

    private void mapItem(PlayerItem playerItem, Map<Integer, String> images) {

        //Get the inventory token
        long inventoryToken = playerItem.getInventory();
        if (inventoryToken == 0) {
            //Item hasn't been found yet
            return;
        }

        BackpackItem backpackItem = new BackpackItem();

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
        backpackItem.setUniqueId(playerItem.getId());

        //Save the original ID
        backpackItem.setOriginalId(playerItem.getOriginalId());

        //Save the defindex
        backpackItem.setDefindex(defindex);

        backpackItem.setImage(images.get(defindex));

        //Save the level
        backpackItem.setLevel(playerItem.getLevel());

        //Save the origin type
        backpackItem.setOrigin(playerItem.getOrigin());

        //Save the tradability
        backpackItem.setTradable(!playerItem.isFlagCannotTrade());

        //Save the craftability
        backpackItem.setCraftable(!playerItem.isFlagCannotCraft());

        if (inventoryToken >= 3221225472L /*11000000000000000000000000000000*/) {
            //The jsonItem doesn't have a designated place i the backpack yet. It's a new jsonItem.
            mNewItems.add(backpackItem);
        } else {
            //Save the position of the jsonItem
            int position = (int) (inventoryToken % ((Double) Math.pow(2, 16)).intValue()) - 1;
            mItems.put(position, backpackItem);
        }

        //Save the quality of the jsonItem
        backpackItem.setQuality(playerItem.getQuality());

        //Save the custom name of the jsonItem
        backpackItem.setCustomName(playerItem.getCustomName());

        //Save the custom description of the jsonItem
        backpackItem.setCustomDescription(playerItem.getCustomDesc());

        //Save the content of the jsonItem TODO show the content of a gift
        // TODO: 2016. 03. 14. values.put(UserBackpackEntry.COLUMN_CONTAINED_ITEM, );

        //Get the other attributes from the attributes JSON object
        addAttributes(backpackItem, playerItem);

        //Save the equipped property of the jsonItem
        backpackItem.setEquipped(playerItem.getEquipped() != null);
    }

    /**
     * Add all the attributes of the item to the backpackItem
     *
     * @param backpackItem the object to which the attributes will be added to
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
                        backpackItem.setPriceIndex((int) attribute.getFloatValue());
                        break;
                    case 134://Particle effect
                        backpackItem.setPriceIndex((int) attribute.getFloatValue());
                        break;
                    case 2041://Taunt particle effect
                        backpackItem.setPriceIndex(Integer.valueOf(attribute.getValue()));
                        break;
                    case 142://Painted
                        backpackItem.setPaint((int) attribute.getFloatValue());
                        break;
                    case 186://Gifted by
                        backpackItem.setGifterName(attribute.getAccountInfo().getPersonaName());
                        break;
                    case 187://Crate series
                        backpackItem.setPriceIndex((int) attribute.getFloatValue());
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
                    case 834:// War Pain
                        // TODO: 2018-09-15
                        break;
                    case 2013://TODO Killstreaker
                        break;
                    case 2014://TODO Killstreak sheen
                        break;
                    case 2025://TODO Killstreak tier
                        break;
                    case 2027://Is australium
                        backpackItem.setAustralium(attribute.getFloatValue() > 0);
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
        void onUserBackpackFinished(List<BackpackItem> newItems, SparseArray<BackpackItem> items,
                                    double rawMetal, int rawKeys, int backpackSlots, int itemCount);

        /**
         * Notify the mCallback that the backpack was private.
         */
        void onPrivateBackpack();

        void onUserBackpackFailed(String errorMessage);
    }
}
