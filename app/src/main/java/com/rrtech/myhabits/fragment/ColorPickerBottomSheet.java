package com.rrtech.myhabits.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rrtech.myhabits.R;
import com.rrtech.myhabits.adapter.ColorAdapter;
import com.rrtech.myhabits.data.model.ColorItem;
import com.rrtech.myhabits.databinding.BottomsheetColorPickerBinding;

import java.util.List;

public class ColorPickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnColorSelectedListener {
        void onColorSelected(ColorItem item);
    }

    private final List<ColorItem> colorItems;
    private final boolean isProUnlocked;
    private final OnColorSelectedListener listener;
    private BottomsheetColorPickerBinding binding;

    public ColorPickerBottomSheet(List<ColorItem> colorItems, boolean isProUnlocked, OnColorSelectedListener listener) {
        this.colorItems = colorItems;
        this.isProUnlocked = isProUnlocked;
        this.listener = listener;
        setStyle(STYLE_NORMAL, R.style.MyTransparentBottomSheet); // style avec fond semi-transparent
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomsheetColorPickerBinding.inflate(inflater, container, false);

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ColorAdapter adapter = new ColorAdapter(colorItems, isProUnlocked, item -> {
            if (!item.isPro() || isProUnlocked) {
                listener.onColorSelected(item);
                dismiss();
            } else {
                Toast.makeText(requireContext(), "Version Pro requise pour cette couleur 🎨", Toast.LENGTH_SHORT).show();
            }
        });

        binding.recyclerColors.setLayoutManager(new GridLayoutManager(requireContext(), 6));
        binding.recyclerColors.setAdapter(adapter);

        binding.buttonCancelColorPicker.setOnClickListener(v -> dismiss());

        // Affichage de l’encart Pro si nécessaire
        boolean hasProColors = colorItems.stream().anyMatch(ColorItem::isPro);
        if (hasProColors && !isProUnlocked) {
            binding.layoutProHint.setVisibility(View.VISIBLE);
            binding.buttonGoPro.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "👉 Lien vers achat version Pro ici", Toast.LENGTH_SHORT).show()
            );
        } else {
            binding.layoutProHint.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
