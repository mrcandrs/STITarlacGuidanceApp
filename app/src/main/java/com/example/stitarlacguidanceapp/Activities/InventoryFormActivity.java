package com.example.stitarlacguidanceapp.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.DuplicateCheckResponse;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.StudentApi;
import com.example.stitarlacguidanceapp.databinding.ActivityInventoryFormBinding;
import com.example.stitarlacguidanceapp.databinding.SiblingRowBinding;
import com.example.stitarlacguidanceapp.databinding.WorkRowBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryFormActivity extends AppCompatActivity {

    private ActivityInventoryFormBinding root;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityInventoryFormBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        sharedPreferences = getSharedPreferences("InventoryForm", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Load student name from Client Consent Form and disable the field
        loadDataFromConsentForm();

        //Live validation for full name
        addLiveValidation(root.edtFullName, input -> {
            return input.isEmpty() ? "Full name is required" : null;
        });


        //Live validation for student number
        addLiveValidation(root.edtStudentNumber, input -> {
            return input.matches("\\d{11}") ? null : "Student Number must be 11 digits";
        });

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

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void saveForm() {
        if (!validateForm())
            return;

        editor.putString("FullName", safeText(root.edtFullName));
        editor.putString("StudentNumber", safeText(root.edtStudentNumber));
        editor.putString("Program", safeText(root.edtProgram));
        editor.putString("Nickname", safeText(root.edtNickname));

        editor.putString("Nationality", safeText(root.edtNationality));
        Object gender = root.spinnerGender.getSelectedItem();
        editor.putString("Gender", gender != null ? gender.toString() : "");
        editor.putString("Religion", safeText(root.edtReligion));
        editor.putString("Birthday", safeText(root.edtBirthday));

        editor.putString("PhoneNumber", safeText(root.edtPhone));
        editor.putString("Email1", safeText(root.edtEmail1));
        editor.putString("Email2", safeText(root.edtEmail2));

        editor.putString("PresentAddress", safeText(root.edtPresentAddress));
        editor.putString("PermanentAddress", safeText(root.edtPermanentAddress));
        editor.putString("ProvincialAddress", safeText(root.edtProvincialAddress));

        Object civilStatus = root.spinnerStatus.getSelectedItem();
        editor.putString("CivilStatus", civilStatus != null ? civilStatus.toString() : "");

        if (root.layoutSpouse.getVisibility() == View.VISIBLE) {
            editor.putString("SpouseName", safeText(root.edtSpouseName));
            editor.putString("SpouseAge", safeText(root.edtSpouseAge));
            editor.putString("SpouseOccupation", safeText(root.edtSpouseOccupation));
            editor.putString("SpouseContact", safeText(root.edtSpouseContact));
        } else {
            editor.remove("SpouseName");
            editor.remove("SpouseAge");
            editor.remove("SpouseOccupation");
            editor.remove("SpouseContact");
        }


        editor.putString("FatherName", safeText(root.edtFatherName));
        editor.putString("FatherOccupation", safeText(root.edtFatherOccupation));
        editor.putString("FatherContact", safeText(root.edtFatherContactNo));
        editor.putString("FatherIncome", safeText(root.edtFatherIncome));
        Object fatherStatus = root.spinnerFatherStatus.getSelectedItem();
        editor.putString("FatherStatus", fatherStatus != null ? fatherStatus.toString() : "");

        editor.putString("MotherName", safeText(root.edtMotherName));
        editor.putString("MotherOccupation", safeText(root.edtMotherOccupation));
        editor.putString("MotherContact", safeText(root.edtMotherContactNo));
        editor.putString("MotherIncome", safeText(root.edtMotherIncome));
        Object motherStatus = root.spinnerMotherStatus.getSelectedItem();
        editor.putString("MotherStatus", motherStatus != null ? motherStatus.toString() : "");

        editor.putString("GuardianName", safeText(root.edtGuardianName));
        editor.putString("GuardianContact", safeText(root.edtGuardianContact));

        editor.putString("Elementary", safeText(root.edtElementary));
        editor.putString("JuniorHigh", safeText(root.edtJuniorHigh));
        editor.putString("SeniorHigh", safeText(root.edtSeniorHigh));
        editor.putString("College", safeText(root.edtCollege));

        editor.putString("Sports", safeText(root.edtSports));
        editor.putString("Hobbies", safeText(root.edtHobbies));
        editor.putString("Talents", safeText(root.edtTalents));
        editor.putString("SocioCivic", safeText(root.edtSocioCivic));
        editor.putString("SchoolOrg", safeText(root.edtSchoolOrgs));

        editor.putBoolean("WasHospitalized", root.checkboxHospitalized.isChecked());
        editor.putString("HospitalizedReason", safeText(root.edtHospitalizedReason));
        editor.putBoolean("HadOperation", root.checkboxOperation.isChecked());
        editor.putString("OperationReason", safeText(root.edtOperationReason));
        editor.putBoolean("HasIllness", root.checkboxCurrentIllness.isChecked());
        editor.putString("IllnessDetails", safeText(root.edtCurrentIllness));
        editor.putBoolean("TakesMedication", root.checkboxPrescriptionDrugs.isChecked());
        editor.putString("MedicationDetails", safeText(root.edtPrescriptionDrugs));
        editor.putBoolean("HasMedicalCertificate", root.checkboxMedCert.isChecked());

        editor.putString("FamilyIllness", safeText(root.edtFamilyIllness));
        editor.putString("LastDoctorVisit", safeText(root.edtLastDoctorVisit));
        editor.putString("VisitReason", safeText(root.edtDoctorVisitReason));

        editor.putString("LossExperience", safeText(root.edtLossExperience));
        editor.putString("Problems", safeText(root.edtProblems));
        editor.putString("RelationshipConcerns", safeText(root.edtRelationshipConcerns));

        // ✅ Save Sibling JSON array
        JSONArray siblingsArray = new JSONArray();
        for (int i = 0; i < root.siblingContainer.getChildCount(); i++) {
            View view = root.siblingContainer.getChildAt(i);
            SiblingRowBinding binding = SiblingRowBinding.bind(view);
            if (binding != null) {
                String name = safeText(binding.edtSiblingName);
                String age = safeText(binding.edtSiblingAge);
                String siblingGender = safeText(binding.edtSiblingGender);
                String program = safeText(binding.edtSiblingProgram);
                String school = safeText(binding.edtSiblingSchool);

                if (!isEmpty(name) || !isEmpty(age) || !isEmpty(siblingGender) || !isEmpty(program) || !isEmpty(school)) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("name", name);
                        obj.put("age", age);
                        obj.put("gender", siblingGender);
                        obj.put("programOrOccupation", program);
                        obj.put("schoolOrCompany", school);
                        siblingsArray.put(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        editor.putString("Siblings", siblingsArray.toString());

        // ✅ Save WorkExperience JSON array
        JSONArray workArray = new JSONArray();
        for (int i = 0; i < root.workContainer.getChildCount(); i++) {
            View view = root.workContainer.getChildAt(i);
            WorkRowBinding binding = WorkRowBinding.bind(view);
            if (binding != null) {
                String position = safeText(binding.edtWorkTitle);
                String company = safeText(binding.edtWorkCompany);
                String duration = safeText(binding.edtWorkDuration);

                if (!isEmpty(position) || !isEmpty(company) || !isEmpty(duration)) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("position", position);
                        obj.put("company", company);
                        obj.put("duration", duration);
                        workArray.put(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        editor.putString("WorkExperience", workArray.toString());

        editor.apply();

        Intent intent = new Intent(this, CareerPlanningFormActivity.class);

        //Pass data here
        intent.putExtra("StudentNumber", safeText(root.edtStudentNumber));
        intent.putExtra("FullName", safeText(root.edtFullName));
        intent.putExtra("Birthday", safeText(root.edtBirthday));
        intent.putExtra("ContactNumber", safeText(root.edtPhone));

        Toast.makeText(this, "Individual Inventory Form saved!", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    private String safeText(android.widget.EditText editText) {
        return editText != null && editText.getText() != null ? editText.getText().toString() : "";
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

    private void checkDuplicateThenSave() {
        String studentNumber = root.edtStudentNumber.getText().toString().trim();
        String email = root.edtEmail1.getText().toString().trim();

        if (!validateForm()) return;

        root.btnSubmit.setEnabled(false);

        StudentApi api = ApiClient.getClient().create(StudentApi.class);
        Call<DuplicateCheckResponse> call = api.checkDuplicate(studentNumber, email);

        call.enqueue(new Callback<DuplicateCheckResponse>() {
            @Override
            public void onResponse(Call<DuplicateCheckResponse> call, Response<DuplicateCheckResponse> response) {
                root.btnSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    boolean emailExists = response.body().isEmailExists();
                    boolean studentNumberExists = response.body().isStudentNumberExists();

                    //Setting error if email or student number already exists in the database
                    if (emailExists || studentNumberExists) {
                        if (emailExists) {
                            root.edtEmail1.setError("Email already exists");
                            root.edtEmail1.requestFocus();
                        }
                        if (studentNumberExists) {
                            root.edtStudentNumber.setError("Student Number already exists");
                            root.edtStudentNumber.requestFocus();
                        }
                    } else {
                        saveForm();
                    }

                } else {
                    Toast.makeText(InventoryFormActivity.this, "Server error. Try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DuplicateCheckResponse> call, Throwable t) {
                root.btnSubmit.setEnabled(true);
                Toast.makeText(InventoryFormActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Validation
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

        // Validate Gender Spinner (assuming first is "Select Gender")
        if (root.spinnerGender.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a Gender", Toast.LENGTH_SHORT).show();
            root.spinnerGender.requestFocus();
            return false;
        }

        // Validate Civil Status Spinner (assuming first is "Select Civil Status")
        if (root.spinnerStatus.getSelectedItemPosition() == 0) {
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

    /**
     * Load student name from Client Consent Form and pre-fill the Full Name field (disabled)
     */
    private void loadDataFromConsentForm() {
        try {
            SharedPreferences consentPrefs = getSharedPreferences("OnboardingData", MODE_PRIVATE);
            String studentName = consentPrefs.getString("Consent_StudentName", "");
            
            // Pre-fill student name and disable the field
            if (!studentName.isEmpty()) {
                root.edtFullName.setText(studentName);
                root.edtFullName.setEnabled(false);
                root.edtFullName.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                root.edtFullName.setTextColor(getResources().getColor(android.R.color.darker_gray));
                Log.d("InventoryForm", "Pre-filled and disabled student name: " + studentName);
            } else {
                Log.d("InventoryForm", "No student name found in consent form");
            }
            
        } catch (Exception e) {
            Log.e("InventoryForm", "Error loading student name from consent form: " + e.getMessage());
        }
    }

}