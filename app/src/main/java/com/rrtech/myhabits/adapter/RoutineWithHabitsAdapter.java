package com.rrtech.myhabits.adapter;

import android.content.ClipData;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.adapter.RoutineListItem;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.Routine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoutineWithHabitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnHabitDropListener {
        void onHabitDropped(Habit habit, Routine fromRoutine, Routine toRoutine);
    }

    public interface OnHabitClickListener {
        void onHabitClicked(Habit habit);           // Pour l'édition
        void onHabitDeleted(Habit habit);           // Pour suppression
        void onHabitDuplicated(Habit habit);        // Pour duplication
    }

    private final List<RoutineListItem> items = new ArrayList<>();
    private final Map<Integer, Routine> routineByHabitId = new HashMap<>();
    private final Set<Habit> selectedHabits = new HashSet<>();
    private boolean editMode = false;

    private OnHabitDropListener habitDropListener;
    private OnHabitClickListener habitClickListener;

    public void setOnHabitDropListener(OnHabitDropListener listener) {
        this.habitDropListener = listener;
    }

    public void setOnHabitClickListener(OnHabitClickListener listener) {
        this.habitClickListener = listener;
    }

    public void setEditMode(boolean enabled) {
        this.editMode = enabled;
        selectedHabits.clear();
        notifyDataSetChanged();
    }

    public boolean isInEditMode() {
        return editMode;
    }

    public void exitEditMode() {
        this.editMode = false;
        selectedHabits.clear();
        notifyDataSetChanged();
    }

    public Set<Habit> getSelectedHabits() {
        return selectedHabits;
    }

    public void submitData(List<Routine> routines, Map<Long, List<Habit>> habitsMap) {
        items.clear();
        routineByHabitId.clear();

        for (Routine routine : routines) {
            items.add(new RoutineListItem(routine));
            List<Habit> habits = habitsMap.getOrDefault((long) routine.getId(), new ArrayList<>());
            for (Habit habit : habits) {
                items.add(new RoutineListItem(habit));
                routineByHabitId.put(habit.getId(), routine);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == RoutineListItem.TYPE_ROUTINE) {
            View view = inflater.inflate(R.layout.item_routine_title, parent, false);
            return new RoutineViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_habit_in_routine, parent, false);
            return new HabitViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RoutineListItem item = items.get(position);

        if (item.type == RoutineListItem.TYPE_ROUTINE) {
            RoutineViewHolder vh = (RoutineViewHolder) holder;
            Routine routine = item.routine;
            vh.routineName.setText(routine.getName());

            vh.itemView.setOnDragListener((v, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setBackgroundColor(v.getContext().getResources().getColor(R.color.routine_drag_highlight));
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                    case DragEvent.ACTION_DRAG_ENDED:
                        v.setBackgroundColor(android.R.color.transparent);
                        return true;
                    case DragEvent.ACTION_DROP:
                        v.setBackgroundColor(android.R.color.transparent);
                        ClipData.Item dragItem = event.getClipData().getItemAt(0);
                        long habitId = Long.parseLong(dragItem.getText().toString());
                        Habit movedHabit = findHabitById((int) habitId);
                        Routine fromRoutine = findRoutineOfHabit(movedHabit);

                        if (movedHabit != null && fromRoutine != null && habitDropListener != null
                                && fromRoutine.getId() != routine.getId()) {
                            habitDropListener.onHabitDropped(movedHabit, fromRoutine, routine);
                        }
                        return true;
                }
                return true;
            });

        } else {
            HabitViewHolder vh = (HabitViewHolder) holder;
            Habit habit = item.habit;

            vh.habitName.setText(habit.getName());
            vh.habitTime.setText(habit.getReminderTime() != null && !habit.getReminderTime().isEmpty()
                    ? habit.getReminderTime() : "--:--");

            vh.checkbox.setVisibility(editMode ? View.VISIBLE : View.GONE);
            vh.checkbox.setChecked(selectedHabits.contains(habit));

            vh.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedHabits.add(habit);
                } else {
                    selectedHabits.remove(habit);
                }
            });

            vh.itemView.setOnClickListener(v -> {
                if (editMode) {
                    vh.checkbox.setChecked(!vh.checkbox.isChecked());
                } else if (habitClickListener != null) {
                    habitClickListener.onHabitClicked(habit);
                }
            });

            vh.itemView.setOnLongClickListener(v -> {
                ClipData data = ClipData.newPlainText("habit_id", String.valueOf(habit.getId()));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDragAndDrop(data, shadowBuilder, v, 0);
                return true;
            });

            vh.deleteButton.setOnClickListener(v -> {
                if (habitClickListener != null) {
                    habitClickListener.onHabitDeleted(habit);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void toggleEditMode() {
        setEditMode(!editMode);
    }

    public void onHabitMovedLocally(int fromPosition, int toPosition) {
        RoutineListItem fromItem = items.get(fromPosition);
        if (fromItem.type != RoutineListItem.TYPE_HABIT) return;

        items.remove(fromPosition);
        items.add(toPosition, fromItem);
        notifyItemMoved(fromPosition, toPosition);
    }

    private Habit findHabitById(int habitId) {
        for (RoutineListItem item : items) {
            if (item.type == RoutineListItem.TYPE_HABIT && item.habit.getId() == habitId) {
                return item.habit;
            }
        }
        return null;
    }

    private Routine findRoutineOfHabit(Habit habit) {
        return routineByHabitId.get(habit.getId());
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView routineName;

        RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            routineName = itemView.findViewById(R.id.routineNameTextView);
        }
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView habitName;
        TextView habitTime;
        ImageButton deleteButton;
        CheckBox checkbox;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            habitName = itemView.findViewById(R.id.habitNameTextView);
            habitTime = itemView.findViewById(R.id.habitTimeTextView);
            deleteButton = itemView.findViewById(R.id.deleteHabitButton);
            checkbox = itemView.findViewById(R.id.habitCheckbox);
        }
    }

    public class HabitItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final RoutineWithHabitsAdapter adapter;

        public HabitItemTouchHelperCallback(RoutineWithHabitsAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return (viewHolder instanceof HabitViewHolder)
                    ? makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0)
                    : 0;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            if (viewHolder instanceof HabitViewHolder && target instanceof HabitViewHolder) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                adapter.onHabitMovedLocally(fromPos, toPos);
                return true;
            }
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
    }
}
