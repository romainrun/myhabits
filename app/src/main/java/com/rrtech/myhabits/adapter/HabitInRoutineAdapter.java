package com.rrtech.myhabits.adapter;

import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.data.model.Habit;

import java.util.Collections;
import java.util.List;

// Adapter pour les habitudes à l'intérieur d'une routine avec drag & drop
public class HabitInRoutineAdapter extends RecyclerView.Adapter<HabitInRoutineAdapter.HabitViewHolder> {

    public interface OnHabitActionListener {
        void onDeleteHabitFromRoutine(Habit habit);
    }

    public interface OnHabitDragListener {
        void onHabitMoved(Habit habit, int fromPosition, int toPosition);
    }

    private List<Habit> habits;
    private OnHabitActionListener actionListener;
    private OnHabitDragListener dragListener;

    public HabitInRoutineAdapter(List<Habit> habits, OnHabitActionListener actionListener, OnHabitDragListener dragListener) {
        this.habits = habits;
        this.actionListener = actionListener;
        this.dragListener = dragListener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit_in_routine, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.habitName.setText(habit.getName());

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Supprimer l’habitude ?")
                    .setMessage("Elle sera retirée de cette routine.")
                    .setPositiveButton("Supprimer", (dialog, which) -> actionListener.onDeleteHabitFromRoutine(habit))
                    .setNegativeButton("Annuler", null)
                    .show();
        });

        holder.itemView.setOnLongClickListener(v -> {
            ClipData data = ClipData.newPlainText("habit_id", String.valueOf(habit.getId()));
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadowBuilder, v, 0);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    public Habit getHabitAt(int position) {
        return habits.get(position);
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < habits.size() && toPosition < habits.size()) {
            Collections.swap(habits, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            if (dragListener != null) {
                dragListener.onHabitMoved(habits.get(toPosition), fromPosition, toPosition);
            }
        }
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView habitName;
        ImageButton deleteButton;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            habitName = itemView.findViewById(R.id.habitNameTextView);
            deleteButton = itemView.findViewById(R.id.deleteHabitButton);
        }
    }

    public void updateHabits(List<Habit> newHabits) {
        this.habits = newHabits;
        notifyDataSetChanged();
    }
}

