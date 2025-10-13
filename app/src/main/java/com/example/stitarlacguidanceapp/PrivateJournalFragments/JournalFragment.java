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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JournalFragment extends Fragment {

    private RecyclerView recyclerView;
    private JournalAdapter adapter;
    private final List<JournalEntry> journalEntries = new ArrayList<>();
    private SharedPreferences prefs;

    public JournalFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);
        recyclerView = view.findViewById(R.id.recyclerJournal);
        TextView txtEmptyMessage = view.findViewById(R.id.txtEmptyMessage);

        prefs = requireContext().getSharedPreferences("journal_prefs", Context.MODE_PRIVATE);

        adapter = new JournalAdapter(
                journalEntries,
                getContext(),
                this::saveToPreferences,
                () -> checkEmptyList(txtEmptyMessage),
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

        loadJournalEntries(); // now the adapter is ready
        precheckTodayEntry(); // pre-check today's entry to enforce one-per-day UX

        checkEmptyList(txtEmptyMessage); // initial empty check

        return view;
    }


    private void precheckTodayEntry() {
        SharedPreferences sessionPrefs = requireContext().getSharedPreferences("student_session", Context.MODE_PRIVATE);
        int studentId = sessionPrefs.getInt("studentId", -1);
        if (studentId == -1) return;

        JournalEntryApi api = ApiClient.getClient().create(JournalEntryApi.class);
        api.getToday(studentId).enqueue(new Callback<JournalEntry>() {
            @Override
            public void onResponse(Call<JournalEntry> call, Response<JournalEntry> response) {
                if (response.isSuccessful()) {
                    JournalEntry today = response.body();
                    if (today != null) {
                        // Normalize date to display format if backend returns yyyy-MM-dd
                        today.setDate(toDisplayDate(today.getDate()));
                        // Ensure today's entry is visible at top
                        boolean exists = false;
                        for (int i = 0; i < journalEntries.size(); i++) {
                            JournalEntry e = journalEntries.get(i);
                            if (e.getJournalId() == today.getJournalId()) {
                                journalEntries.set(i, today);
                                adapter.notifyItemChanged(i);
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            journalEntries.add(0, today);
                            adapter.notifyItemInserted(0);
                        }
                        saveToPreferences();
                    }
                }
            }

            @Override
            public void onFailure(Call<JournalEntry> call, Throwable t) {}
        });
    }

    private void fetchTodayAndOpenEdit(int studentId) {
        JournalEntryApi api = ApiClient.getClient().create(JournalEntryApi.class);
        api.getToday(studentId).enqueue(new Callback<JournalEntry>() {
            @Override
            public void onResponse(Call<JournalEntry> call, Response<JournalEntry> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JournalEntry today = response.body();
                    today.setDate(toDisplayDate(today.getDate()));
                    showEditDialog(today, 0);
                }
            }

            @Override
            public void onFailure(Call<JournalEntry> call, Throwable t) {}
        });
    }

    private boolean isToday(String displayDate) {
        try {
            Date parsed;
            if (isIsoDate(displayDate)) {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                parsed = iso.parse(displayDate);
            } else {
                SimpleDateFormat display = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                parsed = display.parse(displayDate);
            }
            if (parsed == null) return false;
            SimpleDateFormat key = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            key.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
            String entryKey = key.format(parsed);

            // Compute today in PH tz
            Calendar now = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Manila"));
            Date todayPh = new Date(now.getTimeInMillis());
            String todayKey = key.format(todayPh);
            return entryKey.equals(todayKey);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isIsoDate(String value) {
        return value != null && value.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private String toDisplayDate(String value) {
        try {
            if (isIsoDate(value)) {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date d = iso.parse(value);
                if (d != null) {
                    SimpleDateFormat out = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    out.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
                    return out.format(d);
                }
            }
        } catch (Exception ignored) {}
        return value; // already display format
    }

    private boolean sameDay(String d1, String d2) {
        try {
            SimpleDateFormat display = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            Date a = isIsoDate(d1) ? new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(d1) : display.parse(d1);
            Date b = isIsoDate(d2) ? new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(d2) : display.parse(d2);
            if (a == null || b == null) return false;
            SimpleDateFormat key = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return key.format(a).equals(key.format(b));
        } catch (Exception e) {
            return false;
        }
    }

    private String getTodayDisplayPh() {
        Calendar now = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Manila"));
        Date today = new Date(now.getTimeInMillis());
        SimpleDateFormat display = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        display.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
        return display.format(today);
    }

    public void showAddDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_entry, null);
        EditText edtTitle = dialogView.findViewById(R.id.edtTitle);
        EditText edtContent = dialogView.findViewById(R.id.edtContent);
        EditText edtDate = dialogView.findViewById(R.id.edtDate);
        
        // Mood selection views
        TextView btnMoodHappy = dialogView.findViewById(R.id.btnMoodHappy);
        TextView btnMoodSad = dialogView.findViewById(R.id.btnMoodSad);
        TextView btnMoodAngry = dialogView.findViewById(R.id.btnMoodAngry);
        TextView btnMoodExcited = dialogView.findViewById(R.id.btnMoodExcited);
        TextView btnMoodCalm = dialogView.findViewById(R.id.btnMoodCalm);
        TextView txtSelectedMood = dialogView.findViewById(R.id.txtSelectedMood);
        
        // Use arrays to make variables effectively final
        final String[] selectedMood = {""};
        final String[] selectedMoodEmoji = {""};

        // Enforce server rule: only today's entry; set PH-today and disable edits
        String today = getTodayDisplayPh();
        edtDate.setText(today);
        edtDate.setEnabled(false);
        edtDate.setFocusable(false);

        // Mood selection click listeners
        View.OnClickListener moodClickListener = v -> {
            // Reset all mood buttons
            btnMoodHappy.setSelected(false);
            btnMoodSad.setSelected(false);
            btnMoodAngry.setSelected(false);
            btnMoodExcited.setSelected(false);
            btnMoodCalm.setSelected(false);
            
            // Set selected mood
            v.setSelected(true);
            
            if (v == btnMoodHappy) {
                selectedMood[0] = "Happy";
                selectedMoodEmoji[0] = "😊";
            } else if (v == btnMoodSad) {
                selectedMood[0] = "Sad";
                selectedMoodEmoji[0] = "😢";
            } else if (v == btnMoodAngry) {
                selectedMood[0] = "Angry";
                selectedMoodEmoji[0] = "😠";
            } else if (v == btnMoodExcited) {
                selectedMood[0] = "Excited";
                selectedMoodEmoji[0] = "🤩";
            } else if (v == btnMoodCalm) {
                selectedMood[0] = "Calm";
                selectedMoodEmoji[0] = "😌";
            }
            
            txtSelectedMood.setText(selectedMoodEmoji[0] + " " + selectedMood[0]);
        };
        
        btnMoodHappy.setOnClickListener(moodClickListener);
        btnMoodSad.setOnClickListener(moodClickListener);
        btnMoodAngry.setOnClickListener(moodClickListener);
        btnMoodExcited.setOnClickListener(moodClickListener);
        btnMoodCalm.setOnClickListener(moodClickListener);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Save", null)
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
                // Always use PH-today to match backend; ignore any client date edits
                String selectedDate = getTodayDisplayPh();

                // Validate content
                if (content.isEmpty()) {
                    edtContent.setError("Entry cannot be empty!");
                    return;
                }

                // Date is fixed to PH-today; no validation needed beyond not empty
                if (selectedDate.isEmpty()) return;

                //Getting studentID
                SharedPreferences sessionPrefs = requireContext().getSharedPreferences("student_session", Context.MODE_PRIVATE);
                int studentId = sessionPrefs.getInt("studentId", -1);

                if (studentId == -1) {
                    Toast.makeText(getContext(), "Student session error. Please log in again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedMood[0].isEmpty()) {
                    txtSelectedMood.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkred));
                    txtSelectedMood.setText("Please select a mood");
                    return;
                }

                //Save the journal entry
                JournalEntry entry = new JournalEntry(
                        studentId,
                        selectedDate,
                        title.isEmpty() ? "Untitled Entry" : title,
                        content,
                        selectedMoodEmoji[0] + " " + selectedMood[0]
                );

                Log.d("JournalSubmit", "Preparing to submit journal entry:");
                Log.d("JournalSubmit", "Student ID: " + studentId);
                Log.d("JournalSubmit", "Date: " + selectedDate);
                Log.d("JournalSubmit", "Title: " + title);
                Log.d("JournalSubmit", "Content: " + content);
                Log.d("JournalSubmit", "Mood: " + selectedMoodEmoji[0] + " " + selectedMood[0]);


                // Add entry to list immediately for better UX
                journalEntries.add(0, entry); // Add to top of list
                adapter.notifyItemInserted(0);
                saveToPreferences();
                
                // Scroll to top to show new entry
                recyclerView.scrollToPosition(0);

                //Submit to server using Retrofit
                JournalEntryApi api = ApiClient.getClient().create(JournalEntryApi.class);
                Call<JournalEntry> call = api.submitJournalEntry(entry);
                call.enqueue(new Callback<JournalEntry>() {
                    @Override
                    public void onResponse(Call<JournalEntry> call, Response<JournalEntry> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JournalEntry savedEntry = response.body(); // ✅ Has correct journalId from backend
                            // Normalize date for UI/calendar
                            savedEntry.setDate(toDisplayDate(savedEntry.getDate()));
                            // Update the entry with the correct journalId from server
                            if (savedEntry != null) {
                                // Find and update the entry in the list
                                for (int i = 0; i < journalEntries.size(); i++) {
                                    if (sameDay(journalEntries.get(i).getDate(), savedEntry.getDate()) &&
                                            journalEntries.get(i).getContent().equals(savedEntry.getContent())) {
                                        journalEntries.set(i, savedEntry);
                                        adapter.notifyItemChanged(i);
                                        break;
                                    }
                                }
                                saveToPreferences();
                            }

                            Snackbar.make(requireView(), "Journal entry saved.", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue))
                                    .setTextColor(Color.WHITE)
                                    .show();
                        } else if (response.code() == 409) {
                            // Server says entry already exists today — switch to edit mode
                            fetchTodayAndOpenEdit(studentId);

                            // rollback optimistic add
                            journalEntries.remove(0);
                            adapter.notifyItemRemoved(0);
                            saveToPreferences();

                            Snackbar.make(requireView(), "Today's entry already exists. Switched to edit.", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue))
                                    .setTextColor(Color.WHITE)
                                    .show();
                        } else {
                            // Remove the entry if server save failed
                            journalEntries.remove(0);
                            adapter.notifyItemRemoved(0);
                            saveToPreferences();
                            
                            Snackbar.make(requireView(), "Error occurred.", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.darkred))
                                    .setTextColor(Color.WHITE)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JournalEntry> call, Throwable t) {
                        // Remove the entry if network failed
                        journalEntries.remove(0);
                        adapter.notifyItemRemoved(0);
                        saveToPreferences();
                        
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
        EditText edtDate = dialogView.findViewById(R.id.edtDate);
        
        // Mood selection views
        TextView btnMoodHappy = dialogView.findViewById(R.id.btnMoodHappy);
        TextView btnMoodSad = dialogView.findViewById(R.id.btnMoodSad);
        TextView btnMoodAngry = dialogView.findViewById(R.id.btnMoodAngry);
        TextView btnMoodExcited = dialogView.findViewById(R.id.btnMoodExcited);
        TextView btnMoodCalm = dialogView.findViewById(R.id.btnMoodCalm);
        TextView txtSelectedMood = dialogView.findViewById(R.id.txtSelectedMood);
        
        // Use arrays to make variables effectively final
        final String[] selectedMood = {""};
        final String[] selectedMoodEmoji = {""};

        //Populate fields with existing data
        edtTitle.setText(entry.getTitle());
        edtContent.setText(entry.getContent());
        edtDate.setText(entry.getDate());
        
        // Parse existing mood and set selection
        String existingMood = entry.getMood();
        if (existingMood.contains("😊")) {
            selectedMood[0] = "Happy";
            selectedMoodEmoji[0] = "😊";
            btnMoodHappy.setSelected(true);
        } else if (existingMood.contains("😢")) {
            selectedMood[0] = "Sad";
            selectedMoodEmoji[0] = "😢";
            btnMoodSad.setSelected(true);
        } else if (existingMood.contains("😠")) {
            selectedMood[0] = "Angry";
            selectedMoodEmoji[0] = "😠";
            btnMoodAngry.setSelected(true);
        } else if (existingMood.contains("🤩")) {
            selectedMood[0] = "Excited";
            selectedMoodEmoji[0] = "🤩";
            btnMoodExcited.setSelected(true);
        } else if (existingMood.contains("😌")) {
            selectedMood[0] = "Calm";
            selectedMoodEmoji[0] = "😌";
            btnMoodCalm.setSelected(true);
        }
        
        if (!selectedMood[0].isEmpty()) {
            txtSelectedMood.setText(selectedMoodEmoji[0] + " " + selectedMood[0]);
        }

        // Mood selection click listeners
        View.OnClickListener moodClickListener = v -> {
            // Reset all mood buttons
            btnMoodHappy.setSelected(false);
            btnMoodSad.setSelected(false);
            btnMoodAngry.setSelected(false);
            btnMoodExcited.setSelected(false);
            btnMoodCalm.setSelected(false);
            
            // Set selected mood
            v.setSelected(true);
            
            if (v == btnMoodHappy) {
                selectedMood[0] = "Happy";
                selectedMoodEmoji[0] = "😊";
            } else if (v == btnMoodSad) {
                selectedMood[0] = "Sad";
                selectedMoodEmoji[0] = "😢";
            } else if (v == btnMoodAngry) {
                selectedMood[0] = "Angry";
                selectedMoodEmoji[0] = "😠";
            } else if (v == btnMoodExcited) {
                selectedMood[0] = "Excited";
                selectedMoodEmoji[0] = "🤩";
            } else if (v == btnMoodCalm) {
                selectedMood[0] = "Calm";
                selectedMoodEmoji[0] = "😌";
            }
            
            txtSelectedMood.setText(selectedMoodEmoji[0] + " " + selectedMood[0]);
        };
        
        btnMoodHappy.setOnClickListener(moodClickListener);
        btnMoodSad.setOnClickListener(moodClickListener);
        btnMoodAngry.setOnClickListener(moodClickListener);
        btnMoodExcited.setOnClickListener(moodClickListener);
        btnMoodCalm.setOnClickListener(moodClickListener);

        // Date should not be editable (backend controls day); lock the field
        edtDate.setEnabled(false);
        edtDate.setFocusable(false);

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
                String newDate = edtDate.getText().toString().trim();

                if (newContent.isEmpty()) {
                    edtContent.setError("Entry cannot be empty!");
                    return;
                }

                if (newDate.isEmpty()) return;

                Log.d("JournalEditDialog", "Original Journal ID: " + entry.getJournalId());
                SharedPreferences prefs = requireContext().getSharedPreferences("student_session", Context.MODE_PRIVATE);
                int studentId = prefs.getInt("studentId", 0);
                entry.setStudentId(studentId);

                // THEN update the other fields
                entry.setTitle(newTitle.isEmpty() ? "Untitled Entry" : newTitle);
                entry.setContent(newContent);
                entry.setMood(selectedMoodEmoji + " " + selectedMood);
                entry.setDate(newDate);


                JournalEntryApi apiService = ApiClient.getClient().create(JournalEntryApi.class);

                // ✅ PUT THE LOGS HERE
                Log.d("JournalUpdate", "Updating Journal ID: " + entry.getJournalId());
                Log.d("JournalUpdate", "Title: " + entry.getTitle());
                Log.d("JournalUpdate", "Date: " + entry.getDate());

                // If editing today's entry, prefer update-today endpoint, else fallback to id-based update
                JournalEntryApi api = ApiClient.getClient().create(JournalEntryApi.class);
                SharedPreferences sessionPrefs = requireContext().getSharedPreferences("student_session", Context.MODE_PRIVATE);
                int sid = sessionPrefs.getInt("studentId", -1);
                Call<ResponseBody> call;
                if (isToday(entry.getDate())) {
                    call = api.updateToday(sid, entry);
                } else {
                    call = api.updateJournal(entry.getJournalId(), entry);
                }
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            adapter.notifyItemChanged(position);
                            saveToPreferences();
                            checkEmptyList(requireView().findViewById(R.id.txtEmptyMessage));
                            Snackbar.make(requireView(), "Journal updated.", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue))
                                    .setTextColor(Color.WHITE)
                                    .show();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                Log.e("JournalUpdate", "Update failed: " + response.code() + " - " + errorBody);
                            } catch (Exception e) {
                                Log.e("JournalUpdate", "Error reading errorBody", e);
                            }

                            Snackbar.make(requireView(), "Failed to update journal entry.", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.darkred))
                                    .setTextColor(Color.WHITE)
                                    .show();
                        }

                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("JournalUpdate", "Network error: " + t.getMessage());
                        Snackbar.make(requireView(), "Network error during update.", Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.darkred))
                                .setTextColor(Color.WHITE)
                                .show();
                        dialog.dismiss();
                    }
                });
            });
        });

        dialog.show();
    }



    private void saveToPreferences() {
        SharedPreferences sessionPrefs = requireContext().getSharedPreferences("student_session", Context.MODE_PRIVATE);
        int studentId = sessionPrefs.getInt("studentId", -1);
        if (studentId == -1) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("journal_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(journalEntries);
        editor.putString("entries_student_" + studentId, json); // ✅ Per-student
        editor.apply();
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
        SharedPreferences sessionPrefs = requireContext().getSharedPreferences("student_session", Context.MODE_PRIVATE);
        int studentId = sessionPrefs.getInt("studentId", -1);

        if (studentId == -1) {
            Toast.makeText(getContext(), "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        JournalEntryApi api = ApiClient.getClient().create(JournalEntryApi.class);
        Call<List<JournalEntry>> call = api.getJournalEntriesByStudent(studentId);

        call.enqueue(new Callback<List<JournalEntry>>() {
            @Override
            public void onResponse(Call<List<JournalEntry>> call, Response<List<JournalEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<JournalEntry> sortedList = new ArrayList<>(response.body());
                    // Normalize all dates from ISO -> display for UI & CalendarView compatibility
                    for (int i = 0; i < sortedList.size(); i++) {
                        JournalEntry e = sortedList.get(i);
                        e.setDate(toDisplayDate(e.getDate()));
                    }
                    Collections.sort(sortedList, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));

                    adapter.updateEntries(sortedList); // Directly update adapter's list

                    checkEmptyList(requireView().findViewById(R.id.txtEmptyMessage));
                    saveToPreferences();
                } else {
                    Toast.makeText(getContext(), "Failed to load journal entries.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JournalEntry>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error loading journal entries.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("JournalDebug", "onResume called — reloading entries");
        loadJournalEntries(); // always refresh when this fragment is visible
        Log.d("JournalDebug", "Adapter item count: " + adapter.getItemCount());
        Log.d("JournalDebug", "RecyclerView visibility: " + recyclerView.getVisibility());
        recyclerView.setAdapter(adapter); // force rebind
        Log.d("JournalDebug", "Before notify, journalEntries size: " + journalEntries.size());
        adapter.notifyDataSetChanged();   // refresh
        Log.d("JournalDebug", "After notify, adapter item count: " + adapter.getItemCount());
    }

}



