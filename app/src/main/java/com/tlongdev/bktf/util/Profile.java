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

    /**
     * Deletes data about the user
     *
     * @param context the context
     */
    public static void logOut(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.remove(context.getString(R.string.pref_steam_id));
        editor.remove(context.getString(R.string.pref_resolved_steam_id));
        editor.remove(context.getString(R.string.pref_player_avatar_url));
        editor.remove(context.getString(R.string.pref_player_name));
        editor.remove(context.getString(R.string.pref_player_reputation));
        editor.remove(context.getString(R.string.pref_player_profile_created));
        editor.remove(context.getString(R.string.pref_player_state));
        editor.remove(context.getString(R.string.pref_player_last_online));
        editor.remove(context.getString(R.string.pref_player_banned));
        editor.remove(context.getString(R.string.pref_player_scammer));
        editor.remove(context.getString(R.string.pref_player_economy_banned));
        editor.remove(context.getString(R.string.pref_player_vac_banned));
        editor.remove(context.getString(R.string.pref_player_community_banned));
        editor.remove(context.getString(R.string.pref_player_backpack_value_tf2));
        editor.remove(context.getString(R.string.pref_new_avatar));
        editor.remove(context.getString(R.string.pref_last_user_data_update));
        editor.remove(context.getString(R.string.pref_resolved_steam_id));
        editor.remove(context.getString(R.string.pref_player_trust_negative));
        editor.remove(context.getString(R.string.pref_player_trust_positive));
        editor.remove(context.getString(R.string.pref_user_raw_key));
        editor.remove(context.getString(R.string.pref_user_raw_metal));
        editor.remove(context.getString(R.string.pref_user_slots));
        editor.remove(context.getString(R.string.pref_user_items));

        editor.apply();
    }
}
