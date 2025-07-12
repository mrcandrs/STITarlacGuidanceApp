package com.example.stitarlacguidanceapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.Models.CareerPlanningForm;
import com.example.stitarlacguidanceapp.Models.ConsentForm;
import com.example.stitarlacguidanceapp.Models.FullRegistration;
import com.example.stitarlacguidanceapp.Models.InventoryForm;
import com.example.stitarlacguidanceapp.StudentApi;
import com.example.stitarlacguidanceapp.databinding.ActivityRegistrationBinding;
import com.example.stitarlacguidanceapp.Models.Student;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {

    private ActivityRegistrationBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        String studentNumber = getIntent().getStringExtra("StudentNumber");
        if (studentNumber != null) {
            root.edtStudentNumber.setText(studentNumber);
            root.edtStudentNumber.setEnabled(false);
            root.edtStudentNumber.setBackgroundColor(Color.parseColor("#696969"));
            root.edtStudentNumber.setTextColor(Color.LTGRAY);
        }

        //Set them to EditTexts if you have those
        root.edtStudentNumber.setText(studentNumber);

        root.btnRegister.setOnClickListener(v -> registerStudent());
        root.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void registerStudent() {
        String studentNumber = root.edtStudentNumber.getText().toString();
        String email = root.edtEmail.getText().toString();
        String username = root.edtStudentUsername.getText().toString();
        String password = root.edtStudentPassword.getText().toString();

        if (studentNumber.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create student object
        Student student = new Student(studentNumber, email, username, password);

        // -----------------------
        // GET SHARED PREFERENCES
        // -----------------------

        // 1. Consent Form
        SharedPreferences consentPrefs = getSharedPreferences("OnboardingData", MODE_PRIVATE);
        ConsentForm consentForm = new ConsentForm();
        consentForm.parentName = consentPrefs.getString("Consent_ParentName", "");
        consentForm.isAgreed = consentPrefs.getBoolean("Consent_IsAgreed", false);
        consentForm.signedDate = consentPrefs.getString("Consent_SignedDate", "");
        consentForm.counselorId = 1;

        // 2. Inventory Form
        SharedPreferences inventoryPrefs = getSharedPreferences("InventoryForm", MODE_PRIVATE);
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.fullName = inventoryPrefs.getString("FullName", "");
        inventoryForm.studentNumber = inventoryPrefs.getString("StudentNumber", "");
        inventoryForm.program = inventoryPrefs.getString("Program", "");
        inventoryForm.nickname = inventoryPrefs.getString("Nickname", "");
        inventoryForm.nationality = inventoryPrefs.getString("Nationality", "");
        inventoryForm.gender = inventoryPrefs.getString("Gender", "");
        inventoryForm.religion = inventoryPrefs.getString("Religion", "");
        inventoryForm.birthday = inventoryPrefs.getString("Birthday", "");
        inventoryForm.phoneNumber = inventoryPrefs.getString("PhoneNumber", "");
        inventoryForm.email1 = inventoryPrefs.getString("Email1", "");
        inventoryForm.email2 = inventoryPrefs.getString("Email2", "");
        inventoryForm.presentAddress = inventoryPrefs.getString("PresentAddress", "");
        inventoryForm.permanentAddress = inventoryPrefs.getString("PermanentAddress", "");
        inventoryForm.provincialAddress = inventoryPrefs.getString("ProvincialAddress", "");

        inventoryForm.civilStatus = inventoryPrefs.getString("CivilStatus", "");
        inventoryForm.spouseName = inventoryPrefs.getString("SpouseName", "");
        inventoryForm.spouseOccupation = inventoryPrefs.getString("SpouseOccupation", "");
        inventoryForm.spouseContact = inventoryPrefs.getString("SpouseContact", "");

        inventoryForm.fatherName = inventoryPrefs.getString("FatherName", "");
        inventoryForm.fatherOccupation = inventoryPrefs.getString("FatherOccupation", "");
        inventoryForm.fatherContact = inventoryPrefs.getString("FatherContact", "");
        inventoryForm.fatherIncome = inventoryPrefs.getString("FatherIncome", "");
        inventoryForm.fatherStatus = inventoryPrefs.getString("FatherStatus", "");

        inventoryForm.motherName = inventoryPrefs.getString("MotherName", "");
        inventoryForm.motherOccupation = inventoryPrefs.getString("MotherOccupation", "");
        inventoryForm.motherContact = inventoryPrefs.getString("MotherContact", "");
        inventoryForm.motherIncome = inventoryPrefs.getString("MotherIncome", "");
        inventoryForm.motherStatus = inventoryPrefs.getString("MotherStatus", "");

        inventoryForm.guardianName = inventoryPrefs.getString("GuardianName", "");
        inventoryForm.guardianContact = inventoryPrefs.getString("GuardianContact", "");

        inventoryForm.elementary = inventoryPrefs.getString("Elementary", "");
        inventoryForm.juniorHigh = inventoryPrefs.getString("JuniorHigh", "");
        inventoryForm.seniorHigh = inventoryPrefs.getString("SeniorHigh", "");
        inventoryForm.college = inventoryPrefs.getString("College", "");

        inventoryForm.sports = inventoryPrefs.getString("Sports", "");
        inventoryForm.hobbies = inventoryPrefs.getString("Hobbies", "");
        inventoryForm.talents = inventoryPrefs.getString("Talents", "");
        inventoryForm.socioCivic = inventoryPrefs.getString("SocioCivic", "");
        inventoryForm.schoolOrg = inventoryPrefs.getString("SchoolOrg", "");

        inventoryForm.wasHospitalized = inventoryPrefs.getBoolean("WasHospitalized", false);
        inventoryForm.hospitalizedReason = inventoryPrefs.getString("HospitalizedReason", "");
        inventoryForm.hadOperation = inventoryPrefs.getBoolean("HadOperation", false);
        inventoryForm.operationReason = inventoryPrefs.getString("OperationReason", "");
        inventoryForm.hasIllness = inventoryPrefs.getBoolean("HasIllness", false);
        inventoryForm.illnessDetails = inventoryPrefs.getString("IllnessDetails", "");
        inventoryForm.takesMedication = inventoryPrefs.getBoolean("TakesMedication", false);
        inventoryForm.medicationDetails = inventoryPrefs.getString("MedicationDetails", "");
        inventoryForm.hasMedicalCertificate = inventoryPrefs.getBoolean("HasMedicalCertificate", false);

        inventoryForm.familyIllness = inventoryPrefs.getString("FamilyIllness", "");
        inventoryForm.lastDoctorVisit = inventoryPrefs.getString("LastDoctorVisit", "");
        inventoryForm.visitReason = inventoryPrefs.getString("VisitReason", "");

        inventoryForm.lossExperience = inventoryPrefs.getString("LossExperience", "");
        inventoryForm.problems = inventoryPrefs.getString("Problems", "");
        inventoryForm.relationshipConcerns = inventoryPrefs.getString("RelationshipConcerns", "");


        // Safely parse spouseAge as Integer
        String spouseAgeStr = inventoryPrefs.getString("SpouseAge", "");
        try {
            inventoryForm.spouseAge = spouseAgeStr.isEmpty() ? null : Integer.parseInt(spouseAgeStr);
        } catch (NumberFormatException e) {
            inventoryForm.spouseAge = null; // fallback if parsing fails
        }

        // Load siblings and work experience as JSON
        String siblingsJson = inventoryPrefs.getString("Siblings", "[]");
        String workJson = inventoryPrefs.getString("WorkExperience", "[]");

        Gson gson = new Gson();
        Type siblingListType = new TypeToken<List<InventoryForm.Sibling>>(){}.getType();
        Type workListType = new TypeToken<List<InventoryForm.WorkExperience>>(){}.getType();

        inventoryForm.siblings = gson.fromJson(siblingsJson, siblingListType);
        inventoryForm.workExperience = gson.fromJson(workJson, workListType);

        // ✅ Clean the lists of empty entries
        inventoryForm.siblings.removeIf(s -> s.name == null || s.name.trim().isEmpty());
        inventoryForm.workExperience.removeIf(w -> w.company == null || w.company.trim().isEmpty());

        // 🔍 Optional logs to confirm cleaning worked
        Log.d("SIBLING_CHECK", new Gson().toJson(inventoryForm.siblings));
        Log.d("WORK_CHECK", new Gson().toJson(inventoryForm.workExperience));


        // 3. Career Form
        SharedPreferences careerPrefs = getSharedPreferences("CareerPlanningPrefs", MODE_PRIVATE);
        CareerPlanningForm careerForm = new CareerPlanningForm();

        careerForm.studentNo = careerPrefs.getString("studentNo", "");
        careerForm.program = careerPrefs.getString("program", "");
        careerForm.fullName = careerPrefs.getString("fullName", "");
        careerForm.gradeYear = careerPrefs.getString("gradeYear", "");
        careerForm.section = careerPrefs.getString("section", "");
        careerForm.gender = careerPrefs.getString("gender", "");
        careerForm.contactNumber = careerPrefs.getString("contact", "");
        careerForm.birthday = careerPrefs.getString("birthday", "");

        careerForm.topValue1 = careerPrefs.getString("topValue1", "");
        careerForm.topValue2 = careerPrefs.getString("topValue2", "");
        careerForm.topValue3 = careerPrefs.getString("topValue3", "");

        careerForm.topStrength1 = careerPrefs.getString("topStrength1", "");
        careerForm.topStrength2 = careerPrefs.getString("topStrength2", "");
        careerForm.topStrength3 = careerPrefs.getString("topStrength3", "");

        careerForm.topSkill1 = careerPrefs.getString("topSkill1", "");
        careerForm.topSkill2 = careerPrefs.getString("topSkill2", "");
        careerForm.topSkill3 = careerPrefs.getString("topSkill3", "");

        careerForm.topInterest1 = careerPrefs.getString("topInterest1", "");
        careerForm.topInterest2 = careerPrefs.getString("topInterest2", "");
        careerForm.topInterest3 = careerPrefs.getString("topInterest3", "");

        careerForm.programChoice = careerPrefs.getString("programChoice", "");
        careerForm.firstChoice = careerPrefs.getString("firstChoice", "");
        careerForm.originalChoice = careerPrefs.getString("originalChoice", "");
        careerForm.programExpectation = careerPrefs.getString("programExpectation", "");
        careerForm.enrollmentReason = careerPrefs.getString("enrollmentReason", "");
        careerForm.futureVision = careerPrefs.getString("futureVision", "");

        careerForm.mainPlan = careerPrefs.getString("mainPlan", "");
        careerForm.anotherCourse = careerPrefs.getBoolean("anotherCourse", false);
        careerForm.mastersProgram = careerPrefs.getBoolean("mastersProgram", false);
        careerForm.courseField = careerPrefs.getString("courseField", "");

        careerForm.localEmployment = careerPrefs.getBoolean("localEmployment", false);
        careerForm.workAbroad = careerPrefs.getBoolean("workAbroad", false);
        careerForm.natureJob1 = careerPrefs.getString("natureJob1", "");

        careerForm.employmentNature = careerPrefs.getString("employmentNature", "");
        careerForm.currentWorkNature = careerPrefs.getString("currentWorkNature", "");
        careerForm.programChoiceReason = careerPrefs.getString("programChoiceReason", "");
        careerForm.aimPromotion = careerPrefs.getBoolean("aimPromotion", false);
        careerForm.currentWorkAbroad = careerPrefs.getBoolean("currentWorkAbroad", false);
        careerForm.natureJob2 = careerPrefs.getString("natureJob2", "");

        careerForm.businessNature = careerPrefs.getString("businessNature", "");

        // -----------------------
        // COMBINE ALL FORMS
        // -----------------------

        FullRegistration fullRegistration = new FullRegistration();
        fullRegistration.student = student;
        fullRegistration.consentForm = consentForm;
        fullRegistration.inventoryForm = inventoryForm;
        fullRegistration.careerPlanningForm = careerForm;

        Toast.makeText(this, "Data collected. Ready to submit.", Toast.LENGTH_SHORT).show();

        gson = new GsonBuilder().setPrettyPrinting().create();
        Log.d("FULL_JSON", gson.toJson(fullRegistration));

        StudentApi api = ApiClient.getClient().create(StudentApi.class);
        Call<Void> call = api.submitFullRegistration(fullRegistration);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegistrationActivity.this, "All forms submitted successfully!", Toast.LENGTH_LONG).show();
                    finish(); // or go back to login
                } else {
                    Log.e("SUBMIT_ERROR", "Code: " + response.code() + ", Message: " + response.message());

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("SUBMIT_ERROR_BODY", errorBody); // 🔥 This reveals the real problem
                        }
                    } catch (Exception e) {
                        Log.e("SUBMIT_ERROR_BODY", "Error reading error body", e);
                    }

                    Toast.makeText(RegistrationActivity.this, "Submission failed: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegistrationActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

}