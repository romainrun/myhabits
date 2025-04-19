package com.rrtech.myhabits.data.db;

import androidx.room.Entity;

@Entity(
        tableName = "routine_cross_ref",
        primaryKeys = {"habitId", "routineId"}
)
public class HabitRoutineCrossRef {
    public int habitId;
    public int routineId;

    public HabitRoutineCrossRef(int habitId, int routineId) {
        this.habitId = habitId;
        this.routineId = routineId;
    }
}
