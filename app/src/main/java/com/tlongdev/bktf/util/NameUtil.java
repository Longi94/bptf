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

public class NameUtil {

    private static final String TAG = NameUtil.class.getSimpleName();

    private static Map<Integer, String> WAR_PAINT_NAMES;

    public static void loadWarPaintNames(Context context) {
        Gson gson = new Gson();

        try {
            InputStream warPaintStream = context.getAssets().open("war_paint_names.json");

            Type type = new TypeToken<Map<Integer, String>>() {
            }.getType();

            WAR_PAINT_NAMES = gson.fromJson(new InputStreamReader(warPaintStream), type);
        } catch (IOException e) {
            Log.w(TAG, "Failed to load war paint names", e);
        }
    }

    public static String getWarPaintName(int paintId) {
        return WAR_PAINT_NAMES.get(paintId);
    }
}
