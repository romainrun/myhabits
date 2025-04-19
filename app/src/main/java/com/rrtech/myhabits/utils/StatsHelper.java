package com.rrtech.myhabits.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitCheck;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatsHelper {

    /**
     * Calcule le meilleur streak à partir d'une liste de checks.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int calculateStreak(List<HabitCheck> checks) {
        if (checks == null || checks.isEmpty()) return 0;

        Collections.sort(checks, Comparator.comparing(hc -> hc.date));

        int streak = 1;
        int maxStreak = 1;

        for (int i = 1; i < checks.size(); i++) {
            LocalDate prev = LocalDate.parse(checks.get(i - 1).date);
            LocalDate current = LocalDate.parse(checks.get(i).date);
            if (prev.plusDays(1).equals(current)) {
                streak++;
                maxStreak = Math.max(maxStreak, streak);
            } else {
                streak = 1;
            }
        }

        return maxStreak;
    }

    /**
     * Retourne l’habitude avec le plus de checks parmi une liste.
     */
    public static Habit getMostFrequentHabit(List<Habit> habits, List<List<HabitCheck>> allChecks) {
        int maxCount = 0;
        Habit result = null;

        for (int i = 0; i < habits.size(); i++) {
            List<HabitCheck> checks = allChecks.get(i);
            if (checks.size() > maxCount) {
                maxCount = checks.size();
                result = habits.get(i);
            }
        }

        return result;
    }

    /**
     * Calcule le total global de validations.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static int getTotalCheckCount(List<List<HabitCheck>> allChecks) {
        return allChecks.stream().mapToInt(List::size).sum();
    }

    /**
     * Trouve la dernière habitude validée (par date).
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static HabitCheck getLastCheck(List<HabitCheck> checks) {
        return checks.stream()
                .max(Comparator.comparing(hc -> hc.date))
                .orElse(null);
    }
}
