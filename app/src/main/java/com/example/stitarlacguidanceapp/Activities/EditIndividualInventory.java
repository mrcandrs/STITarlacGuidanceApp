package com.example.stitarlacguidanceapp.Activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.CareerPlanningApi;
import com.example.stitarlacguidanceapp.DuplicateCheckResponse;
import com.example.stitarlacguidanceapp.InventoryFormApi;
import com.example.stitarlacguidanceapp.Models.CareerPlanningForm;
import com.example.stitarlacguidanceapp.Models.InventoryForm;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.StudentApi;
import com.example.stitarlacguidanceapp.databinding.ActivityEditIndividualInventoryBinding;
import com.example.stitarlacguidanceapp.databinding.SiblingRowBinding;
import com.example.stitarlacguidanceapp.databinding.WorkRowBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditIndividualInventory extends AppCompatActivity {

    private ActivityEditIndividualInventoryBinding root;
    private int studentId;
    private List<InventoryForm.Sibling> siblingList = new ArrayList<>();
    private List<InventoryForm.WorkExperience> workExperienceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityEditIndividualInventoryBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        studentId = getIntent().getIntExtra("studentId", -1);
        Log.d("IndividualInventoryForm", "studentId from Intent: " + studentId);
        loadIndividualInventoryForm(studentId);

        //Initializations
        if (root.edtFullName != null) {
            root.edtFullName.setEnabled(false);
            root.edtFullName.setBackgroundColor(Color.parseColor("#696969"));
            root.edtFullName.setTextColor(Color.LTGRAY);
        }
        if (root.edtStudentNumber != null) {
            root.edtStudentNumber.setEnabled(false);
            root.edtStudentNumber.setBackgroundColor(Color.parseColor("#696969"));
            root.edtStudentNumber.setTextColor(Color.LTGRAY);
        }

        //Live validation for email 1
        addLiveValidation(root.edtEmail1, input -> {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches() ? null : "Invalid email address";
        });

        //Live validation for email 2
        addLiveValidation(root.edtEmail2, input -> {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches() ? null : "Invalid email address";
        });

        //Live validation for phone number
        addLiveValidation(root.edtPhone, input -> {
            return input.matches("\\d{11}") ? null : "Phone must be 11 digits";
        });

        //Live validation for guardian number
        addLiveValidation(root.edtGuardianContact, input -> {
            return input.matches("\\d{11}") ? null : "Guardian Contact must be 11 digits";
        });

        //Gender AutoCompleteTextView populate
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.inventory_form_gender_options, android.R.layout.simple_dropdown_item_1line);
        root.spinnerGender.setAdapter(genderAdapter);
        root.spinnerGender.setThreshold(0);
        root.spinnerGender.setOnClickListener(v -> root.spinnerGender.showDropDown());

        //Civil status AutoCompleteTextView populate
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this, R.array.civil_status_options, android.R.layout.simple_dropdown_item_1line);
        root.spinnerStatus.setAdapter(statusAdapter);
        root.spinnerStatus.setThreshold(0);
        root.spinnerStatus.setOnClickListener(v -> root.spinnerStatus.showDropDown());

        //Parents status AutoCompleteTextView populate
        ArrayAdapter<CharSequence> parentStatusAdapter = ArrayAdapter.createFromResource(
                this, R.array.parent_status_options, android.R.layout.simple_dropdown_item_1line);
        root.spinnerFatherStatus.setAdapter(parentStatusAdapter);
        root.spinnerFatherStatus.setThreshold(0);
        root.spinnerFatherStatus.setOnClickListener(v -> root.spinnerFatherStatus.showDropDown());
        
        root.spinnerMotherStatus.setAdapter(parentStatusAdapter);
        root.spinnerMotherStatus.setThreshold(0);
        root.spinnerMotherStatus.setOnClickListener(v -> root.spinnerMotherStatus.showDropDown());

        //When married, shows the spouse EditText sections
        root.spinnerStatus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = root.spinnerStatus.getText().toString();
                if (selected.equals("Married")) {
                    root.layoutSpouse.setVisibility(View.VISIBLE);
                } else {
                    root.layoutSpouse.setVisibility(View.GONE);

                    root.edtSpouseName.setText("");
                    root.edtSpouseAge.setText("");
                    root.edtSpouseOccupation.setText("");
                    root.edtSpouseContact.setText("");
                }
            }
        });

        root.btnAddWork.setOnClickListener(v -> addWorkView());
        root.btnAddSibling.setOnClickListener(v -> addSiblingView());

        root.btnSubmit.setOnClickListener(v -> checkDuplicateThenSave());

        root.edtBirthday.setOnClickListener(v -> showBirthdayDatePicker());
        root.edtLastDoctorVisit.setOnClickListener(v -> showLastDoctorVisitDatePicker());

        //Hospitalization checkbox
        root.checkboxHospitalized.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                root.edtHospitalizedReason.setVisibility(View.VISIBLE);
            } else {
                root.edtHospitalizedReason.setText(""); // Clear user input
                root.edtHospitalizedReason.setVisibility(View.GONE);
            }
        });

        //Operation checkbox
        root.checkboxOperation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                root.edtOperationReason.setVisibility(View.VISIBLE);
            } else {
                root.edtOperationReason.setText(""); // Clear user input
                root.edtOperationReason.setVisibility(View.GONE);
            }
        });

        //Current Illness checkbox
        root.checkboxCurrentIllness.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                root.edtCurrentIllness.setVisibility(View.VISIBLE);
            } else {
                root.edtCurrentIllness.setText(""); // Clear user input
                root.edtCurrentIllness.setVisibility(View.GONE);
            }
        });

        //Prescription drugs checkbox
        root.checkboxPrescriptionDrugs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                root.edtPrescriptionDrugs.setVisibility(View.VISIBLE);
            } else {
                root.edtPrescriptionDrugs.setText(""); //Clear user input
                root.edtPrescriptionDrugs.setVisibility(View.GONE);
            }
        });
    }

    private void saveForm() {
        if (!validateForm())
            return;

        InventoryForm form = buildUpdatedFormFromInputs();
        InventoryFormApi api = ApiClient.getClient().create(InventoryFormApi.class);
        api.updateInventory(form.studentId, form).enqueue(new Callback<Void>() {
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
                            .setBackgroundTint(ContextCompat.getColor(EditIndividualInventory.this, R.color.blue))
                            .setTextColor(Color.WHITE)
                            .show();

                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        finish();
                    }, 2000);

                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("InventoryForm", "Update failed: Code=" + response.code()
                                + ", Message=" + response.message()
                                + ", ErrorBody=" + errorBody);

                        Toast.makeText(EditIndividualInventory.this, "Update failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("InventoryForm", "Error reading error body", e);
                        Toast.makeText(EditIndividualInventory.this, "Update failed (IO error)", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditIndividualInventory.this, "Error updating", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkDuplicateThenSave() {
        String email1 = root.edtEmail1.getText().toString().trim();
        String email2 = root.edtEmail2.getText().toString().trim();

        if (!validateForm())
            return;

        root.btnSubmit.setEnabled(false);

        StudentApi api = ApiClient.getClient().create(StudentApi.class);
        Call<DuplicateCheckResponse> call = api.checkDuplicateEmail(email1, email2, studentId);

        call.enqueue(new Callback<DuplicateCheckResponse>() {
            @Override
            public void onResponse(Call<DuplicateCheckResponse> call, Response<DuplicateCheckResponse> response) {
                root.btnSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    boolean email1Exists = response.body().isEmail1Exists();
                    boolean email2Exists = response.body().isEmail2Exists();

                    boolean hasError = false;

                    if (email1Exists) {
                        root.edtEmail1.setError("Email already exists");
                        root.edtEmail1.requestFocus();
                        hasError = true;
                    }

                    if (email2Exists) {
                        root.edtEmail2.setError("Email already exists");
                        if (!email1Exists) root.edtEmail2.requestFocus();
                        hasError = true;
                    }

                    if (!hasError) {
                        saveForm();
                    }

                } else {
                    Toast.makeText(EditIndividualInventory.this, "Server error. Try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DuplicateCheckResponse> call, Throwable t) {
                root.btnSubmit.setEnabled(true);
                Toast.makeText(EditIndividualInventory.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showLastDoctorVisitDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            //Format as YYYY-MM-DD
            String dateString = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            root.edtLastDoctorVisit.setText(dateString);
        }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void showBirthdayDatePicker() {
        final Calendar today = Calendar.getInstance();

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -17);

        int year = maxDate.get(Calendar.YEAR);
        int month = maxDate.get(Calendar.MONTH);
        int day = maxDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String dateString = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            root.edtBirthday.setText(dateString);
        }, year, month, day);

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void addSiblingView() {
        if (root.siblingContainer.getChildCount() >= 10) { // 5 pairs of label + view = 10 children
            Toast.makeText(this, "Maximum of 5 siblings only.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate sibling number based on actual siblings (not total children)
        int siblingCount = (root.siblingContainer.getChildCount() / 2) + 1;
        
        // Create label for sibling
        TextView siblingLabel = new TextView(this);
        siblingLabel.setText("Sibling " + siblingCount);
        siblingLabel.setTextSize(16);
        siblingLabel.setTextColor(getResources().getColor(R.color.blue));
        
        // Fix API level compatibility for font
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            siblingLabel.setTypeface(getResources().getFont(R.font.poppins_semibold));
        } else {
            siblingLabel.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        siblingLabel.setPadding(0, 16, 0, 8);
        
        SiblingRowBinding siblingBinding = SiblingRowBinding.inflate(getLayoutInflater(), root.siblingContainer, false);
        View siblingView = siblingBinding.getRoot();
        siblingBinding.btnRemoveSibling.setOnClickListener(v -> {
            root.siblingContainer.removeView(siblingView);
            root.siblingContainer.removeView(siblingLabel);
            updateSiblingLabels(); // Update remaining labels
        });
        
        root.siblingContainer.addView(siblingLabel);
        root.siblingContainer.addView(siblingView);
    }

    private void addWorkView() {
        if (root.workContainer.getChildCount() >= 10) { // 5 pairs of label + view = 10 children
            Toast.makeText(this, "Maximum of 5 work experiences only.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate work experience number based on actual work experiences (not total children)
        int workCount = (root.workContainer.getChildCount() / 2) + 1;
        
        // Create label for work experience
        TextView workLabel = new TextView(this);
        workLabel.setText("Work Experience " + workCount);
        workLabel.setTextSize(16);
        workLabel.setTextColor(getResources().getColor(R.color.blue));
        
        // Fix API level compatibility for font
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            workLabel.setTypeface(getResources().getFont(R.font.poppins_semibold));
        } else {
            workLabel.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        workLabel.setPadding(0, 16, 0, 8);
        
        WorkRowBinding workBinding = WorkRowBinding.inflate(getLayoutInflater(), root.workContainer, false);
        View workView = workBinding.getRoot();
        workBinding.btnRemoveWork.setOnClickListener(v -> {
            root.workContainer.removeView(workView);
            root.workContainer.removeView(workLabel);
            updateWorkLabels(); // Update remaining labels
        });
        
        root.workContainer.addView(workLabel);
        root.workContainer.addView(workView);
    }

    private void updateWorkLabels() {
        int workCount = 0;
        for (int i = 0; i < root.workContainer.getChildCount(); i++) {
            View child = root.workContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView label = (TextView) child;
                if (label.getText().toString().startsWith("Work Experience")) {
                    workCount++;
                    label.setText("Work Experience " + workCount);
                }
            }
        }
    }

    private void updateSiblingLabels() {
        int siblingCount = 0;
        for (int i = 0; i < root.siblingContainer.getChildCount(); i++) {
            View child = root.siblingContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView label = (TextView) child;
                if (label.getText().toString().startsWith("Sibling")) {
                    siblingCount++;
                    label.setText("Sibling " + siblingCount);
                }
            }
        }
    }

    private InventoryForm buildUpdatedFormFromInputs() {
        InventoryForm form = new InventoryForm();

        // Add the student ID from the intent
        form.studentId = getIntent().getIntExtra("studentId", -1);

        form.fullName = root.edtFullName.getText().toString().trim();
        form.studentNumber = root.edtStudentNumber.getText().toString().trim();
        form.program = root.edtProgram.getText().toString().trim();
        form.nickname = root.edtNickname.getText().toString().trim();
        form.nationality = root.edtNationality.getText().toString().trim();
        form.gender = root.spinnerGender.getText().toString();
        form.civilStatus = root.spinnerStatus.getText().toString();
        form.religion = root.edtReligion.getText().toString().trim();
        form.birthday = root.edtBirthday.getText().toString().trim();
        form.phoneNumber = root.edtPhone.getText().toString().trim();
        form.email1 = root.edtEmail1.getText().toString().trim();
        form.email2 = root.edtEmail2.getText().toString().trim();
        form.presentAddress = root.edtPresentAddress.getText().toString().trim();
        form.permanentAddress = root.edtPermanentAddress.getText().toString().trim();
        form.provincialAddress = root.edtProvincialAddress.getText().toString().trim();

        // Spouse Info
        form.spouseName = root.edtSpouseName.getText().toString().trim();
        form.spouseAge = parseInteger(root.edtSpouseAge.getText().toString().trim());
        form.spouseOccupation = root.edtSpouseOccupation.getText().toString().trim();
        form.spouseContact = root.edtSpouseContact.getText().toString().trim();

        // Family
        form.fatherName = root.edtFatherName.getText().toString().trim();
        form.fatherOccupation = root.edtFatherOccupation.getText().toString().trim();
        form.fatherContact = root.edtFatherContactNo.getText().toString().trim();
        form.fatherIncome = root.edtFatherIncome.getText().toString().trim();
        form.motherName = root.edtMotherName.getText().toString().trim();
        form.motherOccupation = root.edtMotherOccupation.getText().toString().trim();
        form.motherContact = root.edtMotherContactNo.getText().toString().trim();
        form.motherIncome = root.edtMotherIncome.getText().toString().trim();
        form.fatherStatus = root.spinnerFatherStatus.getText().toString();
        form.motherStatus = root.spinnerMotherStatus.getText().toString();
        form.guardianName = root.edtGuardianName.getText().toString().trim();
        form.guardianContact = root.edtGuardianContact.getText().toString().trim();

        // Education
        form.elementary = root.edtElementary.getText().toString().trim();
        form.juniorHigh = root.edtJuniorHigh.getText().toString().trim();
        form.seniorHigh = root.edtSeniorHigh.getText().toString().trim();
        form.college = root.edtCollege.getText().toString().trim();

        // Interests
        form.sports = root.edtSports.getText().toString().trim();
        form.hobbies = root.edtHobbies.getText().toString().trim();
        form.talents = root.edtTalents.getText().toString().trim();
        form.socioCivic = root.edtSocioCivic.getText().toString().trim();
        form.schoolOrg = root.edtSchoolOrgs.getText().toString().trim();

        // Health
        form.wasHospitalized = root.checkboxHospitalized.isChecked();
        form.hospitalizedReason = root.edtHospitalizedReason.getText().toString().trim();
        form.hadOperation = root.checkboxOperation.isChecked();
        form.operationReason = root.edtOperationReason.getText().toString().trim();
        form.hasIllness = root.checkboxCurrentIllness.isChecked();
        form.illnessDetails = root.edtCurrentIllness.getText().toString().trim();
        form.takesMedication = root.checkboxPrescriptionDrugs.isChecked();
        form.medicationDetails = root.edtPrescriptionDrugs.getText().toString().trim();
        form.hasMedicalCertificate = root.checkboxMedCert.isChecked();
        form.familyIllness = root.edtFamilyIllness.getText().toString().trim();
        form.lastDoctorVisit = root.edtLastDoctorVisit.getText().toString().trim();
        form.visitReason = root.edtDoctorVisitReason.getText().toString().trim();

        // Life Circumstances
        form.lossExperience = root.edtLossExperience.getText().toString().trim();
        form.problems = root.edtProblems.getText().toString().trim();
        form.relationshipConcerns = root.edtRelationshipConcerns.getText().toString().trim();

        // Lists
        form.siblings = collectSiblingsFromViews();
        form.workExperience = collectWorkExperienceFromViews();

        return form;
    }

    private List<InventoryForm.WorkExperience> collectWorkExperienceFromViews() {
        List<InventoryForm.WorkExperience> workExperiences = new ArrayList<>();
        for (int i = 0; i < root.workContainer.getChildCount(); i++) {
            View workView = root.workContainer.getChildAt(i);
            // Skip TextView labels, only process actual work row views
            if (workView instanceof TextView) {
                continue;
            }
            WorkRowBinding binding = WorkRowBinding.bind(workView);

            InventoryForm.WorkExperience work = new InventoryForm.WorkExperience();
            work.position = binding.edtWorkTitle.getText().toString().trim();
            work.company = binding.edtWorkCompany.getText().toString().trim();
            work.duration = binding.edtWorkDuration.getText().toString().trim();

            // Only add if at least company is provided
            if (!work.company.isEmpty()) {
                workExperiences.add(work);
            }
        }
        return workExperiences;
    }

    private List<InventoryForm.Sibling> collectSiblingsFromViews() {
        List<InventoryForm.Sibling> siblings = new ArrayList<>();
        for (int i = 0; i < root.siblingContainer.getChildCount(); i++) {
            View siblingView = root.siblingContainer.getChildAt(i);
            // Skip TextView labels, only process actual sibling row views
            if (siblingView instanceof TextView) {
                continue;
            }
            SiblingRowBinding binding = SiblingRowBinding.bind(siblingView);

            InventoryForm.Sibling sibling = new InventoryForm.Sibling();
            sibling.name = binding.edtSiblingName.getText().toString().trim();
            sibling.age = parseInteger(binding.edtSiblingAge.getText().toString().trim());
            sibling.gender = binding.edtSiblingGender.getText().toString().trim();
            sibling.programOrOccupation = binding.edtSiblingProgram.getText().toString().trim();
            sibling.schoolOrCompany = binding.edtSiblingSchool.getText().toString().trim();

            // Only add if at least name is provided
            if (!sibling.name.isEmpty()) {
                siblings.add(sibling);
            }
        }
        return siblings;
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private void loadIndividualInventoryForm(int studentId) {
        InventoryFormApi api = ApiClient.getClient().create(InventoryFormApi.class);
        Call<InventoryForm> call = api.getIndividualInventoryForm(studentId);
        call.enqueue(new Callback<InventoryForm>() {
            @Override
            public void onResponse(Call<InventoryForm> call, Response<InventoryForm> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InventoryForm form = response.body();
                    Log.d("InventoryForm", "Received: " + new Gson().toJson(form)); // <-- log content
                    populateFields(form);
                }  else {
                    Log.e("InventoryForm", "Error: " +
                            "Code=" + response.code() +
                            ", Message=" + response.message());

                    try {
                        if (response.errorBody() != null) {
                            Log.e("InventoryForm", "Error Body: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e("InventoryForm", "Error reading error body", e);
                    }
                }
            }


            @Override
            public void onFailure(Call<InventoryForm> call, Throwable t) {
                Log.e("CareerPlanningForm", "Update failed", t);
                Toast.makeText(EditIndividualInventory.this, "Error loading form.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(InventoryForm form) {
        // Basic Info
        root.edtFullName.setText(form.fullName != null ? form.fullName : "");
        root.edtStudentNumber.setText(form.studentNumber != null ? form.studentNumber : "");
        root.edtProgram.setText(form.program != null ? form.program : "");
        root.edtNickname.setText(form.nickname != null ? form.nickname : "");
        root.edtNationality.setText(form.nationality != null ? form.nationality : "");
        root.spinnerGender.setText(form.gender != null ? form.gender : "");
        root.spinnerStatus.setText(form.civilStatus != null ? form.civilStatus : "");
        root.edtReligion.setText(form.religion != null ? form.religion : "");
        root.edtBirthday.setText(form.birthday != null ? form.birthday : "");
        root.edtPhone.setText(form.phoneNumber != null ? form.phoneNumber : "");
        root.edtEmail1.setText(form.email1 != null ? form.email1 : "");
        root.edtEmail2.setText(form.email2 != null ? form.email2 : "");
        root.edtPresentAddress.setText(form.presentAddress != null ? form.presentAddress : "");
        root.edtPermanentAddress.setText(form.permanentAddress != null ? form.permanentAddress : "");
        root.edtProvincialAddress.setText(form.provincialAddress != null ? form.provincialAddress : "");

        // Spouse Info
        root.edtSpouseName.setText(form.spouseName != null ? form.spouseName : "");
        root.edtSpouseAge.setText(form.spouseAge != null ? String.valueOf(form.spouseAge) : "");
        root.edtSpouseOccupation.setText(form.spouseOccupation != null ? form.spouseOccupation : "");
        root.edtSpouseContact.setText(form.spouseContact != null ? form.spouseContact : "");

        // Family Info
        root.edtFatherName.setText(form.fatherName != null ? form.fatherName : "");
        root.edtFatherOccupation.setText(form.fatherOccupation != null ? form.fatherOccupation : "");
        root.edtFatherContactNo.setText(form.fatherContact != null ? form.fatherContact : "");
        root.edtFatherIncome.setText(form.fatherIncome != null ? form.fatherIncome : "");

        root.edtMotherName.setText(form.motherName != null ? form.motherName : "");
        root.edtMotherOccupation.setText(form.motherOccupation != null ? form.motherOccupation : "");
        root.edtMotherContactNo.setText(form.motherContact != null ? form.motherContact : "");
        root.edtMotherIncome.setText(form.motherIncome != null ? form.motherIncome : "");

        root.spinnerFatherStatus.setText(form.fatherStatus != null ? form.fatherStatus : "");
        root.spinnerMotherStatus.setText(form.motherStatus != null ? form.motherStatus : "");

        root.edtGuardianName.setText(form.guardianName != null ? form.guardianName : "");
        root.edtGuardianContact.setText(form.guardianContact != null ? form.guardianContact : "");

        // Educational Background
        root.edtElementary.setText(form.elementary != null ? form.elementary : "");
        root.edtJuniorHigh.setText(form.juniorHigh != null ? form.juniorHigh : "");
        root.edtSeniorHigh.setText(form.seniorHigh != null ? form.seniorHigh : "");
        root.edtCollege.setText(form.college != null ? form.college : "");

        // Interests and Orgs
        root.edtSports.setText(form.sports != null ? form.sports : "");
        root.edtHobbies.setText(form.hobbies != null ? form.hobbies : "");
        root.edtTalents.setText(form.talents != null ? form.talents : "");
        root.edtSocioCivic.setText(form.socioCivic != null ? form.socioCivic : "");
        root.edtSchoolOrgs.setText(form.schoolOrg != null ? form.schoolOrg : "");

        // Health History
        root.checkboxHospitalized.setChecked(Boolean.TRUE.equals(form.wasHospitalized));
        root.edtHospitalizedReason.setText(form.hospitalizedReason != null ? form.hospitalizedReason : "");
        root.checkboxOperation.setChecked(Boolean.TRUE.equals(form.hadOperation));
        root.edtOperationReason.setText(form.operationReason != null ? form.operationReason : "");
        root.checkboxCurrentIllness.setChecked(Boolean.TRUE.equals(form.hasIllness));
        root.edtCurrentIllness.setText(form.illnessDetails != null ? form.illnessDetails : "");
        root.checkboxPrescriptionDrugs.setChecked(Boolean.TRUE.equals(form.takesMedication));
        root.edtPrescriptionDrugs.setText(form.medicationDetails != null ? form.medicationDetails : "");
        root.checkboxMedCert.setChecked(Boolean.TRUE.equals(form.hasMedicalCertificate));
        root.edtFamilyIllness.setText(form.familyIllness != null ? form.familyIllness : "");
        root.edtLastDoctorVisit.setText(form.lastDoctorVisit != null ? form.lastDoctorVisit : "");
        root.edtDoctorVisitReason.setText(form.visitReason != null ? form.visitReason : "");

        // Life Circumstances
        root.edtLossExperience.setText(form.lossExperience != null ? form.lossExperience : "");
        root.edtProblems.setText(form.problems != null ? form.problems : "");
        root.edtRelationshipConcerns.setText(form.relationshipConcerns != null ? form.relationshipConcerns : "");

        populateDynamicViews(form);
    }

    private void populateDynamicViews(InventoryForm form) {
        // Clear existing views
        root.siblingContainer.removeAllViews();
        root.workContainer.removeAllViews();

        // Populate siblings
        if (form.siblings != null) {
            for (InventoryForm.Sibling sibling : form.siblings) {
                addSiblingViewWithData(sibling);
            }
        }

        // Populate work experiences
        if (form.workExperience != null) {
            for (InventoryForm.WorkExperience work : form.workExperience) {
                addWorkViewWithData(work);
            }
        }
    }

    //Add these methods to create views with pre-populated data:
    private void addSiblingViewWithData(InventoryForm.Sibling sibling) {
        SiblingRowBinding siblingBinding = SiblingRowBinding.inflate(getLayoutInflater(), root.siblingContainer, false);
        View siblingView = siblingBinding.getRoot();

        // Populate with data (with null safety)
        siblingBinding.edtSiblingName.setText(sibling.name != null ? sibling.name : "");
        if (sibling.age != null) {
            siblingBinding.edtSiblingAge.setText(String.valueOf(sibling.age));
        }
        siblingBinding.edtSiblingGender.setText(sibling.gender != null ? sibling.gender : "");
        siblingBinding.edtSiblingProgram.setText(sibling.programOrOccupation != null ? sibling.programOrOccupation : "");
        siblingBinding.edtSiblingSchool.setText(sibling.schoolOrCompany != null ? sibling.schoolOrCompany : "");

        siblingBinding.btnRemoveSibling.setOnClickListener(v -> root.siblingContainer.removeView(siblingView));
        root.siblingContainer.addView(siblingView);
    }

    private void addWorkViewWithData(InventoryForm.WorkExperience work) {
        WorkRowBinding workBinding = WorkRowBinding.inflate(getLayoutInflater(), root.workContainer, false);
        View workView = workBinding.getRoot();

        // Populate with data (with null safety)
        workBinding.edtWorkTitle.setText(work.position != null ? work.position : "");
        workBinding.edtWorkCompany.setText(work.company != null ? work.company : "");
        workBinding.edtWorkDuration.setText(work.duration != null ? work.duration : "");

        workBinding.btnRemoveWork.setOnClickListener(v -> root.workContainer.removeView(workView));
        root.workContainer.addView(workView);
    }

    //Helper function for spinner
    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("\\d{11}");
    }

    private boolean isValidAge(String age) {
        try {
            int a = Integer.parseInt(age);
            return a > 0 && a < 120;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDate(String date) {
        //For simplicity, just check non-empty or add real date parsing if needed
        return !isEmpty(date);
    }

    private String safeText(android.widget.EditText editText) {
        return editText != null && editText.getText() != null ? editText.getText().toString() : "";
    }

    private boolean validateForm() {
        //Validate Full Name
        if (isEmpty(root.edtFullName.getText().toString())) {
            root.edtFullName.setError("Full Name is required");
            root.edtFullName.requestFocus();
            return false;
        }

        //Validate student number
        String studentNumber = root.edtStudentNumber.getText().toString().trim();

        if (studentNumber.isEmpty()) {
            root.edtStudentNumber.setError("Student Number is required");
            root.edtStudentNumber.requestFocus();
            return false;
        }

        if (!studentNumber.matches("\\d{11}")) {
            root.edtStudentNumber.setError("Student Number must be exactly 11 digits");
            root.edtStudentNumber.requestFocus();
            return false;
        }


        //Validate Program
        if (isEmpty(root.edtProgram.getText().toString())) {
            root.edtProgram.setError("Program is required");
            root.edtProgram.requestFocus();
            return false;
        }

        // Validate Birthday
        String birthday = root.edtBirthday.getText().toString();
        if (!isValidDate(birthday)) {
            root.edtBirthday.setError("Valid Birthday is required");
            root.edtBirthday.requestFocus();
            return false;
        }

        // Validate Phone Number
        String phone = root.edtPhone.getText().toString();
        if (!isValidPhoneNumber(phone)) {
            root.edtPhone.setError("Valid Phone Number is required (digits only)");
            root.edtPhone.requestFocus();
            return false;
        }

        // Validate Email 1
        String email1 = root.edtEmail1.getText().toString();
        if (!isValidEmail(email1)) {
            root.edtEmail1.setError("Valid Email is required");
            root.edtEmail1.requestFocus();
            return false;
        }

        // Validate Gender AutoCompleteTextView
        if (root.spinnerGender.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select a Gender", Toast.LENGTH_SHORT).show();
            root.spinnerGender.requestFocus();
            return false;
        }

        // Validate Civil Status AutoCompleteTextView
        if (root.spinnerStatus.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select Civil Status", Toast.LENGTH_SHORT).show();
            root.spinnerStatus.requestFocus();
            return false;
        }

        // If Married, validate spouse info (at least name and contact)
        if (root.layoutSpouse.getVisibility() == View.VISIBLE) {
            if (isEmpty(root.edtSpouseName.getText().toString())) {
                root.edtSpouseName.setError("Spouse Name is required");
                root.edtSpouseName.requestFocus();
                return false;
            }
            // Validate spouse age if entered
            String spouseAge = root.edtSpouseAge.getText().toString();
            if (!isEmpty(spouseAge) && !isValidAge(spouseAge)) {
                root.edtSpouseAge.setError("Enter valid Spouse Age");
                root.edtSpouseAge.requestFocus();
                return false;
            }
            // Validate spouse contact if entered
            String spouseContact = root.edtSpouseContact.getText().toString();
            if (!isEmpty(spouseContact) && !isValidPhoneNumber(spouseContact)) {
                root.edtSpouseContact.setError("Enter valid Spouse Contact Number");
                root.edtSpouseContact.requestFocus();
                return false;
            }
        }

        // Validate parent phone numbers if entered
        String fatherContact = root.edtFatherContactNo.getText().toString();
        if (!isEmpty(fatherContact) && !isValidPhoneNumber(fatherContact)) {
            root.edtFatherContactNo.setError("Enter valid Father's Contact Number");
            root.edtFatherContactNo.requestFocus();
            return false;
        }

        String motherContact = root.edtMotherContactNo.getText().toString();
        if (!isEmpty(motherContact) && !isValidPhoneNumber(motherContact)) {
            root.edtMotherContactNo.setError("Enter valid Mother's Contact Number");
            root.edtMotherContactNo.requestFocus();
            return false;
        }

        String guardianContact = root.edtGuardianContact.getText().toString();
        if (!isEmpty(guardianContact) && !isValidPhoneNumber(guardianContact)) {
            root.edtGuardianContact.setError("Enter valid Guardian Contact Number");
            root.edtGuardianContact.requestFocus();
            return false;
        }

        // Parent/Guardian Info
        if (safeText(root.edtMotherName).isEmpty()) {
            root.edtMotherName.setError("Mother's name is required");
            root.edtMotherName.requestFocus();
            return false;
        }

        if (safeText(root.edtMotherOccupation).isEmpty()) {
            root.edtMotherOccupation.setError("Mother's occupation is required");
            root.edtMotherOccupation.requestFocus();
            return false;
        }

        if (safeText(root.edtFatherName).isEmpty()) {
            root.edtFatherName.setError("Father's name is required");
            root.edtFatherName.requestFocus();
            return false;
        }

        if (safeText(root.edtFatherOccupation).isEmpty()) {
            root.edtFatherOccupation.setError("Father's occupation is required");
            root.edtFatherOccupation.requestFocus();
            return false;
        }


        // Conditional checks based on checkboxes and reasons
        if (root.checkboxHospitalized.isChecked() && isEmpty(root.edtHospitalizedReason.getText().toString())) {
            root.edtHospitalizedReason.setError("Please specify hospitalization reason");
            root.edtHospitalizedReason.requestFocus();
            return false;
        }
        if (root.checkboxOperation.isChecked() && isEmpty(root.edtOperationReason.getText().toString())) {
            root.edtOperationReason.setError("Please specify operation reason");
            root.edtOperationReason.requestFocus();
            return false;
        }
        if (root.checkboxCurrentIllness.isChecked() && isEmpty(root.edtCurrentIllness.getText().toString())) {
            root.edtCurrentIllness.setError("Please specify illness details");
            root.edtCurrentIllness.requestFocus();
            return false;
        }
        if (root.checkboxPrescriptionDrugs.isChecked() && isEmpty(root.edtPrescriptionDrugs.getText().toString())) {
            root.edtPrescriptionDrugs.setError("Please specify medication details");
            root.edtPrescriptionDrugs.requestFocus();
            return false;
        }

        // Validate siblings data if any - example check for name and age numeric
        for (int i = 0; i < root.siblingContainer.getChildCount(); i++) {
            View view = root.siblingContainer.getChildAt(i);
            // Skip TextView labels, only process actual sibling row views
            if (view instanceof TextView) {
                continue;
            }
            SiblingRowBinding binding = SiblingRowBinding.bind(view);
            String sibName = safeText(binding.edtSiblingName);
            String sibAge = safeText(binding.edtSiblingAge);
            if (!isEmpty(sibName) && !isEmpty(sibAge)) {
                if (!isValidAge(sibAge)) {
                    binding.edtSiblingAge.setError("Enter valid age");
                    binding.edtSiblingAge.requestFocus();
                    return false;
                }
            } else if (!isEmpty(sibName) || !isEmpty(sibAge)) {
                // partial entry invalid
                Toast.makeText(this, "Please complete sibling information or leave all fields empty.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Validate Work Experience entries
        for (int i = 0; i < root.workContainer.getChildCount(); i++) {
            View view = root.workContainer.getChildAt(i);
            // Skip TextView labels, only process actual work row views
            if (view instanceof TextView) {
                continue;
            }
            WorkRowBinding binding = WorkRowBinding.bind(view);

            String position = safeText(binding.edtWorkTitle);
            String company = safeText(binding.edtWorkCompany);
            String duration = safeText(binding.edtWorkDuration);

            boolean anyFilled = !isEmpty(position) || !isEmpty(company) || !isEmpty(duration);
            boolean allFilled = !isEmpty(position) && !isEmpty(company) && !isEmpty(duration);

            if (anyFilled && !allFilled) {
                Toast.makeText(this, "Please complete all fields in each Work Experience entry or leave all blank.", Toast.LENGTH_SHORT).show();
                if (isEmpty(position)) {
                    binding.edtWorkTitle.setError("Required");
                    binding.edtWorkTitle.requestFocus();
                } else if (isEmpty(company)) {
                    binding.edtWorkCompany.setError("Required");
                    binding.edtWorkCompany.requestFocus();
                } else if (isEmpty(duration)) {
                    binding.edtWorkDuration.setError("Required");
                    binding.edtWorkDuration.requestFocus();
                }
                return false;
            }
        }
        return true;
    }

    private void addLiveValidation(android.widget.EditText editText, ValidationRule rule) {
        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String input = s.toString().trim();
                String error = rule.validate(input);
                editText.setError(error); // This will show or clear the error
            }
        });
    }


    interface ValidationRule {
        String validate(String input); //Return null if valid, or error message if invalid
    }

}