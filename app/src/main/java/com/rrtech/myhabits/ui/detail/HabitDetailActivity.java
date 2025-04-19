package com.rrtech.myhabits.ui.detail;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.calendar.CalendarAdapter;
import com.rrtech.myhabits.calendar.CalendarDayItem;

import java.time.LocalDate;
import java.util.*;

public class HabitDetailActivity extends AppCompatActivity {

    private RecyclerView calendarRecycler;
    private TextView streakText, titleMonth;
    private LocalDate currentMonth;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        calendarRecycler = findViewById(R.id.calendar_recycler);
        streakText = findViewById(R.id.text_streak);
        titleMonth = findViewById(R.id.text_month);

        calendarRecycler.setLayoutManager(new GridLayoutManager(this, 7));

        currentMonth = LocalDate.now();

        Button btnPrev = findViewById(R.id.btn_prev);
        Button btnNext = findViewById(R.id.btn_next);

        btnPrev.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });

        updateCalendar();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateCalendar() {
        titleMonth.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());

        Set<LocalDate> checkedDays = simulateCheckedDays(); // à remplacer par DAO
        List<CalendarDayItem> days = new ArrayList<>();

        LocalDate first = currentMonth.withDayOfMonth(1);
        int startOffset = first.getDayOfWeek().getValue() % 7;
        LocalDate start = first.minusDays(startOffset);

        for (int i = 0; i < 42; i++) {
            LocalDate day = start.plusDays(i);
            boolean isCurrent = day.getMonth() == currentMonth.getMonth();
            boolean isChecked = checkedDays.contains(day);
            boolean isMissed = !isChecked && isCurrent && day.isBefore(LocalDate.now());
            days.add(new CalendarDayItem(day, isCurrent, isChecked, isMissed));
        }

        calendarRecycler.setAdapter(new CalendarAdapter(days, day -> {
            new AlertDialog.Builder(this)
                    .setTitle("Jour sélectionné")
                    .setMessage("Date : " + day.date +
                            "\nCoché : " + (day.isChecked ? "Oui ✅" : "Non ❌") +
                            "\nRaté : " + (day.isMissed ? "Oui ❌" : "Non"))
                    .setPositiveButton("OK", null)
                    .show();
        }));
        streakText.setText("Chaîne actuelle : " + computeStreak(checkedDays) + " jour(s)");
    }

    private int computeStreak(Set<LocalDate> checked) {
        int streak = 0;
        LocalDate d = LocalDate.now();
        while (checked.contains(d)) {
            streak++;
            d = d.minusDays(1);
        }
        return streak;
    }

    private Set<LocalDate> simulateCheckedDays() {
        Set<LocalDate> result = new HashSet<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 10; i++) {
            result.add(today.minusDays(i * 2)); // simulate tous les 2 jours
        }
        return result;
    }
}
