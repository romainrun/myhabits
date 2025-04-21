package com.rrtech.myhabits.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREF_NAME = "app_settings";
    private static final String KEY_24H_FORMAT = "use_24h_format";

    public static boolean is24HFormat(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_24H_FORMAT, true);
    }

    public static void set24HFormat(Context context, boolean use24H) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_24H_FORMAT, use24H).apply();
    }
}
