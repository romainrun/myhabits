package com.rrtech.myhabits.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rrtech.myhabits.R;
import com.rrtech.myhabits.databinding.BottomsheetSelectTimeBinding;

import java.util.Calendar;

public class TimePickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnTimeSelectedListener {
        void onTimeSelected(int hour, int minute);
    }

    private final OnTimeSelectedListener callback;
    private BottomsheetSelectTimeBinding binding;

    public TimePickerBottomSheet(OnTimeSelectedListener callback) {
        this.callback = callback;
        setStyle(STYLE_NORMAL, R.style.MyTransparentBottomSheet);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomsheetSelectTimeBinding.inflate(inflater, container, false);

        // Animation d'apparition
        binding.getRoot().setAlpha(0f);
        binding.getRoot().setTranslationY(50f);
        binding.getRoot().animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start();

        return binding.getRoot(); // ✅ essentiel pour afficher le layout
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Heure actuelle + 30 minutes
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);
        int defaultHour = calendar.get(Calendar.HOUR_OF_DAY);
        int defaultMinute = calendar.get(Calendar.MINUTE);

        binding.inputHour.setText(String.format("%02d", defaultHour));
        binding.inputMinute.setText(String.format("%02d", defaultMinute));

        binding.buttonCancel.setOnClickListener(v -> dismiss());

        binding.buttonConfirm.setOnClickListener(v -> {
            String hourStr = binding.inputHour.getText().toString().trim();
            String minuteStr = binding.inputMinute.getText().toString().trim();

            if (TextUtils.isEmpty(hourStr) || TextUtils.isEmpty(minuteStr)) {
                Toast.makeText(requireContext(), "Heure et minutes requises", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int hour = Integer.parseInt(hourStr);
                int minute = Integer.parseInt(minuteStr);

                if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                    throw new NumberFormatException();
                }

                callback.onTimeSelected(hour, minute);
                dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Format invalide (HH:MM)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
