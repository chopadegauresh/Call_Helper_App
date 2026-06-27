package com.codewithgauresh.call_helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.codewithgauresh.call_helper.database.CallSchedule;

public class CallScheduler {
    public static void scheduleCall(Context context, CallSchedule schedule) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
                return;
            }
        }

        Intent intent = new Intent(context, CallReceiver.class);
        intent.putExtra("scheduleId", schedule.getId());
        intent.putExtra("phoneNumber", schedule.getPhoneNumber());
        intent.putExtra("contactName", schedule.getContactName());
        intent.putExtra("ringtoneUri", schedule.getRingtoneUri());
        intent.putExtra("notes", schedule.getNotes());
        intent.putExtra("useSpeakerphone", schedule.isUseSpeakerphone());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                schedule.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    schedule.getScheduledTime(),
                    pendingIntent
            );
        }
    }

    public static void cancelCall(Context context, CallSchedule schedule) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CallReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                schedule.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
