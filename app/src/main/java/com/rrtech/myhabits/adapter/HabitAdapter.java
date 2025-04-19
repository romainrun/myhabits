package com.rrtech.myhabits.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.rrtech.myhabits.R;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitCheck;
import com.rrtech.myhabits.databinding.ItemHabitBinding;
import com.rrtech.myhabits.ui.calendar.HabitCalendarActivity;
import com.rrtech.myhabits.ui.main.HabitCheckViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HabitAdapter extends ListAdapter<Habit, HabitAdapter.HabitViewHolder> {

    private final Context context;
    private final HabitCheckViewModel checkViewModel;
    private final View parentView;
    private final Map<Integer, String> routineMap = new HashMap<>();
    private final Consumer<List<Habit>> onHabitsChanged;
    private final Map<Integer, Boolean> checkStatusMap = new HashMap<>();

    public HabitAdapter(Context context, View parentView,
                        HabitCheckViewModel checkViewModel,
                        Consumer<List<Habit>> onHabitsChanged) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.parentView = parentView;
        this.checkViewModel = checkViewModel;
        this.onHabitsChanged = onHabitsChanged;
    }

    private static final DiffUtil.ItemCallback<Habit> DIFF_CALLBACK = new DiffUtil.ItemCallback<Habit>() {
        @Override
        public boolean areItemsTheSame(@NonNull Habit oldItem, @NonNull Habit newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Habit oldItem, @NonNull Habit newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitBinding binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HabitViewHolder(binding);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = getItem(position);
        ItemHabitBinding binding = holder.binding;

        String reminderTime = habit.getReminderTime();
        if (reminderTime != null && !reminderTime.isEmpty()) {
            binding.textReminderTime.setText("🕐 " + reminderTime);
            binding.textReminderTime.setVisibility(View.VISIBLE);
        } else {
            binding.textReminderTime.setVisibility(View.GONE);
        }

        binding.habitName.setText(habit.getName());
        binding.habitIcon.setText(habit.getIcon());
        binding.habitRepeatDays.setText(habit.getRepeatDays());

        int color = Color.parseColor(habit.getColor());
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(color);
        binding.habitIconWrapper.setBackground(iconBg);

        int brightness = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000;
        binding.habitIcon.setTextColor(brightness < 128 ? Color.WHITE : Color.BLACK);

        String importanceSymbol = habit.getImportance() == 3 ? "🔴" : habit.getId() == 2 ? "🔶" : "🔵";
        binding.textImportance.setText(importanceSymbol);

        String routineText = routineMap.get(habit.getId());
        binding.textRoutineName.setVisibility(routineText != null && !routineText.isEmpty() ? View.VISIBLE : View.GONE);
        binding.textRoutineName.setText("Routine : " + routineText);

        boolean isChecked = checkStatusMap.getOrDefault(habit.getId(), false);
        binding.habitCheck.setOnCheckedChangeListener(null);
        binding.habitCheck.setChecked(isChecked);

        binding.cardContainer.setCardBackgroundColor(
                isChecked ? ContextCompat.getColor(context, R.color.checked_background)
                        : ContextCompat.getColor(context, android.R.color.white)
        );

        if (isChecked) {
            binding.habitCheck.setChecked(true);
            binding.habitName.setAlpha(0.6f);
            binding.habitIconWrapper.setAlpha(0.6f);
            binding.habitName.setPaintFlags(binding.habitName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // optionnel
        } else {
            binding.habitCheck.setChecked(false);
            binding.habitName.setAlpha(1f);
            binding.habitIconWrapper.setAlpha(1f);
            binding.habitName.setPaintFlags(binding.habitName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        binding.cardContainer.setOnClickListener(v -> {
            String today = LocalDate.now().toString();
            boolean currentlyChecked = checkStatusMap.getOrDefault(habit.getId(), false);

            if (currentlyChecked) {
                checkViewModel.delete(new HabitCheck(today, habit.getId()));
                checkStatusMap.put(habit.getId(), false);
            } else {
                checkViewModel.insert(new HabitCheck(today, habit.getId()));
                checkStatusMap.put(habit.getId(), true);

                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                Snackbar.make(parentView, "👏 Habitude validée !", Snackbar.LENGTH_SHORT).show();
            }

            if (onHabitsChanged != null) {
                onHabitsChanged.accept(new ArrayList<>(getCurrentList()));
            }
        });

        binding.habitCalendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, HabitCalendarActivity.class);
            intent.putExtra("habitId", habit.getId());
            intent.putExtra("habitName", habit.getName());
            intent.putExtra("habitIcon", habit.getIcon());
            intent.putExtra("habitColor", habit.getColor());
            intent.putExtra("habitRepeatDays", habit.getRepeatDays());
            context.startActivity(intent);
        });
    }

    public void setRoutineMap(@NonNull Map<Integer, String> map) {
        routineMap.clear();
        routineMap.putAll(map);
        notifyDataSetChanged();
    }

    public void setCheckStatusMap(Map<Integer, Boolean> map) {
        checkStatusMap.clear();
        checkStatusMap.putAll(map);
        notifyDataSetChanged();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        final ItemHabitBinding binding;
        public HabitViewHolder(@NonNull ItemHabitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}