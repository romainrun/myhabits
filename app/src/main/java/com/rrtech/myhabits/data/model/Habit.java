package com.rrtech.myhabits.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "habit")
public class Habit {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "icon")
    private String icon;

    @ColumnInfo(name = "color")
    private String color;

    @ColumnInfo(name = "repeat_days")
    private String repeatDays;

    @ColumnInfo(name = "importance")
    private int importance;

    @ColumnInfo(name = "has_reminder")
    private boolean hasReminder;

    @ColumnInfo(name = "reminder_time")
    private String reminderTime;

    @ColumnInfo(name = "reminder_offset_minutes")
    private int reminderOffsetMinutes;

    @ColumnInfo(name = "group_id")
    private Integer groupId;

    @Ignore
    private String levelLabel;

    @Ignore
    private int streakCount = 0;

    @Ignore
    private int level = 1;

    // ✅ Constructeur utilisé par Room
    public Habit(int id, String name, String icon, String color, String repeatDays, int importance,
                 boolean hasReminder, String reminderTime, int reminderOffsetMinutes, Integer groupId) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.repeatDays = repeatDays;
        this.importance = importance;
        this.hasReminder = hasReminder;
        this.reminderTime = reminderTime;
        this.reminderOffsetMinutes = reminderOffsetMinutes;
        this.groupId = groupId;
    }

    // ✅ Constructeurs secondaires pour création
    @Ignore
    public Habit(String name, String icon, String color, String repeatDays, int importance) {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.repeatDays = repeatDays;
        this.importance = importance;
        this.hasReminder = false;
        this.reminderTime = "";
        this.reminderOffsetMinutes = 0;
        this.groupId = null;
    }

    @Ignore
    public Habit(int id, String name, String icon, String color, String repeatDays, int importance) {
        this(name, icon, color, repeatDays, importance);
        this.id = id;
    }

    public List<Integer> getRepeatDaysAsList() {
        List<Integer> list = new ArrayList<>();
        if (repeatDays == null || repeatDays.isEmpty()) return list;
        for (String s : repeatDays.split(",")) {
            list.add(Integer.parseInt(s.trim()));
        }
        return list;
    }

    // ✅ Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getRepeatDays() { return repeatDays; }
    public void setRepeatDays(String repeatDays) { this.repeatDays = repeatDays; }

    public int getImportance() { return importance; }
    public void setImportance(int importance) { this.importance = importance; }

    public boolean isHasReminder() { return hasReminder; }
    public void setHasReminder(boolean hasReminder) { this.hasReminder = hasReminder; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public int getReminderOffsetMinutes() { return reminderOffsetMinutes; }
    public void setReminderOffsetMinutes(int reminderOffsetMinutes) { this.reminderOffsetMinutes = reminderOffsetMinutes; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public String getLevelLabel() { return levelLabel; }
    public void setLevelLabel(String levelLabel) { this.levelLabel = levelLabel; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Habit)) return false;
        Habit habit = (Habit) o;
        return id == habit.getId() &&
                importance == habit.importance &&
                Objects.equals(name, habit.getName()) &&
                Objects.equals(icon, habit.getIcon()) &&
                Objects.equals(color, habit.getColor()) &&
                Objects.equals(repeatDays, habit.getRepeatDays());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, icon, color, repeatDays, importance);
    }
}