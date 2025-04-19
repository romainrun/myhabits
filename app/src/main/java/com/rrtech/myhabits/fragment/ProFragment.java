package com.rrtech.myhabits.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rrtech.myhabits.databinding.FragmentProBinding;

public class ProFragment extends Fragment {

    private FragmentProBinding binding;
    private com.rrtech.myhabits.controller.ProController controller;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = new com.rrtech.myhabits.controller.ProController(requireActivity(), binding);
        controller.setup();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        controller.release();
        binding = null;
    }
}
