package com.rrtech.myhabits.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.Routine;
import com.rrtech.myhabits.data.model.HabitWithRoutines;

import java.util.List;

@Dao
public interface RoutinesDao {

    @Insert void insertHabit(Habit habit);
    @Update void updateHabit(Habit habit);
    @Delete void deleteHabit(Habit habit);
    @Insert
    void insertCrossRef(HabitRoutineCrossRef crossRef);

    // 🔹 Routines simples
    @Query("SELECT * FROM Routine ORDER BY name ASC")
    LiveData<List<Routine>> getAllRoutines();

    @Query("SELECT * FROM Routine WHERE isActive = 1 ORDER BY name ASC")
    LiveData<List<Routine>> getActiveRoutines();

    @Query("SELECT COUNT(*) FROM Routine")
    LiveData<Integer> getRoutineCount();

    @Insert
    void insert(Routine routine);

    @Update
    void update(Routine routine);

    @Delete
    void delete(Routine routine);@Insert
    long insertAndReturnId(Habit habit);@Insert
    long insertRoutineAndReturnId(Routine routine);
    @Insert
    void insertHabitRoutineCrossRef(HabitRoutineCrossRef crossRef); // bon
    // 🔹 Relations avec habitudes
    @Query("SELECT * FROM Routine WHERE id = :id")
    Routine getRoutineByIdSync(int id);

    @Transaction
    @Query("SELECT * FROM habit WHERE id = :habitId")
    LiveData<HabitWithRoutines> getHabitWithRoutines(int habitId);

    @Transaction
    @Query("SELECT * FROM Routine WHERE id = :routineId")
    LiveData<RoutineWithHabits> getRoutineWithHabits(int routineId);

    @Transaction
    @Query("SELECT * FROM habit")
    LiveData<List<HabitWithRoutines>> getAllHabitsWithRoutines();

    @Transaction
    @Query("SELECT * FROM Routine")
    LiveData<List<RoutineWithHabits>> getAllRoutinesWithHabits();
}
