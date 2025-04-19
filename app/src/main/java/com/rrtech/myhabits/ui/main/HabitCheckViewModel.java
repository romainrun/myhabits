package com.rrtech.myhabits.ui.main;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rrtech.myhabits.data.db.AppDatabase;
import com.rrtech.myhabits.data.model.HabitCheck;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HabitCheckViewModel extends AndroidViewModel {

    private final AppDatabase db;

    public HabitCheckViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public LiveData<List<HabitCheck>> getAllChecks(int habitId) {
        return db.habitCheckDao().getAllChecksForHabit(habitId);
    }

    public LiveData<HabitCheck> getCheckForHabit(int habitId, String date) {
        return db.habitCheckDao().getCheckForHabit(habitId, date);
    }

    public void insert(HabitCheck check) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                db.habitCheckDao().insert(check));
    }

    public void delete(HabitCheck check) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                db.habitCheckDao().delete(check));
    }

    public void deleteAllChecksForHabit(int habitId) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                db.habitCheckDao().deleteAllChecksForHabit(habitId));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LiveData<Integer> calculateStreak(int habitId) {
        MutableLiveData<Integer> streakLiveData = new MutableLiveData<>();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<HabitCheck> checks = db.habitCheckDao().getChecksSync(habitId);
            int streak = computeStreak(checks);
            streakLiveData.postValue(streak);
        });
        return streakLiveData;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LiveData<Boolean> hasCheckForToday(int habitId) {
        String today = LocalDate.now().toString();
        return androidx.lifecycle.Transformations.map(
                getCheckForHabit(habitId, today),
                check -> check != null
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int computeStreak(List<HabitCheck> checks) {
        if (checks == null || checks.isEmpty()) return 0;

        Collections.sort(checks, Comparator.comparing((HabitCheck c) -> c.date).reversed());

        LocalDate today = LocalDate.now();
        int streak = 0;

        for (HabitCheck check : checks) {
            try {
                LocalDate checkDate = LocalDate.parse(check.date);
                if (checkDate.equals(today.minusDays(streak))) {
                    streak++;
                } else if (checkDate.isBefore(today.minusDays(streak))) {
                    break;
                }
            } catch (Exception e) {
                // Ignore malformed dates
            }
        }

        return streak;
    }

    /**
     * ✅ Synchronous call for all checks of one habit (used for stats).
     */
    public List<HabitCheck> getChecksSync(int habitId) {
        return db.habitCheckDao().getChecksSync(habitId);
    }

    /**
     * ✅ Retourne le nombre total de validations (tous habits confondus).
     */
    public LiveData<Integer> getTotalCheckCount() {
        return db.habitCheckDao().getTotalCheckCount();
    }
}
