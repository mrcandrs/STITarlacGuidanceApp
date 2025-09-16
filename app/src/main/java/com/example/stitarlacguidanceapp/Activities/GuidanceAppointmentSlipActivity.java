package com.example.stitarlacguidanceapp.Activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.GuidanceAppointmentApi;
import com.example.stitarlacguidanceapp.Models.AvailableSlotForStudent;
import com.example.stitarlacguidanceapp.Models.AvailableTimeSlot;
import com.example.stitarlacguidanceapp.Models.GuidanceAppointment;
import com.example.stitarlacguidanceapp.Models.GuidancePass;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.SimpleTextWatcher;
import com.example.stitarlacguidanceapp.databinding.ActivityGuidanceAppointmentSlipBinding;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.snackbar.Snackbar;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;

import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.stitarlacguidanceapp.NotificationHelper;

    public class GuidanceAppointmentSlipActivity extends AppCompatActivity {

    private ActivityGuidanceAppointmentSlipBinding root;

    private String selectedReason = "";
    private String selectedDate = "";
    private String selectedTime = "";
    private String status = "pending";
    private boolean skipValidation = false;

    private boolean hasPendingAppointment = false;
    private GuidanceAppointment pendingAppointment = null;

    private GuidanceAppointment latestAppointment = null;

    private NotificationHelper notificationHelper;
    private String lastKnownStatus = "";

    private GuidancePass currentGuidancePass = null;

    private final Map<String, List<String>> availableSlots = new HashMap<>();

    private final Set<String> availableDates = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityGuidanceAppointmentSlipBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        // Defensive reset and session gate (ADD THIS HERE)
        status = "pending";
        lastKnownStatus = "";
        hasPendingAppointment = false;
        pendingAppointment = null;
        updateStatusBadge();
        enableFormForNewAppointment();

        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1);
        if (studentId == -1) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize notification helper
        notificationHelper = new NotificationHelper(this);

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

        requestNotificationPermission();

        //Check appointment status when activity loads
        checkAppointmentStatus();

        checkGuidancePass();

        //Start periodic status checking (optional)
        startStatusPolling();
    }

    private void setupAvailability() {
        fetchAvailableTimeSlots();
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
                showAppointmentDetailsDialog(latestAppointment);
                return;
            }
            openDatePicker();
        });

        root.btnSelectTime.setOnClickListener(v -> {
            if (hasPendingAppointment) {
                showAppointmentDetailsDialog(latestAppointment);
                return;
            }
            openTimePicker();
        });

        root.btnSubmit.setOnClickListener(v -> {
        // Block submit only if current appointment is pending
            if (hasPendingAppointment) {
                if (latestAppointment != null) {
                    showAppointmentDetailsDialog(latestAppointment);
                } else {
                    Snackbar.make(root.getRoot(), "Loading appointment details...", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(ContextCompat.getColor(this, R.color.blue))
                            .setTextColor(Color.WHITE)
                            .show();
                    checkAppointmentStatus();
                }
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

            // Create the appointment object
            GuidanceAppointment appointment = new GuidanceAppointment();
            appointment.setStudentId(studentId);
            appointment.setStudentName(studentName);
            appointment.setProgramSection(programSection);
            appointment.setReason(reasonToSubmit);
            appointment.setDate(selectedDate);
            appointment.setTime(selectedTime);
            appointment.setStatus("pending");

            // Add logging to see what's being sent
            Log.d("AppointmentData", "Sending appointment: " +
                    "StudentId=" + studentId +
                    ", StudentName=" + studentName +
                    ", ProgramSection=" + programSection +
                    ", Reason=" + reasonToSubmit +
                    ", Date=" + selectedDate +
                    ", Time=" + selectedTime);

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
                        String errorMessage = "Error " + response.code();
                        String userFriendlyMessage = "Appointment submission failed";

                        try {
                            String errorBody = response.errorBody().string();
                            errorMessage += ": " + errorBody;

                            // Log the error to logcat
                            Log.e("AppointmentError", "HTTP " + response.code() + ": " + errorBody);
                            Log.e("AppointmentError", "Full error message: " + errorMessage);

                            // Extract the actual error message from JSON
                            if (errorBody.contains("\"message\":")) {
                                // Find the message value in the JSON
                                int messageStart = errorBody.indexOf("\"message\":\"") + 11;
                                int messageEnd = errorBody.indexOf("\"", messageStart);
                                if (messageStart > 10 && messageEnd > messageStart) {
                                    userFriendlyMessage = errorBody.substring(messageStart, messageEnd);
                                }
                            }

                        } catch (Exception e) {
                            errorMessage += " (could not read error body)";
                            Log.e("AppointmentError", "Could not read error body: " + e.getMessage());
                        }

                        // Show the actual error message to the user
                        Toast.makeText(GuidanceAppointmentSlipActivity.this, userFriendlyMessage, Toast.LENGTH_LONG).show();
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

    // Update the openDatePicker method to refresh slots when date changes
    private void openDatePicker() {
        if (availableDates.isEmpty()) {
            Snackbar.make(root.getRoot(), "No available dates at the moment.", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.darkred))
                    .setTextColor(Color.WHITE)
                    .show();
            return;
        }

        // Convert allowed yyyy-MM-dd to UTC midnight millis
        Set<Long> allowedDaysMillis = new HashSet<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            for (String d : availableDates) {
                Date date = sdf.parse(d);
                if (date != null) {
                    allowedDaysMillis.add(date.getTime());
                }
            }
        } catch (Exception ignored) {}

        CalendarConstraints.DateValidator validator = new DateValidatorAvailableDates(allowedDaysMillis);

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(validator)
                .build();

        com.google.android.material.datepicker.MaterialDatePicker<Long> picker =
                com.google.android.material.datepicker.MaterialDatePicker.Builder
                        .datePicker()
                        .setTitleText("Select available date")
                        .setCalendarConstraints(constraints)
                        .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            // selection is UTC midnight millis
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            out.setTimeZone(TimeZone.getTimeZone("UTC"));
            String picked = out.format(new Date(selection));

            // Safe: only allowed dates can be picked
            selectedDate = picked;
            root.btnSelectDate.setText(formatDate(picked));
            root.btnSelectDate.setError(null);

            selectedTime = "";
            root.btnSelectTime.setText("Select available time");
            root.btnSelectTime.setEnabled(true);
            root.btnSelectTime.setTextColor(getResources().getColor(R.color.blue));

            // Ensure we have times for this date (fetch if not cached)
            if (!availableSlots.containsKey(picked)) {
                fetchAvailableSlotsForDate(picked);
            }

            checkFormValid();
        });

        picker.show(getSupportFragmentManager(), "available_date_picker");
    }

    // Custom validator that only enables given days (UTC midnight millis)
    public static class DateValidatorAvailableDates implements CalendarConstraints.DateValidator, java.io.Serializable {
        private final HashSet<Long> allowedDays;

        public DateValidatorAvailableDates(Set<Long> allowedDaysMillis) {
            this.allowedDays = new HashSet<>(allowedDaysMillis);
        }

        @Override
        public boolean isValid(long date) {
            // Normalize to UTC midnight
            Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
            utc.setTimeInMillis(date);
            utc.set(Calendar.HOUR_OF_DAY, 0);
            utc.set(Calendar.MINUTE, 0);
            utc.set(Calendar.SECOND, 0);
            utc.set(Calendar.MILLISECOND, 0);
            return allowedDays.contains(utc.getTimeInMillis());
        }

        @Override
        public int describeContents() { return 0; }
        @Override
        public void writeToParcel(android.os.Parcel dest, int flags) {}
        public static final Creator<DateValidatorAvailableDates> CREATOR = new Creator<DateValidatorAvailableDates>() {
            @Override public DateValidatorAvailableDates createFromParcel(android.os.Parcel in) { return null; }
            @Override public DateValidatorAvailableDates[] newArray(int size) { return new DateValidatorAvailableDates[size]; }
        };
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

    // Add method to fetch slots for a specific date
    private void fetchAvailableSlotsForDate(String date) {
        GuidanceAppointmentApi apiService = ApiClient.getClient().create(GuidanceAppointmentApi.class);
        Call<List<AvailableSlotForStudent>> call = apiService.getAvailableSlotsForStudentsByDate(date);

        call.enqueue(new Callback<List<AvailableSlotForStudent>>() {
            @Override
            public void onResponse(Call<List<AvailableSlotForStudent>> call, Response<List<AvailableSlotForStudent>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> times = new ArrayList<>();
                    for (AvailableSlotForStudent slot : response.body()) {
                        times.add(slot.getTime());
                    }
                    if (!times.isEmpty()) {
                        availableSlots.put(date, times);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AvailableSlotForStudent>> call, Throwable t) {
                Log.e("AvailableSlots", "Error fetching slots for date: " + t.getMessage());
            }
        });
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
                    GuidanceAppointment latestAppointment = response.body().get(0);
                    String currentStatus = latestAppointment.getStatus();

                    // Track for dialog use across all statuses
                    GuidanceAppointmentSlipActivity.this.latestAppointment = latestAppointment;

                    // Only check for status changes if we have a previous status to compare
                    if (!lastKnownStatus.isEmpty() && !lastKnownStatus.equals(currentStatus)) {
                        Log.d("StatusChange", "Status changed from " + lastKnownStatus + " to " + currentStatus);
                        showStatusChangeNotification(latestAppointment, lastKnownStatus, currentStatus);
                    }

                    // Update last known status
                    lastKnownStatus = currentStatus;

                    // If not approved anymore, hide the Guidance Pass and allow booking again
                    if (!"approved".equalsIgnoreCase(currentStatus)) {
                        hideGuidancePass();
                    }

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
                    }
                } else {
                    // No appointments found, enable form
                    hasPendingAppointment = false;
                    pendingAppointment = null;
                    enableFormForNewAppointment();
                    hideGuidancePass();
                    lastKnownStatus = "";
                    status = "none";          // NEW
                    updateStatusBadge();      // NEW
                }
            }

            @Override
            public void onFailure(Call<List<GuidanceAppointment>> call, Throwable t) {
                Log.e("AppointmentStatus", "Failed to check status: " + t.getMessage());
            }
        });
        // After checking appointment status, also check for guidance pass
        checkGuidancePass();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Show rationale if needed
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    showNotificationPermissionRationale();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                }
            }
        }
    }

    // Add this method to show rationale for notification permission
    private void showNotificationPermissionRationale() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notification Permission Needed")
                .setMessage("This app needs notification permission to inform you when your appointment status changes. " +
                        "This helps you stay updated about your guidance appointments.")
                .setPositiveButton("Allow", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                })
                .setNegativeButton("Not Now", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Add this method to handle status change notifications
    private void showStatusChangeNotification(GuidanceAppointment appointment, String oldStatus, String newStatus) {
        Log.d("Notification", "Showing status change notification: " + oldStatus + " -> " + newStatus);

        String studentName = appointment.getStudentName();
        String appointmentDate = formatDate(appointment.getDate());
        String appointmentTime = appointment.getTime();

        switch (newStatus.toLowerCase()) {
            case "approved":
                notificationHelper.showAppointmentApprovedNotification(studentName, appointmentDate, appointmentTime);
                break;
            case "rejected":
                notificationHelper.showAppointmentRejectedNotification(studentName, appointmentDate, appointmentTime);
                break;
            default:
                Log.d("Notification", "No notification for status: " + newStatus);
                break;
        }
    }

    // Add this method to periodically check status
    private void startStatusPolling() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Always poll so we catch transitions from approved -> completed
                checkAppointmentStatus();
                handler.postDelayed(this, 10000); // Check every 10 seconds instead of 2 minutes
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAppointmentStatus();
        fetchAvailableTimeSlots(); // Refresh available slots
    }

    // Add this method to fetch available time slots
    // Update the fetchAvailableTimeSlots method in GuidanceAppointmentSlipActivity
    private void fetchAvailableTimeSlots() {
        GuidanceAppointmentApi apiService = ApiClient.getClient().create(GuidanceAppointmentApi.class);
        Call<List<AvailableSlotForStudent>> call = apiService.getAvailableSlotsForStudents();

        call.enqueue(new Callback<List<AvailableSlotForStudent>>() {
            @Override
            public void onResponse(Call<List<AvailableSlotForStudent>> call, Response<List<AvailableSlotForStudent>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateAvailableSlotsFromStudentAPI(response.body());
                } else {
                    Log.e("AvailableSlots", "Failed to fetch available slots: " + response.code());
                    setupFallbackAvailability();
                }
            }

            @Override
            public void onFailure(Call<List<AvailableSlotForStudent>> call, Throwable t) {
                Log.e("AvailableSlots", "Error fetching available slots: " + t.getMessage());
                setupFallbackAvailability();
            }
        });
    }

    // Add this new method to update slots from the student API
    private void updateAvailableSlotsFromStudentAPI(List<AvailableSlotForStudent> slots) {
        availableSlots.clear();
        availableDates.clear();

        for (AvailableSlotForStudent slot : slots) {
            String date = slot.getDate(); // "yyyy-MM-dd"
            availableDates.add(date);
            if (!availableSlots.containsKey(date)) {
                availableSlots.put(date, new ArrayList<>());
            }
            availableSlots.get(date).add(slot.getTime());
        }

        Log.d("AvailableSlots", "Updated available slots from student API: " + availableSlots.toString());
    }

    // Add this method to update available slots from API
    private void updateAvailableSlots(List<AvailableTimeSlot> slots) {
        availableSlots.clear();

        for (AvailableTimeSlot slot : slots) {
            if (slot.isActive()) {
                String date = slot.getDate();
                if (!availableSlots.containsKey(date)) {
                    availableSlots.put(date, new ArrayList<>());
                }
                availableSlots.get(date).add(slot.getTime());
            }
        }

        Log.d("AvailableSlots", "Updated available slots: " + availableSlots.toString());
    }

    // Add fallback method for when API fails
    private void setupFallbackAvailability() {
        availableSlots.put("2025-09-11", Arrays.asList("9:00 AM", "10:00 AM", "2:00 PM", "3:00 PM"));
        availableSlots.put("2025-09-12", Arrays.asList("9:00 AM", "11:00 AM", "1:00 PM"));
        availableSlots.put("2025-09-13", Arrays.asList("10:00 AM", "2:00 PM", "3:00 PM", "4:00 PM"));
        availableSlots.put("2025-09-14", Arrays.asList("9:00 AM", "10:00 AM", "11:00 AM"));
        availableSlots.put("2025-09-15", Arrays.asList("1:00 PM", "2:00 PM", "3:00 PM"));
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
            switch (status == null ? "" : status.toLowerCase()) {
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
                case "completed":   // NEW
                case "closed":      // optional alias
                    root.statusText.setText("Status: Completed");
                    root.statusText.setTextColor(Color.parseColor("#1f2937"));         // neutral gray-800
                    root.statusBadge.setBackgroundColor(Color.parseColor("#E5E7EB"));   // neutral gray-200
                    root.statusIcon.setImageResource(R.drawable.ic_done);               // pick a check/done icon you have
                    break;
                case "none":        // NEW: no active appointment
                    root.statusText.setText("Status: No Active Appointment");
                    root.statusText.setTextColor(Color.parseColor("#374151"));
                    root.statusBadge.setBackgroundColor(Color.parseColor("#F3F4F6"));
                    root.statusIcon.setImageResource(R.drawable.ic_info);
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
    }

    // Method to enable form for new appointment
    // enableFormForNewAppointment — ensure it DOES NOT call hideGuidancePass()
    private void enableFormForNewAppointment() {
        // Only re-enable inputs and reset submit button
        root.etProgramSection.setEnabled(true);
        root.etProgramSection.setBackgroundColor(Color.WHITE);
        root.etProgramSection.setTextColor(Color.BLACK);

        root.rgReason.setEnabled(true);
        for (int i = 0; i < root.rgReason.getChildCount(); i++) {
            View child = root.rgReason.getChildAt(i);
            if (child instanceof RadioButton) {
                child.setEnabled(true);
                child.setAlpha(1f);
            }
        }

        root.etOtherReason.setEnabled(true);
        root.etOtherReason.setBackgroundColor(Color.WHITE);
        root.etOtherReason.setTextColor(Color.BLACK);

        root.btnSelectDate.setEnabled(true);
        root.btnSelectDate.setAlpha(1f);
        root.btnSelectTime.setEnabled(true);
        root.btnSelectTime.setAlpha(1f);

        root.btnSubmit.setEnabled(true);
        root.btnSubmit.setAlpha(1f);
        root.btnSubmit.setText("Set Appointment");

        // IMPORTANT: Do not call hideGuidancePass() here.
        // Do not change guidancePassContainer visibility here.
    }

        // Put inside GuidanceAppointmentSlipActivity
        private void disableFormForActivePass() {
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
            root.btnSubmit.setText("You have an approved appointment");
        }

    // Add this method to your GuidanceAppointmentSlipActivity class
    private void setupStatusClickListener() {
        root.statusBadge.setClickable(true);
        root.statusBadge.setOnClickListener(v -> {
            if (latestAppointment != null) {
                showAppointmentDetailsDialog(latestAppointment);
            } else {
                Snackbar.make(root.getRoot(), "No appointment details available", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(this, R.color.blue))
                        .setTextColor(Color.WHITE)
                        .show();
            }
        });

        root.statusBadge.setBackground(getResources().getDrawable(R.drawable.status_badge_clickable));
    }

    // Enhanced method to show detailed appointment information dialog
    private void showAppointmentDetailsDialog(GuidanceAppointment appointment) {
        if (appointment == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_appointment_details, null);

        TextView tvStudentName = dialogView.findViewById(R.id.tv_student_name);
        TextView tvProgramSection = dialogView.findViewById(R.id.tv_program_section);
        TextView tvReason = dialogView.findViewById(R.id.tv_reason);
        TextView tvDate = dialogView.findViewById(R.id.tv_date);
        TextView tvTime = dialogView.findViewById(R.id.tv_time);
        TextView tvStatus = dialogView.findViewById(R.id.tv_status);
        TextView tvRejectionReason = dialogView.findViewById(R.id.tv_rejection_reason);
        TextView tvSubmittedDate = dialogView.findViewById(R.id.tv_submitted_date);
        TextView tvUpdatedDate = dialogView.findViewById(R.id.tv_updated_date);
        ImageView ivStatusIcon = dialogView.findViewById(R.id.iv_status_icon);
        LinearLayout llStatusContainer = dialogView.findViewById(R.id.ll_status_container);

        tvStudentName.setText(appointment.getStudentName());
        tvProgramSection.setText(appointment.getProgramSection());
        tvReason.setText(appointment.getReason());
        tvDate.setText(formatDate(appointment.getDate()));
        tvTime.setText(appointment.getTime());

        String statusText = appointment.getStatus().toUpperCase();
        tvStatus.setText(statusText);

        switch (appointment.getStatus().toLowerCase()) {
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
            default:
                tvStatus.setTextColor(getResources().getColor(R.color.yellow_700));
                ivStatusIcon.setImageResource(R.drawable.ic_pending);
                llStatusContainer.setBackgroundColor(getResources().getColor(R.color.pending_yellow_light));
                break;
        }

        // Show/hide rejection reason based on status
        if (appointment.getStatus().toLowerCase().equals("rejected") &&
                appointment.getRejectionReason() != null &&
                !appointment.getRejectionReason().isEmpty()) {

            tvRejectionReason.setVisibility(View.VISIBLE);
            tvRejectionReason.setText("Reason: " + appointment.getRejectionReason());
        } else {
            tvRejectionReason.setVisibility(View.GONE);
        }

        if (appointment.getCreatedAt() != null) {
            tvSubmittedDate.setText(formatDateTime(appointment.getCreatedAt()));
        } else {
            tvSubmittedDate.setText("Not available");
        }

        if (appointment.getUpdatedAt() != null) {
            tvUpdatedDate.setText(formatDateTime(appointment.getUpdatedAt()));
        } else {
            tvUpdatedDate.setText("Not updated");
        }

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

        // Add this method to check for guidance pass
        private void checkGuidancePass() {
            SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
            int studentId = prefs.getInt("studentId", -1);

            if (studentId == -1) return;

            GuidanceAppointmentApi apiService = ApiClient.getClient().create(GuidanceAppointmentApi.class);
            Call<GuidancePass> call = apiService.getGuidancePassByStudent(studentId);

            call.enqueue(new Callback<GuidancePass>() {
                @Override
                public void onResponse(Call<GuidancePass> call, Response<GuidancePass> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentGuidancePass = response.body();
                        showGuidancePass();
                    } else {
                        hideGuidancePass();
                    }
                }

                @Override
                public void onFailure(Call<GuidancePass> call, Throwable t) {
                    Log.e("GuidancePass", "Failed to fetch guidance pass: " + t.getMessage());
                    hideGuidancePass();
                }
            });
        }

        private void showGuidancePass() {
            if (currentGuidancePass != null) {
                root.guidancePassContainer.setVisibility(View.VISIBLE);
                root.guidancePassContainer.setBackgroundColor(Color.parseColor("#FFFFFF"));
                root.guidancePassContainer.setPadding(24, 16, 24, 16);
                root.btnViewGuidancePass.setAllCaps(false);
                root.btnViewGuidancePass.setText("View Guidance Pass");
                String passDetails =
                        "Issued: " + formatMaybeIsoDate(currentGuidancePass.getIssuedDate()) + "\n" +
                                "Appt: " + formatMaybeIsoDate(currentGuidancePass.getAppointment().getDate()) +
                                " • " + currentGuidancePass.getAppointment().getTime() + "\n" +
                                "By: " + currentGuidancePass.getCounselor().getName();
                root.tvGuidancePassDetails.setText(passDetails);
                root.btnViewGuidancePass.setOnClickListener(v -> showGuidancePassDialog(currentGuidancePass));

                // Block new bookings while pass is active
                disableFormForActivePass();
            }
        }

        private void hideGuidancePass() {
            root.guidancePassContainer.setVisibility(View.GONE);
            currentGuidancePass = null;

            // If there is no pending appointment, allow booking again
            if (!hasPendingAppointment) {
                enableFormForNewAppointment();
            }
        }

        private String formatMaybeIsoDate(String input) {
            if (input == null) return "";
            // Try ISO first
            try {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date d = iso.parse(input);
                SimpleDateFormat out = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
                return out.format(d);
            } catch (Exception ignored) {}

            // Fallback to yyyy-MM-dd
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date d = sdf.parse(input);
                SimpleDateFormat out = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
                return out.format(d);
            } catch (Exception e) {
                return input;
            }
        }

        private void showGuidancePassDialog(GuidancePass pass) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_guidance_pass, null);

            TextView tvStudentName = dialogView.findViewById(R.id.tv_student_name);
            TextView tvProgramSection = dialogView.findViewById(R.id.tv_program_section);
            TextView tvAppointmentDate = dialogView.findViewById(R.id.tv_appointment_date);
            TextView tvAppointmentTime = dialogView.findViewById(R.id.tv_appointment_time);
            TextView tvIssuedDate = dialogView.findViewById(R.id.tv_issued_date);
            TextView tvIssuedBy = dialogView.findViewById(R.id.tv_issued_by);
            TextView tvNotes = dialogView.findViewById(R.id.tv_notes);

            tvStudentName.setText(pass.getAppointment().getStudentName());
            tvProgramSection.setText(pass.getAppointment().getProgramSection());
            tvAppointmentDate.setText(formatMaybeIsoDate(pass.getAppointment().getDate()));
            tvAppointmentTime.setText(pass.getAppointment().getTime());
            tvIssuedDate.setText(formatMaybeIsoDate(pass.getIssuedDate()));
            tvIssuedBy.setText(pass.getCounselor().getName());

            if (pass.getNotes() != null && !pass.getNotes().isEmpty()) {
                tvNotes.setText(pass.getNotes());
                tvNotes.setVisibility(View.VISIBLE);
            } else {
                tvNotes.setVisibility(View.GONE);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        /*private void printGuidancePass(GuidancePass pass) {
            if (pass == null || pass.getAppointment() == null || pass.getCounselor() == null) {
                Toast.makeText(this, "Guidance Pass data is incomplete.", Toast.LENGTH_SHORT).show();
                return;
            }

            String html =
                    "<!DOCTYPE html><html><head><meta charset='utf-8'/>" +
                            "<style>" +
                            "body{font-family:sans-serif;margin:24px;}" +
                            ".header{text-align:center;margin-bottom:24px}" +
                            ".logo{font-size:22px;font-weight:700;color:#0477BF}" +
                            ".title{font-size:18px;margin-top:6px}" +
                            ".card{border:2px solid #0477BF;padding:16px}" +
                            ".row{margin:8px 0}" +
                            ".label{font-weight:700}" +
                            ".sig{display:flex;justify-content:space-between;margin-top:28px}" +
                            ".line{width:220px;border-bottom:1px solid #000;padding-top:28px;text-align:center}" +
                            "</style></head><body>" +
                            "<div class='header'><div class='logo'>STI</div>" +
                            "<div>STI Guidance and Counseling Office</div>" +
                            "<div class='title'>Guidance Pass</div></div>" +
                            "<div class='card'>" +
                            "<div class='row'><span class='label'>Date:</span> " + formatMaybeIsoDate(pass.getIssuedDate()) + "</div>" +
                            "<div class='row'><span class='label'>Student's Name:</span> " + pass.getAppointment().getStudentName() + "</div>" +
                            "<div class='row'><span class='label'>Program and Section:</span> " + pass.getAppointment().getProgramSection() + "</div>" +
                            "<div class='row'><span class='label'>Room Number:</span> _____________</div>" +
                            "<div class='row'><span class='label'>Time Started:</span> " + pass.getAppointment().getTime() + "</div>" +
                            "<div class='row'><span class='label'>Time Ended:</span> _____________</div>" +
                            "<div class='row'><span class='label'>Prepared by:</span> " + pass.getCounselor().getName() + "</div>" +
                            (pass.getNotes() != null && !pass.getNotes().isEmpty()
                                    ? "<div class='row'><span class='label'>Notes:</span> " + pass.getNotes() + "</div>" : "") +
                            "</div>" +
                            "<div class='sig'>" +
                            "<div><div class='line'></div><div>Name and Signature of Guidance Counselor/Associate</div></div>" +
                            "<div><div class='line'></div><div>Name and Signature of Teacher/Professor</div></div>" +
                            "</div>" +
                            "</body></html>";

            WebView webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(false);
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

            webView.post(() -> {
                PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                if (pm == null) {
                    Toast.makeText(this, "Print service not available.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String jobName = "GuidancePass_" + pass.getAppointment().getStudentName().replaceAll("\\s+", "_");
                PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter(jobName);
                pm.print(jobName, adapter, new PrintAttributes.Builder()
                        .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                        .build());
            });
        }*/

    // Add this method to handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) { // Notification permission request
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("Permission", "Notification permission granted");
                if (notificationHelper != null) {
                    Log.d("NotificationHelper", "Can show notifications: " + notificationHelper.canShowNotifications());
                }
            } else {
                // Permission denied
                Log.d("Permission", "Notification permission denied");
                // You can show a dialog explaining why notifications are useful
                showNotificationPermissionDeniedDialog();
            }
        }
    }

    // Add this method to show a dialog when permission is denied
    private void showNotificationPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notification Permission")
                .setMessage("Notification permission is required to receive updates about your appointment status. " +
                        "You can enable it later in Settings > Apps > STI Tarlac Guidance App > Notifications.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Settings", (dialog, which) -> {
                    // Open app settings
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    dialog.dismiss();
                })
                .show();
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
