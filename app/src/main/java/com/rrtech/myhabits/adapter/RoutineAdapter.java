package com.rrtech.myhabits.adapter;

import android.content.ClipData;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.Routine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Adapter pour afficher les routines et les habitudes associées
public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    public interface OnRoutineActionListener {
        void onDeleteRoutine(Routine routine);
        void onDeleteHabitFromRoutine(Routine routine, Habit habit);
        void onHabitMovedBetweenRoutines(Habit habit, Routine fromRoutine, Routine toRoutine);
    }

    private List<Routine> routines;
    private Map<Long, List<Habit>> habitsByRoutineId;
    private OnRoutineActionListener listener;

    public RoutineAdapter(List<Routine> routines, Map<Long, List<Habit>> habitsByRoutineId, OnRoutineActionListener listener) {
        this.routines = routines;
        this.habitsByRoutineId = habitsByRoutineId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine routine = routines.get(position);
        holder.routineName.setText(routine.getName());

        holder.deleteRoutineButton.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Supprimer la routine ?")
                    .setMessage("Cela supprimera la routine et toutes ses associations.")
                    .setPositiveButton("Supprimer", (dialog, which) -> listener.onDeleteRoutine(routine))
                    .setNegativeButton("Annuler", null)
                    .show();
        });

        List<Habit> habits = habitsByRoutineId.getOrDefault(routine.getId(), new ArrayList<>());
        HabitInRoutineAdapter habitAdapter = new HabitInRoutineAdapter(
                habits,
                habit -> listener.onDeleteHabitFromRoutine(routine, habit),
                (habit, fromPos, toPos) -> { /* Local reorder, pas encore inter-routine */ });

        holder.habitsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.habitsRecyclerView.setAdapter(habitAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new HabitItemTouchHelperCallback(habitAdapter));
        itemTouchHelper.attachToRecyclerView(holder.habitsRecyclerView);

        holder.habitsRecyclerView.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                ClipData.Item item = event.getClipData().getItemAt(0);
                long habitId = Long.parseLong(item.getText().toString());

                // Trouver l’habitude et notifier le listener
                Habit movedHabit = findHabitById(habitId);
                if (movedHabit != null && !habits.contains(movedHabit)) {
                    listener.onHabitMovedBetweenRoutines(movedHabit, findCurrentRoutineOfHabit(movedHabit), routine);
                }
                return true;
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return routines.size();
    }

    public static class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView routineName;
        ImageButton deleteRoutineButton;
        RecyclerView habitsRecyclerView;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            routineName = itemView.findViewById(R.id.routineNameTextView);
            deleteRoutineButton = itemView.findViewById(R.id.deleteRoutineButton);
            habitsRecyclerView = itemView.findViewById(R.id.habitsRecyclerView);
        }
    }

    public void updateRoutines(List<Routine> newRoutines, Map<Long, List<Habit>> newHabitsMap) {
        this.routines = newRoutines;
        this.habitsByRoutineId = newHabitsMap;
        notifyDataSetChanged();
    }



    private Habit findHabitById(long habitId) {
        for (List<Habit> habits : habitsByRoutineId.values()) {
            for (Habit h : habits) {
                if (h.getId() == habitId) return h;
            }
        }
        return null;
    }
    private Routine findCurrentRoutineOfHabit(Habit habit) {
        for (Routine routine : routines) {
            List<Habit> habits = habitsByRoutineId.get(routine.getId());
            if (habits != null && habits.contains(habit)) {
                return routine;
            }
        }
        return null;
    }
}
