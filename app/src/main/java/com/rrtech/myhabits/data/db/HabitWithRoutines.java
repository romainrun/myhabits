package com.rrtech.myhabits.data.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.rrtech.myhabits.data.db.HabitRoutineCrossRef;

import java.util.List;

public class HabitWithRoutines {
    @Embedded
    public Habit habit;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = HabitRoutineCrossRef.class,
                    parentColumn = "habitId",
                    entityColumn = "routineId"
            )
    )
    public List<Routine> routines;
}

