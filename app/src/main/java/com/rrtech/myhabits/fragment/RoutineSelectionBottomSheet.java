package com.rrtech.myhabits.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rrtech.myhabits.R;
import com.rrtech.myhabits.data.model.Routine;
import com.rrtech.myhabits.databinding.BottomsheetSelectOrCreateRoutineBinding;

import java.util.ArrayList;
import java.util.List;

public class RoutineSelectionBottomSheet extends BottomSheetDialogFragment {

    public interface OnRoutineSelectedListener {
        void onRoutineSelected(Routine routine, boolean isNew);
    }

    private final List<Routine> availableRoutines;
    private final OnRoutineSelectedListener callback;
    private BottomsheetSelectOrCreateRoutineBinding binding;

    public RoutineSelectionBottomSheet(List<Routine> availableRoutines, OnRoutineSelectedListener callback) {
        this.availableRoutines = availableRoutines;
        this.callback = callback;
        setStyle(STYLE_NORMAL, R.style.MyTransparentBottomSheet); // Applique fond noir semi-transparent + blur
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomsheetSelectOrCreateRoutineBinding.inflate(inflater, container, false);

        // Applique une animation d’apparition
        binding.getRoot().setAlpha(0f);
        binding.getRoot().setTranslationY(50f);
        binding.getRoot().animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<String> routineNames = new ArrayList<>();
        routineNames.add("➕ Nouvelle routine");
        for (Routine routine : availableRoutines) {
            routineNames.add(routine.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, routineNames);

        binding.spinnerExistingRoutines.setAdapter(adapter);

        binding.spinnerExistingRoutines.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                boolean isNew = (position == 0);
                binding.inputLayoutRoutine.setVisibility(isNew ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        binding.buttonCancel.setOnClickListener(v -> dismiss());

        binding.buttonAdd.setOnClickListener(v -> {
            int pos = binding.spinnerExistingRoutines.getSelectedItemPosition();
            if (pos == 0) {
                String name = binding.editRoutineName.getText() != null
                        ? binding.editRoutineName.getText().toString().trim()
                        : "";

                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Nom de routine requis", Toast.LENGTH_SHORT).show();
                    return;
                }

                Routine newRoutine = new Routine(name, true);
                callback.onRoutineSelected(newRoutine, true);
            } else {
                Routine selected = availableRoutines.get(pos - 1);
                callback.onRoutineSelected(selected, false);
            }

            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
