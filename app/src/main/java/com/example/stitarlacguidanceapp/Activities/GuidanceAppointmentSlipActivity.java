package com.example.stitarlacguidanceapp.Activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.GuidanceAppointmentApi;
import com.example.stitarlacguidanceapp.Models.GuidanceAppointment;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.SimpleTextWatcher;
import com.example.stitarlacguidanceapp.databinding.ActivityGuidanceAppointmentSlipBinding;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuidanceAppointmentSlipActivity extends AppCompatActivity {

    private ActivityGuidanceAppointmentSlipBinding root;

    private String selectedReason = "";
    private String selectedDate = "";
    private String selectedTime = "";
    private String status = "pending";
    private boolean skipValidation = false;

    private boolean hasPendingAppointment = false;
    private GuidanceAppointment pendingAppointment = null;

    private final Map<String, List<String>> availableSlots = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityGuidanceAppointmentSlipBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        String fullName = getSharedPreferences("student_session", MODE_PRIVATE)
                .getString("fullName", null);

        if (fullName != null) {
            root.etStudentName.setText(fullName);
            root.etStudentName.setEnabled(false);
            root.etStudentName.setBackgroundColor(Color.parseColor("#696969"));
            root.etStudentName.setTextColor(Color.LTGRAY);
        }

        setupAvailability();
        setupListeners();
        setupStatusClickListener();
        updateStatusBadge();

        //Check appointment status when activity loads
        checkAppointmentStatus();

        //Start periodic status checking (optional)
        // startStatusPolling();
    }

    private void setupAvailability() {
        availableSlots.put("2025-09-09", Arrays.asList("9:00 AM", "10:00 AM", "2:00 PM", "3:00 PM"));
        availableSlots.put("2025-09-10", Arrays.asList("9:00 AM", "11:00 AM", "1:00 PM"));
        availableSlots.put("2025-09-11", Arrays.asList("10:00 AM", "2:00 PM", "3:00 PM", "4:00 PM"));
        availableSlots.put("2025-09-12", Arrays.asList("9:00 AM", "10:00 AM", "11:00 AM"));
        availableSlots.put("2025-09-13", Arrays.asList("1:00 PM", "2:00 PM", "3:00 PM"));
    }

    private void setupListeners() {
        //RadioGroup listener
        root.rgReason.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selected = findViewById(checkedId);

            if (selected != null) {
                selectedReason = selected.getText().toString().toLowerCase();

                if ("others".equals(selectedReason)) {
                    root.etOtherReason.setVisibility(View.VISIBLE);
                } else {
                    root.etOtherReason.setText("");
                    root.etOtherReason.setVisibility(View.GONE);
                }
            } else {
                // No button selected (e.g., after clearCheck())
                selectedReason = "";
                root.etOtherReason.setText("");
                root.etOtherReason.setVisibility(View.GONE);
            }

            checkFormValid();
        });

        root.etProgramSection.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFormValid();
            }
        });

        root.etOtherReason.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFormValid();
            }
        });

        root.btnSelectDate.setOnClickListener(v -> {
            if (hasPendingAppointment) {
                showAppointmentDetailsDialog();
                return;
            }
            openDatePicker();
        });

        root.btnSelectTime.setOnClickListener(v -> {
            if (hasPendingAppointment) {
                showAppointmentDetailsDialog();
                return;
            }
            openTimePicker();
        });

        root.btnSubmit.setOnClickListener(v -> {
            // Check if student has pending appointment
            if (hasPendingAppointment) {
                showAppointmentDetailsDialog();
                return;
            }
            checkFormValid(); // ✅ Re-validate

            if (!root.btnSubmit.isEnabled()) {
                // ✅ Force visible errors for each field
                if (selectedReason.isEmpty()) {
                    Snackbar.make(root.getRoot(), "Please select a reason.", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(ContextCompat.getColor(this, R.color.darkred))
                            .setTextColor(Color.WHITE)
                            .show();
                } else if (selectedReason.equals("others") && root.etOtherReason.getText().toString().trim().isEmpty()) {
                    root.etOtherReason.setError("Please specify your reason");
                    root.etOtherReason.requestFocus();
                }

                String programSection = root.etProgramSection.getText().toString().trim();
                if (programSection.isEmpty()) {
                    root.etProgramSection.setError("Program and Section is required");
                    root.etProgramSection.requestFocus();
                } else if (!programSection.matches("^[A-Za-z]{2,5}-\\d{1,2}[A-Za-z]?$")) {
                    root.etProgramSection.setError("Format should be like BSIT-2A");
                    root.etProgramSection.requestFocus();
                }

                if (selectedDate.isEmpty()) {
                    root.btnSelectDate.setError("Please select a date");
                }

                if (selectedTime.isEmpty()) {
                    root.btnSelectTime.setError("Please select a time");
                }

                return; //Don't continue if invalid
            }

            // Everything is valid – continue submission

            SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
            int studentId = prefs.getInt("studentId", -1); //default fallback

            // Determine reason to submit
            String reasonToSubmit = selectedReason.equals("others")
                    ? root.etOtherReason.getText().toString().trim()
                    : selectedReason;

            String studentName = root.etStudentName.getText().toString().trim();
            String programSection = root.etProgramSection.getText().toString().trim();

            //Create the appointment object
            GuidanceAppointment appointment = new GuidanceAppointment();
            appointment.setStudentId(studentId); // optional if used in API
            appointment.setStudentName(studentName);
            appointment.setProgramSection(programSection);
            appointment.setReason(reasonToSubmit);
            appointment.setDate(selectedDate);
            appointment.setTime(selectedTime);
            appointment.setStatus("pending");

            //Submit to API
            GuidanceAppointmentApi apiService = ApiClient.getClient().create(GuidanceAppointmentApi.class);
            Call<ResponseBody> call = apiService.submitAppointment(appointment);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Snackbar.make(root.getRoot(), "Appointment set successfully.", Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(ContextCompat.getColor(GuidanceAppointmentSlipActivity.this, R.color.blue))
                                .setTextColor(Color.WHITE)
                                .show();
                        //Safe to reset
                        resetForm();
                        // Check appointment status again to update UI
                        checkAppointmentStatus();
                    } else {
                        Toast.makeText(GuidanceAppointmentSlipActivity.this, "Server Error: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(GuidanceAppointmentSlipActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            // Reset status to pending
            status = "pending";
            updateStatusBadge();

            //Reset the form
            resetForm();

        });


        /*root.btnApprove.setOnClickListener(v -> {
            status = "approved";
            updateStatusBadge();
        });*/

        /*root.btnReject.setOnClickListener(v -> {
            status = "rejected";
            updateStatusBadge();
        });*/

        /*root.btnPending.setOnClickListener(v -> {
            status = "pending";
            updateStatusBadge();
        });*/

        root.etStudentName.setOnFocusChangeListener((v, hasFocus) -> checkFormValid());
        root.etProgramSection.setOnFocusChangeListener((v, hasFocus) -> checkFormValid());
    }

    //Reset Form on Successful Submit
    private void resetForm() {
        skipValidation = true; // 👈 prevent validation during reset

        root.etProgramSection.setText("");
        root.rgReason.clearCheck();
        root.etOtherReason.setText("");
        root.etOtherReason.setVisibility(View.GONE);
        root.btnSelectDate.setText("Select date");
        root.btnSelectTime.setText("Select available time");
        root.btnSelectTime.setEnabled(false);

        selectedReason = "";
        selectedDate = "";
        selectedTime = "";

        //Delay turning validation back on, so listeners won't trigger immediately
        root.etProgramSection.postDelayed(() -> skipValidation = false, 200);
    }

    private void openDatePicker() {
        final Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            if (availableSlots.containsKey(date)) {
                selectedDate = date;
                root.btnSelectDate.setText(formatDate(date));
                root.btnSelectDate.setError(null); // ✅ clear error
                selectedTime = "";
                root.btnSelectTime.setText("Select available time");
                root.btnSelectTime.setEnabled(true);
                root.btnSelectTime.setTextColor(getResources().getColor(R.color.blue));
            } else {
                selectedDate = "";
                root.btnSelectDate.setText("Select date");
                root.btnSelectDate.setError("No available slots for this date");
                Snackbar.make(root.getRoot(), "No available slots for this date.", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(this, R.color.darkred))
                        .setTextColor(Color.WHITE)
                        .show();
            }

            checkFormValid(); // ✅ call validation
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        dialog.show();
    }


    private void openTimePicker() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> slots = availableSlots.get(selectedDate);
        if (slots == null || slots.isEmpty()) {
            Toast.makeText(this, "No available time slots", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] times = slots.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a time")
                .setItems(times, (dialog, which) -> {
                    selectedTime = times[which];
                    root.btnSelectTime.setText(selectedTime);
                    root.btnSelectTime.setError(null); // ✅ clear error
                    checkFormValid();
                }).show();
    }

    // Add this method to check appointment status
    private void checkAppointmentStatus() {
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1);

        if (studentId == -1) {
            Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        GuidanceAppointmentApi apiService = ApiClient.getClient().create(GuidanceAppointmentApi.class);
        Call<List<GuidanceAppointment>> call = apiService.getStudentAppointments(studentId);

        call.enqueue(new Callback<List<GuidanceAppointment>>() {
            @Override
            public void onResponse(Call<List<GuidanceAppointment>> call, Response<List<GuidanceAppointment>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Get the most recent appointment
                    GuidanceAppointment latestAppointment = response.body().get(0);
                    String currentStatus = latestAppointment.getStatus();

                    // Check if there's a pending appointment
                    hasPendingAppointment = "pending".equals(currentStatus);
                    if (hasPendingAppointment) {
                        pendingAppointment = latestAppointment;
                        disableFormForPendingAppointment();
                    } else {
                        pendingAppointment = null;
                        enableFormForNewAppointment();
                    }

                    // Update the status if it has changed
                    if (!currentStatus.equals(status)) {
                        status = currentStatus;
                        updateStatusBadge();

                        // Show notification based on status
                        if ("approved".equals(status)) {
                            Snackbar.make(root.getRoot(), "Your appointment has been approved!", Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(ContextCompat.getColor(GuidanceAppointmentSlipActivity.this, R.color.green_700))
                                    .setTextColor(Color.WHITE)
                                    .show();
                        } else if ("rejected".equals(status)) {
                            Snackbar.make(root.getRoot(), "Your appointment has been rejected.", Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(ContextCompat.getColor(GuidanceAppointmentSlipActivity.this, R.color.red_700))
                                    .setTextColor(Color.WHITE)
                                    .show();
                        }
                    }
                } else {
                    // No appointments found, enable form
                    hasPendingAppointment = false;
                    pendingAppointment = null;
                    enableFormForNewAppointment();
                }
            }

            @Override
            public void onFailure(Call<List<GuidanceAppointment>> call, Throwable t) {
                // Handle error silently for background checks
                Log.e("AppointmentStatus", "Failed to check status: " + t.getMessage());
            }
        });
    }

    // Add this method to periodically check status
    private void startStatusPolling() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                checkAppointmentStatus();
                handler.postDelayed(this, 30000); // Check every 30 seconds
            }
        };
        handler.post(runnable);
    }

    private void checkFormValid() {
        if (skipValidation || hasPendingAppointment) return;

        boolean isValid = true;

        //Program Section Validation
        String programSection = root.etProgramSection.getText().toString().trim();
        if (programSection.isEmpty()) {
            root.etProgramSection.setError("Program and Section is required");
            isValid = false;
        } else if (!programSection.matches("^[A-Za-z]{2,5}-\\d{1,2}[A-Za-z]?$")) {
            root.etProgramSection.setError("Format should be like BSIT-4B");
            isValid = false;
        } else {
            root.etProgramSection.setError(null);
        }

        //Reason Validation
        if (selectedReason.isEmpty()) {
            isValid = false;
        } else if (selectedReason.equals("others")) {
            String otherReason = root.etOtherReason.getText().toString().trim();
            if (otherReason.isEmpty()) {
                root.etOtherReason.setError("Please specify your reason");
                isValid = false;
            } else {
                root.etOtherReason.setError(null);
            }
        }

        // Date and Time Validation
        if (selectedDate.isEmpty()) {
            root.btnSelectDate.setError("Please select a date");
            isValid = false;
        } else {
            root.btnSelectDate.setError(null);
        }

        if (selectedTime.isEmpty()) {
            root.btnSelectTime.setError("Please select a time");
            isValid = false;
        } else {
            root.btnSelectTime.setError(null);
        }

        root.btnSubmit.setEnabled(isValid);
    }

    private void updateStatusBadge() {
        switch (status) {
            case "approved":
                root.statusText.setText("Status: Approved");
                root.statusText.setTextColor(getResources().getColor(R.color.green_700));
                root.statusBadge.setBackgroundColor(getResources().getColor(R.color.approved_green));
                root.statusIcon.setImageResource(R.drawable.ic_approved);
                break;
            case "rejected":
                root.statusText.setText("Status: Rejected");
                root.statusText.setTextColor(getResources().getColor(R.color.red_700));
                root.statusBadge.setBackgroundColor(getResources().getColor(R.color.rejected_red));
                root.statusIcon.setImageResource(R.drawable.ic_rejected);
                break;
            default:
                root.statusText.setText("Status: Pending");
                root.statusText.setTextColor(getResources().getColor(R.color.yellow_700));
                root.statusBadge.setBackgroundColor(getResources().getColor(R.color.pending_yellow));
                root.statusIcon.setImageResource(R.drawable.ic_pending);
                break;
        }
    }

    // Method to disable form when there's a pending appointment
    private void disableFormForPendingAppointment() {
        root.etProgramSection.setEnabled(false);
        root.etProgramSection.setBackgroundColor(Color.parseColor("#f5f5f5"));
        root.etProgramSection.setTextColor(Color.parseColor("#999999"));

        root.rgReason.setEnabled(false);
        for (int i = 0; i < root.rgReason.getChildCount(); i++) {
            View child = root.rgReason.getChildAt(i);
            if (child instanceof RadioButton) {
                child.setEnabled(false);
                child.setAlpha(0.5f);
            }
        }

        root.etOtherReason.setEnabled(false);
        root.etOtherReason.setBackgroundColor(Color.parseColor("#f5f5f5"));
        root.etOtherReason.setTextColor(Color.parseColor("#999999"));

        root.btnSelectDate.setEnabled(false);
        root.btnSelectDate.setAlpha(0.5f);
        root.btnSelectTime.setEnabled(false);
        root.btnSelectTime.setAlpha(0.5f);

        root.btnSubmit.setEnabled(false);
        root.btnSubmit.setAlpha(0.5f);
        root.btnSubmit.setText("You have a pending appointment");

        // Show a message about pending appointment
        Snackbar.make(root.getRoot(), "You already have a pending appointment. Please wait for approval.", Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(this, R.color.yellow_700))
                .setTextColor(Color.WHITE)
                .show();

        // Show detailed dialog instead of simple message
        showAppointmentDetailsDialog();
    }

    // Method to enable form for new appointment
    private void enableFormForNewAppointment() {
        root.etProgramSection.setEnabled(true);
        root.etProgramSection.setBackgroundColor(Color.WHITE);
        root.etProgramSection.setTextColor(Color.BLACK);

        root.rgReason.setEnabled(true);
        for (int i = 0; i < root.rgReason.getChildCount(); i++) {
            View child = root.rgReason.getChildAt(i);
            if (child instanceof RadioButton) {
                child.setEnabled(true);
                child.setAlpha(1.0f);
            }
        }

        root.etOtherReason.setEnabled(true);
        root.etOtherReason.setBackgroundColor(Color.WHITE);
        root.etOtherReason.setTextColor(Color.BLACK);

        root.btnSelectDate.setEnabled(true);
        root.btnSelectDate.setAlpha(1.0f);
        root.btnSelectTime.setEnabled(false); // Will be enabled when date is selected
        root.btnSelectTime.setAlpha(1.0f);

        root.btnSubmit.setEnabled(false); // Will be enabled when form is valid
        root.btnSubmit.setAlpha(1.0f);
        root.btnSubmit.setText("Submit Appointment Request");
    }

    // Add this method to your GuidanceAppointmentSlipActivity class
    private void setupStatusClickListener() {
        // Make the status badge clickable
        root.statusBadge.setClickable(true);
        root.statusBadge.setOnClickListener(v -> {
            if (pendingAppointment != null) {
                showAppointmentDetailsDialog();
            } else {
                // If no pending appointment, show a message
                Snackbar.make(root.getRoot(), "No appointment details available", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(this, R.color.blue))
                        .setTextColor(Color.WHITE)
                        .show();
            }
        });

        // Add visual feedback for clickable status
        root.statusBadge.setBackground(getResources().getDrawable(R.drawable.status_badge_clickable));
    }

    // Enhanced method to show detailed appointment information dialog
    private void showAppointmentDetailsDialog() {
        if (pendingAppointment == null) return;

        // Create a custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_appointment_details, null);

        // Find views in the custom layout
        TextView tvStudentName = dialogView.findViewById(R.id.tv_student_name);
        TextView tvProgramSection = dialogView.findViewById(R.id.tv_program_section);
        TextView tvReason = dialogView.findViewById(R.id.tv_reason);
        TextView tvDate = dialogView.findViewById(R.id.tv_date);
        TextView tvTime = dialogView.findViewById(R.id.tv_time);
        TextView tvStatus = dialogView.findViewById(R.id.tv_status);
        TextView tvSubmittedDate = dialogView.findViewById(R.id.tv_submitted_date);
        TextView tvUpdatedDate = dialogView.findViewById(R.id.tv_updated_date);
        ImageView ivStatusIcon = dialogView.findViewById(R.id.iv_status_icon);
        LinearLayout llStatusContainer = dialogView.findViewById(R.id.ll_status_container);

        // Populate the dialog with appointment data
        tvStudentName.setText(pendingAppointment.getStudentName());
        tvProgramSection.setText(pendingAppointment.getProgramSection());
        tvReason.setText(pendingAppointment.getReason());
        tvDate.setText(formatDate(pendingAppointment.getDate()));
        tvTime.setText(pendingAppointment.getTime());

        // Format and display status
        String statusText = pendingAppointment.getStatus().toUpperCase();
        tvStatus.setText(statusText);

        // Set status-specific styling
        switch (pendingAppointment.getStatus().toLowerCase()) {
            case "approved":
                tvStatus.setTextColor(getResources().getColor(R.color.green_700));
                ivStatusIcon.setImageResource(R.drawable.ic_approved);
                llStatusContainer.setBackgroundColor(getResources().getColor(R.color.approved_green_light));
                break;
            case "rejected":
                tvStatus.setTextColor(getResources().getColor(R.color.red_700));
                ivStatusIcon.setImageResource(R.drawable.ic_rejected);
                llStatusContainer.setBackgroundColor(getResources().getColor(R.color.rejected_red_light));
                break;
            default: // pending
                tvStatus.setTextColor(getResources().getColor(R.color.yellow_700));
                ivStatusIcon.setImageResource(R.drawable.ic_pending);
                llStatusContainer.setBackgroundColor(getResources().getColor(R.color.pending_yellow_light));
                break;
        }

        //Format submission date
        if (pendingAppointment.getCreatedAt() != null) {
            tvSubmittedDate.setText(formatDateTime(pendingAppointment.getCreatedAt()));
        } else {
            tvSubmittedDate.setText("Not available");
        }

        //Format last updated date
        if (pendingAppointment.getUpdatedAt() != null) {
            tvUpdatedDate.setText(formatDateTime(pendingAppointment.getUpdatedAt()));
        } else {
            tvUpdatedDate.setText("Not updated");
        }

        //Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Appointment Details")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Refresh Status", (dialog, which) -> {
                    checkAppointmentStatus();
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Helper method to format date and time
    private String formatDateTime(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);
            Date date = inputFormat.parse(dateTimeString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateTimeString; // Return original string if parsing fails
        }
    }

    private String formatDate(String input) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(input);
            SimpleDateFormat readable = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
            return readable.format(date);
        } catch (Exception e) {
            return input;
        }
    }
}
