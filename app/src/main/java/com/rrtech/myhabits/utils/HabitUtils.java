package com.rrtech.myhabits.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HabitUtils {
    public static int calculateLevel(int streakCount) {
        if (streakCount >= 30) return 5;
        if (streakCount >= 21) return 4;
        if (streakCount >= 14) return 3;
        if (streakCount >= 7) return 2;
        return 1;
    }

    public static String getLevelLabel(int level) {
        switch (level) {
            case 5: return "Niveau 5 – Maître 🏆";
            case 4: return "Niveau 4 – Expert 🥇";
            case 3: return "Niveau 3 – Régulier 🥉";
            case 2: return "Niveau 2 – En chemin 🐢";
            default: return "Niveau 1 – Débutant ✨";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getLastPlannedDayBeforeTodayInt(String repeatDays) {
        if (repeatDays == null || repeatDays.isEmpty()) return null;

        List<Integer> repeatList = new ArrayList<>();
        for (String day : repeatDays.split(",")) {
            try {
                repeatList.add(Integer.parseInt(day.trim()));
            } catch (NumberFormatException ignored) {}
        }

        LocalDate today = LocalDate.now();

        for (int i = 1; i <= 7; i++) {
            LocalDate previousDate = today.minusDays(i);
            int previousDayOfWeek = previousDate.getDayOfWeek().getValue(); // 1 = lundi, 7 = dimanche
            if (repeatList.contains(previousDayOfWeek)) {
                return previousDate.toString(); // ou .format(...) si tu veux une date formatée
            }
        }

        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int getCurrentDayNumber() {
        // 1 = lundi, 7 = dimanche (selon java.time.DayOfWeek)
        return LocalDate.now().getDayOfWeek().getValue();
    }

    public static List<Integer> parseDaysFromString(String daysString) {
        if (daysString == null || daysString.trim().isEmpty()) return Collections.emptyList();
        String[] parts = daysString.split(",");
        List<Integer> result = new java.util.ArrayList<>();
        for (String part : parts) {
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }

    private String formatDays(List<Integer> days) {
        String[] dayLabels = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < days.size(); i++) {
            int index = days.get(i) - 1;
            if (index >= 0 && index < 7) {
                builder.append(dayLabels[index]);
                if (i < days.size() - 1) builder.append(", ");
            }
        }
        return builder.toString();
    }

}
