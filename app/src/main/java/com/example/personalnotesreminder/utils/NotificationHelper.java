package com.example.personalnotesreminder.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.personalnotesreminder.R;
import com.example.personalnotesreminder.activities.MainActivity;
import com.example.personalnotesreminder.notification.NotificationActionReceiver;

public class NotificationHelper {

    public static final String CHANNEL_ID = "note_reminder_channel_v2";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Note Reminders";
            String description = "Notifications for notes, examinations, and assignments";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            PreferenceManager prefManager = new PreferenceManager(context);
            Uri soundUri = prefManager.getActualSoundUri(context);
            if (soundUri != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build();
                channel.setSound(soundUri, audioAttributes);
            }
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(
            Context context,
            int notificationId,
            int noteId,
            String title,
            String message,
            String category) {

        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createNotificationChannel(context);

        // Open Note / Main App Intent
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        openIntent.putExtra("target_note_id", noteId);

        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context,
                notificationId * 10 + 1,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Mark Completed Intent
        Intent markCompletedIntent = new Intent(context, NotificationActionReceiver.class);
        markCompletedIntent.setAction(NotificationActionReceiver.ACTION_MARK_COMPLETED);
        markCompletedIntent.putExtra(NotificationActionReceiver.EXTRA_NOTE_ID, noteId);
        markCompletedIntent.putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent markCompletedPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 2,
                markCompletedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Snooze Intent
        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction(NotificationActionReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra(NotificationActionReceiver.EXTRA_NOTE_ID, noteId);
        snoozeIntent.putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 3,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PreferenceManager prefManager = new PreferenceManager(context);
        Uri soundUri = prefManager.getActualSoundUri(context);

        String categoryHeader = category != null && !category.isEmpty() ? "[" + category + "] " : "";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(categoryHeader + title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openPendingIntent)
                .setSound(soundUri)
                .addAction(0, "Complete", markCompletedPendingIntent)
                .addAction(0, "Snooze 15m", snoozePendingIntent)
                .addAction(0, "Open Note", openPendingIntent);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    public static void showNotification(
            Context context,
            int notificationId,
            String title,
            String message) {
        showNotification(context, notificationId, -1, title, message, "Reminder");
    }
}
