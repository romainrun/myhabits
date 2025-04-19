package com.rrtech.myhabits.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.rrtech.myhabits.data.db.AppDatabase;
import com.rrtech.myhabits.data.db.HabitCheckDao;
import com.rrtech.myhabits.data.model.HabitCheck;

import java.util.List;

public class HabitCheckRepository {

    private final HabitCheckDao checkDao;

    public HabitCheckRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        checkDao = db.habitCheckDao();
    }

    public void insert(HabitCheck check) {
        AppDatabase.databaseWriteExecutor.execute(() -> checkDao.insert(check));
    }

    public void delete(HabitCheck check) {
        AppDatabase.databaseWriteExecutor.execute(() -> checkDao.delete(check));
    }

    public LiveData<HabitCheck> getCheckForHabit(int habitId, String date) {
        return checkDao.getCheckForHabit(habitId, date);
    }

    public LiveData<List<HabitCheck>> getChecksForDate(String date) {
        return checkDao.getChecksForDate(date);
    }
    public void deleteAllChecksForHabit(int habitId) {
        AppDatabase.databaseWriteExecutor.execute(() -> checkDao.deleteAllChecksForHabit(habitId));
    }
}
