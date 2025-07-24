package com.example.stitarlacguidanceapp.PrivateJournalFragments;

import static android.app.ProgressDialog.show;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stitarlacguidanceapp.Adapters.JournalAdapter;
import com.example.stitarlacguidanceapp.Models.JournalEntry;
import com.example.stitarlacguidanceapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalFragment extends Fragment {

    private RecyclerView recyclerView;
    private JournalAdapter adapter;
    private List<JournalEntry> journalEntries = new ArrayList<>();
    private SharedPreferences prefs;

    public JournalFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);
        recyclerView = view.findViewById(R.id.recyclerJournal);
        TextView txtEmptyMessage = view.findViewById(R.id.txtEmptyMessage); // 👈

        prefs = requireContext().getSharedPreferences("journal_prefs", Context.MODE_PRIVATE);

        loadJournalEntries();

        adapter = new JournalAdapter(
                journalEntries,
                getContext(),
                this::saveToPreferences,
                () -> checkEmptyList(txtEmptyMessage), //this passes the callback
                new JournalAdapter.OnJournalActionListener() {
                    @Override
                    public void onEdit(JournalEntry entry, int position) {
                        showEditDialog(entry, position);
                    }

                    @Override
                    public void onDelete(JournalEntry entry, int position) {
                        if (journalEntries.isEmpty()) {
                            txtEmptyMessage.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        checkEmptyList(txtEmptyMessage); // 👈 initial check

        return view;
    }

    public void showAddDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_entry, null);
        EditText edtTitle = dialogView.findViewById(R.id.edtTitle);
        EditText edtContent = dialogView.findViewById(R.id.edtContent);
        EditText edtMood = dialogView.findViewById(R.id.edtMood);
        EditText edtDate = dialogView.findViewById(R.id.edtDate);

        //Set default to today
        String today = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        edtDate.setText(today);

        //Show date picker on click
        edtDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                String dateStr = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(selected.getTime());
                edtDate.setText(dateStr);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis()); // Prevent future dates
            datePicker.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Save", (dialogInterface, which) -> {
                    String title = edtTitle.getText().toString().trim();
                    String content = edtContent.getText().toString().trim();
                    String mood = edtMood.getText().toString().trim();
                    String selectedDate = edtDate.getText().toString().trim(); // 👈 NEW

                    if (content.isEmpty()) {
                        Toast.makeText(getContext(), "Entry cannot be empty!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JournalEntry entry = new JournalEntry(
                            title.isEmpty() ? "Untitled Entry" : title,
                            content, selectedDate, mood
                    );

                    journalEntries.add(0, entry); // Add on top
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);

                    saveToPreferences(); // Save the updated list

                    //Check if we need to hide the "No entries" message
                    checkEmptyList(requireView().findViewById(R.id.txtEmptyMessage));
                })
                .setNegativeButton("Cancel", null)
                .create();

                dialog.setOnShowListener(d -> {
                    Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                    Typeface poppins = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular);
                    if (poppins != null) {
                        saveBtn.setTypeface(poppins);
                        cancelBtn.setTypeface(poppins);
                    }

                    saveBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));
                    cancelBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                    saveBtn.setTextSize(16);
                    cancelBtn.setTextSize(16);
                });

                dialog.show();
    }

    private void showEditDialog(JournalEntry entry, int position) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_entry, null);

        TextView dialogTitle = dialogView.findViewById(R.id.txtDialogTitle);
        dialogTitle.setText("Edit Journal Entry");

        EditText edtTitle = dialogView.findViewById(R.id.edtTitle);
        EditText edtContent = dialogView.findViewById(R.id.edtContent);
        EditText edtMood = dialogView.findViewById(R.id.edtMood);
        EditText edtDate = dialogView.findViewById(R.id.edtDate);

        // Pre-fill fields
        edtTitle.setText(entry.getTitle());
        edtContent.setText(entry.getContent());
        edtMood.setText(entry.getMood());
        edtDate.setText(entry.getDate());

        // Show DatePicker
        edtDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                String dateStr = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(selected.getTime());
                edtDate.setText(dateStr);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePicker.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.setOnShowListener(d -> {
            Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            Typeface poppins = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular);
            if (poppins != null) {
                saveBtn.setTypeface(poppins);
                cancelBtn.setTypeface(poppins);
            }

            saveBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));
            cancelBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            saveBtn.setTextSize(16);
            cancelBtn.setTextSize(16);
        });

            // Now set buttons AFTER dialog creation
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", (dialogInterface, which) -> {
            entry.setTitle(edtTitle.getText().toString().trim());
            entry.setContent(edtContent.getText().toString().trim());
            entry.setMood(edtMood.getText().toString().trim());
            entry.setDate(edtDate.getText().toString().trim());

            adapter.notifyItemChanged(position);
            saveToPreferences();
            Toast.makeText(getContext(), "Journal entry updated", Toast.LENGTH_SHORT).show();
            });

            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, which) -> {
            dialog.dismiss();
            });

        dialog.show();
    }


    private void saveToPreferences() {
        Gson gson = new Gson();
        String json = gson.toJson(journalEntries);
        prefs.edit().putString("entries", json).apply();
    }

    private void checkEmptyList(TextView txtEmptyMessage) {
        if (journalEntries.isEmpty()) {
            txtEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


    private void sortJournalEntriesByDateDescending() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

        Collections.sort(journalEntries, (entry1, entry2) -> {
            try {
                Date date1 = sdf.parse(entry1.getDate());
                Date date2 = sdf.parse(entry2.getDate());
                return date2.compareTo(date1); // DESCENDING
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }



    private void loadJournalEntries() {
        String json = prefs.getString("entries", "");
        if (!json.isEmpty()) {
            Type type = new TypeToken<List<JournalEntry>>(){}.getType();
            journalEntries = new Gson().fromJson(json, type);
            sortJournalEntriesByDateDescending();
        }
    }
}



