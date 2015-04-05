package com.tlongdev.bktf.log;

import android.content.Context;
import android.util.Log;

import com.tlongdev.bktf.Utility;

public class DLog {

    public static void v(Context context, String tag, String... message) {
        if (!Utility.isDebugging(context)) return;
        StringBuilder builder = new StringBuilder();
        for(String s : message) {
            builder.append(s);
        }
        Log.v(tag, builder.toString());
    }

    public static void d(Context context, String tag, String... message) {
        if (!Utility.isDebugging(context)) return;
        StringBuilder builder = new StringBuilder();
        for(String s : message) {
            builder.append(s);
        }
        Log.d(tag, builder.toString());
    }

}
