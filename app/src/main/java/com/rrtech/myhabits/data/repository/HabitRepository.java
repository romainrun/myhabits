package com.rrtech.myhabits.data.repository;

import android.app.Application;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;

import com.rrtech.myhabits.data.db.AppDatabase;
import com.rrtech.myhabits.data.db.HabitDao;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.notifications.ReminderHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HabitRepository {

    private final HabitDao habitDao;
    private final LiveData<List<Habit>> allHabits;
    private final Application app;

    public HabitRepository(Application application) {
        this.app = application;
        AppDatabase db = AppDatabase.getInstance(application);
        habitDao = db.habitDao();
        allHabits = habitDao.getAllHabits();
    }

    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }

    public LiveData<List<Habit>> getAllByImportance() {
        return habitDao.getAllSortedByImportance();
    }

    public void insert(Habit habit) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = habitDao.insert(habit);
            habit.setId((int) id);

            if (habit.isHasReminder() && habit.getReminderTime() != null) {
                ReminderHelper.scheduleHabitReminder(
                        app,
                        habit.getId(),
                        habit.getName(),
                        habit.getReminderTime(),
                        habit.getReminderOffsetMinutes()
                );
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<Long> insertAndReturnId(Habit habit) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = habitDao.insert(habit);
            habit.setId((int) id);

            if (habit.isHasReminder() && habit.getReminderTime() != null) {
                ReminderHelper.scheduleHabitReminder(
                        app, habit.getId(), habit.getName(), habit.getReminderTime(), habit.getReminderOffsetMinutes()
                );
            }

            future.complete(id);
        });
        return future;
    }

    public void update(Habit habit) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            habitDao.update(habit);

            // Mise à jour du rappel
            ReminderHelper.cancelHabitReminder(app, habit.getId());
            if (habit.isHasReminder() && habit.getReminderTime() != null) {
                ReminderHelper.scheduleHabitReminder(
                        app,
                        habit.getId(),
                        habit.getName(),
                        habit.getReminderTime(),
                        habit.getReminderOffsetMinutes()
                );
            }
        });
    }

    public void delete(Habit habit) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            habitDao.delete(habit);
            ReminderHelper.cancelHabitReminder(app, habit.getId());
        });
    }

    public LiveData<Habit> getHabitById(int id) {
        return habitDao.getHabitById(id);
    }

    public LiveData<List<Habit>> getHabitsForGroup(int groupId) {
        return habitDao.getHabitsForGroup(groupId);
    }

    public LiveData<List<Habit>> getHabitsMissedOn(String dayKey, String date) {
        return habitDao.getHabitsMissedOn(dayKey, date);
    }
}
