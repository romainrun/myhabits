package com.rrtech.myhabits.adapter;

import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.Routine;

public class RoutineListItem {
    public static final int TYPE_ROUTINE = 0;
    public static final int TYPE_HABIT = 1;

    public int type;
    public Routine routine;
    public Habit habit;

    public RoutineListItem(Routine routine) {
        this.type = TYPE_ROUTINE;
        this.routine = routine;
    }

    public RoutineListItem(Habit habit) {
        this.type = TYPE_HABIT;
        this.habit = habit;
    }
}
