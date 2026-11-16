package com.example.personalnotesreminder.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.personalnotesreminder.R;
import com.example.personalnotesreminder.model.Note;
import com.example.personalnotesreminder.notification.AlarmScheduler;
import com.example.personalnotesreminder.utils.FileUtils;
import com.example.personalnotesreminder.utils.PreferenceManager;
import com.example.personalnotesreminder.viewmodel.NoteViewModel;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Calendar;

public class AddEditNoteActivity extends AppCompatActivity {

    private TextView txtHeaderTitle, txtReminderDateTime, txtAttachedFileName;
    private EditText edtTitle, edtDescription, edtSubject;
    private MaterialAutoCompleteTextView spinnerNoteCategory, spinnerStudyType, spinnerShoppingCategory, spinnerPriority, spinnerAdvanceReminder;
    private CheckBox checkPin, checkReminder;
    private Button btnSelectDate, btnSelectTime, btnSaveNote, btnAttachDocument;
    private LinearLayout layoutStudyFields, layoutShoppingFields, layoutPriorityField, layoutAttachmentInfo;
    private ImageView btnRenameAttachment, btnRemoveAttachment;

    private NoteViewModel noteViewModel;
    private Note existingNote;
    private PreferenceManager preferenceManager;

    private final Calendar reminderCalendar = Calendar.getInstance();
    private boolean dateSelected = false;
    private boolean timeSelected = false;

    private String attachedFilePath = null;
    private String attachedFileName = null;

    private final String[] mainCategories = {"Personal", "Study", "Work", "Shopping", "Other"};
    private final String[] studyTypes = {"General Note", "Examination", "Assignment"};
    private final String[] shoppingCategories = {
            "Dress & Clothing", "Ornaments & Jewellery", "Books & Stationery",
            "Sports & Fitness", "Home & Furniture", "Electronics & Gadgets",
            "Groceries", "Personal Care", "Others"
    };
    private final String[] priorityLevels = {"Medium", "High", "Low"};
    private final String[] advanceReminderLabels = {
            "At time of event", "15 minutes before", "30 minutes before", "1 hour before", "1 day before"
    };
    private final int[] advanceReminderValues = {0, 15, 30, 60, 1440};

    private final ActivityResultLauncher<String> documentPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    String copiedPath = FileUtils.copyFileToInternalStorage(this, uri);
                    if (copiedPath != null) {
                        attachedFilePath = copiedPath;
                        attachedFileName = FileUtils.getFileName(this, uri);
                        updateAttachmentUI();
                        Toast.makeText(this, "Document attached successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to attach file", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferenceManager = new PreferenceManager(this);
        preferenceManager.applySavedTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        initializeViews();

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        setupDropdowns();

        existingNote = (Note) getIntent().getSerializableExtra("note");
        if (existingNote != null) {
            txtHeaderTitle.setText("Edit Note");
            loadExistingNoteData();
        } else {
            txtHeaderTitle.setText("Create New Note");
            String preCategory = getIntent().getStringExtra("pre_category");
            if (preCategory != null) {
                setDropdownValue(spinnerNoteCategory, preCategory);
                toggleCategoryFields(preCategory);
            } else {
                setDropdownValue(spinnerNoteCategory, mainCategories[0]);
                toggleCategoryFields(mainCategories[0]);
            }
        }

        btnSelectDate.setOnClickListener(v -> selectDate());
        btnSelectTime.setOnClickListener(v -> selectTime());
        btnAttachDocument.setOnClickListener(v -> documentPickerLauncher.launch("*/*"));
        btnRemoveAttachment.setOnClickListener(v -> removeAttachment());
        btnRenameAttachment.setOnClickListener(v -> renameAttachment());
        btnSaveNote.setOnClickListener(v -> saveNote());
    }

    private void initializeViews() {
        txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtReminderDateTime = findViewById(R.id.txtReminderDateTime);
        txtAttachedFileName = findViewById(R.id.txtAttachedFileName);

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtSubject = findViewById(R.id.edtSubject);

        spinnerNoteCategory = findViewById(R.id.spinnerNoteCategory);
        spinnerStudyType = findViewById(R.id.spinnerStudyType);
        spinnerShoppingCategory = findViewById(R.id.spinnerShoppingCategory);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerAdvanceReminder = findViewById(R.id.spinnerAdvanceReminder);

        checkPin = findViewById(R.id.checkPin);
        checkReminder = findViewById(R.id.checkReminder);

        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        btnAttachDocument = findViewById(R.id.btnAttachDocument);
        btnRenameAttachment = findViewById(R.id.btnRenameAttachment);
        btnRemoveAttachment = findViewById(R.id.btnRemoveAttachment);

        layoutStudyFields = findViewById(R.id.layoutStudyFields);
        layoutShoppingFields = findViewById(R.id.layoutShoppingFields);
        layoutPriorityField = findViewById(R.id.layoutPriorityField);
        layoutAttachmentInfo = findViewById(R.id.layoutAttachmentInfo);
    }

    private void setupDropdowns() {
        setupDropdownAdapter(spinnerNoteCategory, mainCategories);
        setupDropdownAdapter(spinnerStudyType, studyTypes);
        setupDropdownAdapter(spinnerShoppingCategory, shoppingCategories);
        setupDropdownAdapter(spinnerPriority, priorityLevels);
        setupDropdownAdapter(spinnerAdvanceReminder, advanceReminderLabels);

        // Set defaults
        setDropdownValue(spinnerStudyType, studyTypes[0]);
        setDropdownValue(spinnerShoppingCategory, shoppingCategories[0]);
        setDropdownValue(spinnerPriority, priorityLevels[0]);
        setDropdownValue(spinnerAdvanceReminder, advanceReminderLabels[0]);

        spinnerNoteCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selected = mainCategories[position];
            toggleCategoryFields(selected);
        });
    }

    private void setupDropdownAdapter(MaterialAutoCompleteTextView dropdown, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        dropdown.setAdapter(adapter);
    }

    private void setDropdownValue(MaterialAutoCompleteTextView dropdown, String value) {
        if (value != null && !value.isEmpty()) {
            dropdown.setText(value, false);
        }
    }

    private void toggleCategoryFields(String category) {
        if (category == null) category = "Personal";
        switch (category) {
            case "Study":
                layoutStudyFields.setVisibility(View.VISIBLE);
                layoutShoppingFields.setVisibility(View.GONE);
                layoutPriorityField.setVisibility(View.VISIBLE);
                break;
            case "Shopping":
                layoutStudyFields.setVisibility(View.GONE);
                layoutShoppingFields.setVisibility(View.VISIBLE);
                layoutPriorityField.setVisibility(View.GONE);
                break;
            case "Work":
                layoutStudyFields.setVisibility(View.GONE);
                layoutShoppingFields.setVisibility(View.GONE);
                layoutPriorityField.setVisibility(View.VISIBLE);
                break;
            case "Personal":
            case "Other":
            default:
                layoutStudyFields.setVisibility(View.GONE);
                layoutShoppingFields.setVisibility(View.GONE);
                layoutPriorityField.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void loadExistingNoteData() {
        edtTitle.setText(existingNote.getTitle());
        edtDescription.setText(existingNote.getDescription());
        checkPin.setChecked(existingNote.isPinned());
        checkReminder.setChecked(existingNote.isReminderEnabled());

        setDropdownValue(spinnerNoteCategory, existingNote.getCategory());
        toggleCategoryFields(existingNote.getCategory());

        setDropdownValue(spinnerStudyType, existingNote.getStudyType());
        setDropdownValue(spinnerShoppingCategory, existingNote.getSubCategory());
        setDropdownValue(spinnerPriority, existingNote.getPriority());

        if (existingNote.getSubject() != null) {
            edtSubject.setText(existingNote.getSubject());
        }

        // Advance reminder label
        int advMin = existingNote.getAdvanceReminderMinutes();
        for (int i = 0; i < advanceReminderValues.length; i++) {
            if (advanceReminderValues[i] == advMin) {
                setDropdownValue(spinnerAdvanceReminder, advanceReminderLabels[i]);
                break;
            }
        }

        // Attachment
        if (existingNote.getAttachmentPath() != null && !existingNote.getAttachmentPath().isEmpty()) {
            attachedFilePath = existingNote.getAttachmentPath();
            attachedFileName = existingNote.getAttachmentName();
            updateAttachmentUI();
        }

        // Reminder Time
        if (existingNote.isReminderEnabled() && existingNote.getReminderTime() > 0) {
            reminderCalendar.setTimeInMillis(existingNote.getReminderTime());
            dateSelected = true;
            timeSelected = true;
            updateReminderText();
        }
    }

    private void updateAttachmentUI() {
        if (attachedFilePath != null && !attachedFilePath.isEmpty()) {
            layoutAttachmentInfo.setVisibility(View.VISIBLE);
            txtAttachedFileName.setText(attachedFileName != null ? attachedFileName : "Document");
            btnAttachDocument.setText("Change Document");
        } else {
            layoutAttachmentInfo.setVisibility(View.GONE);
            btnAttachDocument.setText("Choose Document");
        }
    }

    private void removeAttachment() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Attachment")
                .setMessage("Are you sure you want to remove this attached file?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    FileUtils.deleteFile(attachedFilePath);
                    attachedFilePath = null;
                    attachedFileName = null;
                    updateAttachmentUI();
                    Toast.makeText(this, "Attachment removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void renameAttachment() {
        if (attachedFileName == null) return;
        final EditText input = new EditText(this);
        input.setText(attachedFileName);
        new AlertDialog.Builder(this)
                .setTitle("Rename Attachment")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        attachedFileName = newName;
                        updateAttachmentUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void selectDate() {
        int year = reminderCalendar.get(Calendar.YEAR);
        int month = reminderCalendar.get(Calendar.MONTH);
        int day = reminderCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    reminderCalendar.set(selectedYear, selectedMonth, selectedDay);
                    dateSelected = true;
                    updateReminderText();
                },
                year, month, day
        );
        dialog.show();
    }

    private void selectTime() {
        int hour = reminderCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = reminderCalendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    reminderCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    reminderCalendar.set(Calendar.MINUTE, selectedMinute);
                    reminderCalendar.set(Calendar.SECOND, 0);
                    reminderCalendar.set(Calendar.MILLISECOND, 0);
                    timeSelected = true;
                    updateReminderText();
                },
                hour, minute, preferenceManager.is24HourFormat()
        );
        dialog.show();
    }

    private void updateReminderText() {
        txtReminderDateTime.setText(preferenceManager.formatDateTime(reminderCalendar.getTimeInMillis()));
    }

    private void saveNote() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String category = spinnerNoteCategory.getText().toString().trim();
        if (category.isEmpty()) category = "Personal";

        boolean pinned = checkPin.isChecked();
        boolean reminderEnabled = checkReminder.isChecked();

        if (title.isEmpty()) {
            edtTitle.setError("Please enter a note title");
            edtTitle.requestFocus();
            return;
        }

        if (reminderEnabled && (!dateSelected || !timeSelected)) {
            Toast.makeText(this, "Please select reminder date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        long reminderTime = reminderEnabled ? reminderCalendar.getTimeInMillis() : 0;
        if (reminderEnabled && reminderTime <= System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future reminder time", Toast.LENGTH_SHORT).show();
            return;
        }

        String priority = spinnerPriority.getText().toString().trim();
        if (priority.isEmpty()) priority = "Medium";

        String subCategory = null;
        String studyType = "General Note";
        String subject = null;

        if ("Study".equals(category)) {
            studyType = spinnerStudyType.getText().toString().trim();
            if (studyType.isEmpty()) studyType = "General Note";
            subject = edtSubject.getText().toString().trim();
        } else if ("Shopping".equals(category)) {
            subCategory = spinnerShoppingCategory.getText().toString().trim();
        }

        String advLabel = spinnerAdvanceReminder.getText().toString().trim();
        int advanceMinutes = 0;
        for (int i = 0; i < advanceReminderLabels.length; i++) {
            if (advanceReminderLabels[i].equalsIgnoreCase(advLabel)) {
                advanceMinutes = advanceReminderValues[i];
                break;
            }
        }

        if (existingNote == null) {
            long createdDate = System.currentTimeMillis();
            Note note = new Note(
                    title, description, category, pinned, createdDate, reminderEnabled, reminderTime,
                    false, priority, subCategory, studyType, subject, advanceMinutes, attachedFilePath, attachedFileName
            );

            noteViewModel.insert(note, savedNote -> {
                if (savedNote.isReminderEnabled()) {
                    AlarmScheduler.scheduleReminder(this, savedNote);
                }
            });
            Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            AlarmScheduler.cancelReminder(this, existingNote);

            existingNote.setTitle(title);
            existingNote.setDescription(description);
            existingNote.setCategory(category);
            existingNote.setPinned(pinned);
            existingNote.setReminderEnabled(reminderEnabled);
            existingNote.setReminderTime(reminderTime);
            existingNote.setPriority(priority);
            existingNote.setSubCategory(subCategory);
            existingNote.setStudyType(studyType);
            existingNote.setSubject(subject);
            existingNote.setAdvanceReminderMinutes(advanceMinutes);
            existingNote.setAttachmentPath(attachedFilePath);
            existingNote.setAttachmentName(attachedFileName);

            noteViewModel.update(existingNote);

            if (existingNote.isReminderEnabled() && !existingNote.isCompleted()) {
                AlarmScheduler.scheduleReminder(this, existingNote);
            }

            Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show();
        }

        setResult(Activity.RESULT_OK);
        finish();
    }
}
