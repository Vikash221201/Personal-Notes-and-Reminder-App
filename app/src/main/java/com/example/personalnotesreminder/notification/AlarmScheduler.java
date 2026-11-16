package com.example.personalnotesreminder.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.personalnotesreminder.model.Note;

public class AlarmScheduler {

    public static void scheduleReminder(Context context, Note note) {
        if (note == null || !note.isReminderEnabled() || note.isCompleted()) {
            return;
        }

        long reminderTime = note.getReminderTime();
        if (reminderTime <= System.currentTimeMillis()) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Main Alarm
        int notificationId = getNotificationId(note);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("note_id", note.getId());
        intent.putExtra("note_title", note.getTitle());
        intent.putExtra("note_description", note.getDescription());
        intent.putExtra("note_category", note.getCategory());
        intent.putExtra("notification_id", notificationId);
        intent.putExtra("is_advance", false);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        setAlarm(alarmManager, reminderTime, pendingIntent);

        // Advance Alarm if set
        if (note.getAdvanceReminderMinutes() > 0) {
            long advanceTime = reminderTime - (note.getAdvanceReminderMinutes() * 60 * 1000L);
            if (advanceTime > System.currentTimeMillis()) {
                int advanceId = notificationId + 99999;
                Intent advanceIntent = new Intent(context, AlarmReceiver.class);
                advanceIntent.putExtra("note_id", note.getId());
                advanceIntent.putExtra("note_title", "Upcoming: " + note.getTitle());
                advanceIntent.putExtra("note_description", "Due in " + note.getAdvanceReminderMinutes() + " mins: " + note.getDescription());
                advanceIntent.putExtra("note_category", note.getCategory());
                advanceIntent.putExtra("notification_id", advanceId);
                advanceIntent.putExtra("is_advance", true);

                PendingIntent advancePendingIntent = PendingIntent.getBroadcast(
                        context,
                        advanceId,
                        advanceIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                setAlarm(alarmManager, advanceTime, advancePendingIntent);
            }
        }
    }

    private static void setAlarm(AlarmManager alarmManager, long time, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            } catch (SecurityException e) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }

    public static void cancelReminder(Context context, Note note) {
        if (note == null) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        int notificationId = getNotificationId(note);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);

        // Cancel advance alarm if exists
        int advanceId = notificationId + 99999;
        PendingIntent advancePendingIntent = PendingIntent.getBroadcast(
                context,
                advanceId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(advancePendingIntent);
    }

    private static int getNotificationId(Note note) {
        if (note.getId() > 0) return note.getId();
        long createdDate = note.getCreatedDate();
        return (int) (createdDate ^ (createdDate >>> 32));
    }
}
