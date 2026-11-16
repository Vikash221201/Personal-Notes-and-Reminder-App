package com.example.personalnotesreminder.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.personalnotesreminder.model.Note;

@Database(
        entities = {Note.class},
        version = 2,
        exportSchema = false
)
public abstract class NotesDatabase extends RoomDatabase {

    private static volatile NotesDatabase INSTANCE;

    // Access the DAO
    public abstract NoteDao noteDao();

    // Get database instance
    public static NotesDatabase getDatabase(Context context) {

        if (INSTANCE == null) {

            synchronized (NotesDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    NotesDatabase.class,
                                    "personal_notes_database"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
