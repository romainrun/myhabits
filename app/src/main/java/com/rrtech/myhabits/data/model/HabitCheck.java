package com.rrtech.myhabits.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(
        tableName = "habit_check",
        primaryKeys = {"habitId", "date"} // ✅ Clé composée
)
public class HabitCheck {

    @NonNull
    public String date;

    public int habitId;

    public HabitCheck(@NonNull String date, int habitId) {
        this.date = date;
        this.habitId = habitId;
    }


}
