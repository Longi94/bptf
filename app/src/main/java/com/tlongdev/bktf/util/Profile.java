package com.tlongdev.bktf.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tlongdev.bktf.R;

/**
 * Profile related static utility functions.
 */
public class Profile {

    /**
     * Checks whether the user is signed in or not.
     *
     * @param context the context
     * @return whether the user is signed in or not
     */
    public static boolean isSignedIn(Context context) {
        String id = getSteamId(context);
        return id != null && !id.equals("");
    }

    /**
     * Convenient method for getting the steamId (or vanity user name) of the user.
     *
     * @param context context for getting the shared preferences
     * @return user's steam id
     */
    public static String getSteamId(Context context) {
        //get the steamID from shared preferences if present
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String steamId = prefs.getString(context.getString(R.string.pref_steam_id), null);
        //if steamId is null, steamId.equals will crash
        if (steamId != null && steamId.equals("")) {
            return null;
        }
        return steamId;
    }


    /**
     * Convenient method for getting the resolved steamId of the user.
     *
     * @param context context for getting the shared preferences
     * @return user's resolved steam id
     */
    public static String getResolvedSteamId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_resolved_steam_id), null);
    }
}
