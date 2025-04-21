package com.rrtech.myhabits.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.rrtech.myhabits.data.model.HabitCheck;

import java.util.List;

@Dao
public interface HabitCheckDao {

    @Delete
    void delete(HabitCheck check);

    // ✅ Récupère un check spécifique pour une date
    @Query("SELECT * FROM habit_check WHERE habitId = :habitId AND date = :date LIMIT 1")
    LiveData<HabitCheck> getCheckForHabit(int habitId, String date);

    // ✅ Tous les checks pour un habit (LiveData)
    @Query("SELECT * FROM habit_check WHERE habitId = :habitId")
    LiveData<List<HabitCheck>> getAllChecksForHabit(int habitId);

    // ✅ Tous les checks pour un jour donné (ex: pour les stats globales)
    @Query("SELECT * FROM habit_check WHERE date = :date")
    LiveData<List<HabitCheck>> getChecksForDate(String date);

    // ✅ Nombre total de checks pour une habitude
    @Query("SELECT COUNT(*) FROM habit_check WHERE habitId = :habitId")
    LiveData<Integer> getCheckCountForHabit(int habitId);

    // ✅ Insertion avec remplacement si conflit (habitId + date)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HabitCheck check);

    // ✅ Tous les checks (synchrone, pour calcul de streak)
    @Query("SELECT * FROM habit_check WHERE habitId = :habitId ORDER BY date DESC")
    List<HabitCheck> getChecksSync(int habitId);

    // ✅ Supprimer tous les checks liés à une habitude
    @Query("DELETE FROM habit_check WHERE habitId = :habitId")
    void deleteAllChecksForHabit(int habitId);

    // ✅ Total global de validations (toutes habitudes confondues)
    @Query("SELECT COUNT(*) FROM habit_check")
    LiveData<Integer> getTotalCheckCount();

    // ✅ Check synchrone pour un habitId + date (utile pour vérification dans les threads)
    @Query("SELECT * FROM habit_check WHERE habitId = :habitId AND date = :date LIMIT 1")
    HabitCheck getCheckForHabitSync(int habitId, String date);
}
