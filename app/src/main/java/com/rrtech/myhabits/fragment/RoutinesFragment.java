package com.rrtech.myhabits.fragment;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rrtech.myhabits.R;
import com.rrtech.myhabits.adapter.RoutineWithHabitsAdapter;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.Routine;
import com.rrtech.myhabits.databinding.FragmentRoutineBinding;
import com.rrtech.myhabits.ui.main.RoutinesViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RoutinesFragment extends Fragment {

    private FragmentRoutineBinding binding;
    private RoutinesViewModel viewModel;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private RoutineWithHabitsAdapter adapter;
    private MenuItem editMenuItem;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_routines, menu);
        editMenuItem = menu.findItem(R.id.action_edit); // 🔑 garde une référence
        updateEditMenuIcon(); // 🔄 icône ou texte selon l’état initial
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            toggleEditMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditActions() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Actions sur les habitudes")
                .setItems(new CharSequence[]{"Dupliquer", "Modifier", "Supprimer"}, (dialog, which) -> {
                    Set<Habit> selected = adapter.getSelectedHabits();

                    if (selected.isEmpty()) {
                        Toast.makeText(requireContext(), "Aucune habitude sélectionnée", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    switch (which) {
                        case 0: // Dupliquer
                            for (Habit h : selected) {
                                Habit copy = new Habit(h.getName() + " (copie)", h.getIcon(), h.getColor(), h.getRepeatDays(), h.getImportance());
                                copy.setReminderTime(h.getReminderTime());
                                viewModel.insertHabit(copy);
                            }
                            break;

                        case 1: // Modifier
                            if (selected.size() == 1) {
                                Habit habitToEdit = selected.iterator().next();
                                // TODO : Ouvrir un écran ou une boîte de dialogue d’édition
                                Toast.makeText(requireContext(), "Modifier : " + habitToEdit.getName(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Sélectionne une seule habitude pour modifier", Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case 2: // Supprimer
                            for (Habit h : selected) {
                                viewModel.deleteHabit(h);
                            }
                            break;
                    }

                    adapter.setEditMode(false); // Retour en mode normal
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRoutineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(RoutinesViewModel.class);
        setupRecyclerView();
        observeCombinedData();

        // ✅ Initialisation du BottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetActions);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setDraggable(false); // désactive drag user


        // ✅ Actions
        binding.actionEdit.setOnClickListener(v -> {
            Set<Habit> selected = adapter.getSelectedHabits();
            if (selected.size() == 1) {
                navigateToEditHabit(selected.iterator().next());
            } else {
                Toast.makeText(requireContext(), "Sélectionnez une seule habitude à modifier", Toast.LENGTH_SHORT).show();
            }
        });
        binding.actionDuplicate.setOnClickListener(v -> {
            for (Habit habit : adapter.getSelectedHabits()) {
                Habit copy = new Habit(habit.getName() + " (copie)", habit.getIcon(), habit.getColor(), habit.getRepeatDays(), habit.getImportance());
                copy.setReminderTime(habit.getReminderTime());
                copy.setHasReminder(habit.isHasReminder());
                copy.setReminderOffsetMinutes(habit.getReminderOffsetMinutes());

                Routine routine = viewModel.getRoutineForHabit(habit.getId());

                if (routine != null) {
                    // ⚠️ Appel à une méthode du viewModel qui insère et ajoute à la routine
                    viewModel.insertHabitAndAssignToRoutine(copy, routine.getId());
                }
            }

            exitEditMode();
        });

        binding.actionDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Supprimer les habitudes sélectionnées ?")
                    .setMessage("Cette action est irréversible.")
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        for (Habit habit : adapter.getSelectedHabits()) {
                            viewModel.deleteHabit(habit);
                        }
                        exitEditMode();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupRecyclerView() {
        adapter = new RoutineWithHabitsAdapter();
        binding.recyclerGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerGroups.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(adapter.new HabitItemTouchHelperCallback(adapter));
        itemTouchHelper.attachToRecyclerView(binding.recyclerGroups);

        adapter.setOnHabitDropListener((habit, fromRoutine, toRoutine) -> {
            viewModel.moveHabitToAnotherRoutine(habit.getId(), fromRoutine.getId(), toRoutine.getId());

            Vibrator vibrator = (Vibrator) requireContext().getSystemService(requireContext().VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            }

            String message = "\"" + habit.getName() + "\" déplacée de \"" +
                    fromRoutine.getName() + "\" vers \"" + toRoutine.getName() + "\" ✅";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void observeCombinedData() {
        viewModel.getAllRoutines().observe(getViewLifecycleOwner(), routines -> {
            viewModel.getHabitsByRoutine().observe(getViewLifecycleOwner(), habitsMap -> {
                adapter.submitData(routines, habitsMap);
                toggleEmptyState(routines);
            });
        });
    }

    private void toggleEmptyState(List<Routine> routines) {
        boolean isEmpty = routines == null || routines.isEmpty();
        binding.recyclerGroups.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void toggleEditMode() {
        boolean enabled = !adapter.isInEditMode();
        adapter.setEditMode(enabled);

        if (enabled) {
            binding.bottomSheetActions.setVisibility(View.VISIBLE); // ✅
            binding.bottomSheetActions.setAlpha(0f);
            binding.bottomSheetActions.animate().alpha(1f).setDuration(200).start();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            binding.bottomSheetActions.setVisibility(View.GONE); // ✅
        }

        updateEditMenuIcon();
    }

    private void navigateToEditHabit(Habit habit) {
        Bundle args = new Bundle();
        args.putInt("habitId", habit.getId()); // 🔑 passer l’ID de l’habitude

        // Naviguer vers le fragment avec l’ID
        requireActivity()
                .getSupportFragmentManager()
                .setFragmentResult("edit_habit_request", args); // pour écoute dans le AddEditHabitFragment

        // Utilise Navigation Component si configuré
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_routinesFragment_to_addEditHabitFragment, args);
    }


    private void exitEditMode() {
        adapter.exitEditMode();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        binding.bottomSheetActions.setVisibility(View.GONE); // ✅
    }


    private void updateEditMenuIcon() {
        if (editMenuItem != null) {
            editMenuItem.setTitle(adapter.isInEditMode() ? "Terminer" : "Éditer");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
