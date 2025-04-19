package com.rrtech.myhabits.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ProManager {

    private static ProManager instance;
    private final Context context;
    private final SharedPreferences prefs;

    private static final String PREFS_NAME = "pro_prefs";
    private static final String KEY_IS_PRO = "is_pro";

    private ProManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ProManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProManager(context);
        }
        return instance;
    }

    public boolean isPro() {

        return true;
   //    return prefs.getBoolean(KEY_IS_PRO, false);
    }

    public void setPro(boolean value) {
        prefs.edit().putBoolean(KEY_IS_PRO, value).apply();
    }

    public void resetProStatus() {
        prefs.edit().remove(KEY_IS_PRO).apply();
    }
}
