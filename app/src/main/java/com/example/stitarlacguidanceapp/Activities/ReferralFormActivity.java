package com.example.stitarlacguidanceapp.Activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.Models.ReferralForm;
import com.example.stitarlacguidanceapp.NotificationHelper;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.ReferralApi;
import com.example.stitarlacguidanceapp.databinding.ActivityReferralFormBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ReferralFormActivity extends AppCompatActivity {

    private ActivityReferralFormBinding root;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityReferralFormBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());


        setupDatePicker(root.txtDateReferred, root.layoutDateReferred);
        setupDatePicker(root.edtFeedbackDate, root.layoutFeedbackDate);


        //for Live Validations of null fields
        setupLiveValidation();

        //for real-time validation of student no. and age
        setupRealTimeValidation();

        //Allows only numbers, and max length of 2 digits for Age
        root.edtAge.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(2),
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return ""; //block non-digit characters
                        }
                    }
                    return null;
                }
        });

        //Listeners for Chip Selections
        //Others - Areas of Concern
        root.chipOthersAOC.setOnCheckedChangeListener((buttonView, isChecked) -> {
            root.edtOthersAOC.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) root.edtOthersAOC.setText("");
        });

        //Others - Action Requested
        root.chipOthersAR.setOnCheckedChangeListener((buttonView, isChecked) -> {
            root.edtOthersAR.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) root.edtOthersAR.setText("");
        });

        //"Before this date" - Priority Level
        root.chipScheduledDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            root.tvBeforeDate.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) root.tvBeforeDate.setText("Select Date");
        });


        LinearLayout layoutFeedbackSection = findViewById(R.id.layoutFeedbackSection);

        root.btnToggleFeedback.setOnClickListener(v -> {
            if (layoutFeedbackSection.getVisibility() == View.GONE) {
                layoutFeedbackSection.setVisibility(View.VISIBLE);
                layoutFeedbackSection.setAlpha(0f);
                layoutFeedbackSection.animate().alpha(1f).setDuration(300);
                root.btnToggleFeedback.setText("Hide Feedback Slip");
                loadLatestReferralAndFeedback(); // fetch on open
            } else {
                layoutFeedbackSection.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                    layoutFeedbackSection.setVisibility(View.GONE);
                });
                root.btnToggleFeedback.setText("View Latest Feedback Slip");
            }
        });

        //Previous referrals button
        root.btnViewPreviousReferrals.setOnClickListener(v -> loadAndShowPreviousReferrals());

        //Submit button
        root.btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                ReferralForm form = buildReferralForm();
                submitReferralForm(form); //this will call Retrofit
            }
        });


        //Before Date Chip Date Picker
        root.tvBeforeDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    ReferralFormActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        root.tvBeforeDate.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            //Only allows today’s date and future
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });


        //Calling the Academic Level mutual exclusivity toggle method
        setupAcademicLevelToggle();

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.referral_form_gender_options,
                android.R.layout.simple_dropdown_item_1line //for AutoCompleteTextView
        );
        root.edtGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> programAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.referral_form_program_options,
                android.R.layout.simple_dropdown_item_1line //for AutoCompleteTextView
        );
        root.edtProgram.setAdapter(programAdapter);
    }

    private void loadAndShowPreviousReferrals() {
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1);
        if (studentId <= 0) {
            Toast.makeText(this, "Missing student session.", Toast.LENGTH_SHORT).show();
            return;
        }

        ReferralApi api = ApiClient.getReferralFormApi();
        api.getAllReferrals(studentId).enqueue(new Callback<List<ReferralForm>>() {
            @Override public void onResponse(Call<List<ReferralForm>> call, Response<List<ReferralForm>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(ReferralFormActivity.this, "No previous referrals found.", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<ReferralForm> list = resp.body();
                if (list.isEmpty()) {
                    Toast.makeText(ReferralFormActivity.this, "No previous referrals found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Optional: sort client-side just in case
                list.sort((a, b) -> safeDateStr(b.getSubmissionDate()).compareTo(safeDateStr(a.getSubmissionDate())));

                List<java.util.Map<String, String>> data = new ArrayList<>();
                for (ReferralForm r : list) {
                    String date = safeDate(r.getDateReferred());      // uses yyyy-MM-dd
                    String rel  = relativeDays(r.getDateReferred());  // Manila TZ
                    boolean hasFeedback =
                            notEmpty(r.getCounselorName()) ||
                                    notEmpty(r.getCounselorFeedbackDateReferred()) ||
                                    notEmpty(r.getCounselorActionsTaken());

                    String title = String.format(Locale.getDefault(),
                            "%s %s • Feedback: %s",
                            date, rel, hasFeedback ? "Yes" : "None");

                    String reason = trimForList(r.getReferralReasons());
                    String sub = reason.equals("-") ? "(No reason provided)" : reason;

                    java.util.Map<String, String> row = new java.util.HashMap<>();
                    row.put("title", title);
                    row.put("subtitle", sub);
                    data.add(row);
                }

                android.widget.SimpleAdapter adapter = new android.widget.SimpleAdapter(
                        ReferralFormActivity.this,
                        data,
                        android.R.layout.simple_list_item_2,
                        new String[] { "title", "subtitle" },
                        new int[] { android.R.id.text1, android.R.id.text2 }
                );

                new androidx.appcompat.app.AlertDialog.Builder(ReferralFormActivity.this)
                        .setTitle("Previous Referrals")
                        .setAdapter(adapter, (dialog, which) -> {
                            ReferralForm selected = list.get(which);
                            populateFeedbackSection(selected);
                            View layoutFeedbackSection = findViewById(R.id.layoutFeedbackSection);
                            if (layoutFeedbackSection.getVisibility() == View.GONE) {
                                root.btnToggleFeedback.performClick();
                            }
                        })
                        .setPositiveButton("Close", null)
                        .show();
            }
            @Override public void onFailure(Call<List<ReferralForm>> call, Throwable t) {
                Toast.makeText(ReferralFormActivity.this, "Failed to load previous referrals.", Toast.LENGTH_SHORT).show();
                Log.e("REFERRAL_FETCH_ALL", t.getMessage(), t);
            }
        });
    }

    private boolean notEmpty(String s) { return s != null && !s.trim().isEmpty(); }
    private String safeDateStr(String iso) { return iso == null ? "" : iso; }
    private String safeDate(String iso) {
        if (iso == null) return "-";
        try {
            java.text.SimpleDateFormat input = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.text.SimpleDateFormat output = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            output.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
            Date date = input.parse(iso.substring(0, Math.min(10, iso.length())));
            return output.format(date);
        } catch (Exception e) {
            return iso.length() >= 10 ? iso.substring(0,10) : iso;
        }
    }
    private String relativeDays(String iso) {
        try {
            if (iso == null) return "";
            String d = safeDate(iso);
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            f.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
            Date dt = f.parse(d);
            if (dt == null) return "";

            // Get current Manila time
            java.util.Calendar now = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Manila"));
            java.util.Calendar then = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Manila"));
            then.setTime(dt);

            long days = (now.getTimeInMillis() - then.getTimeInMillis()) / (1000*60*60*24);
            if (days == 0) return "(today)";
            if (days == 1) return "(1d ago)";
            return "(" + days + "d ago)";
        } catch (Exception e) { return ""; }
    }
    private String trimForList(String s) {
        if (s == null) return "-";
        s = s.trim().replaceAll("\\s+", " ");
        if (s.length() > 80) return s.substring(0, 77) + "...";
        return s;
    }
    private String nullToDash(String s) { return s == null || s.trim().isEmpty() ? "-" : s; }

    private void populateFeedbackSection(ReferralForm ref) {
        String fbName = ref.getCounselorFeedbackStudentName();
        if (fbName == null || fbName.trim().isEmpty()) fbName = ref.getFullName();
        root.edtStudentName.setText(nullToDash(fbName));
        root.edtDateReferred.setText(formatIsoDate(ref.getCounselorFeedbackDateReferred()));
        root.edtSessionDate.setText(formatIsoDate(ref.getCounselorSessionDate()));
        TextView actionsTakenView = findViewById(R.id.actionsTakenTextView);
        if (actionsTakenView != null) actionsTakenView.setText(nullToDash(ref.getCounselorActionsTaken()));
        root.edtCounselorName.setText(nullToDash(ref.getCounselorName()));
    }

    private void setupDatePicker(EditText editText, TextInputLayout layout) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ReferralFormActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editText.setText(formattedDate);
                        layout.setError(null); // clear error
                    },
                    year, month, day
            );

            //Limit to only today
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

            datePickerDialog.show();
        });
    }

    private void setupLiveValidation() {
        //Text fields
        clearErrorOnTextChanged(root.edtFullName, root.layoutFullName);
        clearErrorOnTextChanged(root.edtStudentNumber, root.layoutStudentNumber);
        clearErrorOnTextChanged(root.edtProgram, root.layoutProgram);
        clearErrorOnTextChanged(root.edtAge, root.layoutAge);
        clearErrorOnTextChanged(root.edtGender, root.layoutGender);
        clearErrorOnTextChanged(root.edtActionsTakenBefore, root.layoutActionsTakenBefore);
        clearErrorOnTextChanged(root.edtReferralReasons, root.layoutReferralReasons);
        clearErrorOnTextChanged(root.edtCounselorInitialAction, root.layoutCounselorInitialAction);
        clearErrorOnTextChanged(root.txtPersonWhoReferred, null); // plain EditText
        clearErrorOnTextChanged(root.txtDateReferred, null); // plain EditText

        //ChipGroups
        clearErrorOnChipSelection(root.chipGroupReferredBy, root.tvChipReferredByError);
        clearErrorOnChipSelection(root.chipGroupAreasOfConcern, root.tvChipAreasOfConcernError);
        clearErrorOnChipSelection(root.chipGroupActionRequested, root.tvChipActionRequestedError);
        clearErrorOnChipSelection(root.chipGroupPriorityLevel, root.tvChipPriorityLevelError);
    }

    //For real-time Student No. and Age validation
    private void setupRealTimeValidation() {
        //Student Number: only digits, max 11
        root.edtStudentNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().matches("\\d{11}")) {
                    root.layoutStudentNumber.setError(null);
                } else {
                    root.layoutStudentNumber.setError("Must be exactly 11 digits");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        //Age: only numbers allowed
        root.edtAge.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().matches("\\d+")) {
                    root.layoutAge.setError(null);
                } else {
                    root.layoutAge.setError("Only numeric value allowed");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }


    private boolean validateForm() {
        boolean isValid = true;

        //Validate Academic Level
        if (!validateAcademicLevel()) isValid = false;

        //Name
        if (isEmpty(root.edtFullName)) {
            root.layoutFullName.setError("Required");
            isValid = false;
        } else {
            root.layoutFullName.setError(null);
        }

        //Student Number
        String studentNumber = root.edtStudentNumber.getText().toString().trim();
        if (studentNumber.isEmpty()) {
            root.layoutStudentNumber.setError("Required");
            isValid = false;
        } else if (!studentNumber.matches("\\d{11}")) {
            root.layoutStudentNumber.setError("Must be exactly 11 digits");
            isValid = false;
        } else {
            root.layoutStudentNumber.setError(null);
        }

        //Program
        if (isEmpty(root.edtProgram)) {
            root.layoutProgram.setError("Required");
            isValid = false;
        } else {
            root.layoutProgram.setError(null);
        }

        // Age
        String ageText = root.edtAge.getText().toString().trim();
        if (ageText.isEmpty()) {
            root.layoutAge.setError("Required");
            isValid = false;
        } else if (ageText.length() > 0 && (Integer.parseInt(ageText) < 10 || Integer.parseInt(ageText) > 99)) {
            root.layoutAge.setError("Enter valid age (10–99)");
            isValid = false;
        } else {
            root.layoutAge.setError(null);
        }

        //Gender
        if (isEmpty(root.edtGender)) {
            root.layoutGender.setError("Required");
            isValid = false;
        } else {
            root.layoutGender.setError(null);
        }

        //Actions Taken Before Referral
        if (isEmpty(root.edtActionsTakenBefore)) {
            root.layoutActionsTakenBefore.setError("Required");
            isValid = false;
        } else {
            root.layoutActionsTakenBefore.setError(null);
        }

        //Reason for Referral
        if (isEmpty(root.edtReferralReasons)) {
            root.layoutReferralReasons.setError("Required");
            isValid = false;
        } else {
            root.layoutReferralReasons.setError(null);
        }

        //Counselor's Initial Action
        if (isEmpty(root.edtCounselorInitialAction)) {
            root.layoutCounselorInitialAction.setError("Required");
            isValid = false;
        } else {
            root.layoutCounselorInitialAction.setError(null);
        }

        //Person Who Referred
        if (isEmpty(root.txtPersonWhoReferred)) {
            root.txtPersonWhoReferred.setError("Required");
            isValid = false;
        } else {
            root.txtPersonWhoReferred.setError(null);
        }

        //Date Referred
        if (isEmpty(root.txtDateReferred)) {
            root.txtDateReferred.setError("Required");
            isValid = false;
        } else {
            root.txtDateReferred.setError(null);
        }

        //ChipGroup: Referred By
        if (!isChipGroupChecked(root.chipGroupReferredBy)) {
            root.tvChipReferredByError.setVisibility(View.VISIBLE);
            root.tvChipReferredByError.setText("Please select at least one.");
            isValid = false;
        } else {
            root.tvChipReferredByError.setVisibility(View.GONE);
        }

        //ChipGroup: Areas of Concern
        if (!isChipGroupChecked(root.chipGroupAreasOfConcern)) {
            root.tvChipAreasOfConcernError.setVisibility(View.VISIBLE);
            root.tvChipAreasOfConcernError.setText("Please select at least one.");
            isValid = false;
        } else {
            root.tvChipAreasOfConcernError.setVisibility(View.GONE);
        }

        //ChipGroup: Action Requested
        if (!isChipGroupChecked(root.chipGroupActionRequested)) {
            root.tvChipActionRequestedError.setVisibility(View.VISIBLE);
            root.tvChipActionRequestedError.setText("Please select at least one.");
            isValid = false;
        } else {
            root.tvChipActionRequestedError.setVisibility(View.GONE);
        }

        //ChipGroup: Priority Level
        if (!isChipGroupChecked(root.chipGroupPriorityLevel)) {
            root.tvChipPriorityLevelError.setVisibility(View.VISIBLE);
            root.tvChipPriorityLevelError.setText("Please select one.");
            isValid = false;
        } else {
            root.tvChipPriorityLevelError.setVisibility(View.GONE);
        }

        //If "Others" is checked in Areas of Concern, require text
        if (root.chipOthersAOC.isChecked() && isEmpty(root.edtOthersAOC)) {
            root.edtOthersAOC.setError("Please specify");
            isValid = false;
        } else {
            root.edtOthersAOC.setError(null);
        }

        //If "Others" is checked in Action Requested, require text
        if (root.chipOthersAR.isChecked() && isEmpty(root.edtOthersAR)) {
            root.edtOthersAR.setError("Please specify");
            isValid = false;
        } else {
            root.edtOthersAR.setError(null);
        }

        //If "Before this Date" is selected, require date
        if (root.chipScheduledDate.isChecked() && root.tvBeforeDate.getText().toString().equals("Select Date")) {
            root.tvChipPriorityLevelError.setVisibility(View.VISIBLE);
            root.tvChipPriorityLevelError.setText("Please select a date for 'Before this Date'");
            isValid = false;
        } else if (!root.chipScheduledDate.isChecked()) {
            root.tvChipPriorityLevelError.setVisibility(View.GONE);
        }

        return isValid;
    }

    //Logic for handling the Tertiary/Senior High mutual exclusivity
    private void setupAcademicLevelToggle() {
        root.chipGroupSeniorHigh.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                // Disable Tertiary group
                setChipGroupEnabled(root.chipGroupTertiary, false);
            } else {
                // Re-enable Tertiary group
                setChipGroupEnabled(root.chipGroupTertiary, true);
            }
        });

        root.chipGroupTertiary.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                // Disable Senior High group
                setChipGroupEnabled(root.chipGroupSeniorHigh, false);
            } else {
                // Re-enable Senior High group
                setChipGroupEnabled(root.chipGroupSeniorHigh, true);
            }
        });
    }

    //Utility Method to Enable/Disable a Chip Group
    private void setChipGroupEnabled(com.google.android.material.chip.ChipGroup chipGroup, boolean enabled) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View chip = chipGroup.getChildAt(i);
            chip.setEnabled(enabled);
        }
    }

    private boolean validateAcademicLevel() {
        boolean isSeniorHighSelected = !root.chipGroupSeniorHigh.getCheckedChipIds().isEmpty();
        boolean isTertiarySelected = !root.chipGroupTertiary.getCheckedChipIds().isEmpty();

        if (!isSeniorHighSelected && !isTertiarySelected) {
            root.tvChipTertiaryError.setVisibility(View.VISIBLE);
            root.tvChipSeniorHighError.setVisibility(View.VISIBLE);
            return false;
        } else {
            root.tvChipTertiaryError.setVisibility(View.GONE);
            root.tvChipSeniorHighError.setVisibility(View.GONE);
            return true;
        }
    }

    private ReferralForm buildReferralForm() {
        ReferralForm form = new ReferralForm();

        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1);
        form.setStudentId(studentId);

        form.setFullName(root.edtFullName.getText().toString().trim());
        form.setStudentNumber(root.edtStudentNumber.getText().toString().trim());
        form.setProgram(root.edtProgram.getText().toString().trim());
        form.setAge(Integer.parseInt(root.edtAge.getText().toString().trim()));
        form.setGender(root.edtGender.getText().toString().trim());

        form.setAcademicLevel(getSelectedAcademicLevel());
        form.setReferredBy(getCheckedChipsAsString(root.chipGroupReferredBy));
        form.setAreasOfConcern(getCheckedChipsAsString(root.chipGroupAreasOfConcern));
        form.setAreasOfConcernOtherDetail(root.edtOthersAOC.getText().toString().trim());
        form.setActionRequested(getCheckedChipsAsString(root.chipGroupActionRequested));
        form.setActionRequestedOtherDetail(root.edtOthersAR.getText().toString().trim());
        form.setPriorityLevel(getCheckedChipsAsString(root.chipGroupPriorityLevel));

        if (root.chipScheduledDate.isChecked()) {
            form.setPriorityDate(root.tvBeforeDate.getText().toString().trim());
        }

        form.setActionsTakenBefore(root.edtActionsTakenBefore.getText().toString().trim());
        form.setReferralReasons(root.edtReferralReasons.getText().toString().trim());
        form.setCounselorInitialAction(root.edtCounselorInitialAction.getText().toString().trim());
        form.setPersonWhoReferred(root.txtPersonWhoReferred.getText().toString().trim());
        form.setDateReferred(root.txtDateReferred.getText().toString().trim());

        form.setSubmissionDate(currentDate());

        form.setCounselorFeedbackStudentName(null);
        form.setCounselorFeedbackDateReferred(null);
        form.setCounselorSessionDate(null);
        form.setCounselorActionsTaken(null);
        form.setCounselorName(null);

        return form;
    }


    //Helper method for getting chips as string
    private String getCheckedChipsAsString(ChipGroup group) {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            if (chip.isChecked()) {
                selected.add(chip.getText().toString());
            }
        }
        return TextUtils.join(", ", selected);
    }

    private void submitReferralForm(ReferralForm form) {
        Log.d("REFERRAL_SUBMIT", "Sending ReferralForm: " + new Gson().toJson(form));
        ReferralApi apiService = ApiClient.getReferralFormApi();
        Call<ResponseBody> call = apiService.submitReferralForm(form);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ReferralFormActivity.this, "Form submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // or navigate elsewhere
                } else {
                    //Try to print backend error message (like "Student ID does not exist")
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("REFERRAL_SUBMIT", "Submission failed: " + response.code());
                        Log.e("REFERRAL_SUBMIT", "Error body: " + errorBody);
                        Toast.makeText(ReferralFormActivity.this, "Submission failed: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e("REFERRAL_SUBMIT", "Error reading errorBody", e);
                        Toast.makeText(ReferralFormActivity.this, "Submission failed, and errorBody could not be read.", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("REFERRAL_SUBMIT", "Network error: " + t.getMessage());
                Toast.makeText(ReferralFormActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private String currentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getSelectedAcademicLevel() {
        List<Integer> checkedSH = root.chipGroupSeniorHigh.getCheckedChipIds();
        List<Integer> checkedTertiary = root.chipGroupTertiary.getCheckedChipIds();

        if (!checkedSH.isEmpty()) {
            Chip chip = root.chipGroupSeniorHigh.findViewById(checkedSH.get(0));
            return chip.getText().toString();
        } else if (!checkedTertiary.isEmpty()) {
            Chip chip = root.chipGroupTertiary.findViewById(checkedTertiary.get(0));
            return chip.getText().toString();
        }
        return "";
    }


    //Helper method for text input fields
    private void clearErrorOnTextChanged(android.widget.EditText editText, com.google.android.material.textfield.TextInputLayout layout) {
        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout != null) {
                    layout.setError(null);
                } else {
                    editText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    //Helper method for Chip Groups
    private void clearErrorOnChipSelection(com.google.android.material.chip.ChipGroup chipGroup, android.widget.TextView errorTextView) {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                errorTextView.setVisibility(View.GONE);
            }
        });
    }

    //Checking if EditText or AutoCompleteTextView is empty
    private boolean isEmpty(android.widget.TextView field) {
        return field.getText().toString().trim().isEmpty();
    }

    //Showing error on TextInputLayout
    private void showError(com.google.android.material.textfield.TextInputLayout layout, String message) {
        layout.setError(message);
        layout.requestFocus();
    }

    //Validate ChipGroup (must have at least one checked chip)
    private boolean isChipGroupChecked(com.google.android.material.chip.ChipGroup chipGroup) {
        return chipGroup.getCheckedChipIds().size() > 0;
    }

    private void loadLatestReferralAndFeedback() {
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1);
        if (studentId <= 0) return;

        ReferralApi apiService = ApiClient.getReferralFormApi();
        apiService.getLatestReferral(studentId).enqueue(new Callback<ReferralForm>() {
            @Override public void onResponse(Call<ReferralForm> call, Response<ReferralForm> response) {
                if (!response.isSuccessful() || response.body() == null) { return; }
                ReferralForm ref = response.body();

                // Fill feedback section (read-only)
                String fbName = ref.getCounselorFeedbackStudentName();
                if (fbName == null || fbName.trim().isEmpty()) {
                    fbName = ref.getFullName();
                }
                root.edtStudentName.setText(nullToDash(fbName));
                root.edtDateReferred.setText(formatIsoDate(ref.getCounselorFeedbackDateReferred()));
                root.edtSessionDate.setText(formatIsoDate(ref.getCounselorSessionDate()));
                // The “Actions Taken” is shown as a centered TextView (“-” initially)
                // Update that TextView:
                TextView actionsTakenView = findViewById(R.id.actionsTakenTextView /* set an id on the "-" TextView */);
                if (actionsTakenView != null) {
                    actionsTakenView.setText(nullToDash(ref.getCounselorActionsTaken()));
                }
                root.edtCounselorName.setText(nullToDash(ref.getCounselorName()));
            }
            @Override public void onFailure(Call<ReferralForm> call, Throwable t) {
                Log.e("REFERRAL_FETCH", "Failed to get latest referral: " + t.getMessage());
            }
        });
    }

    private String formatIsoDate(String iso) {
        return iso == null || iso.isEmpty() ? "-" : iso.substring(0, Math.min(10, iso.length()));
    }

}