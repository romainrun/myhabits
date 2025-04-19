package com.rrtech.myhabits.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.rrtech.myhabits.data.db.AppDatabase;
import com.rrtech.myhabits.data.db.HabitRoutineCrossRef;
import com.rrtech.myhabits.data.db.HabitRoutineDao;
import com.rrtech.myhabits.data.db.RoutinesDao;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitWithRoutines;
import com.rrtech.myhabits.data.model.Routine;
import com.rrtech.myhabits.data.model.RoutineWithHabits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabitRoutineRepository {

    private final RoutinesDao routinesDao;
    private final HabitRoutineDao habitRoutineDao;

    public HabitRoutineRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.routinesDao = db.routinesDao();
        this.habitRoutineDao = db.habitRoutineDao();
    }

    // 🔹 Routines simples
    public LiveData<List<Routine>> getAllRoutines() {
        return routinesDao.getAllRoutines();
    }

    public LiveData<List<Routine>> getActiveRoutines() {
        return routinesDao.getActiveRoutines();
    }

    public LiveData<Integer> getRoutineCount() {
        return routinesDao.getRoutineCount();
    }

    public void insertRoutine(Routine routine) {
        AppDatabase.databaseWriteExecutor.execute(() -> routinesDao.insert(routine));
    }

    public void updateRoutine(Routine routine) {
        AppDatabase.databaseWriteExecutor.execute(() -> routinesDao.update(routine));
    }

    public void deleteRoutine(Routine routine) {
        AppDatabase.databaseWriteExecutor.execute(() -> routinesDao.delete(routine));
    }

    // 🔹 Relations Habitude ⇄ Routine
    public void addHabitToRoutine(int habitId, int routineId) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                habitRoutineDao.insert(new HabitRoutineCrossRef(habitId, routineId)));
    }

    public void removeHabitFromRoutine(int habitId, int routineId) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                habitRoutineDao.delete(habitId, routineId));
    }

    // 🔹 Récupération des jointures
    public LiveData<HabitWithRoutines> getHabitWithRoutines(int habitId) {
        return routinesDao.getHabitWithRoutines(habitId);
    }

    public LiveData<List<HabitWithRoutines>> getAllHabitsWithRoutines() {
        return habitRoutineDao.getHabitsWithRoutines();
    }

    public LiveData<List<RoutineWithHabits>> getAllRoutinesWithHabits() {
        return habitRoutineDao.getRoutinesWithHabits();
    }

    public LiveData<Map<Long, List<Habit>>> getHabitsByRoutine() {
        MediatorLiveData<Map<Long, List<Habit>>> result = new MediatorLiveData<>();
        LiveData<List<RoutineWithHabits>> source = getAllRoutinesWithHabits();

        result.addSource(source, routineWithHabitsList -> {
            Map<Long, List<Habit>> map = new HashMap<>();
            for (RoutineWithHabits rw : routineWithHabitsList) {
                map.put((long) rw.routine.getId(), new ArrayList<>(rw.habits));
            }
            result.setValue(map);
        });

        return result;
    }
}
