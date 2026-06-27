package com.codewithgauresh.call_helper;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithgauresh.call_helper.database.AppDatabase;
import com.codewithgauresh.call_helper.database.CallSchedule;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CallScheduleAdapter adapter = new CallScheduleAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CallScheduleAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(CallSchedule schedule) {
                // Not used in History
            }

            @Override
            public void onDeleteClick(CallSchedule schedule) {
                deleteHistoryItem(schedule);
            }
        });

        AppDatabase.getInstance(this).callScheduleDao().getCallHistory().observe(this, adapter::setSchedules);
    }

    private void deleteHistoryItem(CallSchedule schedule) {
        new Thread(() -> {
            AppDatabase.getInstance(this).callScheduleDao().delete(schedule);
            runOnUiThread(() -> Toast.makeText(this, "History item deleted", Toast.LENGTH_SHORT).show());
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_history) {
            showClearHistoryDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_history)
                .setMessage("Are you sure you want to delete all call history?")
                .setPositiveButton("Clear All", (dialog, which) -> clearAllHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllHistory() {
        new Thread(() -> {
            AppDatabase.getInstance(this).callScheduleDao().deleteAllHistory();
            runOnUiThread(() -> Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show());
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
