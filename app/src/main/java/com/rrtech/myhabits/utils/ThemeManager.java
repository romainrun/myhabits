package com.rrtech.myhabits.utils;

import android.app.Activity;
import android.content.SharedPreferences;

import com.rrtech.myhabits.R;

public class ThemeManager {
    public static void applyTheme(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("settings", Activity.MODE_PRIVATE);
        int theme = prefs.getInt("theme", 0);
        int accent = prefs.getInt("accent", 0);

        if (theme == 1) activity.setTheme(R.style.Theme_MyHabits);
        else {
            switch (accent) {
                case 1: activity.setTheme(R.style.Theme_MyHabits); break;
                case 2: activity.setTheme(R.style.Theme_MyHabits); break;
                default: activity.setTheme(R.style.Theme_MyHabits); break;
            }
        }
    }
}
