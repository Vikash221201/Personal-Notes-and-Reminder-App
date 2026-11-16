package com.example.personalnotesreminder.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.personalnotesreminder.database.NoteDao;
import com.example.personalnotesreminder.database.NotesDatabase;
import com.example.personalnotesreminder.model.Note;

import java.util.concurrent.Executors;

public class NotificationActionReceiver extends BroadcastReceiver {

    public static final String ACTION_MARK_COMPLETED = "com.example.personalnotesreminder.ACTION_MARK_COMPLETED";
    public static final String ACTION_SNOOZE = "com.example.personalnotesreminder.ACTION_SNOOZE";

    public static final String EXTRA_NOTE_ID = "extra_note_id";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        int noteId = intent.getIntExtra(EXTRA_NOTE_ID, -1);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

        // Cancel notification on action tap
        if (notificationId != -1) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(notificationId);
            }
        }

        String action = intent.getAction();
        if (ACTION_MARK_COMPLETED.equals(action)) {
            if (noteId != -1) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    NoteDao dao = NotesDatabase.getDatabase(context).noteDao();
                    Note note = dao.getNoteByIdSync(noteId);
                    if (note != null) {
                        note.setCompleted(true);
                        dao.update(note);
                        AlarmScheduler.cancelReminder(context, note);
                    }
                });
                Toast.makeText(context, "Note marked as completed", Toast.LENGTH_SHORT).show();
            }
        } else if (ACTION_SNOOZE.equals(action)) {
            if (noteId != -1) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    NoteDao dao = NotesDatabase.getDatabase(context).noteDao();
                    Note note = dao.getNoteByIdSync(noteId);
                    if (note != null) {
                        // Snooze for 15 minutes
                        long snoozeTime = System.currentTimeMillis() + (15 * 60 * 1000);
                        note.setReminderTime(snoozeTime);
                        note.setReminderEnabled(true);
                        dao.update(note);
                        AlarmScheduler.scheduleReminder(context, note);
                    }
                });
                Toast.makeText(context, "Reminder snoozed for 15 minutes", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
