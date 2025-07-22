package com.example.stitarlacguidanceapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stitarlacguidanceapp.Models.JournalEntry;
import com.example.stitarlacguidanceapp.R;

import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {

    private List<JournalEntry> journalList;

    public JournalAdapter(List<JournalEntry> journalList) {
        this.journalList = journalList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtTitle, txtContent, txtMood;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtMood = itemView.findViewById(R.id.txtMood);
        }
    }

    @NonNull
    @Override
    public JournalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalAdapter.ViewHolder holder, int position) {
        JournalEntry entry = journalList.get(position);
        holder.txtDate.setText(entry.getDate());
        holder.txtTitle.setText(entry.getTitle());
        holder.txtContent.setText(entry.getContent());
        holder.txtMood.setText(entry.getMood());
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }
}

