package com.rrtech.myhabits.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.gridlayout.widget.GridLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rrtech.myhabits.R;
import com.rrtech.myhabits.databinding.BottomsheetEmojiPickerBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmojiPickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnEmojiSelectedListener {
        void onEmojiSelected(String emoji);
    }

    private final boolean isPro;
    private final OnEmojiSelectedListener callback;
    private BottomsheetEmojiPickerBinding binding;

    public EmojiPickerBottomSheet(boolean isPro, OnEmojiSelectedListener callback) {
        this.isPro = isPro;
        this.callback = callback;
        setStyle(STYLE_NORMAL, R.style.MyTransparentBottomSheet);
    }

    private final List<String> freeEmojis = Arrays.asList(
            "💪", "📚", "🏃", "🧘", "🥗", "🛏️", "💧", "🦷", "📖", "🎯", "☀️", "🚿"
    );

    private final List<String> proEmojis = Arrays.asList(
            "🚶", "🚴", "📅", "📓", "🔒", "📵", "🛍️", "🎵", "🍎", "🧼", "📝", "🕐",
            "🧠", "👟", "🖼️", "🧩", "👨‍💻", "😴", "🍵", "🏋️", "🧃", "🐶", "🧑‍🍳",
            "🖊️", "🔔", "🧪", "💻", "💼", "🧴"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomsheetEmojiPickerBinding.inflate(inflater, container, false);

        // Animation d'apparition douce
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

        if (!isPro) {
            binding.layoutProHint.setVisibility(View.VISIBLE);
            binding.buttonGoPro.setOnClickListener(v -> {
                dismiss();
                // Navigation to ProFragment can be added here
            });
        } else {
            binding.layoutProHint.setVisibility(View.GONE);
        }

        List<String> allEmojis = new ArrayList<>(freeEmojis);
        if (isPro) allEmojis.addAll(proEmojis);

        GridLayout grid = binding.gridEmojiContainer;
        grid.removeAllViews();

        for (int i = 0; i < allEmojis.size(); i++) {

            String emoji = allEmojis.get(i);
            boolean isLocked = !isPro && i >= freeEmojis.size();


            View emojiView = LayoutInflater.from(requireContext()).inflate(R.layout.item_emoji_choice, grid, false);
            com.google.android.material.textview.MaterialTextView emojiText = emojiView.findViewById(R.id.text_emoji);

            ImageView lockIcon = emojiView.findViewById(R.id.icon_lock);
            emojiText.setText(emoji);
            emojiText.setAlpha(isLocked ? 0.4f : 1.0f);
            lockIcon.setVisibility(isLocked ? View.VISIBLE : View.GONE);

            emojiText.setText(emoji);
            emojiText.setAlpha(isLocked ? 0.4f : 1.0f);
            emojiText.setEnabled(!isLocked);

            if (isLocked) {
                emojiText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_lock, 0);
                emojiText.setCompoundDrawablePadding(8);
            }

            emojiView.setOnClickListener(v -> {
                if (isLocked) {
                    Toast.makeText(requireContext(), "Disponible dans la version Pro 🔓", Toast.LENGTH_SHORT).show();
                } else {
                    emojiView.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                        emojiView.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        callback.onEmojiSelected(emoji);
                        dismiss();
                    }).start();
                }
            });

            grid.addView(emojiView);
        }

        binding.buttonCancelEmojiPicker.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
