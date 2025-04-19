package com.rrtech.myhabits.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rrtech.myhabits.adapter.RoutineAdapter;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.Routine;
import com.rrtech.myhabits.databinding.FragmentRoutineBinding;
import com.rrtech.myhabits.ui.main.RoutinesViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutinesFragment extends Fragment {

    private FragmentRoutineBinding binding;
    private RoutinesViewModel viewModel;
    private RoutineAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRoutineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(RoutinesViewModel.class);
        setupRecyclerView();
        observeData();

        binding.fabAddGroup.setOnClickListener(v -> showAddGroupDialog());
    }

    private void setupRecyclerView() {
        adapter = new RoutineAdapter(
                new java.util.ArrayList<>(),
                new HashMap<>(),
                new RoutineAdapter.OnRoutineActionListener() {
                    @Override
                    public void onDeleteRoutine(Routine routine) {
                        viewModel.delete(routine);
                    }

                    @Override
                    public void onDeleteHabitFromRoutine(Routine routine, Habit habit) {
                        viewModel.removeHabitFromRoutine(routine.getId(), habit.getId());
                    }

                    @Override
                    public void onHabitMovedBetweenRoutines(Habit habit, Routine fromRoutine, Routine toRoutine) {
                        viewModel.moveHabitToAnotherRoutine(habit.getId(), fromRoutine.getId(), toRoutine.getId());
                    }
                }
        );
        binding.recyclerGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerGroups.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getAllRoutines().observe(getViewLifecycleOwner(), routines -> {
            viewModel.getHabitsByRoutine().observe(getViewLifecycleOwner(), habitsMap -> {
                adapter.updateRoutines(routines, habitsMap);
                toggleEmptyState(routines);
            });
        });
    }

    private void toggleEmptyState(List<Routine> groups) {
        if (groups == null || groups.isEmpty()) {
            binding.textEmptyGroups.setVisibility(View.VISIBLE);
        } else {
            binding.textEmptyGroups.setVisibility(View.GONE);
        }
    }

    private void showAddGroupDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Nom de la routine");

        new AlertDialog.Builder(requireContext())
                .setTitle("Nouvelle routine")
                .setView(input)
                .setPositiveButton("Créer", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        Routine routine = new Routine(name, true);
                        viewModel.insert(routine);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
