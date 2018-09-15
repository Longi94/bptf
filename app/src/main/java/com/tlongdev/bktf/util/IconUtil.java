package com.tlongdev.bktf.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

public class IconUtil {

    private static final String TAG = IconUtil.class.getSimpleName();

    private static Map<Integer, String> AUSTRALIUM_ICONS;

    public static void loadIcons(Context context) {
        Gson gson = new Gson();

        try {
            InputStream australiumStream = context.getAssets().open("australium_images.json");

            Type type = new TypeToken<Map<Integer, String>>() {
            }.getType();

            AUSTRALIUM_ICONS = gson.fromJson(new InputStreamReader(australiumStream), type);
        } catch (IOException e) {
            Log.w(TAG, "Failed to load australium icon links", e);
        }
    }

    public static String getAustraliumIcon(int defindex) {
        return AUSTRALIUM_ICONS.get(defindex);
    }
}
