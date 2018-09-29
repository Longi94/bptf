package com.tlongdev.bktf.interactor;

import android.app.Application;
import android.os.AsyncTask;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.network.SteamUserInterface;
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
 * @since 2016. 03. 22.
 */
public class SearchUserInteractor extends AsyncTask<Void, Void, Integer> {

    @Inject
    SteamUserInterface mInterface;

    @Inject
    Application mContext;

    private String mQuery;
    private final Callback mCallback;

    private User mUser;

    public SearchUserInteractor(BptfApplication application, String query, Callback callback) {
        application.getInteractorComponent().inject(this);
        mQuery = query;
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if (mQuery == null) {
            return -1;
        }

        try {
            if (!Utility.isSteamId(mQuery)) {
                Response<VanityUrl> response =
                        mInterface.resolveVanityUrl(
                                mContext.getString(R.string.api_key_steam_web), mQuery).execute();

                if (response.body() != null) {
                    VanityUrl vanityUrl = response.body();

                    if (vanityUrl.getResponse().getSteamid() == null) {
                        return -1;
                    }
                    mQuery = vanityUrl.getResponse().getSteamid();
                } else if (response.raw().code() >= 500) {
                    return -1;
                } else if (response.raw().code() >= 400) {
                    return -1;
                }
            }

            if (isCancelled()) {
                return -1;
            }

            mUser = new User();
            mUser.setResolvedSteamId(mQuery);

            Response<UserSummariesPayload> steamResponse =
                    mInterface.getUserSummaries(
                            mContext.getString(R.string.api_key_steam_web), mQuery).execute();

            if (isCancelled()) {
                return -1;
            }

            if (steamResponse.body() != null) {
                getSteamData(steamResponse.body());
            } else if (steamResponse.raw().code() >= 500) {
                return -1;
            } else if (steamResponse.raw().code() >= 400) {
                return -1;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    protected void onPostExecute(Integer integer) {
        //Finished fetching the backpack
        if (mCallback != null) {
            if (integer >= 0) {
                //notify the mCallback, that the fetching has finished.
                mCallback.onUserFound(mUser);
            } else {
                mCallback.onUserNotFound();
            }
        }
    }

    private void getSteamData(UserSummariesPayload payload) {
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

    public interface Callback {
        void onUserFound(User user);
        void onUserNotFound();
    }
}
