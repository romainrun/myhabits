package com.rrtech.myhabits.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitCheck;
import com.rrtech.myhabits.databinding.FragmentStatsBinding;
import com.rrtech.myhabits.ui.main.HabitCheckViewModel;
import com.rrtech.myhabits.ui.main.HabitViewModel;
import com.rrtech.myhabits.utils.HabitAnalytics;

import java.util.List;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private HabitViewModel habitViewModel;
    private HabitCheckViewModel checkViewModel;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        habitViewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);
        checkViewModel = new ViewModelProvider(requireActivity()).get(HabitCheckViewModel.class);

        habitViewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            binding.textTotalHabits.setText("Total d’habitudes : " + habits.size());

            int maxStreak = 0;
            Habit mostRegular = null;
            int mostChecks = 0;

            for (Habit habit : habits) {
                List<HabitCheck> checks = checkViewModel.getChecksSync(habit.getId());
                int streak = HabitAnalytics.calculateStreak(checks);
                if (streak > maxStreak) maxStreak = streak;
                if (checks.size() > mostChecks) {
                    mostChecks = checks.size();
                    mostRegular = habit;
                }
            }

            binding.textBestStreak.setText("Meilleur enchaînement : " + maxStreak + " jours");
            binding.textMostFrequent.setText("Habitude la plus régulière : " +
                    (mostRegular != null ? mostRegular.getName() : "-"));
        });

        checkViewModel.getTotalCheckCount().observe(getViewLifecycleOwner(), count ->
                binding.textTotalChecks.setText("Total de validations : " + count));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
