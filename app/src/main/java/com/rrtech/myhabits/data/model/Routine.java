package com.rrtech.myhabits.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "routine")
public class Routine {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String name;

    private boolean isActive;

    @Ignore
    private boolean isPlaceholder = false; // ✅ ajouté pour la logique UI

    public Routine(@NonNull String name, boolean isActive) {
        this.name = name;
        this.isActive = isActive;
    }

    @Ignore
    public Routine(@NonNull String name, boolean isActive, boolean isPlaceholder) {
        this.name = name;
        this.isActive = isActive;
        this.isPlaceholder = isPlaceholder;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    @NonNull
    public String getName() { return name; }

    public void setName(@NonNull String name) { this.name = name; }

    public boolean isActive() { return isActive; }

    public void setActive(boolean active) { isActive = active; }

    public boolean isPlaceholder() { return isPlaceholder; }

    public void setPlaceholder(boolean placeholder) { isPlaceholder = placeholder; }
}
