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
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityInventoryFormBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        sharedPreferences = getSharedPreferences("InventoryForm", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Load student name from Client Consent Form and disable the field
        loadDataFromConsentForm();

        //Enhanced live validation for full name
        addLiveValidation(root.edtFullName, input -> {
            if (isEmpty(input)) return "Full name is required";
            if (input.length() < 2) return "Name must be at least 2 characters";
            if (input.length() > 100) return "Name must not exceed 100 characters";
            if (!input.matches("^[a-zA-Z\\s\\-\\.']+$")) return "Name contains invalid characters";
            if (input.contains("  ")) return "Name cannot contain multiple consecutive spaces";
            return null;
        });

        //Enhanced live validation for student number
        addLiveValidation(root.edtStudentNumber, input -> {
            if (isEmpty(input)) return "Student Number is required";
            if (!input.matches("\\d{11}")) return "Student Number must be exactly 11 digits";
            return null;
        });

        //Enhanced live validation for program
        addLiveValidation(root.edtProgram, input -> {
            if (isEmpty(input)) return "Program is required";
            if (input.length() < 3) return "Program must be at least 3 characters";
            if (input.length() > 100) return "Program must not exceed 100 characters";
            return null;
        });

        //Enhanced live validation for email 1
        addLiveValidation(root.edtEmail1, input -> {
            if (isEmpty(input)) return "Email is required";
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) return "Invalid email address";
            return null;
        });

        //Enhanced live validation for email 2
        addLiveValidation(root.edtEmail2, input -> {
            if (!isEmpty(input) && !android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                return "Invalid email address";
            }
            return null;
        });

        //Enhanced live validation for phone number
        addLiveValidation(root.edtPhone, input -> {
            if (isEmpty(input)) return "Phone number is required";
            if (!input.matches("\\d{11}")) return "Phone must be exactly 11 digits";
            return null;
        });

        //Enhanced live validation for guardian number
        addLiveValidation(root.edtGuardianContact, input -> {
            if (!isEmpty(input) && !input.matches("\\d{11}")) {
                return "Guardian Contact must be exactly 11 digits";
            }
            return null;
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
        root.spinnerStatus.setOnItemClickListener((parent, view, position, id) -> {
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
        });

        root.btnAddWork.setOnClickListener(v -> addWorkView());
        root.btnAddSibling.setOnClickListener(v -> addSiblingView());
        root.btnSubmit.setOnClickListener(v -> {
            if (isSubmitting) {
                Toast.makeText(this, "Please wait, form is being submitted...", Toast.LENGTH_SHORT).show();
                return;
            }
            checkDuplicateThenSave();
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
        Object gender = root.spinnerGender.getText().toString();
        editor.putString("Gender", gender != null ? gender.toString() : "");
        editor.putString("Religion", safeText(root.edtReligion));
        editor.putString("Birthday", safeText(root.edtBirthday));

        editor.putString("PhoneNumber", safeText(root.edtPhone));
        editor.putString("Email1", safeText(root.edtEmail1));
        editor.putString("Email2", safeText(root.edtEmail2));

        editor.putString("PresentAddress", safeText(root.edtPresentAddress));
        editor.putString("PermanentAddress", safeText(root.edtPermanentAddress));
        editor.putString("ProvincialAddress", safeText(root.edtProvincialAddress));

        Object civilStatus = root.spinnerStatus.getText().toString();
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
        Object fatherStatus = root.spinnerFatherStatus.getText().toString();
        editor.putString("FatherStatus", fatherStatus != null ? fatherStatus.toString() : "");

        editor.putString("MotherName", safeText(root.edtMotherName));
        editor.putString("MotherOccupation", safeText(root.edtMotherOccupation));
        editor.putString("MotherContact", safeText(root.edtMotherContactNo));
        editor.putString("MotherIncome", safeText(root.edtMotherIncome));
        Object motherStatus = root.spinnerMotherStatus.getText().toString();
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
    
    /**
     * Enhanced name validation with comprehensive checks
     */
    private boolean isValidName(String name) {
        if (isEmpty(name)) return false;
        
        // Check minimum length
        if (name.trim().length() < 2) return false;
        
        // Check maximum length
        if (name.length() > 100) return false;
        
        // Check for valid characters (letters, spaces, hyphens, periods, apostrophes)
        if (!name.matches("^[a-zA-Z\\s\\-\\.']+$")) return false;
        
        // Check for multiple consecutive spaces
        if (name.contains("  ")) return false;
        
        return true;
    }
    
    /**
     * Enhanced student number validation
     */
    private boolean isValidStudentNumber(String studentNumber) {
        if (isEmpty(studentNumber)) return false;
        
        // Must be exactly 11 digits
        if (!studentNumber.matches("\\d{11}")) return false;
        
        // Additional checks can be added here (e.g., specific format requirements)
        return true;
    }
    
    /**
     * Enhanced program validation
     */
    private boolean isValidProgram(String program) {
        if (isEmpty(program)) return false;
        
        // Check minimum length
        if (program.trim().length() < 3) return false;
        
        // Check maximum length
        if (program.length() > 100) return false;
        
        return true;
    }

    private void checkDuplicateThenSave() {
        String studentNumber = root.edtStudentNumber.getText().toString().trim();
        String email = root.edtEmail1.getText().toString().trim();

        if (!validateForm()) return;

        isSubmitting = true;
        root.btnSubmit.setEnabled(false);
        root.btnSubmit.setText("Submitting...");

        StudentApi api = ApiClient.getClient().create(StudentApi.class);
        Call<DuplicateCheckResponse> call = api.checkDuplicate(studentNumber, email);

        call.enqueue(new Callback<DuplicateCheckResponse>() {
            @Override
            public void onResponse(Call<DuplicateCheckResponse> call, Response<DuplicateCheckResponse> response) {
                resetSubmitButton();

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
                resetSubmitButton();
                Toast.makeText(InventoryFormActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Reset submit button state in case of error
     */
    private void resetSubmitButton() {
        isSubmitting = false;
        root.btnSubmit.setEnabled(true);
        root.btnSubmit.setText("Submit");
    }


    //Enhanced Validation
    private boolean validateForm() {
        boolean isValid = true;
        
        //Validate Full Name
        String fullName = root.edtFullName.getText().toString().trim();
        if (!isValidName(fullName)) {
            if (isEmpty(fullName)) {
                root.edtFullName.setError("Full Name is required");
            } else if (fullName.length() < 2) {
                root.edtFullName.setError("Name must be at least 2 characters");
            } else if (fullName.length() > 100) {
                root.edtFullName.setError("Name must not exceed 100 characters");
            } else if (!fullName.matches("^[a-zA-Z\\s\\-\\.']+$")) {
                root.edtFullName.setError("Name contains invalid characters");
            } else if (fullName.contains("  ")) {
                root.edtFullName.setError("Name cannot contain multiple consecutive spaces");
            }
            root.edtFullName.requestFocus();
            isValid = false;
        } else {
            root.edtFullName.setError(null);
        }

        //Validate student number
        String studentNumber = root.edtStudentNumber.getText().toString().trim();
        if (!isValidStudentNumber(studentNumber)) {
            if (isEmpty(studentNumber)) {
                root.edtStudentNumber.setError("Student Number is required");
            } else {
                root.edtStudentNumber.setError("Student Number must be exactly 11 digits");
            }
            root.edtStudentNumber.requestFocus();
            isValid = false;
        } else {
            root.edtStudentNumber.setError(null);
        }

        //Validate Program
        String program = root.edtProgram.getText().toString().trim();
        if (!isValidProgram(program)) {
            if (isEmpty(program)) {
                root.edtProgram.setError("Program is required");
            } else if (program.length() < 3) {
                root.edtProgram.setError("Program must be at least 3 characters");
            } else {
                root.edtProgram.setError("Program must not exceed 100 characters");
            }
            root.edtProgram.requestFocus();
            isValid = false;
        } else {
            root.edtProgram.setError(null);
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
        String gender = root.spinnerGender.getText().toString().trim();
        if (isEmpty(gender)) {
            Toast.makeText(this, "Please select a Gender", Toast.LENGTH_SHORT).show();
            root.spinnerGender.requestFocus();
            return false;
        }

        // Validate Civil Status AutoCompleteTextView
        String civilStatus = root.spinnerStatus.getText().toString().trim();
        if (isEmpty(civilStatus)) {
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