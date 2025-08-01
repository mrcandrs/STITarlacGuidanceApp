package com.example.stitarlacguidanceapp.Activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.GuidanceAppointmentApi;
import com.example.stitarlacguidanceapp.Models.GuidanceAppointment;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivityGuidanceAppointmentSlipBinding;

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
        updateStatusBadge();

        root.btnApprove.setVisibility(View.GONE);
        root.btnReject.setVisibility(View.GONE);
        root.btnPending.setVisibility(View.GONE);
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


        root.btnSelectDate.setOnClickListener(v -> openDatePicker());

        root.btnSelectTime.setOnClickListener(v -> openTimePicker());

        root.btnSubmit.setOnClickListener(v -> {
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
                        Toast.makeText(GuidanceAppointmentSlipActivity.this, "Appointment sent successfully", Toast.LENGTH_LONG).show();
                        //Safe to reset
                        resetForm();
                    } else {
                        Toast.makeText(GuidanceAppointmentSlipActivity.this, "Server Error: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(GuidanceAppointmentSlipActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            root.btnSubmit.setEnabled(false); //prevents double tap

            // Reset status to pending
            status = "pending";
            updateStatusBadge();

            //Reset the form
            resetForm();

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

    //Reset Form on Successful Submit
    private void resetForm() {
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

        checkFormValid();
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
                root.btnSelectTime.setTextColor(getResources().getColor(R.color.blue));
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
                && (!selectedReason.isEmpty() && (!selectedReason.equals("others") || !root.etOtherReason.getText().toString().trim().isEmpty()))
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
