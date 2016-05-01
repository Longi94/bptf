/**
 * Copyright 2016 Long Tran
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

import android.content.Context;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.BuildConfig;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.network.BackpackTfInterface;
import com.tlongdev.bktf.network.SteamUserInterface;
import com.tlongdev.bktf.network.model.bptf.BackpackTfPayload;
import com.tlongdev.bktf.network.model.bptf.BackpackTfPlayer;
import com.tlongdev.bktf.network.model.steam.UserSummariesPayload;
import com.tlongdev.bktf.network.model.steam.UserSummariesPlayer;
import com.tlongdev.bktf.network.model.steam.UserSummariesResponse;
import com.tlongdev.bktf.network.model.steam.VanityUrl;
import com.tlongdev.bktf.util.Utility;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Response;

/**
 * @author Long
 * @since 2016. 03. 19.
 */
public class GetSearchedUserDataInteractor extends AsyncTask<Void, Void, Integer> {

    @Inject BackpackTfInterface mBackpackTfInterface;
    @Inject SteamUserInterface mSteamUserInterface;
    @Inject Context mContext;

    //The error message that will be presented to the user, when an error occurs
    private String errorMessage;

    //The mCallback that will be notified when the fetching finishes
    private final Callback mCallback;

    private String mSteamId;

    private User mUser;

    /**
     * Constructor.
     */
    public GetSearchedUserDataInteractor(BptfApplication application, String steamId,
                                 Callback callback) {
        application.getInteractorComponent().inject(this);
        mCallback = callback;
        mSteamId = steamId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer doInBackground(Void... params) {

        try {
            //Check if the given steamid is a resolved 64bit steamId
            if (!Utility.isSteamId(mSteamId)) {
                //First we try to resolve the steamId if the provided one isn't a 64bit steamId
                Response<VanityUrl> response = mSteamUserInterface.resolveVanityUrl(BuildConfig.STEAM_WEB_API_KEY, mSteamId).execute();
                if (response.body() != null) {
                    VanityUrl vanityUrl = response.body();

                    if (vanityUrl.getResponse().getSteamid() == null) {
                        errorMessage = vanityUrl.getResponse().getMessage();
                        return -1;
                    }
                    mSteamId = vanityUrl.getResponse().getSteamid();
                } else if (response.raw().code() >= 500) {
                    errorMessage = "Server error: " + response.raw().code();
                    return 1;
                } else if (response.raw().code() >= 400) {
                    errorMessage = "Client error: " + response.raw().code();
                    return 1;
                }
            }

            mUser = new User();

            //Save the resolved steamId
            mUser.setResolvedSteamId(mSteamId);

            Response<BackpackTfPayload> bptfResponse = mBackpackTfInterface.getUserData(mSteamId).execute();

            if (bptfResponse.body() != null) {
                saveUserData(bptfResponse.body());
            } else if (bptfResponse.raw().code() >= 500) {
                errorMessage = "Server error: " + bptfResponse.raw().code();
                return -1;
            } else if (bptfResponse.raw().code() >= 400) {
                errorMessage = "Client error: " + bptfResponse.raw().code();
                return -1;
            }

            Response<UserSummariesPayload> steamResponse =
                    mSteamUserInterface.getUserSummaries(BuildConfig.STEAM_WEB_API_KEY, mSteamId).execute();

            if (steamResponse.body() != null) {
                saveSteamData(steamResponse.body());
            } else if (steamResponse.raw().code() >= 500) {
                errorMessage = "Server error: " + steamResponse.raw().code();
                return -1;
            } else if (steamResponse.raw().code() >= 400) {
                errorMessage = "Client error: " + steamResponse.raw().code();
                return -1;
            }

            return 0;
        } catch (IOException e) {
            //There was a network error
            errorMessage = mContext.getString(R.string.error_network);
            Crashlytics.logException(e);
            return -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(Integer integer) {
        //Finished fetching the backpack
        if (mCallback != null) {
            if (integer >= 0) {
                //notify the mCallback, that the fetching has finished.
                mCallback.onUserInfoFinished(mUser);
            } else {
                mCallback.onUserInfoFailed(errorMessage);
            }
        }
    }

    private void saveSteamData(UserSummariesPayload payload) {
        if (payload.getResponse() != null) {
            UserSummariesResponse response = payload.getResponse();
            if (response.getPlayers() != null && response.getPlayers().size() > 0) {
                UserSummariesPlayer player = response.getPlayers().get(0);
                if (player != null) {
                    mUser.setName(player.getPersonaName());
                    mUser.setLastOnline(player.getLastLogoff());
                    mUser.setState(player.getPersonaState());
                    mUser.setProfileCreated(player.getTimeCreated());
                    mUser.setAvatarUrl(player.getAvatarFull());

                    if (player.getGameId() != 0) {
                        mUser.setState(7);
                    }
                }
            }
        }
    }

    private void saveUserData(BackpackTfPayload payload) {
        if (payload.getResponse() == null) {
            return;
        }

        if (payload.getResponse().getSuccess() == 0) {
            //The api query was unsuccessful, nothing to do.
            return;
        }

        if (payload.getResponse().getPlayers() == null) {
            return;
        }

        BackpackTfPlayer player = payload.getResponse().getPlayers().get(mSteamId);
        if (player == null) {
            return;
        }

        if (player.getSuccess() == 1) {
            mUser.setBackpackValue(player.getBackpackValue().get440());
            mUser.setName(player.getName());
            mUser.setReputation(player.getBackpackTfReputation());
            mUser.setInGroup(player.getBackpackTfGroup());
            mUser.setBanned(player.getBackpackTfBanned());
            mUser.setScammer(player.getSteamrepScammer());
            mUser.setEconomyBanned(player.getBanCommunity());
            mUser.setCommunityBanned(player.getBanCommunity());
            mUser.setVacBanned(player.getBanVac());

            if (player.getBackpackTfTrust() != null) {
                mUser.setTrustNegative(player.getBackpackTfTrust().getAgainst());
                mUser.setTrustPositive(player.getBackpackTfTrust().getFor());
            }
        }
    }

    /**
     * Listener interface.
     */
    public interface Callback {

        /**
         * Callback which will be called when both tasks finish.
         *
         * @param user the user
         */
        void onUserInfoFinished(User user);

        void onUserInfoFailed(String errorMessage);
    }
}
