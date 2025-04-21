package com.rrtech.myhabits.fragment;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.adapter.HabitAdapter;
import com.rrtech.myhabits.adapter.HabitCompactAdapter;
import com.rrtech.myhabits.data.db.AppDatabase;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitCheck;
import com.rrtech.myhabits.data.model.Routine;
import com.rrtech.myhabits.data.model.HabitWithRoutines;
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
    private HabitAdapter todayAdapter;
    private HabitCompactAdapter nextAdapter, missedAdapter;
    private boolean celebrationTriggered = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // autorise le fragment à injecter un menu
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu); // menu_main.xml à adapter à ton fichier
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Lance le SettingsFragment (ou une activité si tu préfères)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeFragment_to_settingsFragment); // ou autre action
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

        todayAdapter = new HabitAdapter(requireContext(), binding.getRoot(), checkViewModel, this::refreshProgress);
        missedAdapter = new HabitCompactAdapter(requireContext(), true);
        nextAdapter = new HabitCompactAdapter(requireContext(), false);

        binding.recyclerHabitsToday.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHabitsMissed.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHabitsTomorrow.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.recyclerHabitsToday.setAdapter(todayAdapter);
        binding.recyclerHabitsMissed.setAdapter(missedAdapter);
        binding.recyclerHabitsTomorrow.setAdapter(nextAdapter);

        binding.textMotivation.setText(QuoteHelper.getRandomQuote());

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate yesterday = today.minusDays(1);

        int todayInt = today.getDayOfWeek().getValue();
        int tomorrowInt = tomorrow.getDayOfWeek().getValue();
        int yesterdayInt = yesterday.getDayOfWeek().getValue();

        AppDatabase.getInstance(requireContext()).routinesDao()
                .getAllHabitsWithRoutines()
                .observe(getViewLifecycleOwner(), habitWithRoutinesList -> {

                    List<Habit> todayHabits = new ArrayList<>();
                    Map<Integer, String> habitIdToRoutine = new HashMap<>();
                    List<Habit> tomorrowHabits = new ArrayList<>();
                    List<Habit> missedHabits = new ArrayList<>();
                    List<List<HabitCheck>> missedChecksList = new ArrayList<>();
                    List<List<HabitCheck>> tomorrowChecksList = new ArrayList<>();

                    for (HabitWithRoutines hwr : habitWithRoutinesList) {
                        Habit habit = hwr.habit;
                        if (habit.getRepeatDays() == null || habit.getRepeatDays().isEmpty()) continue;

                        List<Integer> repeatDays;
                        try {
                            repeatDays = Arrays.stream(habit.getRepeatDays().split(","))
                                    .map(String::trim)
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                        } catch (NumberFormatException e) {
                            continue;
                        }

                        if (repeatDays.contains(todayInt)) todayHabits.add(habit);
                        if (repeatDays.contains(tomorrowInt)) tomorrowHabits.add(habit);
                        if (repeatDays.contains(yesterdayInt)) missedHabits.add(habit);

                        String routineNames = hwr.routines.stream()
                                .map(Routine::getName)
                                .collect(Collectors.joining(", "));
                        habitIdToRoutine.put(habit.getId(), routineNames);

                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<HabitCheck> allChecks = checkViewModel.getChecksSync(habit.getId());
                            if (repeatDays.contains(yesterdayInt)) missedChecksList.add(allChecks);
                            if (repeatDays.contains(tomorrowInt)) tomorrowChecksList.add(allChecks);

                            if (missedChecksList.size() == missedHabits.size() &&
                                    tomorrowChecksList.size() == tomorrowHabits.size()) {
                                requireActivity().runOnUiThread(() -> {
                                    missedAdapter.submitList(missedHabits, missedChecksList);
                                    nextAdapter.submitList(tomorrowHabits, tomorrowChecksList);
                                });
                            }
                        });
                    }

                    todayAdapter.setRoutineMap(habitIdToRoutine);
                    missedAdapter.setRoutineMap(habitIdToRoutine);
                    nextAdapter.setRoutineMap(habitIdToRoutine);

                    habitViewModel.sortHabitsByReminderTime(todayHabits);
                    todayAdapter.submitList(new ArrayList<>(todayHabits));
                    binding.textEmptyToday.setVisibility(todayHabits.isEmpty() ? View.VISIBLE : View.GONE);
                    refreshProgress(todayHabits);
                });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void refreshProgress(List<Habit> todayHabits) {
        int total = todayHabits.size();
        if (total == 0) {
            updateProgressUI(0);
            return;
        }

        final int[] completed = {0};
        final int[] observed = {0};
        final Map<Integer, Boolean> checkStatusMap = new HashMap<>();

        for (Habit habit : todayHabits) {
            checkViewModel.hasCheckForToday(habit.getId()).observe(getViewLifecycleOwner(), isChecked -> {
                observed[0]++;
                if (Boolean.TRUE.equals(isChecked)) {
                    completed[0]++;
                }

                checkStatusMap.put(habit.getId(), Boolean.TRUE.equals(isChecked));
                todayAdapter.setCheckStatusMap(checkStatusMap);
                reorderAndSubmitHabits(todayHabits, checkStatusMap);

                if (observed[0] == total) {
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

        EmitterConfig emitterConfig = new Emitter(100L, java.util.concurrent.TimeUnit.MILLISECONDS).max(100);
        Party party = new PartyFactory(emitterConfig)
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
        int colorRes;

        if (progress < 34) {
            colorRes = R.color.progress_red;
        } else if (progress < 67) {
            colorRes = R.color.progress_orange;
        } else if (progress < 100) {
            colorRes = R.color.progress_yellow;
        } else {
            colorRes = R.color.progress_green;
        }

        int color = requireContext().getColor(colorRes);

        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.glow_progress);
        if (drawable != null) {
            drawable = drawable.mutate();
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            ClipDrawable clip = (ClipDrawable) layerDrawable.findDrawableByLayerId(android.R.id.progress);
            if (clip != null) {
                Drawable progressShape = clip.getDrawable();
                if (progressShape != null) {
                    progressShape.setTint(color);
                }
            }
            binding.progressDay.setProgressDrawable(drawable);
        }
    }

    private void reorderAndSubmitHabits(List<Habit> allHabits, Map<Integer, Boolean> checkStatusMap) {
        List<Habit> reordered = new ArrayList<>();
        boolean hasUncheckedAfter = false;

        for (int i = allHabits.size() - 1; i >= 0; i--) {
            Habit habit = allHabits.get(i);
            boolean isChecked = checkStatusMap.getOrDefault(habit.getId(), false);
            if (!isChecked) {
                hasUncheckedAfter = true;
                break;
            }
        }

        if (!hasUncheckedAfter) {
            // Pas besoin de réordonner si tous les items sont cochés
            todayAdapter.submitList(new ArrayList<>(allHabits));
            return;
        }

        List<Habit> unchecked = new ArrayList<>();
        List<Habit> checked = new ArrayList<>();

        for (Habit habit : allHabits) {
            boolean isChecked = checkStatusMap.getOrDefault(habit.getId(), false);
            if (isChecked) {
                checked.add(habit);
            } else {
                unchecked.add(habit);
            }
        }

        reordered.addAll(unchecked);
        reordered.addAll(checked);

        todayAdapter.submitList(reordered);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
