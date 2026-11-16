package com.example.personalnotesreminder.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.personalnotesreminder.model.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    Note getNoteByIdSync(int id);

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    LiveData<Note> getNoteById(int id);

    // Get all notes ordered by pinned DESC, createdDate DESC
    @Query("SELECT * FROM notes ORDER BY pinned DESC, createdDate DESC")
    LiveData<List<Note>> getAllNotes();

    // Get today's notes & reminders
    @Query("SELECT * FROM notes WHERE (reminderTime >= :startOfDay AND reminderTime <= :endOfDay) " +
            "OR (createdDate >= :startOfDay AND createdDate <= :endOfDay) " +
            "ORDER BY pinned DESC, createdDate DESC")
    LiveData<List<Note>> getTodayNotes(long startOfDay, long endOfDay);

    // Get all scheduled notes & reminders
    @Query("SELECT * FROM notes WHERE reminderEnabled = 1 AND reminderTime > 0 ORDER BY reminderTime ASC")
    LiveData<List<Note>> getScheduledNotes();

    // Get flagged/important notes
    @Query("SELECT * FROM notes WHERE pinned = 1 ORDER BY createdDate DESC")
    LiveData<List<Note>> getFlaggedNotes();

    // Get notes by category
    @Query("SELECT * FROM notes WHERE category = :category ORDER BY pinned DESC, createdDate DESC")
    LiveData<List<Note>> getNotesByCategory(String category);

    // Get study notes by studyType (General Note, Examination, Assignment)
    @Query("SELECT * FROM notes WHERE category = 'Study' AND studyType = :studyType ORDER BY reminderTime ASC, createdDate DESC")
    LiveData<List<Note>> getStudyNotesByType(String studyType);

    // Get study notes that have attachments
    @Query("SELECT * FROM notes WHERE category = 'Study' AND attachmentPath IS NOT NULL AND attachmentPath != '' ORDER BY createdDate DESC")
    LiveData<List<Note>> getStudyNotesWithAttachments();

    // Get notes by date range (for calendar view)
    @Query("SELECT * FROM notes WHERE reminderTime >= :startOfDay AND reminderTime <= :endOfDay ORDER BY reminderTime ASC")
    LiveData<List<Note>> getNotesForDateRange(long startOfDay, long endOfDay);

    // Search notes
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :searchQuery || '%' " +
            "OR description LIKE '%' || :searchQuery || '%' " +
            "OR category LIKE '%' || :searchQuery || '%' " +
            "OR subject LIKE '%' || :searchQuery || '%' " +
            "OR subCategory LIKE '%' || :searchQuery || '%' " +
            "ORDER BY pinned DESC, createdDate DESC")
    LiveData<List<Note>> searchNotes(String searchQuery);

    // Delete all notes
    @Query("DELETE FROM notes")
    void deleteAllNotes();
}
