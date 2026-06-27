package com.codewithgauresh.call_helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";
    public static final String KEY_DND_BYPASS = "dnd_bypass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RadioGroup rgTheme = findViewById(R.id.rgTheme);
        MaterialSwitch switchDndBypass = findViewById(R.id.switchDndBypass);
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        boolean dndBypass = prefs.getBoolean(KEY_DND_BYPASS, false);

        switchDndBypass.setChecked(dndBypass);
        switchDndBypass.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(KEY_DND_BYPASS, isChecked).apply());

        if (savedTheme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            rgTheme.check(R.id.rbThemeDefault);
        } else if (savedTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            rgTheme.check(R.id.rbThemeLight);
        } else if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            rgTheme.check(R.id.rbThemeDark);
        }

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rbThemeLight) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rbThemeDark) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            
            prefs.edit().putInt(KEY_THEME, mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    public static void applyTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);
    }
}
