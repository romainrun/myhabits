package com.rrtech.myhabits.adapter;

import android.content.ClipData;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.utils.HabitUtils;

import java.text.DateFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HabitInRoutineAdapter extends RecyclerView.Adapter<HabitInRoutineAdapter.HabitViewHolder> {

    public interface OnHabitActionListener {
        void onDeleteHabitFromRoutine(Habit habit);
    }

    public interface OnHabitDragListener {
        void onHabitMoved(Habit habit, int fromPosition, int toPosition);
    }

    private List<Habit> habits;
    private final OnHabitActionListener actionListener;
    private final OnHabitDragListener dragListener;

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
        Log.d("DEBUG_HABIT_ADAPTER", "Habit affiché : " + habit.getName());

        holder.habitName.setText(habit.getName());

        // Heure
        if (habit.getReminderTime() != null && !habit.getReminderTime().isEmpty()) {
            holder.habitTime.setText(habit.getReminderTime());
            holder.habitTime.setVisibility(View.VISIBLE);
        } else {
            holder.habitTime.setVisibility(View.GONE);
        }

        // Jours
        if (habit.getRepeatDays() != null && !habit.getRepeatDays().isEmpty()) {
            List<Integer> repeatDays = HabitUtils.parseDaysFromString(habit.getRepeatDays());
            String daysFormatted = formatDays(repeatDays);

            holder.habitDays.setText(daysFormatted);
            holder.habitDays.setVisibility(View.VISIBLE);
        } else {
            holder.habitDays.setVisibility(View.GONE);
        }

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

    public void updateHabits(List<Habit> newHabits) {
        this.habits = newHabits;
        notifyDataSetChanged();
    }

    private String formatDays(List<Integer> days) {
        String[] shortDays = new DateFormatSymbols(Locale.getDefault()).getShortWeekdays();
        return days.stream()
                .filter(d -> d >= 1 && d <= 7)
                .map(d -> shortDays[d]) // 1 = Sunday, 7 = Saturday
                .map(this::capitalizeFirstLetter)
                .collect(Collectors.joining(", "));
    }

    private String capitalizeFirstLetter(String s) {
        if (s == null || s.length() == 0) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView habitName;
        TextView habitTime;
        TextView habitDays;
        ImageButton deleteButton;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            habitName = itemView.findViewById(R.id.habitNameTextView);
            habitTime = itemView.findViewById(R.id.habitTimeTextView);
            habitDays = itemView.findViewById(R.id.habitDaysTextView);
            deleteButton = itemView.findViewById(R.id.deleteHabitButton);
        }
    }
}
