package com.tlongdev.bktf;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ThanhLong on 2014.12.25..
 */
public class Utility {

    public static String getSteamId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_steam_id), null);
    }
}
