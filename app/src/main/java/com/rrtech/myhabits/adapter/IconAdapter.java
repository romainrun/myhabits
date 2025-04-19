package com.rrtech.myhabits.adapter;

import android.os.Build;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rrtech.myhabits.R;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

    private final List<String> icons;
    private String selectedIcon;
    private final OnIconSelected listener;

    public interface OnIconSelected {
        void onIconSelected(String icon);
    }

    public IconAdapter(List<String> icons, OnIconSelected listener) {
        this.icons = icons;
        this.listener = listener;
        this.selectedIcon = icons.get(0);
    }

    public void setSelectedIcon(String icon) {
        this.selectedIcon = icon;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView iconView = new TextView(parent.getContext());
        int size = (int) (48 * parent.getContext().getResources().getDisplayMetrics().density);
        iconView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        iconView.setTextSize(24);
        iconView.setGravity(android.view.Gravity.CENTER);
        iconView.setPadding(12, 12, 12, 12);
        return new IconViewHolder(iconView);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        String icon = icons.get(position);
        TextView view = (TextView) holder.itemView;
        view.setText(icon);

        view.setBackgroundResource(icon.equals(selectedIcon) ? R.drawable.bg_selected : 0);
        view.setForeground(ContextCompat.getDrawable(view.getContext(), R.drawable.ripple_light));

        view.setOnClickListener(v -> {
            animateClick(view);
            selectedIcon = icon;
            notifyDataSetChanged();
            listener.onIconSelected(icon);
        });
    }

    private void animateClick(View view) {
        view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(100)).start();
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
