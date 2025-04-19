package com.rrtech.myhabits.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitWithRoutines;
import com.rrtech.myhabits.data.model.Routine;
import com.rrtech.myhabits.data.model.RoutineWithHabits;
import com.rrtech.myhabits.data.repository.HabitRoutineRepository;

import java.util.List;
import java.util.Map;

public class RoutinesViewModel extends AndroidViewModel {

    private final HabitRoutineRepository repository;

    public RoutinesViewModel(@NonNull Application application) {
        super(application);
        repository = new HabitRoutineRepository(application);
    }

    /**
     * Routines actives ou non, triées par nom.
     */
    public LiveData<List<Routine>> getAllRoutines() {
        return repository.getAllRoutines();
    }

    public LiveData<List<Routine>> getActiveRoutines() {
        return repository.getActiveRoutines();
    }

    /**
     * Insère une routine.
     */
    public void insert(Routine routine) {
        repository.insertRoutine(routine);
    }

    public void update(Routine routine) {
        repository.updateRoutine(routine);
    }

    public void delete(Routine routine) {
        repository.deleteRoutine(routine);
    }

    /**
     * Association/dissociation avec des habitudes.
     */
    public void addHabitToRoutine(int habitId, int routineId) {
        repository.addHabitToRoutine(habitId, routineId);
    }

    public void removeHabitFromRoutine(long routineId, long habitId) {
        repository.removeHabitFromRoutine((int) habitId, (int) routineId);
    }

    public void moveHabitToAnotherRoutine(long habitId, long fromRoutineId, long toRoutineId) {
        repository.removeHabitFromRoutine((int) habitId, (int) fromRoutineId);
        repository.addHabitToRoutine((int) habitId, (int) toRoutineId);
    }

    /**
     * Jointures.
     */
    public LiveData<List<HabitWithRoutines>> getHabitsWithRoutines() {
        return repository.getAllHabitsWithRoutines();
    }

    public LiveData<List<RoutineWithHabits>> getRoutinesWithHabits() {
        return repository.getAllRoutinesWithHabits();
    }

    public LiveData<Map<Long, List<Habit>>> getHabitsByRoutine() {
        return repository.getHabitsByRoutine();
    }
}
