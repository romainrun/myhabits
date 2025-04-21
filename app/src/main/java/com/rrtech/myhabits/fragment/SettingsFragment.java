package com.rrtech.myhabits.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rrtech.myhabits.databinding.FragmentSettingsBinding;
import com.rrtech.myhabits.utils.SettingsManager;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

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

        boolean is24H = SettingsManager.is24HFormat(requireContext());
        binding.switchFormat24H.setChecked(is24H);

        binding.switchFormat24H.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsManager.set24HFormat(requireContext(), isChecked);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
