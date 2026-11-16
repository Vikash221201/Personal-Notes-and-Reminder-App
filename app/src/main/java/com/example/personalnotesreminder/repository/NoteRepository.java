package com.example.personalnotesreminder.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.personalnotesreminder.database.NoteDao;
import com.example.personalnotesreminder.database.NotesDatabase;
import com.example.personalnotesreminder.model.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {

    private final NoteDao noteDao;
    private final LiveData<List<Note>> allNotes;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public NoteRepository(Application application) {
        NotesDatabase database = NotesDatabase.getDatabase(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> getTodayNotes(long startOfDay, long endOfDay) {
        return noteDao.getTodayNotes(startOfDay, endOfDay);
    }

    public LiveData<List<Note>> getScheduledNotes() {
        return noteDao.getScheduledNotes();
    }

    public LiveData<List<Note>> getFlaggedNotes() {
        return noteDao.getFlaggedNotes();
    }

    public LiveData<List<Note>> getNotesByCategory(String category) {
        return noteDao.getNotesByCategory(category);
    }

    public LiveData<List<Note>> getStudyNotesByType(String studyType) {
        return noteDao.getStudyNotesByType(studyType);
    }

    public LiveData<List<Note>> getStudyNotesWithAttachments() {
        return noteDao.getStudyNotesWithAttachments();
    }

    public LiveData<List<Note>> getNotesForDateRange(long startOfDay, long endOfDay) {
        return noteDao.getNotesForDateRange(startOfDay, endOfDay);
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return noteDao.searchNotes(query);
    }

    public LiveData<Note> getNoteById(int id) {
        return noteDao.getNoteById(id);
    }

    public void insert(Note note, OnNoteSavedListener listener) {
        executorService.execute(() -> {
            long id = noteDao.insert(note);
            note.setId((int) id);
            if (listener != null) {
                listener.onNoteSaved(note);
            }
        });
    }

    public void insert(Note note) {
        insert(note, null);
    }

    public void update(Note note) {
        executorService.execute(() -> noteDao.update(note));
    }

    public void delete(Note note) {
        executorService.execute(() -> noteDao.delete(note));
    }

    public void deleteAllNotes() {
        executorService.execute(() -> noteDao.deleteAllNotes());
    }

    public interface OnNoteSavedListener {
        void onNoteSaved(Note note);
    }
}
