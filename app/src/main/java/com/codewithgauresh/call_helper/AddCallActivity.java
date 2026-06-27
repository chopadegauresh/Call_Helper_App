package com.codewithgauresh.call_helper;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.codewithgauresh.call_helper.database.AppDatabase;
import com.codewithgauresh.call_helper.database.CallSchedule;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddCallActivity extends AppCompatActivity {

    private TextInputEditText etContactName, etPhoneNumber, etNotes;
    private Button btnPickDate, btnPickTime, btnPickRingtone;
    private TextView tvSelectedDateTime;
    private MaterialSwitch switchRecurring, switchSpeakerphone;

    private final Calendar calendar = Calendar.getInstance();
    private String selectedRingtoneUri = "";
    private int scheduleId = -1;

    private ActivityResultLauncher<Intent> ringtonePickerLauncher;
    private ActivityResultLauncher<Intent> contactPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_call);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etContactName = findViewById(R.id.etContactName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etNotes = findViewById(R.id.etNotes);
        switchRecurring = findViewById(R.id.switchRecurring);
        switchSpeakerphone = findViewById(R.id.switchSpeakerphone);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnPickRingtone = findViewById(R.id.btnPickRingtone);
        ImageButton btnPickContact = findViewById(R.id.btnPickContact);
        Button btnSave = findViewById(R.id.btnSave);
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime);

        if (getIntent().hasExtra("schedule_id")) {
            scheduleId = getIntent().getIntExtra("schedule_id", -1);
            loadScheduleData(scheduleId);
            btnSave.setText(R.string.save_call_schedule); // Should be "Update" but we use one string for simplicity or add another
        }

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnPickRingtone.setOnClickListener(v -> pickRingtone());
        btnPickContact.setOnClickListener(v -> pickContact());
        btnSave.setOnClickListener(v -> saveSchedule());

        ringtonePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            selectedRingtoneUri = uri.toString();
                            String ringtoneTitle = RingtoneManager.getRingtone(this, uri).getTitle(this);
                            btnPickRingtone.setText(String.format("%s: %s", getString(R.string.pick_ringtone), ringtoneTitle));
                            Toast.makeText(this, "Ringtone selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        contactPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri contactUri = result.getData().getData();
                        if (contactUri != null) {
                            String[] projection = {
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                            };
                            try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                                if (cursor != null && cursor.moveToFirst()) {
                                    etContactName.setText(cursor.getString(0));
                                    etPhoneNumber.setText(cursor.getString(1));
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateTimeText();
            btnPickDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            updateDateTimeText();
            btnPickTime.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void updateDateTimeText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
        tvSelectedDateTime.setText(sdf.format(calendar.getTime()));
    }

    private void pickRingtone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        ringtonePickerLauncher.launch(intent);
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
    }

    private void loadScheduleData(int id) {
        new Thread(() -> {
            CallSchedule schedule = AppDatabase.getInstance(this).callScheduleDao().getScheduleById(id);
            if (schedule != null) {
                runOnUiThread(() -> {
                    etContactName.setText(schedule.getContactName());
                    etPhoneNumber.setText(schedule.getPhoneNumber());
                    etNotes.setText(schedule.getNotes());
                    switchRecurring.setChecked(schedule.isRecurring());
                    switchSpeakerphone.setChecked(schedule.isUseSpeakerphone());
                    calendar.setTimeInMillis(schedule.getScheduledTime());
                    updateDateTimeText();
                    
                    btnPickDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
                    btnPickTime.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.getTime()));
                    
                    if (schedule.getRingtoneUri() != null && !schedule.getRingtoneUri().isEmpty()) {
                        try {
                            String title = RingtoneManager.getRingtone(this, Uri.parse(schedule.getRingtoneUri())).getTitle(this);
                            btnPickRingtone.setText(String.format("%s: %s", getString(R.string.pick_ringtone), title));
                        } catch (Exception e) {
                            btnPickRingtone.setText(R.string.pick_ringtone);
                        }
                    }
                });
            }
        }).start();
    }

    private void saveSchedule() {
        String name = etContactName.getText() != null ? etContactName.getText().toString() : "";
        String number = etPhoneNumber.getText() != null ? etPhoneNumber.getText().toString() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString() : "";
        boolean isRecurring = switchRecurring.isChecked();
        boolean useSpeakerphone = switchSpeakerphone.isChecked();

        if (name.isEmpty() || number.isEmpty()) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            CallSchedule schedule;
            if (scheduleId == -1) {
                schedule = new CallSchedule(number, name, calendar.getTimeInMillis(), selectedRingtoneUri, true);
                schedule.setNotes(notes);
                schedule.setRecurring(isRecurring);
                schedule.setUseSpeakerphone(useSpeakerphone);
                schedule.setStatus("Pending");
                long id = AppDatabase.getInstance(this).callScheduleDao().insert(schedule);
                schedule.setId((int) id);
            } else {
                schedule = AppDatabase.getInstance(this).callScheduleDao().getScheduleById(scheduleId);
                if (schedule != null) {
                    schedule.setContactName(name);
                    schedule.setPhoneNumber(number);
                    schedule.setScheduledTime(calendar.getTimeInMillis());
                    schedule.setRingtoneUri(selectedRingtoneUri);
                    schedule.setNotes(notes);
                    schedule.setRecurring(isRecurring);
                    schedule.setUseSpeakerphone(useSpeakerphone);
                    schedule.setStatus("Pending");
                    schedule.setCompleted(false);
                    AppDatabase.getInstance(this).callScheduleDao().update(schedule);
                }
            }

            if (schedule != null) {
                runOnUiThread(() -> {
                    CallScheduler.scheduleCall(this, schedule);
                    Toast.makeText(this, scheduleId == -1 ? "Call Scheduled" : "Schedule Updated", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }
}
