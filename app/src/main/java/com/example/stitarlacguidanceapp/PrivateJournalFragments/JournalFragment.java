package com.example.stitarlacguidanceapp.PrivateJournalFragments;

import static android.app.ProgressDialog.show;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
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
import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.JournalEntryApi;
import com.example.stitarlacguidanceapp.Models.JournalEntry;
import com.example.stitarlacguidanceapp.R;
import com.google.android.material.snackbar.Snackbar;
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
                .setPositiveButton("Save", null) //We override this later
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

            saveBtn.setOnClickListener(v -> {
                String title = edtTitle.getText().toString().trim();
                String content = edtContent.getText().toString().trim();
                String mood = edtMood.getText().toString().trim();
                String selectedDate = edtDate.getText().toString().trim();

                // Validate content
                if (content.isEmpty()) {
                    edtContent.setError("Entry cannot be empty!");
                    return;
                }

                // Validate date
                if (selectedDate.isEmpty()) {
                    edtDate.setError("Date is required");
                    return;
                }

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    Date date = sdf.parse(selectedDate);
                    if (date.after(new Date())) {
                        edtDate.setError("Date cannot be in the future");
                        return;
                    }
                } catch (ParseException e) {
                    edtDate.setError("Invalid date format");
                    return;
                }

                //Getting studentID
                SharedPreferences sessionPrefs = requireContext().getSharedPreferences("student_session", Context.MODE_PRIVATE);
                int studentId = sessionPrefs.getInt("studentId", -1);

                if (studentId == -1) {
                    Toast.makeText(getContext(), "Student session error. Please log in again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mood.isEmpty()) {
                    edtMood.setError("Mood is required");
                    return;
                }

                //Save the journal entry
                JournalEntry entry = new JournalEntry(
                        studentId,
                        selectedDate,
                        title.isEmpty() ? "Untitled Entry" : title,
                        content,
                        mood
                );

                Log.d("JournalSubmit", "Preparing to submit journal entry:");
                Log.d("JournalSubmit", "Student ID: " + studentId);
                Log.d("JournalSubmit", "Date: " + selectedDate);
                Log.d("JournalSubmit", "Title: " + title);
                Log.d("JournalSubmit", "Content: " + content);
                Log.d("JournalSubmit", "Mood: " + mood);


                journalEntries.add(0, entry);
                adapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);

                saveToPreferences(); //Save the updated list

                //Submit to server using Retrofit
                JournalEntryApi api = ApiClient.getClient().create(JournalEntryApi.class);
                api.submitJournalEntry(entry).enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            Snackbar.make(requireView(), "Journal entry saved.", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue))
                                    .setTextColor(Color.WHITE)
                                    .show();
                        } else {
                            Snackbar.make(requireView(), "Error occurred.", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.darkred))
                                    .setTextColor(Color.WHITE)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to connect to server.", Toast.LENGTH_SHORT).show();
                    }
                });

                checkEmptyList(requireView().findViewById(R.id.txtEmptyMessage));

                dialog.dismiss(); //Close dialog manually only when valid
            });
        });

        dialog.show();
    }


    public void showEditDialog(JournalEntry entry, int position) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_entry, null);

        //Set the title to "Edit Journal Entry"
        TextView dialogTitle = dialogView.findViewById(R.id.txtDialogTitle);
        dialogTitle.setText("Edit Journal Entry");

        EditText edtTitle = dialogView.findViewById(R.id.edtTitle);
        EditText edtContent = dialogView.findViewById(R.id.edtContent);
        EditText edtMood = dialogView.findViewById(R.id.edtMood);
        EditText edtDate = dialogView.findViewById(R.id.edtDate);

        // Populate fields with existing data
        edtTitle.setText(entry.getTitle());
        edtContent.setText(entry.getContent());
        edtMood.setText(entry.getMood());
        edtDate.setText(entry.getDate());

        // Date picker
        edtDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                Date parsed = sdf.parse(entry.getDate());
                if (parsed != null) calendar.setTime(parsed);
            } catch (ParseException e) {
                e.printStackTrace();
            }

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
                .setPositiveButton("Update", null) //Override later
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button updateBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            Typeface poppins = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular);
            if (poppins != null) {
                updateBtn.setTypeface(poppins);
                cancelBtn.setTypeface(poppins);
            }

            updateBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));
            cancelBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            updateBtn.setTextSize(16);
            cancelBtn.setTextSize(16);

            updateBtn.setOnClickListener(v -> {
                String newTitle = edtTitle.getText().toString().trim();
                String newContent = edtContent.getText().toString().trim();
                String newMood = edtMood.getText().toString().trim();
                String newDate = edtDate.getText().toString().trim();

                if (newContent.isEmpty()) {
                    edtContent.setError("Entry cannot be empty!");
                    return;
                }

                if (newDate.isEmpty()) {
                    edtDate.setError("Date is required");
                    return;
                }

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    Date parsedDate = sdf.parse(newDate);
                    if (parsedDate.after(new Date())) {
                        edtDate.setError("Date cannot be in the future");
                        return;
                    }
                } catch (ParseException e) {
                    edtDate.setError("Invalid date format");
                    return;
                }

                // Update the entry
                entry.setTitle(newTitle.isEmpty() ? "Untitled Entry" : newTitle);
                entry.setContent(newContent);
                entry.setMood(newMood);
                entry.setDate(newDate);

                adapter.notifyItemChanged(position);
                saveToPreferences(); //Persist updates
                checkEmptyList(requireView().findViewById(R.id.txtEmptyMessage));
                Snackbar.make(requireView(), "Journal updated.", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue))
                        .setTextColor(Color.WHITE)
                        .show();
                dialog.dismiss();
            });
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



