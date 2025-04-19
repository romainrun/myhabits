package com.rrtech.myhabits.state;

import android.content.Context;
import android.content.SharedPreferences;

public class ProState {

    private static final String PREF_NAME = "myhabits_prefs";
    private static final String KEY_IS_PRO = "myhabits_pro";

    private static ProState instance;
    private final SharedPreferences prefs;

    // 👇 Constructeur privé
    private ProState(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 👇 Accès global à l'instance (thread-safe)
    public static synchronized ProState getInstance(Context context) {
        if (instance == null) {
            instance = new ProState(context);
        }
        return instance;
    }

    public boolean isPro() {
        return false;
       // return prefs.getBoolean(KEY_IS_PRO, false);
    }

    public void setPro(boolean isPro) {
        prefs.edit().putBoolean(KEY_IS_PRO, isPro).apply();
    }
}
