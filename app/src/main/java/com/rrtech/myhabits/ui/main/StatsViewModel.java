package com.rrtech.myhabits.ui.main;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rrtech.myhabits.data.db.AppDatabase;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitCheck;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StatsViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public LiveData<List<Habit>> getAllHabits() {
        return db.habitDao().getAllHabits();
    }

    public LiveData<List<HabitCheck>> getChecksForDate(String date) {
        return db.habitCheckDao().getChecksForDate(date);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LiveData<Integer> getTodayCompletionRate() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            List<Habit> habits = db.habitDao().getAllHabitsSync();
            int total = 0, checked = 0;
            String today = LocalDate.now().toString();

            for (Habit habit : habits) {
                if (habit.getRepeatDays() == null) continue;

                String day = LocalDate.now().getDayOfWeek().name().substring(0, 1).toUpperCase()
                        + LocalDate.now().getDayOfWeek().name().substring(1, 3).toLowerCase();
                List<String> repeatList = List.of(habit.getRepeatDays().split(","));

                if (repeatList.contains(day)) {
                    total++;
                    boolean isChecked = db.habitCheckDao()
                            .getChecksSync(habit.getId())
                            .stream()
                            .anyMatch(c -> c.date.equals(today));
                    if (isChecked) checked++;
                }
            }

            int progress = total == 0 ? 0 : (int) ((checked / (float) total) * 100);
            result.postValue(progress);
        });
        return result;
    }

    public LiveData<Integer> getTotalHabits() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            int count = db.habitDao().getAllHabitsSync().size();
            result.postValue(count);
        });
        return result;
    }

    public LiveData<Integer> getTotalChecks() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            int count = 0;
            List<Habit> habits = db.habitDao().getAllHabitsSync();
            for (Habit h : habits) {
                count += db.habitCheckDao().getChecksSync(h.getId()).size();
            }
            result.postValue(count);
        });
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LiveData<Integer> getBestStreak() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            int maxStreak = 0;
            for (Habit h : db.habitDao().getAllHabitsSync()) {
                List<HabitCheck> checks = db.habitCheckDao().getChecksSync(h.getId());
                int streak = computeStreak(checks);
                if (streak > maxStreak) maxStreak = streak;
            }
            result.postValue(maxStreak);
        });
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int computeStreak(List<HabitCheck> checks) {
        if (checks == null || checks.isEmpty()) return 0;

        Collections.sort(checks, Comparator.comparing((HabitCheck c) -> c.date).reversed());

        LocalDate today = LocalDate.now();
        int streak = 0;

        for (HabitCheck check : checks) {
            LocalDate checkDate = LocalDate.parse(check.date);
            if (checkDate.equals(today.minusDays(streak))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
