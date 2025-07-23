package com.example.stitarlacguidanceapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stitarlacguidanceapp.Models.JournalEntry;
import com.example.stitarlacguidanceapp.R;

import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {

    private List<JournalEntry> journalList;
    private OnJournalActionListener listener;
    private Context context;
    private Runnable saveToPreferences;

    //Used to notify the fragment that the list may be empty
    private Runnable checkEmptyCallback;


    public JournalAdapter(List<JournalEntry> journalList, Context context, Runnable saveToPreferences, Runnable checkEmptyCallback, OnJournalActionListener listener) {
        this.journalList = journalList;
        this.context = context;
        this.listener = listener;
        this.saveToPreferences = saveToPreferences;
        this.checkEmptyCallback = checkEmptyCallback;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtTitle, txtContent, txtMood;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtMood = itemView.findViewById(R.id.txtMood);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // ✅ Updated interface to include position
    public interface OnJournalActionListener {
        void onEdit(JournalEntry entry, int position);
        void onDelete(JournalEntry entry, int position);
    }

    @NonNull
    @Override
    public JournalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JournalEntry entry = journalList.get(position);
        holder.txtDate.setText(entry.getDate());
        holder.txtTitle.setText(entry.getTitle());
        holder.txtContent.setText(entry.getContent());
        holder.txtMood.setText(entry.getMood());

        holder.btnEdit.setOnClickListener(v -> {
            listener.onEdit(entry, holder.getAdapterPosition());
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this journal entry?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        journalList.remove(position);
                        notifyItemRemoved(position);
                        saveToPreferences.run();

                        if (journalList.isEmpty() && checkEmptyCallback != null) {
                            checkEmptyCallback.run(); // This will show "No entries" if the list is now empty
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }
}
