package com.rrtech.myhabits.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rrtech.myhabits.R;
import java.util.List;

public class StreakAdapter extends RecyclerView.Adapter<StreakAdapter.DayViewHolder> {
    private final List<String> completedDays;

    public StreakAdapter(List<String> completedDays) {
        this.completedDays = completedDays;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_streak_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.date.setText(completedDays.get(position));
    }

    @Override
    public int getItemCount() {
        return completedDays.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView date;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.day_text);
        }
    }
}
