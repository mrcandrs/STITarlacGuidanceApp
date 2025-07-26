package com.example.stitarlacguidanceapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.JournalEntryApi;
import com.example.stitarlacguidanceapp.Models.JournalEntry;
import com.example.stitarlacguidanceapp.R;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            JournalEntry toDelete = journalList.get(adapterPosition);

                            // Make API call to delete
                            JournalEntryApi api = ApiClient.getClient().create(JournalEntryApi.class);
                            Call<ResponseBody> call = api.deleteJournalEntry(toDelete.getJournalId());

                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        journalList.remove(adapterPosition);
                                        notifyItemRemoved(adapterPosition);
                                        saveToPreferences.run();
                                        if (journalList.isEmpty() && listener != null) {
                                            listener.onDelete(null, adapterPosition);
                                        }
                                        Toast.makeText(context, "Entry deleted successfully.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Failed to delete entry.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(context, "Network error.", Toast.LENGTH_SHORT).show();
                                }
                            });
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
