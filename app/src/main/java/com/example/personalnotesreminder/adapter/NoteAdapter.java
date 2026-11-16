package com.example.personalnotesreminder.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalnotesreminder.R;
import com.example.personalnotesreminder.model.Note;
import com.example.personalnotesreminder.utils.FileUtils;
import com.example.personalnotesreminder.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList = new ArrayList<>();
    private final OnNoteActionListener listener;

    public interface OnNoteActionListener {
        void onNoteClick(Note note);
        void onNoteCompleteToggle(Note note, boolean isCompleted);
        void onNoteFlagToggle(Note note, boolean isFlagged);
        void onNoteDelete(Note note);
    }

    public NoteAdapter(OnNoteActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        Context context = holder.itemView.getContext();
        PreferenceManager prefManager = new PreferenceManager(context);

        // Title & Description
        holder.txtTitle.setText(note.getTitle());
        holder.txtDescription.setText(note.getDescription());

        // Strikethrough text if completed
        if (note.isCompleted()) {
            holder.txtTitle.setPaintFlags(holder.txtTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtTitle.setAlpha(0.6f);
            holder.txtDescription.setAlpha(0.6f);
        } else {
            holder.txtTitle.setPaintFlags(holder.txtTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.txtTitle.setAlpha(1.0f);
            holder.txtDescription.setAlpha(1.0f);
        }

        // Checkbox completion
        holder.checkComplete.setOnCheckedChangeListener(null);
        holder.checkComplete.setChecked(note.isCompleted());
        holder.checkComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onNoteCompleteToggle(note, isChecked);
            }
        });

        // Category
        String category = note.getCategory();
        holder.txtCategory.setText(category != null ? category : "Personal");
        setCategoryColor(holder.txtCategory, category, context);

        // Subcategory (Shopping or Study)
        if ("Shopping".equals(category) && note.getSubCategory() != null && !note.getSubCategory().isEmpty()) {
            holder.txtSubCategory.setVisibility(View.VISIBLE);
            holder.txtSubCategory.setText(note.getSubCategory());
        } else if ("Study".equals(category) && note.getStudyType() != null && !note.getStudyType().equals("General Note")) {
            holder.txtSubCategory.setVisibility(View.VISIBLE);
            holder.txtSubCategory.setText(note.getStudyType());
        } else {
            holder.txtSubCategory.setVisibility(View.GONE);
        }

        // Subject (for Study)
        if ("Study".equals(category) && note.getSubject() != null && !note.getSubject().isEmpty()) {
            holder.txtSubject.setVisibility(View.VISIBLE);
            holder.txtSubject.setText("Subject: " + note.getSubject());
        } else {
            holder.txtSubject.setVisibility(View.GONE);
        }

        // Priority Badge
        String priority = note.getPriority();
        if ("Personal".equals(category) || "Work".equals(category) || "High".equals(priority)) {
            holder.txtPriority.setVisibility(View.VISIBLE);
            holder.txtPriority.setText(priority.toUpperCase());
            if ("High".equals(priority)) {
                holder.txtPriority.setTextColor(ContextCompat.getColor(context, R.color.priorityHigh));
            } else if ("Medium".equals(priority)) {
                holder.txtPriority.setTextColor(ContextCompat.getColor(context, R.color.priorityMedium));
            } else {
                holder.txtPriority.setTextColor(ContextCompat.getColor(context, R.color.priorityLow));
            }
        } else {
            holder.txtPriority.setVisibility(View.GONE);
        }

        // Status Badge (Overdue / Approaching / Completed)
        if (note.isCompleted()) {
            holder.txtStatusBadge.setVisibility(View.VISIBLE);
            holder.txtStatusBadge.setText("COMPLETED");
            holder.txtStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.statusCompleted));
        } else if (note.isReminderEnabled() && note.getReminderTime() > 0) {
            long now = System.currentTimeMillis();
            if (note.getReminderTime() < now) {
                holder.txtStatusBadge.setVisibility(View.VISIBLE);
                holder.txtStatusBadge.setText("OVERDUE");
                holder.txtStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.statusOverdue));
            } else if (note.getReminderTime() - now <= (24 * 60 * 60 * 1000L)) {
                holder.txtStatusBadge.setVisibility(View.VISIBLE);
                holder.txtStatusBadge.setText("APPROACHING");
                holder.txtStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.statusApproaching));
            } else {
                holder.txtStatusBadge.setVisibility(View.GONE);
            }
        } else {
            holder.txtStatusBadge.setVisibility(View.GONE);
        }

        // Flag / Pin Button
        if (note.isPinned()) {
            holder.btnFlag.setImageResource(R.drawable.ic_flagged);
            holder.btnFlag.setColorFilter(ContextCompat.getColor(context, R.color.pinColor));
        } else {
            holder.btnFlag.setImageResource(R.drawable.ic_flagged);
            holder.btnFlag.setColorFilter(ContextCompat.getColor(context, R.color.textSecondary));
        }

        holder.btnFlag.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteFlagToggle(note, !note.isPinned());
            }
        });

        // Attachment Pill
        if (note.getAttachmentPath() != null && !note.getAttachmentPath().isEmpty()) {
            holder.layoutAttachment.setVisibility(View.VISIBLE);
            String name = note.getAttachmentName() != null ? note.getAttachmentName() : "Document";
            holder.txtAttachmentName.setText(name);
            holder.layoutAttachment.setOnClickListener(v -> {
                FileUtils.openFile(context, note.getAttachmentPath());
            });
        } else {
            holder.layoutAttachment.setVisibility(View.GONE);
        }

        // Reminder Time Info
        if (note.isReminderEnabled() && note.getReminderTime() > 0) {
            holder.imgClock.setVisibility(View.VISIBLE);
            holder.txtReminderInfo.setVisibility(View.VISIBLE);
            holder.txtReminderInfo.setText(prefManager.formatDateTime(note.getReminderTime()));
        } else {
            holder.imgClock.setVisibility(View.GONE);
            holder.txtReminderInfo.setVisibility(View.GONE);
        }

        // Delete Button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteDelete(note);
            }
        });

        // Item View Click -> Edit
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note);
            }
        });
    }

    private void setCategoryColor(TextView textView, String category, Context context) {
        if (category == null) category = "Personal";
        switch (category) {
            case "Study":
                textView.setTextColor(ContextCompat.getColor(context, R.color.catStudy));
                break;
            case "Work":
                textView.setTextColor(ContextCompat.getColor(context, R.color.catWork));
                break;
            case "Shopping":
                textView.setTextColor(ContextCompat.getColor(context, R.color.catShopping));
                break;
            case "Personal":
            default:
                textView.setTextColor(ContextCompat.getColor(context, R.color.catPersonal));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void setNotes(List<Note> notes) {
        noteList = notes != null ? new ArrayList<>(notes) : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtCategory, txtSubCategory, txtSubject, txtPriority, txtStatusBadge, txtAttachmentName, txtReminderInfo;
        CheckBox checkComplete;
        ImageView btnFlag, imgClock, btnDelete;
        LinearLayout layoutAttachment;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtSubCategory = itemView.findViewById(R.id.txtSubCategory);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtPriority = itemView.findViewById(R.id.txtPriority);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadge);
            txtAttachmentName = itemView.findViewById(R.id.txtAttachmentName);
            txtReminderInfo = itemView.findViewById(R.id.txtReminderInfo);
            checkComplete = itemView.findViewById(R.id.checkComplete);
            btnFlag = itemView.findViewById(R.id.btnFlag);
            imgClock = itemView.findViewById(R.id.imgClock);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            layoutAttachment = itemView.findViewById(R.id.layoutAttachment);
        }
    }
}
