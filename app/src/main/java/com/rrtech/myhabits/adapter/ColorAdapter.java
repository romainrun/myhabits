package com.rrtech.myhabits.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.data.model.ColorItem;
import com.rrtech.myhabits.databinding.ItemColorPickerBinding;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    public interface OnColorClickListener {
        void onColorClicked(ColorItem colorItem);
    }

    private final List<ColorItem> colors;
    private final boolean proUnlocked;
    private final OnColorClickListener listener;

    private int selectedColor = -1;

    public ColorAdapter(List<ColorItem> colors, boolean proUnlocked, OnColorClickListener listener) {
        this.colors = colors;
        this.proUnlocked = proUnlocked;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ColorViewHolder(ItemColorPickerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        ColorItem colorItem = colors.get(position);
        int color = colorItem.getColor();
        boolean isLocked = colorItem.isPro() && !proUnlocked;

        GradientDrawable bg = (GradientDrawable) holder.binding.viewColor.getBackground();
        bg.setColor(color);

        // Affichage d’un halo ou bordure si sélectionné
        boolean isSelected = (color == selectedColor);
        holder.binding.selectedOutline.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        // Verrouillage visuel
        holder.binding.lockIcon.setVisibility(isLocked ? View.VISIBLE : View.GONE);
        holder.binding.viewColor.setAlpha(isLocked ? 0.5f : 1.0f);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (!isLocked) {
                int previous = selectedColor;
                selectedColor = color;
                notifyDataSetChanged(); // tu peux aussi optimiser avec notifyItemChanged si besoin
                listener.onColorClicked(colorItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    public void setSelectedColor(int color) {
        selectedColor = color;
        notifyDataSetChanged();
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        final ItemColorPickerBinding binding;

        public ColorViewHolder(@NonNull ItemColorPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
