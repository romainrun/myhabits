package com.rrtech.myhabits.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rrtech.myhabits.data.model.Habit;

import java.util.List;

@Dao
public interface HabitDao {

    @Insert
    long insert(Habit habit);

    @Update
    void update(Habit habit);

    @Delete
    void delete(Habit habit);
    @Insert
    long insertAndReturnId(Habit habit);@Insert
    void insertCrossRef(HabitRoutineCrossRef crossRef);


    @Query("SELECT * FROM habit ORDER BY id DESC")
    LiveData<List<Habit>> getAllHabits();
    @Query("SELECT * FROM habit ORDER BY importance DESC")
    LiveData<List<Habit>> getAllSortedByImportance();
    @Query("SELECT * FROM habit WHERE id = :id LIMIT 1")
    LiveData<Habit> getHabitById(int id);

    @Query("SELECT * FROM habit WHERE group_id = :groupId")
    LiveData<List<Habit>> getHabitsForGroup(int groupId);

    // ✅ méthode synchrone utilisée pour vérifier si la base est vide
    @Query("SELECT * FROM habit")
    List<Habit> getAllHabitsSync();

    // ✅ Habits attendus un jour donné, non cochés à la date
    @Query("""
        SELECT * FROM habit 
        WHERE repeat_days LIKE '%' || :dayKey || '%' 
        AND id NOT IN (
            SELECT habitId FROM habit_check WHERE date = :date
        )
    """)
    LiveData<List<Habit>> getHabitsMissedOn(String dayKey, String date);
}
