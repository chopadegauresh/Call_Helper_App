package com.codewithgauresh.call_helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.codewithgauresh.call_helper.database.AppDatabase;
import com.codewithgauresh.call_helper.database.CallSchedule;

import java.util.Locale;

public class UpcomingCallActivity extends AppCompatActivity {
    private Ringtone ringtone;
    private CountDownTimer countDownTimer;
    private String phoneNumber, contactName;
    private boolean useSpeakerphone;
    private int scheduleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (getSystemService(android.app.KeyguardManager.class) != null) {
                getSystemService(android.app.KeyguardManager.class).requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        
        setContentView(R.layout.activity_upcoming_call);

        TextView tvTitle = findViewById(R.id.tvAlertTitle);
        tvTitle.setText(R.string.upcoming_call);

        scheduleId = getIntent().getIntExtra("scheduleId", -1);
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        contactName = getIntent().getStringExtra("contactName");
        String notes = getIntent().getStringExtra("notes");
        useSpeakerphone = getIntent().getBooleanExtra("useSpeakerphone", false);
        String ringtoneUriString = getIntent().getStringExtra("ringtoneUri");

        TextView tvName = findViewById(R.id.tvUpcomingName);
        TextView tvNumber = findViewById(R.id.tvUpcomingNumber);
        TextView tvNotes = findViewById(R.id.tvUpcomingNotes);
        TextView tvCountdown = findViewById(R.id.tvCountdown);
        Button btnCancel = findViewById(R.id.btnCancelUpcoming);
        Button btnSnooze = findViewById(R.id.btnSnoozeUpcoming);

        tvName.setText(contactName);
        tvNumber.setText(phoneNumber);
        tvNotes.setText(notes != null && !notes.isEmpty() ? String.format("%s: %s", getString(R.string.notes), notes) : "");

        startRinging(ringtoneUriString);

        countDownTimer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                makeCall();
            }
        }.start();

        btnCancel.setOnClickListener(v -> {
            cancelEverything();
            finish();
        });

        btnSnooze.setOnClickListener(v -> {
            snoozeCall();
            finish();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
                Toast.makeText(this, "Ringtone Muted", Toast.LENGTH_SHORT).show();
                return true; 
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startRinging(String uriString) {
        Uri ringtoneUri;
        if (uriString != null && !uriString.isEmpty()) {
            ringtoneUri = Uri.parse(uriString);
        } else {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }
        
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        
        if (ringtone != null) {
            SharedPreferences prefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
            boolean dndBypass = prefs.getBoolean(SettingsActivity.KEY_DND_BYPASS, false);

            if (dndBypass) {
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                ringtone.setAudioAttributes(attributes);
                
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVol / 2, 0);
                }
            }
            ringtone.play();
        }
    }

    private void snoozeCall() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        new Thread(() -> {
            CallSchedule original = AppDatabase.getInstance(this).callScheduleDao().getScheduleById(scheduleId);
            if (original != null) {
                CallSchedule historyLog = new CallSchedule(
                        original.getPhoneNumber(),
                        original.getContactName(),
                        System.currentTimeMillis(),
                        original.getRingtoneUri(),
                        false
                );
                historyLog.setCompleted(true);
                historyLog.setStatus("Snoozed");
                historyLog.setNotes(original.getNotes());
                AppDatabase.getInstance(this).callScheduleDao().insert(historyLog);

                original.setScheduledTime(System.currentTimeMillis() + 5 * 60 * 1000);
                original.setSnoozeCount(original.getSnoozeCount() + 1);
                original.setStatus("Snoozed");
                AppDatabase.getInstance(this).callScheduleDao().update(original);
                
                runOnUiThread(() -> {
                    CallScheduler.scheduleCall(this, original);
                    Toast.makeText(this, "Call Snoozed for 5 minutes", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, 
                mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CallHelperChannel")
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("Call Snoozed")
                    .setContentText("Will call " + contactName + " in 5 minutes.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);
            nm.notify(1, builder.build());
        }
    }

    private void makeCall() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(1);
        }

        if (scheduleId != -1) {
            new Thread(() -> {
                CallSchedule original = AppDatabase.getInstance(this).callScheduleDao().getScheduleById(scheduleId);
                if (original != null) {
                    CallSchedule historyLog = new CallSchedule(
                            original.getPhoneNumber(),
                            original.getContactName(),
                            System.currentTimeMillis(),
                            original.getRingtoneUri(),
                            false
                    );
                    historyLog.setCompleted(true);
                    historyLog.setStatus("Picked Up");
                    historyLog.setNotes(original.getNotes());
                    historyLog.setSnoozeCount(original.getSnoozeCount());
                    AppDatabase.getInstance(this).callScheduleDao().insert(historyLog);

                    if (original.isRecurring()) {
                        original.setScheduledTime(original.getScheduledTime() + 24 * 60 * 60 * 1000);
                        original.setSnoozeCount(0); 
                        original.setStatus("Pending");
                        AppDatabase.getInstance(this).callScheduleDao().update(original);
                        runOnUiThread(() -> CallScheduler.scheduleCall(this, original));
                    } else {
                        original.setStatus("Finished - Tap Edit to Reschedule");
                        original.setSnoozeCount(0);
                        AppDatabase.getInstance(this).callScheduleDao().update(original);
                    }
                }
            }).start();
        }
        
        if (useSpeakerphone) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audioManager.setSpeakerphoneOn(true);
            }
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            startActivity(callIntent);
        } catch (SecurityException e) {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(dialIntent);
        }
        finish();
    }

    private void cancelEverything() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (scheduleId != -1) {
            new Thread(() -> {
                CallSchedule original = AppDatabase.getInstance(this).callScheduleDao().getScheduleById(scheduleId);
                if (original != null) {
                    CallSchedule historyLog = new CallSchedule(
                            original.getPhoneNumber(),
                            original.getContactName(),
                            System.currentTimeMillis(),
                            original.getRingtoneUri(),
                            false
                    );
                    historyLog.setCompleted(true);
                    historyLog.setStatus("Cancelled");
                    historyLog.setNotes(original.getNotes());
                    AppDatabase.getInstance(this).callScheduleDao().insert(historyLog);

                    if (original.isRecurring()) {
                        original.setScheduledTime(original.getScheduledTime() + 24 * 60 * 60 * 1000);
                        original.setStatus("Pending");
                        AppDatabase.getInstance(this).callScheduleDao().update(original);
                        runOnUiThread(() -> CallScheduler.scheduleCall(this, original));
                    } else {
                        original.setStatus("Cancelled - Tap Edit to Reschedule");
                        AppDatabase.getInstance(this).callScheduleDao().update(original);
                    }
                }
            }).start();
        }

        Intent historyIntent = new Intent(this, HistoryActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, 
                historyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CallHelperChannel")
                    .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
                    .setContentTitle("Call Cancelled")
                    .setContentText(String.format(Locale.getDefault(), "Call to %s was cancelled.", contactName))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);
            nm.notify(1, builder.build());
        }

        Toast.makeText(this, "Call Cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
