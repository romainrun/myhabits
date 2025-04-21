package com.rrtech.myhabits.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.data.db.AppDatabase;
import com.rrtech.myhabits.data.model.ColorItem;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.Routine;
import com.rrtech.myhabits.databinding.FragmentAddEditHabitBinding;
import com.rrtech.myhabits.state.ProState;
import com.rrtech.myhabits.ui.main.HabitViewModel;
import com.rrtech.myhabits.ui.main.RoutinesViewModel;
import com.rrtech.myhabits.utils.SettingsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddEditHabitFragment extends Fragment {

    private FragmentAddEditHabitBinding binding;
    private HabitViewModel habitViewModel;
    private RoutinesViewModel routinesViewModel;
    private final List<Spinner> routineSpinners = new ArrayList<>();
    private final List<ArrayAdapter<String>> spinnerAdapters = new ArrayList<>();
    private final List<Routine> routineList = new ArrayList<>();

    private String selectedHabitIcon = "💪";
    private String selectedHabitColor = "#4CAF50";
    private String reminderTime = "";
    private int reminderOffsetMinutes = 0;
    private int editingHabitId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddEditHabitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        habitViewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);
        routinesViewModel = new ViewModelProvider(requireActivity()).get(RoutinesViewModel.class);

        boolean isPro = ProState.getInstance(requireContext()).isPro();

        if (getArguments() != null && getArguments().containsKey("habitId")) {
            editingHabitId = getArguments().getInt("habitId", -1);
            if (editingHabitId != -1) {
                habitViewModel.getHabitById(editingHabitId).observe(getViewLifecycleOwner(), habit -> {
                    if (habit != null) {
                        binding.editHabitName.setText(habit.getName());
                        selectedHabitIcon = habit.getIcon();
                        selectedHabitColor = habit.getColor();
                        binding.textSelectedIcon.setText(selectedHabitIcon);
                        binding.viewSelectedColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(selectedHabitColor)));
                        reminderTime = habit.getReminderTime();
                        binding.editReminderTime.setText(reminderTime);
                        binding.switchReminder.setChecked(habit.isHasReminder());

                        String[] days = habit.getRepeatDays().split(",");
                        for (String d : days) {
                            switch (d.trim()) {
                                case "1": binding.chipMon.setChecked(true); break;
                                case "2": binding.chipTue.setChecked(true); break;
                                case "3": binding.chipWed.setChecked(true); break;
                                case "4": binding.chipThu.setChecked(true); break;
                                case "5": binding.chipFri.setChecked(true); break;
                                case "6": binding.chipSat.setChecked(true); break;
                                case "7": binding.chipSun.setChecked(true); break;
                            }
                        }
                    }
                });
            }
        }

        reorderDayChips();

        observeRoutines();

        binding.buttonAddRoutine.setOnClickListener(v -> {
            if (!isPro && routineSpinners.size() >= 1) {
                Toast.makeText(requireContext(), "Version gratuite : une seule routine par habitude.", Toast.LENGTH_SHORT).show();
            } else {
                showRoutineSelectionBottomSheet();
            }
        });

        binding.viewSelectedColor.setOnClickListener(v -> {
            List<ColorItem> colorItems = Arrays.asList(
                    new ColorItem(0xFF4CAF50, false),
                    new ColorItem(0xFF2196F3, false),
                    new ColorItem(0xFFFF9800, true),
                    new ColorItem(0xFFE91E63, true),
                    new ColorItem(0xFF9C27B0, false)
            );

            ColorPickerBottomSheet sheet = new ColorPickerBottomSheet(colorItems, isPro, item -> {
                selectedHabitColor = String.format("#%06X", (0xFFFFFF & item.getColor()));
                binding.viewSelectedColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(selectedHabitColor)));
            });

            sheet.show(getParentFragmentManager(), "ColorPicker");
        });

        binding.textSelectedIcon.setOnClickListener(v -> {
            EmojiPickerBottomSheet sheet = new EmojiPickerBottomSheet(isPro, emoji -> {
                selectedHabitIcon = emoji;
                binding.textSelectedIcon.setText(emoji);
            });
            sheet.show(getParentFragmentManager(), "EmojiPicker");
        });

        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.layoutReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        binding.layoutTimePicker.setOnClickListener(v -> {
            TimePickerBottomSheet sheet = new TimePickerBottomSheet((hour, minute) -> {
                reminderTime = String.format("%02d:%02d", hour, minute);
                binding.editReminderTime.setText(reminderTime);
            });
            sheet.show(getParentFragmentManager(), "TimePickerBottomSheet");
        });

        binding.editReminderTime.setOnClickListener(v -> binding.layoutTimePicker.performClick());

        ArrayAdapter<String> offsetAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Exactement", "5 min avant", "10 min avant", "15 min avant", "30 min avant")
        );
        binding.spinnerReminderOffset.setAdapter(offsetAdapter);
        binding.spinnerReminderOffset.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1: reminderOffsetMinutes = 5; break;
                    case 2: reminderOffsetMinutes = 10; break;
                    case 3: reminderOffsetMinutes = 15; break;
                    case 4: reminderOffsetMinutes = 30; break;
                    default: reminderOffsetMinutes = 0; break;
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        habitViewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            if (!isPro && habits.size() >= 5) {
                binding.buttonSaveHabit.setEnabled(false);
                binding.buttonSaveHabit.setAlpha(0.5f);
                binding.textLimitInfo.setVisibility(View.VISIBLE);
            } else {
                binding.buttonSaveHabit.setEnabled(true);
                binding.buttonSaveHabit.setAlpha(1f);
                binding.textLimitInfo.setVisibility(View.GONE);
            }
        });

        binding.buttonSaveHabit.setOnClickListener(v -> saveHabit(isPro));

        if (editingHabitId != -1) {
            binding.buttonDeleteHabit.setVisibility(View.VISIBLE);
        }
        binding.buttonDeleteHabit.setOnClickListener(v -> {
            if (editingHabitId != -1) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Supprimer cette habitude ?")
                        .setMessage("Cette action est irréversible.")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            habitViewModel.getHabitById(editingHabitId).observe(getViewLifecycleOwner(), habit -> {
                                if (habit != null) {
                                    habitViewModel.delete(habit);
                                    navigateToHome();
                                }
                            });
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
            }
        });
    }

    private void saveHabit(boolean isPro) {
        String habitName = binding.editHabitName.getText().toString().trim();
        if (habitName.isEmpty()) {
            binding.editHabitName.setError("Veuillez nommer l’habitude");
            return;
        }

        List<Integer> repeatDays = new ArrayList<>();
        if (binding.chipMon.isChecked()) repeatDays.add(1);
        if (binding.chipTue.isChecked()) repeatDays.add(2);
        if (binding.chipWed.isChecked()) repeatDays.add(3);
        if (binding.chipThu.isChecked()) repeatDays.add(4);
        if (binding.chipFri.isChecked()) repeatDays.add(5);
        if (binding.chipSat.isChecked()) repeatDays.add(6);
        if (binding.chipSun.isChecked()) repeatDays.add(7);

        String repeatDaysString = TextUtils.join(",", repeatDays);

        Habit habit = new Habit(habitName, selectedHabitIcon, selectedHabitColor, repeatDaysString, 1);
        habit.setHasReminder(binding.switchReminder.isChecked());
        habit.setReminderTime(reminderTime);
        habit.setReminderOffsetMinutes(reminderOffsetMinutes);

        List<Integer> selectedRoutineIds = new ArrayList<>();
        for (Spinner spinner : routineSpinners) {
            int selectedIndex = spinner.getSelectedItemPosition();
            if (selectedIndex >= 0 && selectedIndex < routineList.size()) {
                selectedRoutineIds.add(routineList.get(selectedIndex).getId());
            }
        }

        if (editingHabitId != -1) {
            habit.setId(editingHabitId);
            habitViewModel.update(habit);
            for (int routineId : selectedRoutineIds) {
                routinesViewModel.addHabitToRoutine(editingHabitId, routineId);
            }
            requireActivity().onBackPressed();
        } else {
            habitViewModel.insertAndReturnId(habit).thenAccept(habitId -> {
                requireActivity().runOnUiThread(() -> {
                    for (int routineId : selectedRoutineIds) {
                        routinesViewModel.addHabitToRoutine(habitId.intValue(), routineId);
                    }
                    requireActivity().onBackPressed();
                });
            });
        }
    }private void navigateToHome() {
        if (getActivity() != null) {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack(); // OU : navController.navigate(R.id.homeFragment);
        }
    }

    private void observeRoutines() {
        routinesViewModel.getAllRoutines().observe(getViewLifecycleOwner(), routines -> {
            routineList.clear();
            if (routines != null) {
                routineList.addAll(routines);
                for (int i = 0; i < routineSpinners.size(); i++) {
                    ArrayAdapter<String> adapter = spinnerAdapters.get(i);
                    adapter.clear();
                    for (Routine routine : routineList) {
                        adapter.add(routine.getName());
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void showRoutineSelectionBottomSheet() {
        RoutineSelectionBottomSheet sheet = new RoutineSelectionBottomSheet(routineList, (routine, isNew) -> {
            if (isNew) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    int id = (int) AppDatabase.getInstance(requireContext())
                            .routinesDao().insertRoutineAndReturnId(routine);
                    routine.setId(id);
                    requireActivity().runOnUiThread(() -> {
                        routineList.add(routine);
                        addRoutineSpinner(routine);
                    });
                });
            } else {
                addRoutineSpinner(routine);
            }
        });
        sheet.show(getParentFragmentManager(), "RoutineBottomSheet");
    }

    private void addRoutineSpinner(@Nullable Routine selectedRoutine) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View routineRow = inflater.inflate(R.layout.item_routine_spinner_with_delete, binding.layoutRoutinesContainer, false);

        Spinner spinner = routineRow.findViewById(R.id.spinner_routine);
        ImageButton deleteButton = routineRow.findViewById(R.id.button_delete_routine);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());

        for (Routine routine : routineList) {
            adapter.add(routine.getName());
        }

        spinner.setAdapter(adapter);
        if (selectedRoutine != null) {
            int index = routineList.indexOf(selectedRoutine);
            if (index >= 0) spinner.setSelection(index);
        }

        routineSpinners.add(spinner);
        spinnerAdapters.add(adapter);

        deleteButton.setOnClickListener(v -> {
            routineRow.animate()
                    .alpha(0f)
                    .translationY(-routineRow.getHeight() / 2f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        binding.layoutRoutinesContainer.removeView(routineRow);
                        routineSpinners.remove(spinner);
                        spinnerAdapters.remove(adapter);
                    })
                    .start();
        });

        routineRow.setAlpha(0f);
        routineRow.setTranslationY(50f);
        binding.layoutRoutinesContainer.addView(routineRow);
        routineRow.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .start();
    }
    private void reorderDayChips() {
        int firstDay = SettingsManager.getFirstDayOfWeek(requireContext()); // 1 = Dimanche, 2 = Lundi, etc.
        List<View> allChips = new ArrayList<>();
        allChips.add(binding.chipSun); // index 0 -> Sunday
        allChips.add(binding.chipMon);
        allChips.add(binding.chipTue);
        allChips.add(binding.chipWed);
        allChips.add(binding.chipThu);
        allChips.add(binding.chipFri);
        allChips.add(binding.chipSat);

        // Réorganise les chips
        List<View> reordered = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int index = (firstDay - 1 + i) % 7;
            reordered.add(allChips.get(index));
        }

        binding.chipGroupDays.removeAllViews();
        for (View chip : reordered) {
            binding.chipGroupDays.addView(chip);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
