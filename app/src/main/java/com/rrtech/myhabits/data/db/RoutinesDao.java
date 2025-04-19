package com.rrtech.myhabits.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.rrtech.myhabits.data.model.Routine;
import com.rrtech.myhabits.data.model.HabitWithRoutines;
import com.rrtech.myhabits.data.model.RoutineWithHabits;

import java.util.List;

@Dao
public interface RoutinesDao {

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
    void delete(Routine routine);
    @Insert
    long insertAndReturnId(Routine routine);

    // 🔹 Relations avec habitudes

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
