package com.rrtech.myhabits.calendar;

import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;


import com.rrtech.myhabits.R;

import java.util.List;
import java.util.function.Consumer;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private final List<CalendarDayItem> days;
    private final Consumer<CalendarDayItem> onDayClick;

    public CalendarAdapter(List<CalendarDayItem> days, Consumer<CalendarDayItem> onDayClick) {
        this.days = days;
        this.onDayClick = onDayClick;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDayItem item = days.get(position);
        holder.dayText.setText(String.valueOf(item.date.getDayOfMonth()));

        if (!item.isCurrentMonth) {
            holder.dayText.setTextColor(Color.LTGRAY);
        } else if (item.isChecked) {
            holder.dayText.setBackgroundColor(Color.parseColor("#A5D6A7"));
        } else if (item.isMissed) {
            holder.dayText.setBackgroundColor(Color.parseColor("#EF9A9A"));
        } else {
            holder.dayText.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setScaleX(1f);
        holder.itemView.setScaleY(1f);
        holder.itemView.animate()
                .scaleX(0.9f).scaleY(0.9f)
                .setDuration(80)
                .withEndAction(() -> holder.itemView.animate()
                        .scaleX(1f).scaleY(1f).setDuration(80))
                .start();
        holder.itemView.setOnClickListener(v -> onDayClick.accept(item));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;
        DayViewHolder(View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.text_day);
        }
    }
}
