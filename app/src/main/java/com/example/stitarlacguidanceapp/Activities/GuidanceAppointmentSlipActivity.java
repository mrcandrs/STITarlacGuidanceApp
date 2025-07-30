package com.example.stitarlacguidanceapp.Activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivityGuidanceAppointmentSlipBinding;

import java.text.SimpleDateFormat;
import java.util.*;

public class GuidanceAppointmentSlipActivity extends AppCompatActivity {

    private ActivityGuidanceAppointmentSlipBinding root;

    private String selectedReason = "";
    private String selectedDate = "";
    private String selectedTime = "";
    private String status = "pending";

    private final Map<String, List<String>> availableSlots = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityGuidanceAppointmentSlipBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        setupAvailability();
        setupListeners();
        updateStatusBadge();
    }

    private void setupAvailability() {
        availableSlots.put("2025-08-01", Arrays.asList("9:00 AM", "10:00 AM", "2:00 PM", "3:00 PM"));
        availableSlots.put("2025-08-02", Arrays.asList("9:00 AM", "11:00 AM", "1:00 PM"));
        availableSlots.put("2025-08-05", Arrays.asList("10:00 AM", "2:00 PM", "3:00 PM", "4:00 PM"));
        availableSlots.put("2025-08-06", Arrays.asList("9:00 AM", "10:00 AM", "11:00 AM"));
        availableSlots.put("2025-08-07", Arrays.asList("1:00 PM", "2:00 PM", "3:00 PM"));
    }

    private void setupListeners() {
        //RadioGroup listener
        root.rgReason.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selected = findViewById(checkedId);
            selectedReason = selected.getText().toString().toLowerCase();

            if ("others".equals(selectedReason)) {
                root.etOtherReason.setVisibility(View.VISIBLE);
            } else {
                root.etOtherReason.setVisibility(View.GONE);
            }
            checkFormValid();
        });

        root.btnSelectDate.setOnClickListener(v -> openDatePicker());

        root.btnSelectTime.setOnClickListener(v -> openTimePicker());

        root.btnSubmit.setOnClickListener(v -> {
            Toast.makeText(this, "Appointment request submitted successfully!", Toast.LENGTH_SHORT).show();
            status = "pending";
            updateStatusBadge();
        });

        root.btnApprove.setOnClickListener(v -> {
            status = "approved";
            updateStatusBadge();
        });

        root.btnReject.setOnClickListener(v -> {
            status = "rejected";
            updateStatusBadge();
        });

        root.btnPending.setOnClickListener(v -> {
            status = "pending";
            updateStatusBadge();
        });

        root.etStudentName.setOnFocusChangeListener((v, hasFocus) -> checkFormValid());
        root.etProgramSection.setOnFocusChangeListener((v, hasFocus) -> checkFormValid());
    }

    private void openDatePicker() {
        final Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            if (availableSlots.containsKey(date)) {
                selectedDate = date;
                root.btnSelectDate.setText(formatDate(date));
                selectedTime = "";
                root.btnSelectTime.setText("Select available time");
                root.btnSelectTime.setEnabled(true);
                checkFormValid();
            } else {
                Toast.makeText(this, "No available slots for this date", Toast.LENGTH_SHORT).show();
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
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
                    checkFormValid();
                }).show();
    }

    private void checkFormValid() {
        boolean isValid = !root.etStudentName.getText().toString().trim().isEmpty()
                && !root.etProgramSection.getText().toString().trim().isEmpty()
                && !selectedReason.isEmpty()
                && !selectedDate.isEmpty()
                && !selectedTime.isEmpty();

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
