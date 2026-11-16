package com.example.personalnotesreminder.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notes")
public class Note implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private String category; // Personal, Study, Work, Shopping, Other

    private boolean pinned; // Flagged / Important

    private long createdDate;

    private boolean reminderEnabled;
    private long reminderTime;

    private boolean completed; // Completion status

    private String priority; // High, Medium, Low
    private String subCategory; // For Shopping: Groceries, Dress & Clothing, etc.
    private String studyType; // For Study: General Note, Examination, Assignment
    private String subject; // For Study: Subject name
    private int advanceReminderMinutes; // 0, 15, 30, 60, 1440
    private String attachmentPath; // Path/URI for study document
    private String attachmentName; // Display name for document

    // Default constructor required by Room
    public Note() {
        this.priority = "Medium";
        this.completed = false;
        this.studyType = "General Note";
    }

    // Comprehensive constructor
    @Ignore
    public Note(String title, String description, String category, boolean pinned,
                long createdDate, boolean reminderEnabled, long reminderTime,
                boolean completed, String priority, String subCategory,
                String studyType, String subject, int advanceReminderMinutes,
                String attachmentPath, String attachmentName) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.pinned = pinned;
        this.createdDate = createdDate;
        this.reminderEnabled = reminderEnabled;
        this.reminderTime = reminderTime;
        this.completed = completed;
        this.priority = priority != null ? priority : "Medium";
        this.subCategory = subCategory;
        this.studyType = studyType != null ? studyType : "General Note";
        this.subject = subject;
        this.advanceReminderMinutes = advanceReminderMinutes;
        this.attachmentPath = attachmentPath;
        this.attachmentName = attachmentName;
    }

    // Basic constructor compatibility
    @Ignore
    public Note(String title, String description,
                String category, boolean pinned,
                long createdDate, boolean reminderEnabled,
                long reminderTime) {
        this(title, description, category, pinned, createdDate, reminderEnabled, reminderTime,
                false, "Medium", null, "General Note", null, 0, null, null);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getPriority() {
        return priority != null ? priority : "Medium";
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getStudyType() {
        return studyType != null ? studyType : "General Note";
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getAdvanceReminderMinutes() {
        return advanceReminderMinutes;
    }

    public void setAdvanceReminderMinutes(int advanceReminderMinutes) {
        this.advanceReminderMinutes = advanceReminderMinutes;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }
}
