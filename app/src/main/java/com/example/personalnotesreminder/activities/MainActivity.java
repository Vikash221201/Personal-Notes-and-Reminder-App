package com.example.personalnotesreminder.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalnotesreminder.R;
import com.example.personalnotesreminder.adapter.NoteAdapter;
import com.example.personalnotesreminder.model.Note;
import com.example.personalnotesreminder.notification.AlarmScheduler;
import com.example.personalnotesreminder.utils.NotificationHelper;
import com.example.personalnotesreminder.utils.PreferenceManager;
import com.example.personalnotesreminder.viewmodel.NoteViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteActionListener {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navView;
    private RecyclerView recyclerNotes, recyclerCalendarNotes;
    private EditText edtSearch;
    private Chip chipFilterActive, chipFilterCompleted, chipFilterAll, chipSortDue, chipSortPriority;
    private LinearLayout layoutTodaySummary, layoutEmptyState;
    private TextView txtCountOverdue, txtCountAcademic, txtCountPending, txtEmptyTitle, txtEmptySub, txtCalendarDateHeader;
    private ScrollView containerCalendarView, containerSettingsView;
    private CalendarView calendarView;
    private FloatingActionButton fabAddNote;

    // Settings Views
    private RadioGroup radioGroupTheme, radioGroupTimeFormat, radioGroupAudio;
    private RadioButton radioLight, radioDark, radioSystem, radio12h, radio24h, radioAudioDefault, radioAudioAlarm, radioAudioCustom;
    private Button btnSelectCustomAudio, btnPreviewSound, btnTestNotification;

    private NoteAdapter mainAdapter;
    private NoteAdapter calendarAdapter;
    private NoteViewModel noteViewModel;
    private PreferenceManager preferenceManager;
    private MediaPlayer mediaPlayer;

    private LiveData<List<Note>> currentNotesLiveData;
    private List<Note> loadedNotes = new ArrayList<>();

    private enum Section {
        TODAY, SCHEDULED, ALL, FLAGGED, CALENDAR, SETTINGS, CAT_PERSONAL, CAT_STUDY, CAT_WORK, CAT_SHOPPING
    }

    private Section currentSection = Section.ALL;
    private String statusFilter = "Active"; // "Active", "Completed", "All"
    private String sortMode = "None"; // "None", "DueDate", "Priority"

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission denied. Reminders may not sound.", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<String> audioPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    preferenceManager.setSoundUri(uri.toString());
                    preferenceManager.setSoundType("custom");
                    radioAudioCustom.setChecked(true);
                    Toast.makeText(this, "Custom sound selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferenceManager = new PreferenceManager(this);
        preferenceManager.applySavedTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        setupToolbarAndDrawer();
        setupRecyclerViews();
        setupFiltersAndSearch();
        setupSettingsUI();
        setupCalendarView();

        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermission();

        fabAddNote.setOnClickListener(v -> openAddEditNote(null));

        handleIntentExtras(getIntent());

        // Default Section: ALL NOTES (shows all saved notes)
        switchSection(Section.ALL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh active section query whenever coming back to MainActivity
        switchSection(currentSection);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentExtras(intent);
    }

    private void handleIntentExtras(Intent intent) {
        if (intent != null && intent.hasExtra("target_note_id")) {
            int noteId = intent.getIntExtra("target_note_id", -1);
            if (noteId != -1) {
                noteViewModel.getNoteById(noteId).observe(this, note -> {
                    if (note != null) {
                        openAddEditNote(note);
                    }
                });
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.navView);

        recyclerNotes = findViewById(R.id.recyclerNotes);
        recyclerCalendarNotes = findViewById(R.id.recyclerCalendarNotes);
        edtSearch = findViewById(R.id.edtSearch);

        chipFilterActive = findViewById(R.id.chipFilterActive);
        chipFilterCompleted = findViewById(R.id.chipFilterCompleted);
        chipFilterAll = findViewById(R.id.chipFilterAll);
        chipSortDue = findViewById(R.id.chipSortDue);
        chipSortPriority = findViewById(R.id.chipSortPriority);

        layoutTodaySummary = findViewById(R.id.layoutTodaySummary);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        txtCountOverdue = findViewById(R.id.txtCountOverdue);
        txtCountAcademic = findViewById(R.id.txtCountAcademic);
        txtCountPending = findViewById(R.id.txtCountPending);
        txtEmptyTitle = findViewById(R.id.txtEmptyTitle);
        txtEmptySub = findViewById(R.id.txtEmptySub);
        txtCalendarDateHeader = findViewById(R.id.txtCalendarDateHeader);

        containerCalendarView = findViewById(R.id.containerCalendarView);
        containerSettingsView = findViewById(R.id.containerSettingsView);
        calendarView = findViewById(R.id.calendarView);
        fabAddNote = findViewById(R.id.fabAddNote);

        // Settings
        radioGroupTheme = findViewById(R.id.radioGroupTheme);
        radioGroupTimeFormat = findViewById(R.id.radioGroupTimeFormat);
        radioGroupAudio = findViewById(R.id.radioGroupAudio);
        radioLight = findViewById(R.id.radioLight);
        radioDark = findViewById(R.id.radioDark);
        radioSystem = findViewById(R.id.radioSystem);
        radio12h = findViewById(R.id.radio12h);
        radio24h = findViewById(R.id.radio24h);
        radioAudioDefault = findViewById(R.id.radioAudioDefault);
        radioAudioAlarm = findViewById(R.id.radioAudioAlarm);
        radioAudioCustom = findViewById(R.id.radioAudioCustom);
        btnSelectCustomAudio = findViewById(R.id.btnSelectCustomAudio);
        btnPreviewSound = findViewById(R.id.btnPreviewSound);
        btnTestNotification = findViewById(R.id.btnTestNotification);
    }

    private void setupToolbarAndDrawer() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.open());

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_today) {
                switchSection(Section.TODAY);
            } else if (id == R.id.nav_scheduled) {
                switchSection(Section.SCHEDULED);
            } else if (id == R.id.nav_all) {
                switchSection(Section.ALL);
            } else if (id == R.id.nav_flagged) {
                switchSection(Section.FLAGGED);
            } else if (id == R.id.nav_calendar) {
                switchSection(Section.CALENDAR);
            } else if (id == R.id.nav_settings) {
                switchSection(Section.SETTINGS);
            } else if (id == R.id.nav_cat_personal) {
                switchSection(Section.CAT_PERSONAL);
            } else if (id == R.id.nav_cat_study) {
                switchSection(Section.CAT_STUDY);
            } else if (id == R.id.nav_cat_work) {
                switchSection(Section.CAT_WORK);
            } else if (id == R.id.nav_cat_shopping) {
                switchSection(Section.CAT_SHOPPING);
            }
            drawerLayout.close();
            return true;
        });
    }

    private void setupRecyclerViews() {
        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        mainAdapter = new NoteAdapter(this);
        recyclerNotes.setAdapter(mainAdapter);

        recyclerCalendarNotes.setLayoutManager(new LinearLayoutManager(this));
        calendarAdapter = new NoteAdapter(this);
        recyclerCalendarNotes.setAdapter(calendarAdapter);
    }

    private void setupFiltersAndSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyCurrentFiltersAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipFilterActive.setOnClickListener(v -> {
            statusFilter = "Active";
            applyCurrentFiltersAndSort();
        });

        chipFilterCompleted.setOnClickListener(v -> {
            statusFilter = "Completed";
            applyCurrentFiltersAndSort();
        });

        chipFilterAll.setOnClickListener(v -> {
            statusFilter = "All";
            applyCurrentFiltersAndSort();
        });

        chipSortDue.setOnClickListener(v -> {
            sortMode = chipSortDue.isChecked() ? "DueDate" : "None";
            if (chipSortDue.isChecked()) chipSortPriority.setChecked(false);
            applyCurrentFiltersAndSort();
        });

        chipSortPriority.setOnClickListener(v -> {
            sortMode = chipSortPriority.isChecked() ? "Priority" : "None";
            if (chipSortPriority.isChecked()) chipSortDue.setChecked(false);
            applyCurrentFiltersAndSort();
        });
    }

    private void switchSection(Section section) {
        currentSection = section;

        // Hide containers
        recyclerNotes.setVisibility(View.GONE);
        containerCalendarView.setVisibility(View.GONE);
        containerSettingsView.setVisibility(View.GONE);
        layoutTodaySummary.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        fabAddNote.setVisibility(View.VISIBLE);

        if (currentNotesLiveData != null) {
            currentNotesLiveData.removeObservers(this);
        }

        switch (section) {
            case TODAY:
                toolbar.setTitle("Today");
                navView.setCheckedItem(R.id.nav_today);
                layoutTodaySummary.setVisibility(View.VISIBLE);
                recyclerNotes.setVisibility(View.VISIBLE);
                loadTodayNotes();
                break;
            case SCHEDULED:
                toolbar.setTitle("Scheduled");
                navView.setCheckedItem(R.id.nav_scheduled);
                recyclerNotes.setVisibility(View.VISIBLE);
                observeLiveData(noteViewModel.getScheduledNotes());
                break;
            case ALL:
                toolbar.setTitle("All Notes");
                navView.setCheckedItem(R.id.nav_all);
                recyclerNotes.setVisibility(View.VISIBLE);
                observeLiveData(noteViewModel.getAllNotes());
                break;
            case FLAGGED:
                toolbar.setTitle("Flagged / Important");
                navView.setCheckedItem(R.id.nav_flagged);
                recyclerNotes.setVisibility(View.VISIBLE);
                observeLiveData(noteViewModel.getFlaggedNotes());
                break;
            case CALENDAR:
                toolbar.setTitle("Calendar View");
                navView.setCheckedItem(R.id.nav_calendar);
                containerCalendarView.setVisibility(View.VISIBLE);
                loadCalendarDateNotes(calendarView.getDate());
                break;
            case SETTINGS:
                toolbar.setTitle("Settings");
                navView.setCheckedItem(R.id.nav_settings);
                containerSettingsView.setVisibility(View.VISIBLE);
                fabAddNote.setVisibility(View.GONE);
                break;
            case CAT_PERSONAL:
                toolbar.setTitle("Personal Notes");
                navView.setCheckedItem(R.id.nav_cat_personal);
                recyclerNotes.setVisibility(View.VISIBLE);
                observeLiveData(noteViewModel.getNotesByCategory("Personal"));
                break;
            case CAT_STUDY:
                toolbar.setTitle("Study Notes & Academic");
                navView.setCheckedItem(R.id.nav_cat_study);
                recyclerNotes.setVisibility(View.VISIBLE);
                observeLiveData(noteViewModel.getNotesByCategory("Study"));
                break;
            case CAT_WORK:
                toolbar.setTitle("Work Notes");
                navView.setCheckedItem(R.id.nav_cat_work);
                recyclerNotes.setVisibility(View.VISIBLE);
                observeLiveData(noteViewModel.getNotesByCategory("Work"));
                break;
            case CAT_SHOPPING:
                toolbar.setTitle("Shopping Lists");
                navView.setCheckedItem(R.id.nav_cat_shopping);
                recyclerNotes.setVisibility(View.VISIBLE);
                observeLiveData(noteViewModel.getNotesByCategory("Shopping"));
                break;
        }
    }

    private void loadTodayNotes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endOfDay = cal.getTimeInMillis();

        observeLiveData(noteViewModel.getTodayNotes(startOfDay, endOfDay));
    }

    private void observeLiveData(LiveData<List<Note>> liveData) {
        if (currentNotesLiveData != null) {
            currentNotesLiveData.removeObservers(this);
        }
        currentNotesLiveData = liveData;
        currentNotesLiveData.observe(this, notes -> {
            loadedNotes = notes != null ? notes : new ArrayList<>();
            updateSummaryCounters();
            applyCurrentFiltersAndSort();
        });
    }

    private void updateSummaryCounters() {
        long now = System.currentTimeMillis();
        int overdue = 0;
        int academic = 0;
        int pending = 0;

        for (Note n : loadedNotes) {
            if (!n.isCompleted()) {
                pending++;
                if (n.isReminderEnabled() && n.getReminderTime() > 0 && n.getReminderTime() < now) {
                    overdue++;
                }
            }
            if ("Study".equals(n.getCategory()) && ("Examination".equals(n.getStudyType()) || "Assignment".equals(n.getStudyType()))) {
                academic++;
            }
        }

        txtCountOverdue.setText(String.valueOf(overdue));
        txtCountAcademic.setText(String.valueOf(academic));
        txtCountPending.setText(String.valueOf(pending));
    }

    private void applyCurrentFiltersAndSort() {
        if (currentSection == Section.CALENDAR || currentSection == Section.SETTINGS) return;

        String query = edtSearch.getText().toString().trim().toLowerCase();
        List<Note> filtered = new ArrayList<>();

        for (Note n : loadedNotes) {
            // Search Query Filter
            if (!query.isEmpty()) {
                boolean matchTitle = n.getTitle() != null && n.getTitle().toLowerCase().contains(query);
                boolean matchDesc = n.getDescription() != null && n.getDescription().toLowerCase().contains(query);
                boolean matchCat = n.getCategory() != null && n.getCategory().toLowerCase().contains(query);
                boolean matchSub = n.getSubject() != null && n.getSubject().toLowerCase().contains(query);
                boolean matchSubCat = n.getSubCategory() != null && n.getSubCategory().toLowerCase().contains(query);
                if (!matchTitle && !matchDesc && !matchCat && !matchSub && !matchSubCat) {
                    continue;
                }
            }

            // Status Filter
            if ("Active".equals(statusFilter) && n.isCompleted()) {
                continue;
            } else if ("Completed".equals(statusFilter) && !n.isCompleted()) {
                continue;
            }

            filtered.add(n);
        }

        // Sorting
        if ("DueDate".equals(sortMode)) {
            Collections.sort(filtered, (n1, n2) -> Long.compare(n1.getReminderTime(), n2.getReminderTime()));
        } else if ("Priority".equals(sortMode)) {
            Collections.sort(filtered, (n1, n2) -> {
                int p1 = getPriorityRank(n1.getPriority());
                int p2 = getPriorityRank(n2.getPriority());
                return Integer.compare(p1, p2);
            });
        }

        mainAdapter.setNotes(filtered);

        if (filtered.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            txtEmptyTitle.setText("No Notes Found");
            txtEmptySub.setText("No notes match your current filters or search query.");
        } else {
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private int getPriorityRank(String priority) {
        if ("High".equalsIgnoreCase(priority)) return 1;
        if ("Medium".equalsIgnoreCase(priority)) return 2;
        if ("Low".equalsIgnoreCase(priority)) return 3;
        return 4;
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfDay = cal.getTimeInMillis();

            cal.set(year, month, dayOfMonth, 23, 59, 59);
            cal.set(Calendar.MILLISECOND, 999);
            long endOfDay = cal.getTimeInMillis();

            txtCalendarDateHeader.setText("Notes for " + preferenceManager.formatDate(startOfDay));
            noteViewModel.getNotesForDateRange(startOfDay, endOfDay).observe(this, notes -> {
                calendarAdapter.setNotes(notes);
            });
        });
    }

    private void loadCalendarDateNotes(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endOfDay = cal.getTimeInMillis();

        txtCalendarDateHeader.setText("Notes for " + preferenceManager.formatDate(startOfDay));
        noteViewModel.getNotesForDateRange(startOfDay, endOfDay).observe(this, notes -> {
            calendarAdapter.setNotes(notes);
        });
    }

    private void setupSettingsUI() {
        // Theme
        int themeMode = preferenceManager.getThemeMode();
        if (themeMode == PreferenceManager.THEME_LIGHT) radioLight.setChecked(true);
        else if (themeMode == PreferenceManager.THEME_DARK) radioDark.setChecked(true);
        else radioSystem.setChecked(true);

        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedMode = PreferenceManager.THEME_SYSTEM;
            if (checkedId == R.id.radioLight) selectedMode = PreferenceManager.THEME_LIGHT;
            else if (checkedId == R.id.radioDark) selectedMode = PreferenceManager.THEME_DARK;

            preferenceManager.setThemeMode(selectedMode);
            PreferenceManager.applyTheme(selectedMode);
        });

        // Time Format
        if (preferenceManager.is24HourFormat()) radio24h.setChecked(true);
        else radio12h.setChecked(true);

        radioGroupTimeFormat.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio24h) {
                preferenceManager.setTimeFormat(PreferenceManager.FORMAT_24H);
            } else {
                preferenceManager.setTimeFormat(PreferenceManager.FORMAT_12H);
            }
            mainAdapter.notifyDataSetChanged();
            calendarAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Time format updated", Toast.LENGTH_SHORT).show();
        });

        // Audio
        String soundType = preferenceManager.getSoundType();
        if ("alarm".equals(soundType)) radioAudioAlarm.setChecked(true);
        else if ("custom".equals(soundType)) {
            radioAudioCustom.setChecked(true);
            btnSelectCustomAudio.setVisibility(View.VISIBLE);
        } else radioAudioDefault.setChecked(true);

        radioGroupAudio.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioAudioAlarm) {
                preferenceManager.setSoundType("alarm");
                btnSelectCustomAudio.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioAudioCustom) {
                preferenceManager.setSoundType("custom");
                btnSelectCustomAudio.setVisibility(View.VISIBLE);
            } else {
                preferenceManager.setSoundType("default");
                btnSelectCustomAudio.setVisibility(View.GONE);
            }
        });

        btnSelectCustomAudio.setOnClickListener(v -> audioPickerLauncher.launch("audio/*"));

        btnPreviewSound.setOnClickListener(v -> previewSelectedSound());

        btnTestNotification.setOnClickListener(v -> {
            NotificationHelper.showNotification(
                    this,
                    (int) System.currentTimeMillis(),
                    -1,
                    "Test Reminder",
                    "This is a test notification with your configured reminder sound.",
                    "Settings Test"
            );
            Toast.makeText(this, "Test notification triggered", Toast.LENGTH_SHORT).show();
        });
    }

    private void previewSelectedSound() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            Uri soundUri = preferenceManager.getActualSoundUri(this);
            if (soundUri != null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, soundUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
                Toast.makeText(this, "Playing sound preview...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not play audio preview", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAddEditNote(Note note) {
        Intent intent = new Intent(this, AddEditNoteActivity.class);
        if (note != null) {
            intent.putExtra("note", note);
        } else {
            // Pre-select category if viewing a specific category
            if (currentSection == Section.CAT_PERSONAL) intent.putExtra("pre_category", "Personal");
            else if (currentSection == Section.CAT_STUDY) intent.putExtra("pre_category", "Study");
            else if (currentSection == Section.CAT_WORK) intent.putExtra("pre_category", "Work");
            else if (currentSection == Section.CAT_SHOPPING) intent.putExtra("pre_category", "Shopping");
        }
        startActivity(intent);
    }

    @Override
    public void onNoteClick(Note note) {
        openAddEditNote(note);
    }

    @Override
    public void onNoteCompleteToggle(Note note, boolean isCompleted) {
        note.setCompleted(isCompleted);
        noteViewModel.update(note);
        if (isCompleted) {
            AlarmScheduler.cancelReminder(this, note);
            Toast.makeText(this, "Marked as completed", Toast.LENGTH_SHORT).show();
        } else {
            if (note.isReminderEnabled() && note.getReminderTime() > System.currentTimeMillis()) {
                AlarmScheduler.scheduleReminder(this, note);
            }
            Toast.makeText(this, "Marked as active", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNoteFlagToggle(Note note, boolean isFlagged) {
        note.setPinned(isFlagged);
        noteViewModel.update(note);
        Toast.makeText(this, isFlagged ? "Note flagged as important" : "Note unflagged", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNoteDelete(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete '" + note.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AlarmScheduler.cancelReminder(this, note);
                    noteViewModel.delete(note);
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
