package com.example.personalnotesreminder.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.personalnotesreminder.model.Note;
import com.example.personalnotesreminder.repository.NoteRepository;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> getTodayNotes(long startOfDay, long endOfDay) {
        return repository.getTodayNotes(startOfDay, endOfDay);
    }

    public LiveData<List<Note>> getScheduledNotes() {
        return repository.getScheduledNotes();
    }

    public LiveData<List<Note>> getFlaggedNotes() {
        return repository.getFlaggedNotes();
    }

    public LiveData<List<Note>> getNotesByCategory(String category) {
        return repository.getNotesByCategory(category);
    }

    public LiveData<List<Note>> getStudyNotesByType(String studyType) {
        return repository.getStudyNotesByType(studyType);
    }

    public LiveData<List<Note>> getStudyNotesWithAttachments() {
        return repository.getStudyNotesWithAttachments();
    }

    public LiveData<List<Note>> getNotesForDateRange(long startOfDay, long endOfDay) {
        return repository.getNotesForDateRange(startOfDay, endOfDay);
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    public LiveData<Note> getNoteById(int id) {
        return repository.getNoteById(id);
    }

    public void insert(Note note, NoteRepository.OnNoteSavedListener listener) {
        repository.insert(note, listener);
    }

    public void insert(Note note) {
        repository.insert(note);
    }

    public void update(Note note) {
        repository.update(note);
    }

    public void delete(Note note) {
        repository.delete(note);
    }

    public void deleteAllNotes() {
        repository.deleteAllNotes();
    }
}
