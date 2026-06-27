package com.codewithgauresh.call_helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class CallReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "CallHelperChannel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent upcomingIntent = new Intent(context, UpcomingCallActivity.class);
        upcomingIntent.putExtra("scheduleId", intent.getIntExtra("scheduleId", -1));
        upcomingIntent.putExtra("phoneNumber", intent.getStringExtra("phoneNumber"));
        upcomingIntent.putExtra("contactName", intent.getStringExtra("contactName"));
        upcomingIntent.putExtra("notes", intent.getStringExtra("notes"));
        upcomingIntent.putExtra("ringtoneUri", intent.getStringExtra("ringtoneUri"));
        upcomingIntent.putExtra("useSpeakerphone", intent.getBooleanExtra("useSpeakerphone", false));
        upcomingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                upcomingIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, 
                mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setContentTitle(context.getString(R.string.upcoming_call))
                .setContentText(context.getString(R.string.upcoming_call) + " " + intent.getStringExtra("contactName"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
        
        context.startActivity(upcomingIntent);
    }
}
