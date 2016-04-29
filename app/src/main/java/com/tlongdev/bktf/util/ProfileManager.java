/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.User;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

/**
 * Profile related static utility functions.
 */
public class ProfileManager {

    @Inject SharedPreferences mPrefs;
    @Inject SharedPreferences.Editor mEditor;
    @Inject Gson mGson;
    @Inject Context mContext;

    private User mUserCache;

    private List<OnUpdateListener> mListeners = new LinkedList<>();

    public ProfileManager(BptfApplication application) {
        application.getProfileManagerComponent().inject(this);
    }

    /**
     * Checks whether the user is signed in or not.
     *
     * @return whether the user is signed in or not
     */
    public boolean isSignedIn() {
        return mPrefs.contains(mContext.getString(R.string.pref_user_data));
    }

    /**
     * Convenient method for getting the steamId (or vanity user name) of the user.
     *
     * @return user's steam id
     */
    public String getSteamId() {
        if (mUserCache == null) {
            mUserCache = getUser();
        }
        return mUserCache.getSteamId();
    }

    public User getUser() {
        if (mUserCache == null) {
            String json = mPrefs.getString(mContext.getString(R.string.pref_user_data), "{}");
            mUserCache = mGson.fromJson(json, User.class);
        }
        return mUserCache;
    }

    public void saveUser(User user) {
        mUserCache = user;
        String json = mGson.toJson(user);
        mEditor.putString(mContext.getString(R.string.pref_user_data), json);
        mEditor.apply();

        for (OnUpdateListener listener : mListeners) {
            listener.onUpdate(mUserCache);
        }
    }

    /**
     * Convenient method for getting the resolved steamId of the user.
     *
     * @return user's resolved steam id
     */
    public String getResolvedSteamId() {
        if (mUserCache == null) {
            mUserCache = getUser();
        }
        return mUserCache.getResolvedSteamId();
    }

    /**
     * Deletes data about the user
     */
    public void logOut() {
        mEditor.remove(mContext.getString(R.string.pref_user_data));
        mEditor.apply();

        for (OnUpdateListener listener : mListeners) {
            listener.onLogOut();
        }
    }

    public void addOnProfileUpdateListener(OnUpdateListener listener) {
        mListeners.add(listener);
    }

    public void removeOnUpdateListener(OnUpdateListener listener) {
        mListeners.remove(listener);
    }

    public interface OnUpdateListener {
        void onLogOut();

        void onUpdate(User user);
    }
}
