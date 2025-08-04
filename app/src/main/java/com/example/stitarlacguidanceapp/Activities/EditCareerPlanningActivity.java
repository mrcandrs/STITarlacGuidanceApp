package com.example.stitarlacguidanceapp.Activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.CareerPlanningApi;
import com.example.stitarlacguidanceapp.Models.CareerPlanningForm;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.StudentApi;
import com.example.stitarlacguidanceapp.databinding.ActivityEditCareerPlanningBinding;
import com.example.stitarlacguidanceapp.databinding.ActivityStudentDashboardBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCareerPlanningActivity extends AppCompatActivity {

    private ActivityEditCareerPlanningBinding root;

    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityEditCareerPlanningBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        int studentId = getIntent().getIntExtra("studentId", -1);
        Log.d("CareerPlanningForm", "studentId from Intent: " + studentId);
        loadCareerPlanningForm(studentId);

        scrollView = root.editCareerPlanningScrollView;

        if (root.fullNameInput != null) {
            root.fullNameInput.setEnabled(false);
            root.fullNameInput.setBackgroundColor(Color.parseColor("#696969"));
            root.fullNameInput.setTextColor(Color.LTGRAY);
        }
        if (root.studentNoInput != null) {
            root.studentNoInput.setEnabled(false);
            root.studentNoInput.setBackgroundColor(Color.parseColor("#696969"));
            root.studentNoInput.setTextColor(Color.LTGRAY);
        }

        //Array Adapters for Spinners/AutoCompleteTextView/RadioButtons
        ArrayAdapter<CharSequence> courseAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.career_form_course_options,
                android.R.layout.simple_dropdown_item_1line //for AutoCompleteTextView
        );
        root.courseInput.setAdapter(courseAdapter);

        // Always show all items on dropdown
        root.courseInput.setOnClickListener(v -> {
            root.courseInput.showDropDown();
        });

        root.courseInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                root.courseInput.showDropDown();
            }
        });

        // Disable filtering based on typed text
        root.courseInput.setThreshold(0); // Show dropdown with 0 characters


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

        root.birthdayInput.setFocusable(false); // Prevent manual keyboard input
        root.birthdayInput.setOnClickListener(v -> showDatePicker());

        setupListeners();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(selectedYear, selectedMonth, selectedDay);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - selectedYear;
            if (today.get(Calendar.DAY_OF_YEAR) < selectedDate.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            if (age >= 17) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                root.birthdayInput.setText(sdf.format(selectedDate.getTime()));
            } else {
                Toast.makeText(this, "Student must be at least 17 years old.", Toast.LENGTH_SHORT).show();
            }
        }, year, month, day);

        //Limit max date to today - 17 years
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -17);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
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
                CareerPlanningForm form = buildUpdatedFormFromInputs();
                CareerPlanningApi api = ApiClient.getClient().create(CareerPlanningApi.class);
                api.updateCareerPlanningForm(form.studentId, form).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            //Hide keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            View view = getCurrentFocus();
                            if (imm != null && view != null) {
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                            Snackbar.make(root.getRoot(), "Successfully updated.", Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(ContextCompat.getColor(EditCareerPlanningActivity.this, R.color.blue))
                                    .setTextColor(Color.WHITE)
                                    .show();

                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                finish();
                            }, 2000);

                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                Log.e("CareerPlanningForm", "Update failed: Code=" + response.code()
                                        + ", Message=" + response.message()
                                        + ", ErrorBody=" + errorBody);

                                Toast.makeText(EditCareerPlanningActivity.this, "Update failed: " + response.message(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e("CareerPlanningForm", "Error reading error body", e);
                                Toast.makeText(EditCareerPlanningActivity.this, "Update failed (IO error)", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(EditCareerPlanningActivity.this, "Error updating", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;
        View firstInvalidField = null;

        String studentNo = root.studentNoInput.getText().toString().trim();
        String program = root.courseInput.getText().toString().trim();
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
            root.courseInput.setError("Required");
            if (firstInvalidField == null) firstInvalidField = root.courseInput;
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

        // Scroll to the first invalid field
        if (!isValid && firstInvalidField != null && scrollView != null) {
            final View targetView = firstInvalidField; // make effectively final
            scrollView.post(() -> {
                int offset = targetView.getTop() - 60;
                scrollView.smoothScrollTo(0, Math.max(offset, 0));
                targetView.requestFocus();
            });
        }

        return isValid;
    }

    private void loadCareerPlanningForm(int studentId) {
        CareerPlanningApi api = ApiClient.getClient().create(CareerPlanningApi.class);
        Call<CareerPlanningForm> call = api.getCareerPlanningForm(studentId);
        call.enqueue(new Callback<CareerPlanningForm>() {
            @Override
            public void onResponse(Call<CareerPlanningForm> call, Response<CareerPlanningForm> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CareerPlanningForm form = response.body();
                    Log.d("CareerPlanningForm", "Received: " + new Gson().toJson(form)); // <-- log content
                    populateFields(form);
                }  else {
                    Log.e("CareerPlanningForm", "Error: " +
                            "Code=" + response.code() +
                            ", Message=" + response.message());

                    try {
                        if (response.errorBody() != null) {
                            Log.e("CareerPlanningForm", "Error Body: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e("CareerPlanningForm", "Error reading error body", e);
                    }
                }
            }


            @Override
            public void onFailure(Call<CareerPlanningForm> call, Throwable t) {
                Log.e("CareerPlanningForm", "Update failed", t);
                Toast.makeText(EditCareerPlanningActivity.this, "Error loading form.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private CareerPlanningForm buildUpdatedFormFromInputs() {
        CareerPlanningForm form = new CareerPlanningForm();

        // Personal Info
        form.setFullName(root.fullNameInput.getText().toString().trim());
        form.setStudentNo(root.studentNoInput.getText().toString().trim());
        form.setProgram(root.courseInput.getText().toString().trim());
        form.setContactNumber(root.contactNumberInput.getText().toString().trim());
        form.setBirthday(root.birthdayInput.getText().toString().trim());
        form.setSection(root.sectionInput.getText().toString().trim());

        form.setGender(root.genderSpinner.getSelectedItem().toString());
        form.setGradeYear(root.gradeYearSpinner.getSelectedItem().toString());

        // self-assessment
        form.setTopValue1(root.topValue1.getText().toString().trim());
        form.setTopValue2(root.topValue2.getText().toString().trim());
        form.setTopValue3(root.topValue3.getText().toString().trim());

        form.setTopStrength1(root.topStrength1.getText().toString().trim());
        form.setTopStrength2(root.topStrength2.getText().toString().trim());
        form.setTopStrength3(root.topStrength3.getText().toString().trim());

        form.setTopSkill1(root.topSkill1.getText().toString().trim());
        form.setTopSkill2(root.topSkill2.getText().toString().trim());
        form.setTopSkill3(root.topSkill3.getText().toString().trim());

        form.setTopInterest1(root.topInterest1.getText().toString().trim());
        form.setTopInterest2(root.topInterest2.getText().toString().trim());
        form.setTopInterest3(root.topInterest3.getText().toString().trim());

        // Career Choices
        form.setProgramChoice(root.programChoiceInput.getText().toString().trim());
        form.setFirstChoice(root.firstChoiceYes.isChecked() ? "Yes" : "No");
        form.setOriginalChoice(root.originalChoiceInput.getText().toString().trim());
        form.setProgramExpectation(root.programExpectationInput.getText().toString().trim());
        form.setEnrollmentReason(root.enrollmentReasonInput.getText().toString().trim());
        form.setFutureVision(root.futureVisionInput.getText().toString().trim());

        // Ensure all required fields are explicitly set
        form.setNatureJob1(root.editNatureOfJob1.getText().toString().trim()); // May be empty if not applicable
        form.setNatureJob2(root.editNatureOfJob2.getText().toString().trim()); // Required even if not selected
        form.setCourseField(root.editCourseField.getText().toString().trim());
        form.setBusinessNature(root.editNatureOfBusiness.getText().toString().trim());
        form.setEmploymentNature("");
        form.setCurrentWorkNature("");
        form.setProgramChoiceReason("");


        // Plans After Graduation

        int selectedId = root.mainPlanRadioGroup.getCheckedRadioButtonId();
        String mainPlan = "";

        if (selectedId == R.id.radio_continue_schooling) {
            mainPlan = "Continue Schooling";
            form.setAnotherCourse(root.checkboxAnotherCourse.isChecked());
            form.setMastersProgram(root.checkboxMastersProgram.isChecked());
            form.setCourseField(root.editCourseField.getText().toString().trim());
        } else if (selectedId == R.id.radio_get_employed) {
            mainPlan = "Get Employed";
            form.setLocalEmployment(root.checkboxLocalEmployment.isChecked());
            form.setWorkAbroad(root.checkboxWorkAbroad.isChecked());
            form.setNatureJob1(root.editNatureOfJob1.getText().toString().trim());
        } else if (selectedId == R.id.radio_continue_work) {
            mainPlan = "Continue Current Work";
            form.setAimPromotion(root.checkboxAimPromotion.isChecked());
            form.setCurrentWorkAbroad(root.checkboxCurrentWorkAbroad.isChecked());
            form.setNatureJob2(root.editNatureOfJob2.getText().toString().trim());
        } else if (selectedId == R.id.radio_go_business) {
            mainPlan = "Go Into Business";
            form.setBusinessNature(root.editNatureOfBusiness.getText().toString().trim());
        }

        form.setMainPlan(mainPlan);

        // Also set the studentId if retrieved from SharedPreferences or Intent
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1); // -1 is default if not found
        Log.d("CareerPlanningForm", "studentId from SharedPreferences: " + studentId);
        form.setStudentId(studentId);
        return form;
    }


    private void populateFields(CareerPlanningForm form) {
        root.studentNoInput.setText(form.studentNo);
        Log.d("Bind", form.program);
        root.courseInput.setText(form.program);
        root.fullNameInput.setText(form.fullName);
        root.contactNumberInput.setText(form.contactNumber);
        root.birthdayInput.setText(form.birthday);
        root.sectionInput.setText(form.section);

        // You can set gradeYear & gender Spinners like:
        setSpinnerSelection(root.gradeYearSpinner, form.gradeYear);
        setSpinnerSelection(root.genderSpinner, form.gender);

        root.topValue1.setText(form.topValue1);
        root.topValue2.setText(form.topValue2);
        root.topValue3.setText(form.topValue3);

        root.topStrength1.setText(form.topStrength1);
        root.topStrength2.setText(form.topStrength2);
        root.topStrength3.setText(form.topStrength3);

        root.topSkill1.setText(form.topSkill1);
        root.topSkill2.setText(form.topSkill2);
        root.topSkill3.setText(form.topSkill3);

        root.topInterest1.setText(form.topInterest1);
        root.topInterest2.setText(form.topInterest2);
        root.topInterest3.setText(form.topInterest3);

        root.programChoiceInput.setText(form.programChoice);
        root.programExpectationInput.setText(form.programExpectation);
        root.enrollmentReasonInput.setText(form.enrollmentReason);
        root.futureVisionInput.setText(form.futureVision);

        if ("Yes".equalsIgnoreCase(form.firstChoice)) {
            root.firstChoiceYes.setChecked(true);
        } else {
            root.firstChoiceNo.setChecked(true);
            root.originalChoiceInput.setVisibility(View.VISIBLE);
            root.originalChoiceInput.setText(form.originalChoice);
        }

        // mainPlan = Continue Schooling / Get Employed / etc.
        switch (form.mainPlan) {
            case "Continue Schooling":
                root.radioContinueSchooling.setChecked(true);
                root.schoolingOptions.setVisibility(View.VISIBLE);
                root.checkboxAnotherCourse.setChecked(form.anotherCourse);
                root.checkboxMastersProgram.setChecked(form.mastersProgram);
                root.editCourseField.setText(form.courseField);
                break;
            case "Get Employed":
                root.radioGetEmployed.setChecked(true);
                root.employedOptions.setVisibility(View.VISIBLE);
                root.checkboxLocalEmployment.setChecked(form.localEmployment);
                root.checkboxCurrentWorkAbroad.setChecked(form.workAbroad);
                root.editNatureOfJob1.setText(form.natureJob1);
                break;
            case "Continue Current Work":
                root.radioContinueWork.setChecked(true);
                root.currentWorkOptions.setVisibility(View.VISIBLE);
                root.checkboxAimPromotion.setChecked(form.aimPromotion);
                root.checkboxCurrentWorkAbroad.setChecked(form.currentWorkAbroad);
                root.editNatureOfJob2.setText(form.natureJob2);
                break;
            case "Go Into Business":
                root.radioGoBusiness.setChecked(true);
                root.businessOptions.setVisibility(View.VISIBLE);
                root.editNatureOfBusiness.setText(form.businessNature);
                break;
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
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