package com.rrtech.myhabits.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitCheck;
import com.rrtech.myhabits.databinding.ItemHabitCompactBinding;
import com.rrtech.myhabits.ui.calendar.HabitCalendarActivity;
import com.rrtech.myhabits.utils.HabitUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HabitCompactAdapter extends RecyclerView.Adapter<HabitCompactAdapter.HabitCompactViewHolder> {

    private final Context context;
    private final boolean isMissedAdapter;
    private final List<Habit> habits = new ArrayList<>();
    private final List<List<HabitCheck>> allChecks = new ArrayList<>();
    private final Map<Integer, String> routineMap = new java.util.HashMap<>();

    public HabitCompactAdapter(Context context, boolean isMissedAdapter) {
        this.context = context;
        this.isMissedAdapter = isMissedAdapter;
    }

    public void submitList(List<Habit> list, List<List<HabitCheck>> checksList) {
        habits.clear();
        allChecks.clear();

        if (list != null) habits.addAll(list);
        if (checksList != null) allChecks.addAll(checksList);

        notifyDataSetChanged();
    }

    public void setRoutineMap(@NonNull Map<Integer, String> map) {
        routineMap.clear();
        routineMap.putAll(map);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitCompactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitCompactBinding binding = ItemHabitCompactBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HabitCompactViewHolder(binding);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull HabitCompactViewHolder holder, int position) {
        Habit habit = habits.get(position);
        List<HabitCheck> checks = (position < allChecks.size()) ? allChecks.get(position) : new ArrayList<>();
        ItemHabitCompactBinding binding = holder.binding;

        binding.habitNameCompact.setText(habit.getName());
        binding.habitIconCompact.setText(habit.getIcon());

        int color = Color.parseColor(habit.getColor());
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        binding.habitIconCompactWrapper.setBackground(bg);

        // 🔁 Routines
        String routineText = routineMap.get(habit.getId());
        if (routineText != null && !routineText.isEmpty()) {
            binding.textRoutineNameCompact.setText(routineText);
            binding.textRoutineNameCompact.setVisibility(View.VISIBLE);
        } else {
            binding.textRoutineNameCompact.setVisibility(View.GONE);
        }

        // Dernier jour prévu
        String lastDay = HabitUtils.getLastPlannedDayBeforeTodayInt(habit.getRepeatDays());
        if (lastDay != null) {
            binding.textLastPlanned.setText("Dernier jour prévu : " + lastDay);
            binding.textLastPlanned.setVisibility(View.VISIBLE);
        } else {
            binding.textLastPlanned.setVisibility(View.GONE);
        }

        // Jours manqués
        if (isMissedAdapter) {
            int missedDays = calculateMissedDays(checks, habit.getRepeatDays());
            if (missedDays > 0) {
                binding.textMissedCount.setText("Non validée depuis " + missedDays + " jour" + (missedDays > 1 ? "s" : ""));
                binding.textMissedCount.setVisibility(View.VISIBLE);
            } else {
                binding.textMissedCount.setVisibility(View.GONE);
            }
        } else {
            binding.textMissedCount.setVisibility(View.GONE);
        }

        binding.habitCompactRoot.setOnClickListener(v -> {
            Intent intent = new Intent(context, HabitCalendarActivity.class);
            intent.putExtra("habitId", habit.getId());
            intent.putExtra("habitName", habit.getName());
            intent.putExtra("habitIcon", habit.getIcon());
            intent.putExtra("habitColor", habit.getColor());
            intent.putExtra("habitRepeatDays", habit.getRepeatDays());
            context.startActivity(intent);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int calculateMissedDays(List<HabitCheck> checks, String repeatDays) {
        if (repeatDays == null || repeatDays.isEmpty()) return 0;

        List<Integer> repeatList = new ArrayList<>();
        for (String s : repeatDays.split(",")) {
            try {
                repeatList.add(Integer.parseInt(s.trim()));
            } catch (NumberFormatException ignored) {}
        }

        LocalDate today = LocalDate.now();
        int missedDays = 0;

        for (int i = 1; i <= 30; i++) {
            LocalDate date = today.minusDays(i);
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1 = lundi, 7 = dimanche

            if (!repeatList.contains(dayOfWeek)) continue;

            boolean checked = checks.stream().anyMatch(c -> c.date.equals(date.toString()));
            if (!checked) missedDays++;
            else break;
        }

        return missedDays;
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitCompactViewHolder extends RecyclerView.ViewHolder {
        final ItemHabitCompactBinding binding;

        public HabitCompactViewHolder(@NonNull ItemHabitCompactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
