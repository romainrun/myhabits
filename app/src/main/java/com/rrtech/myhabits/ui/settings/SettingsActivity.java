package com.rrtech.myhabits.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.rrtech.myhabits.R;
import com.rrtech.myhabits.utils.ThemeManager;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Spinner themeSpinner = findViewById(R.id.spinner_theme);
        Spinner colorSpinner = findViewById(R.id.spinner_color);

        String[] themes = {"Clair", "Sombre"};
        String[] colors = {"Bleu", "Vert", "Violet"};

        themeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, themes));
        colorSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, colors));

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        themeSpinner.setSelection(prefs.getInt("theme", 0));
        colorSpinner.setSelection(prefs.getInt("accent", 0));

        findViewById(R.id.button_save).setOnClickListener(v -> {
            prefs.edit()
                    .putInt("theme", themeSpinner.getSelectedItemPosition())
                    .putInt("accent", colorSpinner.getSelectedItemPosition())
                    .apply();
            recreate();
        });
    }
}
