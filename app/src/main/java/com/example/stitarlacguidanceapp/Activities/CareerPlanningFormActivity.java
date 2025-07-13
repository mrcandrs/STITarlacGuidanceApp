package com.example.stitarlacguidanceapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivityCareerPlanningFormBinding;

public class CareerPlanningFormActivity extends AppCompatActivity {

    private ActivityCareerPlanningFormBinding root;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "CareerPlanningPrefs";

    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityCareerPlanningFormBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        scrollView = findViewById(R.id.careerPlanningScrollView);

        Intent intent = getIntent();

        String fullName = intent.getStringExtra("FullName");
        String studentNumber = intent.getStringExtra("StudentNumber");
        String birthday = intent.getStringExtra("Birthday");
        String contactNumber = intent.getStringExtra("ContactNumber");

        if (fullName != null) {
            root.fullNameInput.setText(fullName);
            root.fullNameInput.setEnabled(false);
            root.fullNameInput.setBackgroundColor(Color.parseColor("#696969"));
            root.fullNameInput.setTextColor(Color.LTGRAY);
        }
        if (studentNumber != null) {
            root.studentNoInput.setText(studentNumber);
            root.studentNoInput.setEnabled(false);
            root.studentNoInput.setBackgroundColor(Color.parseColor("#696969"));
            root.studentNoInput.setTextColor(Color.LTGRAY);
        }
        if (birthday != null) {
            root.birthdayInput.setText(birthday);
            root.birthdayInput.setEnabled(false);
            root.birthdayInput.setBackgroundColor(Color.parseColor("#696969"));
            root.birthdayInput.setTextColor(Color.LTGRAY);
        }
        if (contactNumber != null) {
            root.contactNumberInput.setText(contactNumber);
            root.contactNumberInput.setEnabled(false);
            root.contactNumberInput.setBackgroundColor(Color.parseColor("#696969"));
            root.contactNumberInput.setTextColor(Color.LTGRAY);
        }

        // Set the values to your EditTexts (replace with your binding or findViewById)
        root.studentNoInput.setText(studentNumber);
        root.fullNameInput.setText(fullName);
        root.contactNumberInput.setText(contactNumber);
        root.birthdayInput.setText(birthday);

        // Disable those fields so user cannot edit
        root.studentNoInput.setEnabled(false);
        root.fullNameInput.setEnabled(false);
        root.contactNumberInput.setEnabled(false);
        root.birthdayInput.setEnabled(false);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        setupListeners();

        ArrayAdapter<CharSequence> gradeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.grade_year_options,
                android.R.layout.simple_spinner_item
        );
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        root.gradeYearSpinner.setAdapter(gradeAdapter);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.career_form_gender_options,
                android.R.layout.simple_spinner_item
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        root.genderSpinner.setAdapter(genderAdapter);

        root.firstChoiceGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == root.firstChoiceNo.getId()) {
                root.originalChoiceInput.setVisibility(View.VISIBLE);
            } else {
                root.originalChoiceInput.setVisibility(View.GONE);
                root.originalChoiceInput.setText("");  // Optional: clear text when hidden
                root.originalChoiceInput.setError(null); // clear errors when hidden
            }
        });




    }

    private void setupListeners() {
        root.mainPlanRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            root.schoolingOptions.setVisibility(View.GONE);
            root.employedOptions.setVisibility(View.GONE);
            root.currentWorkOptions.setVisibility(View.GONE);
            root.businessOptions.setVisibility(View.GONE);

            // Clear all option inputs first
            clearSchoolingOptions();
            clearEmployedOptions();
            clearCurrentWorkOptions();
            clearBusinessOptions();

            if (checkedId == root.radioContinueSchooling.getId()) {
                root.schoolingOptions.setVisibility(View.VISIBLE);
            } else if (checkedId == root.radioGetEmployed.getId()) {
                root.employedOptions.setVisibility(View.VISIBLE);
            } else if (checkedId == root.radioContinueWork.getId()) {
                root.currentWorkOptions.setVisibility(View.VISIBLE);
            } else if (checkedId == root.radioGoBusiness.getId()) {
                root.businessOptions.setVisibility(View.VISIBLE);
            }
        });

        root.btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                Toast.makeText(this, "Career Planning Form saved!", Toast.LENGTH_SHORT).show();
                Intent registrationIntent = new Intent(this, RegistrationActivity.class);

                //Pass data
                registrationIntent.putExtra("StudentNumber", root.studentNoInput.getText().toString().trim());
                registrationIntent.putExtra("FullName", root.fullNameInput.getText().toString().trim());
                registrationIntent.putExtra("program", root.programInput.getText().toString().trim());
                registrationIntent.putExtra("gradeYear", root.gradeYearSpinner.getSelectedItem().toString().trim());

                startActivity(registrationIntent);
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;
        View firstInvalidField = null;

        String studentNo = root.studentNoInput.getText().toString().trim();
        String program = root.programInput.getText().toString().trim();
        String fullName = root.fullNameInput.getText().toString().trim();
        String section = root.sectionInput.getText().toString().trim();
        String contact = root.contactNumberInput.getText().toString().trim();
        String birthday = root.birthdayInput.getText().toString().trim();

        if (studentNo.isEmpty()) {
            root.studentNoInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.studentNoInput;
            isValid = false;
        }

        if (program.isEmpty()) {
            root.programInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.programInput;
            isValid = false;
        }

        if (fullName.isEmpty()) {
            root.fullNameInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.fullNameInput;
            isValid = false;
        }

        if (section.isEmpty()) {
            root.sectionInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.sectionInput;
            isValid = false;
        }

        if (contact.isEmpty()) {
            root.contactNumberInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.contactNumberInput;
            isValid = false;
        } else if (contact.length() < 10) {
            root.contactNumberInput.setError("Invalid contact number");
            if (firstInvalidField == null) firstInvalidField = root.contactNumberInput;
            isValid = false;
        }

        if (birthday.isEmpty()) {
            root.birthdayInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.birthdayInput;
            isValid = false;
        }

        if (root.gradeYearSpinner.getSelectedItem().toString().equals("Grade/Year")) {
            Toast.makeText(this, "Please select a valid Grade/Year", Toast.LENGTH_SHORT).show();
            if (firstInvalidField == null) firstInvalidField = root.gradeYearSpinner;
            isValid = false;
        }

        if (root.genderSpinner.getSelectedItem().toString().equals("Gender")) {
            Toast.makeText(this, "Please select a valid Gender", Toast.LENGTH_SHORT).show();
            if (firstInvalidField == null) firstInvalidField = root.genderSpinner;
            isValid = false;
        }

        if (root.topValue1.getText().toString().trim().isEmpty()) {
            root.topValue1.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.topValue1;
            isValid = false;
        }

        if (root.topStrength1.getText().toString().trim().isEmpty()) {
            root.topStrength1.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.topStrength1;
            isValid = false;
        }

        if (root.topSkill1.getText().toString().trim().isEmpty()) {
            root.topSkill1.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.topSkill1;
            isValid = false;
        }

        if (root.topInterest1.getText().toString().trim().isEmpty()) {
            root.topInterest1.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.topInterest1;
            isValid = false;
        }

        if (root.programChoiceInput.getText().toString().trim().isEmpty()) {
            root.programChoiceInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.programChoiceInput;
            isValid = false;
        }

        int firstChoiceId = root.firstChoiceGroup.getCheckedRadioButtonId();
        if (firstChoiceId == -1) {
            Toast.makeText(this, "Please select whether this was your first choice", Toast.LENGTH_SHORT).show();
            if (firstInvalidField == null) firstInvalidField = root.firstChoiceGroup;
            isValid = false;
        } else if (firstChoiceId == root.firstChoiceNo.getId()) {
            if (root.originalChoiceInput.getText().toString().trim().isEmpty()) {
                root.originalChoiceInput.setError("Please enter your original choice");
                if (firstInvalidField == null) firstInvalidField = root.originalChoiceInput;
                isValid = false;
            }
        }

        if (root.programExpectationInput.getText().toString().trim().isEmpty()) {
            root.programExpectationInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.programExpectationInput;
            isValid = false;
        }

        if (root.enrollmentReasonInput.getText().toString().trim().isEmpty()) {
            root.enrollmentReasonInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.enrollmentReasonInput;
            isValid = false;
        }

        if (root.futureVisionInput.getText().toString().trim().isEmpty()) {
            root.futureVisionInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.futureVisionInput;
            isValid = false;
        }

        int mainPlanId = root.mainPlanRadioGroup.getCheckedRadioButtonId();
        if (mainPlanId == -1) {
            Toast.makeText(this, "Please select a plan after graduation", Toast.LENGTH_SHORT).show();
            if (firstInvalidField == null) firstInvalidField = root.mainPlanRadioGroup;
            isValid = false;
        } else {
            if (mainPlanId == root.radioContinueSchooling.getId()) {
                if (!root.checkboxAnotherCourse.isChecked() && !root.checkboxMastersProgram.isChecked()) {
                    Toast.makeText(this, "Please select at least one schooling option", Toast.LENGTH_SHORT).show();
                    if (firstInvalidField == null) firstInvalidField = root.checkboxAnotherCourse;
                    isValid = false;
                }
                if (root.editCourseField.getText().toString().trim().isEmpty()) {
                    root.editCourseField.setError("Please specify course/field");
                    if (firstInvalidField == null) firstInvalidField = root.editCourseField;
                    isValid = false;
                }
            } else if (mainPlanId == root.radioGetEmployed.getId()) {
                if (!root.checkboxLocalEmployment.isChecked() && !root.checkboxWorkAbroad.isChecked()) {
                    Toast.makeText(this, "Please select at least one employment option", Toast.LENGTH_SHORT).show();
                    if (firstInvalidField == null) firstInvalidField = root.checkboxLocalEmployment;
                    isValid = false;
                }
                if (root.editNatureOfJob1.getText().toString().trim().isEmpty()) {
                    root.editNatureOfJob1.setError("Please describe the nature of job");
                    if (firstInvalidField == null) firstInvalidField = root.editNatureOfJob1;
                    isValid = false;
                }
            } else if (mainPlanId == root.radioContinueWork.getId()) {
                if (!root.checkboxAimPromotion.isChecked() && !root.checkboxCurrentWorkAbroad.isChecked()) {
                    Toast.makeText(this, "Please select at least one current work option", Toast.LENGTH_SHORT).show();
                    if (firstInvalidField == null) firstInvalidField = root.checkboxAimPromotion;
                    isValid = false;
                }
                if (root.editNatureOfJob2.getText().toString().trim().isEmpty()) {
                    root.editNatureOfJob2.setError("Please describe the nature of job");
                    if (firstInvalidField == null) firstInvalidField = root.editNatureOfJob2;
                    isValid = false;
                }
            } else if (mainPlanId == root.radioGoBusiness.getId()) {
                if (root.editNatureOfBusiness.getText().toString().trim().isEmpty()) {
                    root.editNatureOfBusiness.setError("Please describe nature of business");
                    if (firstInvalidField == null) firstInvalidField = root.editNatureOfBusiness;
                    isValid = false;
                }
            }
        }

        //Scroll to the first invalid field
        if (!isValid && firstInvalidField != null) {
            final View scrollToView = firstInvalidField;
            scrollView.post(() -> scrollView.smoothScrollTo(0, scrollToView.getTop()));
        }

        return isValid;
    }



    private void saveFormData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Personal Info
        editor.putString("studentNo", root.studentNoInput.getText().toString());
        editor.putString("program", root.programInput.getText().toString());
        editor.putString("fullName", root.fullNameInput.getText().toString());
        editor.putString("gradeYear", root.gradeYearSpinner.getSelectedItem().toString());
        editor.putString("section", root.sectionInput.getText().toString());
        editor.putString("gender", root.genderSpinner.getSelectedItem().toString());
        editor.putString("contact", root.contactNumberInput.getText().toString());
        editor.putString("birthday", root.birthdayInput.getText().toString());

        //Self Assessment
        editor.putString("topValue1", root.topValue1.getText().toString());
        editor.putString("topValue2", root.topValue2.getText().toString());
        editor.putString("topValue3", root.topValue3.getText().toString());

        editor.putString("topStrength1", root.topStrength1.getText().toString());
        editor.putString("topStrength2", root.topStrength2.getText().toString());
        editor.putString("topStrength3", root.topStrength3.getText().toString());

        editor.putString("topSkill1", root.topSkill1.getText().toString());
        editor.putString("topSkill2", root.topSkill2.getText().toString());
        editor.putString("topSkill3", root.topSkill3.getText().toString());

        editor.putString("topInterest1", root.topInterest1.getText().toString());
        editor.putString("topInterest2", root.topInterest2.getText().toString());
        editor.putString("topInterest3", root.topInterest3.getText().toString());

        //Career Choices
        editor.putString("employmentNature", root.editNatureOfJob1.getText().toString());
        editor.putString("currentWorkNature", root.editNatureOfJob2.getText().toString());
        editor.putString("programChoiceReason", root.programChoiceInput.getText().toString()); // if this is intended as a separate explanation

        editor.putString("programChoice", root.programChoiceInput.getText().toString());

        int firstChoiceId = root.firstChoiceGroup.getCheckedRadioButtonId();
        if (firstChoiceId != -1) {
            RadioButton selected = findViewById(firstChoiceId);
            editor.putString("firstChoice", selected.getText().toString());
        }

        editor.putString("originalChoice", root.originalChoiceInput.getText().toString());
        editor.putString("programExpectation", root.programExpectationInput.getText().toString());
        editor.putString("enrollmentReason", root.enrollmentReasonInput.getText().toString());
        editor.putString("futureVision", root.futureVisionInput.getText().toString());

        //Plans After Graduation
        int mainPlanId = root.mainPlanRadioGroup.getCheckedRadioButtonId();
        if (mainPlanId != -1) {
            RadioButton selectedMain = findViewById(mainPlanId);
            editor.putString("mainPlan", selectedMain.getText().toString());
        }

        editor.putBoolean("anotherCourse", root.checkboxAnotherCourse.isChecked());
        editor.putBoolean("mastersProgram", root.checkboxMastersProgram.isChecked());
        editor.putString("courseField", root.editCourseField.getText().toString());

        editor.putBoolean("localEmployment", root.checkboxLocalEmployment.isChecked());
        editor.putBoolean("workAbroad", root.checkboxWorkAbroad.isChecked());
        editor.putString("natureJob1", root.editNatureOfJob1.getText().toString());

        editor.putBoolean("aimPromotion", root.checkboxAimPromotion.isChecked());
        editor.putBoolean("currentWorkAbroad", root.checkboxCurrentWorkAbroad.isChecked());
        editor.putString("natureJob2", root.editNatureOfJob2.getText().toString());

        editor.putString("businessNature", root.editNatureOfBusiness.getText().toString());

        editor.apply();
    }

    private void clearSchoolingOptions() {
        root.checkboxAnotherCourse.setChecked(false);
        root.checkboxMastersProgram.setChecked(false);
        root.editCourseField.setText("");
    }

    private void clearEmployedOptions() {
        root.checkboxLocalEmployment.setChecked(false);
        root.checkboxWorkAbroad.setChecked(false);
        root.editNatureOfJob1.setText("");
    }

    private void clearCurrentWorkOptions() {
        root.checkboxAimPromotion.setChecked(false);
        root.checkboxCurrentWorkAbroad.setChecked(false);
        root.editNatureOfJob2.setText("");
    }

    private void clearBusinessOptions() {
        root.editNatureOfBusiness.setText("");
    }

}