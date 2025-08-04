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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.CareerPlanningApi;
import com.example.stitarlacguidanceapp.InventoryFormApi;
import com.example.stitarlacguidanceapp.Models.CareerPlanningForm;
import com.example.stitarlacguidanceapp.Models.InventoryForm;
import com.example.stitarlacguidanceapp.R;
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
    private List<InventoryForm.Sibling> siblingList = new ArrayList<>();
    private List<InventoryForm.WorkExperience> workExperienceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityEditIndividualInventoryBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        int studentId = getIntent().getIntExtra("studentId", -1);
        Log.d("IndividualInventoryForm", "studentId from Intent: " + studentId);
        loadIndividualInventoryForm(studentId);

        //Gender spinner populate
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.inventory_form_gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        root.spinnerGender.setAdapter(genderAdapter);

        //Civil status spinner populate
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this, R.array.civil_status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        root.spinnerStatus.setAdapter(statusAdapter);

        root.spinnerGender.setSelection(0);
        root.spinnerStatus.setSelection(0);

        //Parents status spinner populate
        ArrayAdapter<CharSequence> parentStatusAdapter = ArrayAdapter.createFromResource(
                this, R.array.parent_status_options, android.R.layout.simple_spinner_item);
        parentStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        root.spinnerFatherStatus.setAdapter(parentStatusAdapter);
        root.spinnerMotherStatus.setAdapter(parentStatusAdapter);

        //When married, shows the spouse EditText sections
        root.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object selected = root.spinnerStatus.getSelectedItem();
                if (selected != null && selected.toString().equals("Married")) {
                    root.layoutSpouse.setVisibility(View.VISIBLE);
                } else {
                    root.layoutSpouse.setVisibility(View.GONE);

                    root.edtSpouseName.setText("");
                    root.edtSpouseAge.setText("");
                    root.edtSpouseOccupation.setText("");
                    root.edtSpouseContact.setText("");
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        root.btnAddWork.setOnClickListener(v -> addWorkView());
        root.btnAddSibling.setOnClickListener(v -> addSiblingView());


        root.btnSubmit.setOnClickListener(v -> {
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
        });

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
        if (root.siblingContainer.getChildCount() >= 5) {
            Toast.makeText(this, "Maximum of 5 siblings only.", Toast.LENGTH_SHORT).show();
            return;
        }

        SiblingRowBinding siblingBinding = SiblingRowBinding.inflate(getLayoutInflater(), root.siblingContainer, false);
        View siblingView = siblingBinding.getRoot();
        siblingBinding.btnRemoveSibling.setOnClickListener(v -> root.siblingContainer.removeView(siblingView));
        root.siblingContainer.addView(siblingView);
    }

    private void addWorkView() {
        if (root.workContainer.getChildCount() >= 5) {
            Toast.makeText(this, "Maximum of 5 work experiences only.", Toast.LENGTH_SHORT).show();
            return;
        }

        WorkRowBinding workBinding = WorkRowBinding.inflate(getLayoutInflater(), root.workContainer, false);
        View workView = workBinding.getRoot();
        workBinding.btnRemoveWork.setOnClickListener(v -> root.workContainer.removeView(workView));
        root.workContainer.addView(workView);
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
        form.gender = root.spinnerGender.getSelectedItem().toString();
        form.civilStatus = root.spinnerStatus.getSelectedItem().toString();
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
        form.fatherStatus = root.spinnerFatherStatus.getSelectedItem().toString();
        form.motherStatus = root.spinnerMotherStatus.getSelectedItem().toString();
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
        root.spinnerGender.setSelection(getSpinnerIndex(root.spinnerGender, form.gender));
        root.spinnerStatus.setSelection(getSpinnerIndex(root.spinnerStatus, form.civilStatus));
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

        root.spinnerFatherStatus.setSelection(getSpinnerIndex(root.spinnerFatherStatus, form.fatherStatus));
        root.spinnerMotherStatus.setSelection(getSpinnerIndex(root.spinnerMotherStatus, form.motherStatus));

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

    // Add these methods to create views with pre-populated data:
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

    // Helper function for spinner
    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

}