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
public interface HabitRoutineDao {

    // Insertion / Modification / Suppression
    @Insert
    long insert(Routine routine);

    @Update
    void update(Routine routine);

    @Delete
    void delete(Routine routine);

    // Liste des routines
    @Query("SELECT * FROM Routine ORDER BY name ASC")
    LiveData<List<Routine>> getAllRoutines();

    @Query("SELECT * FROM Routine WHERE isActive = 1 ORDER BY name ASC")
    LiveData<List<Routine>> getActiveRoutines();

    // Jointures : Habit ↔ Routine
    @Insert
    void insert(HabitRoutineCrossRef crossRef);

    @Query("DELETE FROM routine_cross_ref WHERE habitId = :habitId AND routineId = :routineId")
    void delete(int habitId, int routineId);

    @Transaction
    @Query("SELECT * FROM habit")
    LiveData<List<HabitWithRoutines>> getHabitsWithRoutines();

    @Transaction
    @Query("SELECT * FROM Routine")
    LiveData<List<RoutineWithHabits>> getRoutinesWithHabits();
}
