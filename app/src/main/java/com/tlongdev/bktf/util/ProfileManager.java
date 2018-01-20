package com.tlongdev.bktf.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.User;

import java.util.LinkedList;
import java.util.List;

/**
 * Profile related static utility functions.
 */
public class ProfileManager {

    private static ProfileManager ourInstance;

    public static ProfileManager getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new ProfileManager(context);
        }
        return ourInstance;
    }

    private SharedPreferences mPrefs;
    private Gson mGson;
    private Context mContext;

    private User mUserCache;

    private List<OnUpdateListener> mListeners = new LinkedList<>();

    private ProfileManager(Context context) {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mGson = new Gson();
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
        mPrefs.edit().putString(mContext.getString(R.string.pref_user_data), json).apply();

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
        mPrefs.edit().remove(mContext.getString(R.string.pref_user_data)).apply();

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
