package com.rrtech.myhabits.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.rrtech.myhabits.data.db.AppDatabase;
import com.rrtech.myhabits.data.db.HabitRoutineCrossRef;
import com.rrtech.myhabits.data.db.RoutineWithHabits;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitWithRoutines;
import com.rrtech.myhabits.data.model.Routine;

import com.rrtech.myhabits.data.repository.HabitRoutineRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import android.util.Pair;


public class RoutinesViewModel extends AndroidViewModel {
    private final MediatorLiveData<Pair<List<Routine>, Map<Long, List<Habit>>>> combinedData = new MediatorLiveData<>();
    private final HabitRoutineRepository repository;
    private final Map<Integer, Routine> routineByHabitId = new HashMap<>();

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
    public void insertHabitAndAssignToRoutine(Habit habit, int routineId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long newId = AppDatabase.getInstance(getApplication())
                    .habitDao()
                    .insertAndReturnId(habit);

            AppDatabase.getInstance(getApplication())
                    .routinesDao()
                    .insertCrossRef(new HabitRoutineCrossRef((int) newId, routineId));

            Routine routine = routineByHabitId.get(habit.getId());
            if (routine == null) {
                routine = AppDatabase.getInstance(getApplication())
                        .routinesDao()
                        .getRoutineByIdSync(routineId);
            }

            if (routine != null) {
                routineByHabitId.put((int) newId, routine);
            }
        });
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

    public LiveData<Pair<List<Routine>, Map<Long, List<Habit>>>> getCombinedRoutineData() {
        combinedData.addSource(getAllRoutines(), routines -> {
            Map<Long, List<Habit>> currentMap = combinedData.getValue() != null ? combinedData.getValue().second : new HashMap<>();
            combinedData.setValue(new Pair<>(routines, currentMap));
        });

        combinedData.addSource(getHabitsByRoutine(), map -> {
            List<Routine> currentRoutines = combinedData.getValue() != null ? combinedData.getValue().first : new ArrayList<>();
            combinedData.setValue(new Pair<>(currentRoutines, map));
        });

        return combinedData;
    }

    public void updateHabitRoutineMapping(List<Routine> routines, Map<Long, List<Habit>> habitsMap) {
        routineByHabitId.clear();
        for (Routine routine : routines) {
            List<Habit> habits = habitsMap.get((long) routine.getId());
            if (habits != null) {
                for (Habit habit : habits) {
                    routineByHabitId.put(habit.getId(), routine);
                }
            }
        }
    }

    public CompletableFuture<Long> insertHabitAndReturnId(Habit habit) {
        return repository.insertHabitAndReturnId(habit);
    }

    public Routine getRoutineForHabit(int habitId) {
        return routineByHabitId.get(habitId);
    }

    // ✅ Insérer une nouvelle habitude (pour duplication)
    public void insertHabit(Habit habit) {
        repository.insertHabit(habit);
    }

    // ✅ Supprimer une habitude
    public void deleteHabit(Habit habit) {
        repository.deleteHabit(habit);
    }

    // ✅ Mettre à jour une habitude (pour modification)
    public void updateHabit(Habit habit) {
        repository.updateHabit(habit);
    }
}
