package com.rrtech.myhabits.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.rrtech.myhabits.data.model.HabitCheck;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HabitAnalytics {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int calculateStreak(List<HabitCheck> checks) {
        if (checks == null || checks.isEmpty()) return 0;

        // On convertit les dates cochées en Set pour accès rapide
        Set<String> checkedDates = new HashSet<>();
        for (HabitCheck check : checks) {
            checkedDates.add(check.date); // format: yyyy-MM-dd
        }

        int streak = 0;
        LocalDate date = LocalDate.now();

        // Tant que le jour est dans les dates cochées, on incrémente
        while (checkedDates.contains(date.toString())) {
            streak++;
            date = date.minusDays(1);
        }

        return streak;
    }
}
