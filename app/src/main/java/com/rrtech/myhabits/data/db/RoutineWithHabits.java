package com.rrtech.myhabits.data.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.rrtech.myhabits.data.db.HabitRoutineCrossRef;

import java.util.List;

public class RoutineWithHabits {

    @Embedded
    public Routine routine;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = HabitRoutineCrossRef.class,
                    parentColumn = "routineId",
                    entityColumn = "habitId"
            )
    )
    public List<Habit> habits;
}
