package com.rrtech.myhabits.ui.calendar;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.calendar.CalendarAdapter;
import com.rrtech.myhabits.calendar.CalendarDayItem;
import com.rrtech.myhabits.data.model.HabitCheck;
import com.rrtech.myhabits.ui.main.HabitCheckViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HabitCalendarActivity extends AppCompatActivity {

    private RecyclerView calendarRecycler;
    private TextView textMonth, textStreak;
    private Button btnPrev, btnNext;
    private Switch switchMode;

    private LocalDate currentMonth;
    private int habitId;
    private Set<LocalDate> checkedDays = new HashSet<>();

    private HabitCheckViewModel viewModel;
    private boolean isWeekly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_calendar);

        habitId = getIntent().getIntExtra("habitId", -1);
        String habitName = getIntent().getStringExtra("habitName");
        String habitIcon = getIntent().getStringExtra("habitIcon");
        String habitColor = getIntent().getStringExtra("habitColor");
        String habitRepeat = getIntent().getStringExtra("habitRepeatDays");

        if (habitId == -1) finish();

        TextView textTitle = findViewById(R.id.text_title);
        TextView textIcon = findViewById(R.id.habit_icon);
        TextView textRepeat = findViewById(R.id.text_repeat);

        textTitle.setText(habitName);
        textIcon.setText(habitIcon);
        textRepeat.setText(habitRepeat);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(android.graphics.Color.parseColor(habitColor));
        textIcon.setBackground(bg);

        calendarRecycler = findViewById(R.id.calendar_recycler);
        textMonth = findViewById(R.id.text_month);
        textStreak = findViewById(R.id.text_streak);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        switchMode = findViewById(R.id.switch_mode);


        calendarRecycler.setLayoutManager(new GridLayoutManager(this, 7));
        viewModel = new ViewModelProvider(this).get(HabitCheckViewModel.class);

        currentMonth = LocalDate.now();

        // ✅ 1. Onboarding au premier lancement
        SharedPreferences prefs = getSharedPreferences("myhabits_prefs", MODE_PRIVATE);
        boolean firstLaunch = prefs.getBoolean("calendar_first_open", true);
        if (firstLaunch) {
            new AlertDialog.Builder(this)
                    .setTitle("Bienvenue dans le calendrier 📅")
                    .setMessage("Visualisez les jours où vous avez tenu vos habitudes.\n\n" +
                            "🟢 fait\n🔴 manqué\nTouchez un jour pour cocher ou décocher.")
                    .setPositiveButton("Compris !", (dialog, which) ->
                            prefs.edit().putBoolean("calendar_first_open", false).apply()
                    )
                    .show();
        }

        // ✅ 2. Tooltips
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            btnPrev.setTooltipText("Voir le mois/semaine précédent(e)");
            btnNext.setTooltipText("Voir le mois/semaine suivant(e)");
            switchMode.setTooltipText("Basculer entre vue mois et semaine");
        }

        viewModel.getAllChecks(habitId).observe(this, habitChecks -> {
            checkedDays.clear();
            for (HabitCheck hc : habitChecks) {
                checkedDays.add(LocalDate.parse(hc.date));
            }
            updateCalendar();
        });

        btnPrev.setOnClickListener(v -> {
            currentMonth = isWeekly ? currentMonth.minusWeeks(1) : currentMonth.minusMonths(1);
            updateCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth = isWeekly ? currentMonth.plusWeeks(1) : currentMonth.plusMonths(1);
            updateCalendar();
        });

        switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isWeekly = isChecked;
            updateCalendar();
        });
    }

    private void updateCalendar() {
        List<CalendarDayItem> days = new ArrayList<>();
        textMonth.setText(isWeekly ? "Semaine du " + currentMonth.toString() :
                currentMonth.getMonth().toString() + " " + currentMonth.getYear());

        if (isWeekly) {
            LocalDate monday = currentMonth.minusDays(currentMonth.getDayOfWeek().getValue() - 1);
            for (int i = 0; i < 7; i++) {
                LocalDate date = monday.plusDays(i);
                boolean isChecked = checkedDays.contains(date);
                boolean isMissed = !isChecked && date.isBefore(LocalDate.now());
                days.add(new CalendarDayItem(date, true, isChecked, isMissed));
            }
        } else {
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
        }

        calendarRecycler.setAdapter(new CalendarAdapter(days, clickedDay -> {
            // ✅ 3. Animation clic
            calendarRecycler.post(() -> {
                calendarRecycler.findViewHolderForAdapterPosition(days.indexOf(clickedDay))
                        .itemView.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                        .withEndAction(() -> calendarRecycler.findViewHolderForAdapterPosition(days.indexOf(clickedDay))
                                .itemView.animate().scaleX(1f).scaleY(1f).setDuration(100)).start();
            });

            // ✅ toggle check
            if (checkedDays.contains(clickedDay.date)) {
                viewModel.delete(new HabitCheck(clickedDay.date.toString(), habitId));
            } else {
                viewModel.insert(new HabitCheck(clickedDay.date.toString(), habitId));
            }
        }));

        textStreak.setText("Chaîne actuelle : " + computeStreak() + " jour(s)");
    }

    private int computeStreak() {
        int streak = 0;
        LocalDate d = LocalDate.now();
        while (checkedDays.contains(d)) {
            streak++;
            d = d.minusDays(1);
        }
        return streak;
    }
}
