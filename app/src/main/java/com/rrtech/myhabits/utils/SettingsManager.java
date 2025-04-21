package com.rrtech.myhabits.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

public class SettingsManager {

    private static final String PREF_NAME = "app_settings";

    private static final String KEY_24H_FORMAT = "use_24h_format";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_REMINDER_SOUND = "reminder_sound_uri";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_SNOOZE_DURATION = "snooze_duration_minutes";

    private static final String KEY_FIRST_DAY_OF_WEEK = "first_day_of_week";

    // Format 24h
    public static boolean is24HFormat(Context context) {
        return getPrefs(context).getBoolean(KEY_24H_FORMAT, true);
    }

    public static void set24HFormat(Context context, boolean use24H) {
        getPrefs(context).edit().putBoolean(KEY_24H_FORMAT, use24H).apply();
    }

    // Notifications globales
    public static boolean areNotificationsEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public static void setNotificationsEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    // Vibration
    public static boolean isVibrationEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    public static void setVibrationEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
    }

    // Son de rappel
    public static String getReminderSound(Context context) {
        return getPrefs(context).getString(KEY_REMINDER_SOUND, null); // null = silence
    }

    public static void setReminderSound(Context context, String uriString) {
        getPrefs(context).edit().putString(KEY_REMINDER_SOUND, uriString).apply();
    }

    // Durée de snooze
    public static int getSnoozeDuration(Context context) {
        return getPrefs(context).getInt(KEY_SNOOZE_DURATION, 10); // 10 min par défaut
    }

    public static void setSnoozeDuration(Context context, int minutes) {
        getPrefs(context).edit().putInt(KEY_SNOOZE_DURATION, minutes).apply();
    }

    // Réinitialisation des préférences aux valeurs par défaut
    public static void resetToDefaults(Context context) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(KEY_24H_FORMAT, true);
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        editor.putBoolean(KEY_VIBRATION_ENABLED, true);
        editor.putString(KEY_REMINDER_SOUND, null);
        editor.putInt(KEY_SNOOZE_DURATION, 10);
        editor.apply();
    }

    // Accès aux prefs
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int getFirstDayOfWeek(Context context) {
        return getPrefs(context).getInt(KEY_FIRST_DAY_OF_WEEK, Calendar.MONDAY); // par défaut : lundi
    }

    public static void setFirstDayOfWeek(Context context, int day) {
        getPrefs(context).edit().putInt(KEY_FIRST_DAY_OF_WEEK, day).apply();
    }
}
