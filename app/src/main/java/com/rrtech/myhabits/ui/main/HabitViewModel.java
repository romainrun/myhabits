package com.rrtech.myhabits.ui.main;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.Query;

import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.repository.HabitRepository;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HabitViewModel extends AndroidViewModel {

    private final HabitRepository repository;
    private final LiveData<List<Habit>> allHabits;
    private final LiveData<List<Habit>> allByImportance;

    public HabitViewModel(@NonNull Application application) {
        super(application);
        repository = new HabitRepository(application);
        allHabits = repository.getAllHabits();
        allByImportance = repository.getAllByImportance();
    }

    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }
    public LiveData<Habit> getHabitById(int habitId) {
        return repository.getHabitById(habitId);
    }
    public LiveData<List<Habit>> getAllByImportance() {
        return allByImportance;
    }

    public void insert(Habit habit) {
        repository.insert(habit);
    }

    public void update(Habit habit) {
        repository.update(habit);
    }

    public void delete(Habit habit) {
        repository.delete(habit);
    }

    public LiveData<List<Habit>> getHabitsMissedOn(String dayKey, String date) {
        return repository.getHabitsMissedOn(dayKey, date);
    }

    /**
     * ⏱️ Insertion avec retour d'ID (asynchrone)
     */
    public CompletableFuture<Long> insertAndReturnId(Habit habit) {
        return repository.insertAndReturnId(habit);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sortHabitsByReminderTime(List<Habit> habits) {
        if (habits == null) return;

        Collections.sort(habits, (h1, h2) -> {
            try {
                if (h1.getReminderTime() == null || h2.getReminderTime() == null) return 0;
                LocalTime t1 = LocalTime.parse(h1.getReminderTime(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime t2 = LocalTime.parse(h2.getReminderTime(), DateTimeFormatter.ofPattern("HH:mm"));
                return t1.compareTo(t2);
            } catch (Exception e) {
                return 0;
            }
        });
    }

}
