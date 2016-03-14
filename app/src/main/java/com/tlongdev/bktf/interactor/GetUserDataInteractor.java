/**
 * Copyright 2015 Long Tran
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.interactor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.BuildConfig;
import com.tlongdev.bktf.R;
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
 * Task for fetching data about the user in the background.
 */
public class GetUserDataInteractor extends AsyncTask<String, Void, Integer> {

    private static final String LOG_TAG = GetUserDataInteractor.class.getSimpleName();

    @Inject BackpackTfInterface mBackpackTfInterface;
    @Inject SteamUserInterface mSteamUserInterface;
    @Inject Tracker mTracker;
    @Inject SharedPreferences.Editor mEditor;
    @Inject SharedPreferences mPrefs;

    //The context the task is running in
    private Context mContext;

    //Whether it was a user initiated update
    private boolean manualSync;

    //The error message that will be presented to the user, when an error occurs
    private String errorMessage;

    //The listener that will be notified when the fetching finishes
    private OnUserInfoListener listener = null;

    private String steamId;

    /**
     * Constructor.
     *
     * @param context    the context the task will run in
     * @param manualSync whether the updated was initiated by the user
     */
    public GetUserDataInteractor(Context context, BptfApplication application, boolean manualSync) {
        application.getInteractorComponent().inject(this);
        this.mContext = context;
        this.manualSync = manualSync;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer doInBackground(String... params) {
        if (System.currentTimeMillis() - mPrefs.getLong(mContext.getString(R.string.pref_last_user_data_update), 0)
                < 3600000L && !manualSync) {
            //This task ran less than an hour ago and wasn't a manual sync, nothing to do.
            return -1;
        }

        try {
            //Check if there is a resolve steamId saved
            steamId = params[1];
            if (steamId == null || steamId.equals("")) {
                //There is no resolved steam id saved
                steamId = params[0];
                if (steamId == null) {
                    //There is no steam id saved
                    errorMessage = mContext.getString(R.string.error_no_steam_id);
                    return -1;
                }
            }

            //Check if the given steamid is a resolved 64bit steamId
            if (!Utility.isSteamId(steamId)) {
                //First we try to resolve the steamId if the provided one isn't a 64bit steamId
                Response<VanityUrl> response = mSteamUserInterface.resolveVanityUrl(BuildConfig.STEAM_WEB_API_KEY, steamId).execute();
                if (response.body() != null) {
                    VanityUrl vanityUrl = response.body();

                    if (vanityUrl.getResponse().getSteamid() == null) {
                        errorMessage = vanityUrl.getResponse().getMessage();
                        return -1;
                    }
                    steamId = vanityUrl.getResponse().getSteamid();
                } else if (response.raw().code() >= 500) {
                    errorMessage = "Server error: " + response.raw().code();
                    return 1;
                } else if (response.raw().code() >= 400) {
                    errorMessage = "Client error: " + response.raw().code();
                    return 1;
                }
            }

            //Save the resolved steamId
            mEditor.putString(mContext
                    .getString(R.string.pref_resolved_steam_id), steamId).apply();

            Response<BackpackTfPayload> bptfResponse = mBackpackTfInterface.getUserData(steamId).execute();

            if (bptfResponse.body() != null) {
                saveUserData(bptfResponse.body());
            } else if (bptfResponse.raw().code() >= 500) {
                errorMessage = "Server error: " + bptfResponse.raw().code();
                return 1;
            } else if (bptfResponse.raw().code() >= 400) {
                errorMessage = "Client error: " + bptfResponse.raw().code();
                return 1;
            }

            Response<UserSummariesPayload> steamResponse =
                    mSteamUserInterface.getUserSummaries(BuildConfig.STEAM_WEB_API_KEY, steamId).execute();

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
            e.printStackTrace();

            mTracker.send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Network exception:GetUserInfo, Message: " + e.getMessage())
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
        //Finished fetching the backpack
        if (listener != null) {
            if (integer >= 0) {
                //notify the listener, that the fetching has finished.
                listener.onUserInfoFinished(steamId);
            } else {
                listener.onUserInfoFailed(errorMessage);
            }
        }
    }

    private void saveSteamData(UserSummariesPayload payload) {
        if (payload.getResponse() != null) {
            UserSummariesResponse response = payload.getResponse();
            if (response.getPlayers() != null && response.getPlayers().size() > 0) {
                UserSummariesPlayer player = response.getPlayers().get(0);
                if (player != null) {
                    mEditor.putString(mContext.getString(R.string.pref_player_name), player.getPersonaName())
                            .putLong(mContext.getString(R.string.pref_player_last_online), player.getLastLogoff())
                            .putInt(mContext.getString(R.string.pref_player_state), player.getPersonaState())
                            .putLong(mContext.getString(R.string.pref_player_profile_created), player.getTimeCreated());

                    if (player.getGameId() != 0) {
                        mEditor.putInt(mContext.getString(R.string.pref_player_state), 7);
                    }

                    String avatarUrl = mPrefs.getString(mContext.getString(R.string.pref_player_avatar_url), "");

                    if (!avatarUrl.equals(player.getAvatarFull())) {
                        mEditor.putBoolean(mContext.getString(R.string.pref_new_avatar), true);
                        mEditor.putString(mContext.getString(R.string.pref_player_avatar_url), player.getAvatarFull());
                    }
                    mEditor.apply();
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

        BackpackTfPlayer player = payload.getResponse().getPlayers().get(steamId);
        if (player == null) {
            return;
        }

        if (player.getSuccess() == 1) {
            // TODO: 2016. 03. 14. save booleans?
            Utility.putDouble(mEditor, mContext.getString(R.string.pref_player_backpack_value_tf2),
                    player.getBackpackValue().get440())
                    .putString(mContext.getString(R.string.pref_player_name), player.getName())
                    .putInt(mContext.getString(R.string.pref_player_reputation), player.getBackpackTfReputation())
                    .putInt(mContext.getString(R.string.pref_player_group), player.getBackpackTfGroup() ? 1 : 0)
                    .putInt(mContext.getString(R.string.pref_player_banned), player.getBackpackTfBanned() ? 1 : 0)
                    .putInt(mContext.getString(R.string.pref_player_scammer), player.getSteamrepScammer() ? 1 : 0)
                    .putInt(mContext.getString(R.string.pref_player_community_banned), player.getBanCommunity() ? 1 : 0)
                    .putInt(mContext.getString(R.string.pref_player_economy_banned), player.getBanEconomy() ? 1 : 0)
                    .putInt(mContext.getString(R.string.pref_player_vac_banned), player.getBanVac() ? 1 : 0);

            if (player.getBackpackTfTrust() != null) {
                mEditor.putInt(mContext.getString(R.string.pref_player_trust_positive),
                        player.getBackpackTfTrust().getFor())
                        .putInt(mContext.getString(R.string.pref_player_trust_negative),
                                player.getBackpackTfTrust().getAgainst());
            }

            mEditor.apply();
        }
    }

    /**
     * Register a listener, that will be notified when the fetching finishes
     *
     * @param listener the listener to be registered
     */
    public void registerFetchUserInfoListener(OnUserInfoListener listener) {
        this.listener = listener;
    }

    /**
     * Listener interface.
     */
    public interface OnUserInfoListener {

        /**
         * Callback which will be called when both tasks finish.
         *
         * @param steamId the steamid of the zser
         */
        void onUserInfoFinished(String steamId);

        void onUserInfoFailed(String errorMessage);
    }
}