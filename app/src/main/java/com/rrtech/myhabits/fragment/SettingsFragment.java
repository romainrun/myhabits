package com.rrtech.myhabits.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rrtech.myhabits.databinding.FragmentSettingsBinding;
import com.rrtech.myhabits.utils.SettingsManager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private Uri selectedRingtoneUri;
    private MediaPlayer mediaPlayer;

    private final ActivityResultLauncher<Intent> ringtonePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    selectedRingtoneUri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (selectedRingtoneUri != null) {
                        SettingsManager.setReminderSound(requireContext(), selectedRingtoneUri.toString());
                        updateSoundSummary();
                        playSoundPreview(selectedRingtoneUri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Format 24h
        binding.switchFormat24H.setChecked(SettingsManager.is24HFormat(requireContext()));
        binding.switchFormat24H.setOnCheckedChangeListener((btn, checked) ->
                SettingsManager.set24HFormat(requireContext(), checked));

        // Notifications
        binding.switchNotifications.setChecked(SettingsManager.areNotificationsEnabled(requireContext()));
        binding.switchNotifications.setOnCheckedChangeListener((btn, checked) ->
                SettingsManager.setNotificationsEnabled(requireContext(), checked));

        // Vibration
        binding.switchVibration.setChecked(SettingsManager.isVibrationEnabled(requireContext()));
        binding.switchVibration.setOnCheckedChangeListener((btn, checked) ->
                SettingsManager.setVibrationEnabled(requireContext(), checked));

        // Son
        updateSoundSummary();
        binding.buttonSelectSound.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Sélectionner un son");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            String currentSound = SettingsManager.getReminderSound(requireContext());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    currentSound == null || currentSound.isEmpty() ? null : Uri.parse(currentSound));
            ringtonePickerLauncher.launch(intent);
        });

        // Snooze
        binding.editSnoozeDuration.setText(String.valueOf(SettingsManager.getSnoozeDuration(requireContext())));
        binding.editSnoozeDuration.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    int value = Integer.parseInt(binding.editSnoozeDuration.getText().toString());
                    SettingsManager.setSnoozeDuration(requireContext(), value);
                } catch (NumberFormatException e) {
                    binding.editSnoozeDuration.setError("Durée invalide");
                }
            }
        });

        // Premier jour de la semaine
        List<String> dayNames = Arrays.asList("Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi");
        List<Integer> dayConstants = Arrays.asList(
                Calendar.SUNDAY,
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                dayNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFirstDay.setAdapter(adapter);

        int savedDay = SettingsManager.getFirstDayOfWeek(requireContext());
        int savedIndex = dayConstants.indexOf(savedDay);
        if (savedIndex >= 0) {
            binding.spinnerFirstDay.setSelection(savedIndex);
        }

        binding.spinnerFirstDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SettingsManager.setFirstDayOfWeek(requireContext(), dayConstants.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Réinitialiser
        binding.buttonResetPreferences.setOnClickListener(v -> {
            SettingsManager.resetToDefaults(requireContext());
            Toast.makeText(requireContext(), "Paramètres réinitialisés", Toast.LENGTH_SHORT).show();
            onViewCreated(view, savedInstanceState); // Recharger visuellement
        });
    }

    private void updateSoundSummary() {
        String uriStr = SettingsManager.getReminderSound(requireContext());
        if (uriStr == null || uriStr.isEmpty()) {
            binding.textSoundSummary.setText("Aucun son sélectionné");
        } else {
            Uri uri = Uri.parse(uriStr);
            binding.textSoundSummary.setText(RingtoneManager.getRingtone(requireContext(), uri).getTitle(requireContext()));
        }
    }

    private void playSoundPreview(Uri soundUri) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(requireContext(), soundUri);
            if (mediaPlayer != null) mediaPlayer.start();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Impossible de lire le son", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        binding = null;
    }
}
