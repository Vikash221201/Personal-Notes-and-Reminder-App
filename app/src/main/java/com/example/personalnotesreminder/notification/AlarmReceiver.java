package com.example.personalnotesreminder.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.personalnotesreminder.utils.NotificationHelper;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        int noteId = intent.getIntExtra("note_id", -1);
        String title = intent.getStringExtra("note_title");
        String description = intent.getStringExtra("note_description");
        String category = intent.getStringExtra("note_category");
        int notificationId = intent.getIntExtra("notification_id", 100);

        if (title == null || title.trim().isEmpty()) {
            title = "Scheduled Reminder";
        }

        if (description == null || description.trim().isEmpty()) {
            description = "You have a scheduled reminder.";
        }

        NotificationHelper.showNotification(
                context,
                notificationId,
                noteId,
                title,
                description,
                category
        );
    }
}
