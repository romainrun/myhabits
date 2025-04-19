package com.rrtech.myhabits.ui.addedit;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.adapter.ColorAdapter;
import com.rrtech.myhabits.adapter.IconAdapter;
import com.rrtech.myhabits.data.model.ColorItem;
import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.fragment.TimePickerBottomSheet;
import com.rrtech.myhabits.state.ProState;
import com.rrtech.myhabits.ui.main.HabitViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddEditHabitActivity extends AppCompatActivity {

    private HabitViewModel viewModel;
    private EditText inputName;
    private String selectedIcon = "🔥";
    private String selectedColor = "#2196F3";

    private Switch switchReminder;
    private TextView textReminderTime;
    private Spinner spinnerReminderOffset;

    private String reminderTime = "";
    private int reminderOffsetMinutes = 0;

    private CheckBox checkMon, checkTue, checkWed, checkThu, checkFri, checkSat, checkSun;
    private int habitId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_MyHabits);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_habit);

        inputName = findViewById(R.id.input_habit_name);
        checkMon = findViewById(R.id.check_mon);
        checkTue = findViewById(R.id.check_tue);
        checkWed = findViewById(R.id.check_wed);
        checkThu = findViewById(R.id.check_thu);
        checkFri = findViewById(R.id.check_fri);
        checkSat = findViewById(R.id.check_sat);
        checkSun = findViewById(R.id.check_sun);

        switchReminder = findViewById(R.id.switch_reminder);
        textReminderTime = findViewById(R.id.text_reminder_time);
        spinnerReminderOffset = findViewById(R.id.spinner_reminder_offset);

        RecyclerView iconRecycler = findViewById(R.id.recycler_icons);
        RecyclerView colorRecycler = findViewById(R.id.recycler_colors);

        viewModel = new ViewModelProvider(this).get(HabitViewModel.class);

        List<String> icons = Arrays.asList("💪", "📚", "🧘", "🚰", "📝", "🥗", "🏃", "🎵", "🛎️");
        IconAdapter iconAdapter = new IconAdapter(icons, icon -> selectedIcon = icon);

        List<ColorItem> colorItems = Arrays.asList(
                new ColorItem(0xFF2196F3, false),
                new ColorItem(0xFF4CAF50, false),
                new ColorItem(0xFF9C27B0, true),
                new ColorItem(0xFFFF9800, true),
                new ColorItem(0xFFE91E63, false)
        );

        ColorAdapter colorAdapter = new ColorAdapter(colorItems, ProState.getInstance(getApplicationContext()).isPro(), item -> {
            selectedColor = String.format("#%06X", (0xFFFFFF & item.getColor()));
        });

        iconRecycler.setLayoutManager(new GridLayoutManager(this, 5));
        iconRecycler.setAdapter(iconAdapter);

        colorRecycler.setLayoutManager(new GridLayoutManager(this, 5));
        colorRecycler.setAdapter(colorAdapter);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("À l’heure exacte", "5 min avant", "10 min avant", "30 min", "1h")
        );
        spinnerReminderOffset.setAdapter(spinnerAdapter);

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) ->
                findViewById(R.id.layout_reminder_time).setVisibility(isChecked ? View.VISIBLE : View.GONE));

        textReminderTime.setOnClickListener(v -> {
            TimePickerBottomSheet sheet = new TimePickerBottomSheet((hour, minute) -> {
                reminderTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                textReminderTime.setText(reminderTime);
            });
            sheet.show(getSupportFragmentManager(), "TimePickerBottomSheet");
        });

        spinnerReminderOffset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1: reminderOffsetMinutes = 5; break;
                    case 2: reminderOffsetMinutes = 10; break;
                    case 3: reminderOffsetMinutes = 30; break;
                    case 4: reminderOffsetMinutes = 60; break;
                    default: reminderOffsetMinutes = 0;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (getIntent().hasExtra("habitId")) {
            habitId = getIntent().getIntExtra("habitId", -1);
            viewModel.getAllHabits().observe(this, habits -> {
                for (Habit h : habits) {
                    if (h.getId() == habitId) {
                        inputName.setText(h.getName());
                        selectedIcon = h.getIcon();
                        selectedColor = h.getColor();
                        iconAdapter.setSelectedIcon(selectedIcon);

                        try {
                            int parsedColor = Color.parseColor(selectedColor);
                            colorAdapter.setSelectedColor(parsedColor);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }

                        if (h.getRepeatDays() != null) {
                            for (String day : h.getRepeatDays().split(",")) {
                                switch (day.trim()) {
                                    case "Mon": checkMon.setChecked(true); break;
                                    case "Tue": checkTue.setChecked(true); break;
                                    case "Wed": checkWed.setChecked(true); break;
                                    case "Thu": checkThu.setChecked(true); break;
                                    case "Fri": checkFri.setChecked(true); break;
                                    case "Sat": checkSat.setChecked(true); break;
                                    case "Sun": checkSun.setChecked(true); break;
                                }
                            }
                        }

                        if (h.isHasReminder()) {
                            switchReminder.setChecked(true);
                            reminderTime = h.getReminderTime();
                            textReminderTime.setText(reminderTime);
                            reminderOffsetMinutes = h.getReminderOffsetMinutes();
                            spinnerReminderOffset.setSelection(getOffsetSpinnerIndex(reminderOffsetMinutes));
                        }
                        break;
                    }
                }
            });
        }

        findViewById(R.id.btn_save_habit).setOnClickListener(v -> saveHabit());
    }

    private int getOffsetSpinnerIndex(int minutes) {
        switch (minutes) {
            case 5: return 1;
            case 10: return 2;
            case 30: return 3;
            case 60: return 4;
            default: return 0;
        }
    }

    private void saveHabit() {
        String name = inputName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Entrez un nom pour l’habitude", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedDays = new ArrayList<>();
        if (checkMon.isChecked()) selectedDays.add("Mon");
        if (checkTue.isChecked()) selectedDays.add("Tue");
        if (checkWed.isChecked()) selectedDays.add("Wed");
        if (checkThu.isChecked()) selectedDays.add("Thu");
        if (checkFri.isChecked()) selectedDays.add("Fri");
        if (checkSat.isChecked()) selectedDays.add("Sat");
        if (checkSun.isChecked()) selectedDays.add("Sun");

        String repeatDays = String.join(",", selectedDays);
        boolean hasReminder = switchReminder.isChecked();

        Habit habit = (habitId == -1)
                ? new Habit(name, selectedIcon, selectedColor, repeatDays, 1)
                : new Habit(habitId, name, selectedIcon, selectedColor, repeatDays, 1);

        habit.setHasReminder(hasReminder);
        habit.setReminderTime(reminderTime);
        habit.setReminderOffsetMinutes(reminderOffsetMinutes);

        if (habitId == -1) {
            viewModel.insert(habit);
            Toast.makeText(this, "Habitude ajoutée", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.update(habit);
            Toast.makeText(this, "Habitude mise à jour", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
