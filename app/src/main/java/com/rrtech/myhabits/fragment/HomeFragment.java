package com.rrtech.myhabits.fragment;// ... imports inchangés ...

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.rrtech.myhabits.R;
import com.rrtech.myhabits.adapter.HabitAdapter;
import com.rrtech.myhabits.data.db.AppDatabase;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitWithRoutines;
import com.rrtech.myhabits.data.model.Routine;
import com.rrtech.myhabits.databinding.FragmentHomeBinding;
import com.rrtech.myhabits.ui.main.HabitCheckViewModel;
import com.rrtech.myhabits.ui.main.HabitViewModel;
import com.rrtech.myhabits.utils.QuoteHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HabitViewModel habitViewModel;
    private HabitCheckViewModel checkViewModel;
    private HabitAdapter habitAdapter;

    private final List<Habit> allTodayHabits = new ArrayList<>();
    private final List<Habit> allMissedHabits = new ArrayList<>();
    private final List<Habit> allTomorrowHabits = new ArrayList<>();
    private final Map<Integer, String> habitRoutineMap = new HashMap<>();
    private final Map<Integer, Boolean> checkStatusMap = new HashMap<>();
    private boolean celebrationTriggered = false;

    private enum HabitFilter { TODAY, MISSED, TOMORROW }
    private HabitFilter currentFilter = HabitFilter.TODAY;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_settingsFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        habitViewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);
        checkViewModel = new ViewModelProvider(requireActivity()).get(HabitCheckViewModel.class);

        habitAdapter = new HabitAdapter(requireContext(), binding.getRoot(), checkViewModel, this::refreshProgress);
        binding.recyclerHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHabits.setAdapter(habitAdapter);
        binding.textMotivation.setText(QuoteHelper.getRandomQuote());

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Aujourd’hui"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Ratés"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Demain"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = HabitFilter.values()[tab.getPosition()];
                updateDisplayedHabits();
                if (currentFilter == HabitFilter.TODAY) {
                    refreshProgress(allTodayHabits);
                } else {
                    updateProgressUI(0);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadHabits();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadHabits() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate yesterday = today.minusDays(1);

        int todayInt = today.getDayOfWeek().getValue();
        int tomorrowInt = tomorrow.getDayOfWeek().getValue();
        int yesterdayInt = yesterday.getDayOfWeek().getValue();

        AppDatabase.getInstance(requireContext()).routinesDao()
                .getAllHabitsWithRoutines()
                .observe(getViewLifecycleOwner(), habitWithRoutinesList -> {

                    allTodayHabits.clear();
                    allTomorrowHabits.clear();
                    allMissedHabits.clear();
                    habitRoutineMap.clear();
                    checkStatusMap.clear();

                    List<LiveData<Boolean>> missedChecks = new ArrayList<>();
                    List<Habit> pendingMissedCheckHabits = new ArrayList<>();

                    for (HabitWithRoutines hwr : habitWithRoutinesList) {
                        Habit habit = hwr.habit;
                        if (habit.getRepeatDays() == null || habit.getRepeatDays().isEmpty()) continue;

                        List<Integer> repeatDays = Arrays.stream(habit.getRepeatDays().split(","))
                                .map(String::trim).map(Integer::parseInt).collect(Collectors.toList());

                        if (repeatDays.contains(todayInt)) allTodayHabits.add(habit);
                        if (repeatDays.contains(tomorrowInt)) allTomorrowHabits.add(habit);
                        if (repeatDays.contains(yesterdayInt)) {
                            pendingMissedCheckHabits.add(habit);
                            missedChecks.add(checkViewModel.hasCheckForDate(habit.getId(), yesterday));
                        }

                        String routineNames = hwr.routines.stream().map(Routine::getName).collect(Collectors.joining(", "));
                        habitRoutineMap.put(habit.getId(), routineNames);
                    }

                    MediatorLiveData<Void> mediator = new MediatorLiveData<>();
                    final int[] received = {0};

                    for (int i = 0; i < missedChecks.size(); i++) {
                        Habit habit = pendingMissedCheckHabits.get(i);
                        LiveData<Boolean> liveData = missedChecks.get(i);
                        mediator.addSource(liveData, new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean checked) {
                                if (!Boolean.TRUE.equals(checked)) {
                                    allMissedHabits.add(habit);
                                }
                                received[0]++;
                                mediator.removeSource(liveData);

                                if (received[0] == missedChecks.size()) {
                                    updateDisplayedHabits();
                                }
                            }
                        });
                    }

                    updateDisplayedHabits();
                    refreshProgress(allTodayHabits);
                });
    }

    private void updateDisplayedHabits() {
        List<Habit> habitsToShow;
        switch (currentFilter) {
            case MISSED: habitsToShow = allMissedHabits; break;
            case TOMORROW: habitsToShow = allTomorrowHabits; break;
            case TODAY:
            default: habitsToShow = allTodayHabits; break;
        }

        habitAdapter.setRoutineMap(habitRoutineMap);
        habitAdapter.setCheckStatusMap(checkStatusMap); // 👈 Important
        habitAdapter.submitList(new ArrayList<>(habitsToShow));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void refreshProgress(List<Habit> habits) {
        if (habits.isEmpty()) {
            updateProgressUI(0);
            return;
        }

        final int[] completed = {0};
        final int[] observed = {0};
        int total = habits.size();

        for (Habit habit : habits) {
            checkViewModel.hasCheckForToday(habit.getId()).observe(getViewLifecycleOwner(), isChecked -> {
                observed[0]++;
                checkStatusMap.put(habit.getId(), Boolean.TRUE.equals(isChecked));

                if (Boolean.TRUE.equals(isChecked)) completed[0]++;

                if (observed[0] == total) {
                    updateDisplayedHabits(); // 👈 met à jour l'affichage des couleurs vertes
                    int progress = (int) ((completed[0] / (float) total) * 100);
                    updateProgressUI(progress);
                    if (progress == 100 && !celebrationTriggered) {
                        celebrationTriggered = true;
                        triggerCelebration();
                    } else if (progress < 100) {
                        celebrationTriggered = false;
                    }
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateProgressUI(int progress) {
        binding.progressDay.setProgress(progress);
        binding.textProgressPercentage.setText(progress + "%");
        updateProgressColor(progress);
        binding.progressDay.post(() -> {
            Animation glow = AnimationUtils.loadAnimation(requireContext(), R.anim.progress_glow_anim);
            binding.progressDay.startAnimation(glow);
        });
    }

    private void triggerCelebration() {
        Toast.makeText(requireContext(), "Objectif atteint ! 🎯", Toast.LENGTH_SHORT).show();
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        EmitterConfig config = new Emitter(100L, java.util.concurrent.TimeUnit.MILLISECONDS).max(100);
        Party party = new PartyFactory(config)
                .angle(Angle.TOP)
                .spread(90)
                .setSpeedBetween(10f, 30f)
                .position(new Position.Relative(0.5, 0.0))
                .sizes(new Size(12, 5f, 1f))
                .shapes(Shape.Circle.INSTANCE)
                .build();

        KonfettiView konfettiView = requireActivity().findViewById(R.id.konfettiView);
        if (konfettiView != null) {
            konfettiView.start(party);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateProgressColor(int progress) {
        int colorRes = (progress < 34) ? R.color.progress_red :
                (progress < 67) ? R.color.progress_orange :
                        (progress < 100) ? R.color.progress_yellow :
                                R.color.progress_green;

        int color = requireContext().getColor(colorRes);
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.glow_progress);
        if (drawable instanceof LayerDrawable) {
            LayerDrawable layer = (LayerDrawable) drawable.mutate();
            ClipDrawable clip = (ClipDrawable) layer.findDrawableByLayerId(android.R.id.progress);
            if (clip != null) {
                Drawable progressShape = clip.getDrawable();
                if (progressShape != null) progressShape.setTint(color);
            }
            binding.progressDay.setProgressDrawable(drawable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
