package com.rrtech.myhabits.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rrtech.myhabits.R;
import com.rrtech.myhabits.data.model.Habit;

import java.util.List;

public class EditHabitsBottomSheet extends BottomSheetDialogFragment {

    public interface EditActionListener {
        void onEdit(Habit habit);
        void onDelete(List<Habit> habits);
        void onDuplicate(List<Habit> habits);
    }

    private List<Habit> selectedHabits;
    private EditActionListener listener;

    public EditHabitsBottomSheet(List<Habit> selectedHabits, EditActionListener listener) {
        this.selectedHabits = selectedHabits;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_edit_habits, container, false);

        Button buttonEdit = view.findViewById(R.id.button_edit);
        Button buttonDelete = view.findViewById(R.id.button_delete);
        Button buttonDuplicate = view.findViewById(R.id.button_duplicate);

        buttonEdit.setEnabled(selectedHabits.size() == 1);

        buttonEdit.setOnClickListener(v -> {
            dismiss();
            if (listener != null && selectedHabits.size() == 1) {
                listener.onEdit(selectedHabits.get(0));
            }
        });

        buttonDelete.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onDelete(selectedHabits);
            }
        });

        buttonDuplicate.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onDuplicate(selectedHabits);
            }
        });

        return view;
    }
}
