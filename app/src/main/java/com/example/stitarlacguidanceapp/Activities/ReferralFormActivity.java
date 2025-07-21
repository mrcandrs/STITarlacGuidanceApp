package com.example.stitarlacguidanceapp.Activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivityReferralFormBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

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
            } else {
                layoutFeedbackSection.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                    layoutFeedbackSection.setVisibility(View.GONE);
                });
                root.btnToggleFeedback.setText("View Latest Feedback Slip");
            }
        });

        //Submit button
        root.btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                // TODO: Proceed with submission (next step)
                Snackbar.make(root.getRoot(), "Form Successfully Submitted.", Snackbar.LENGTH_LONG).show();
            }
        });

        //Before Date Chip Date Picker
        root.tvBeforeDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    ReferralFormActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String selectedDate = (month + 1) + "/" + dayOfMonth + "/" + year;
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

}