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
import com.rrtech.myhabits.utils.SettingsManager;

import java.util.Calendar;
import java.util.Locale;

public class TimePickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnTimeSelectedListener {
        void onTimeSelected(int hour, int minute);
    }

    private final OnTimeSelectedListener callback;
    private BottomsheetSelectTimeBinding binding;
    private boolean is24HFormat;

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

        // Animation d’apparition
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        is24HFormat = SettingsManager.is24HFormat(requireContext());
        binding.amPmToggle.setVisibility(is24HFormat ? View.GONE : View.VISIBLE);

        // Valeurs par défaut : heure actuelle + 30min
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (is24HFormat) {
            binding.inputHour.setText(String.format(Locale.getDefault(), "%02d", hour));
        } else {
            int hour12 = hour % 12;
            if (hour12 == 0) hour12 = 12;
            binding.inputHour.setText(String.format(Locale.getDefault(), "%02d", hour12));
            binding.radioAm.setChecked(hour < 12);
            binding.radioPm.setChecked(hour >= 12);
        }

        binding.inputMinute.setText(String.format(Locale.getDefault(), "%02d", minute));

        // Annuler
        binding.buttonCancel.setOnClickListener(v -> dismiss());

        // Confirmer
        binding.buttonConfirm.setOnClickListener(v -> {
            String hourStr = binding.inputHour.getText().toString().trim();
            String minuteStr = binding.inputMinute.getText().toString().trim();

            if (TextUtils.isEmpty(hourStr) || TextUtils.isEmpty(minuteStr)) {
                Toast.makeText(requireContext(), "Heure et minutes requises", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int hourInput = Integer.parseInt(hourStr);
                int minuteInput = Integer.parseInt(minuteStr);

                // Validation
                if (hourInput < 0 || minuteInput < 0 || minuteInput > 59 ||
                        (is24HFormat && hourInput > 23) ||
                        (!is24HFormat && (hourInput > 12 || hourInput == 0))) {
                    throw new NumberFormatException();
                }

                int finalHour = hourInput;
                if (!is24HFormat) {
                    boolean isPM = binding.radioPm.isChecked();
                    if (isPM && finalHour < 12) finalHour += 12;
                    if (!isPM && finalHour == 12) finalHour = 0;
                }

                callback.onTimeSelected(finalHour, minuteInput);
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
