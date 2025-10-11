package com.example.stitarlacguidanceapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.DuplicateCheckResponse;
import com.example.stitarlacguidanceapp.Models.CareerPlanningForm;
import com.example.stitarlacguidanceapp.Models.ConsentForm;
import com.example.stitarlacguidanceapp.Models.FullRegistration;
import com.example.stitarlacguidanceapp.Models.InventoryForm;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.StudentApi;
import com.example.stitarlacguidanceapp.databinding.ActivityRegistrationBinding;
import com.example.stitarlacguidanceapp.Models.Student;

import com.google.android.material.textfield.TextInputEditText;
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

        //Live validation for email address
        addLiveValidation(root.layoutEmail, root.edtEmail, input -> {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
                    ? null
                    : "Invalid email address";
        });

        //Live validation for password
        addLiveValidation(root.layoutStudentPassword, root.edtStudentPassword, input -> {
            String passwordPattern = "^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{6,}$";
            return input.matches(passwordPattern)
                    ? null
                    : "Password must be 6+ characters, include 1 uppercase and 1 special character";
        });



        //getStringExtra for Student Number
        String studentNumber = getIntent().getStringExtra("StudentNumber");
        if (studentNumber != null) {
            root.edtStudentNumber.setText(studentNumber);
            root.edtStudentNumber.setEnabled(false);
            root.edtStudentNumber.setBackgroundColor(Color.parseColor("#696969"));
            root.edtStudentNumber.setTextColor(Color.LTGRAY);
        }

        //getStringExtra for Full Name
        String fullName = getIntent().getStringExtra("FullName");
        if (fullName != null) {
            root.edtFullName.setText(fullName);
            root.edtFullName.setEnabled(false);
            root.edtFullName.setBackgroundColor(Color.parseColor("#696969"));
            root.edtFullName.setTextColor(Color.LTGRAY);
        }

        //getStringExtra for Program
        String program = getIntent().getStringExtra("program");
        if (program != null) {
            root.edtProgram.setText(program);
            root.edtProgram.setEnabled(false);
            root.edtProgram.setBackgroundColor(Color.parseColor("#696969"));
            root.edtProgram.setTextColor(Color.LTGRAY);
        }

        //getStringExtra for Grade/Year
        String gradeYear = getIntent().getStringExtra("gradeYear");
        if (gradeYear != null) {
            root.edtGradeYear.setText(gradeYear);
            root.edtGradeYear.setEnabled(false);
            root.edtGradeYear.setBackgroundColor(Color.parseColor("#696969"));
            root.edtGradeYear.setTextColor(Color.LTGRAY);
        }

        // Prefill Email Address from Individual Inventory form
        SharedPreferences inventoryPrefs = getSharedPreferences("InventoryForm", MODE_PRIVATE);
        String emailFromInventory = inventoryPrefs.getString("Email1", "");
        if (!emailFromInventory.isEmpty()) {
            root.edtEmail.setText(emailFromInventory);
            root.edtEmail.setEnabled(false);
            root.edtEmail.setBackgroundColor(Color.parseColor("#696969"));
            root.edtEmail.setTextColor(Color.LTGRAY);
        }


        // Terms and Conditions click listener
        root.txtTermsConditionsLink.setOnClickListener(v -> showTermsAndConditionsDialog());
        
        // Terms checkbox listener to enable/disable Register button
        root.checkboxTermsConditions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkAllFieldsAndEnableButton();
        });
        
        // Add listeners to all required fields to check validation
        root.edtStudentUsername.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                checkAllFieldsAndEnableButton();
            }
        });
        
        root.edtStudentPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                checkAllFieldsAndEnableButton();
            }
        });
        
        root.btnRegister.setOnClickListener(v -> checkDuplicateThenSave());
        root.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void checkAllFieldsAndEnableButton() {
        // Check if all required fields are filled and Terms are accepted
        boolean usernameFilled = !root.edtStudentUsername.getText().toString().trim().isEmpty();
        boolean passwordFilled = !root.edtStudentPassword.getText().toString().trim().isEmpty();
        boolean termsAccepted = root.checkboxTermsConditions.isChecked();
        
        // Enable button only if all conditions are met
        root.btnRegister.setEnabled(usernameFilled && passwordFilled && termsAccepted);
    }

    private void addLiveValidation(com.google.android.material.textfield.TextInputLayout layout,
                                   android.widget.EditText editText,
                                   ValidationRule rule) {

        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String input = s.toString().trim();
                String error = rule.validate(input);
                layout.setError(error); // Show or clear error in TextInputLayout
            }
        });
    }

    private void checkDuplicateThenSave() {
        // Check if Terms and Conditions are accepted
        if (!root.checkboxTermsConditions.isChecked()) {
            Toast.makeText(this, "Please accept the Terms and Conditions to continue", Toast.LENGTH_LONG).show();
            return;
        }
        
        String email = root.edtEmail.getText().toString().trim();
        String username = root.edtStudentUsername.getText().toString().trim();

        root.btnRegister.setEnabled(false);

        StudentApi api = ApiClient.getClient().create(StudentApi.class);
        Call<DuplicateCheckResponse> call = api.checkDuplicate(email, username);

        call.enqueue(new Callback<DuplicateCheckResponse>() {
            @Override
            public void onResponse(Call<DuplicateCheckResponse> call, Response<DuplicateCheckResponse> response) {
                root.btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    boolean emailExists = response.body().isEmailExists();
                    boolean userNameExists = response.body().isUserNameExists();

                    showFieldErrors(emailExists, userNameExists);

                    // ✅ Only call registerStudent if both are false
                    if (!emailExists && !userNameExists) {
                        registerStudent();
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String json = response.errorBody().string();
                            Gson gson = new Gson();
                            DuplicateCheckResponse error = gson.fromJson(json, DuplicateCheckResponse.class);

                            boolean emailExists = error.isEmailExists();
                            boolean userNameExists = error.isUserNameExists();

                            Log.d("DUPLICATE_JSON", json);
                            Log.d("DUPLICATE_CHECK", "emailExists=" + emailExists + ", userNameExists=" + userNameExists);

                            showFieldErrors(emailExists, userNameExists);

                        }
                    } catch (Exception e) {
                        Log.e("PARSE_ERROR", "Failed to parse errorBody", e);
                    }

                    Toast.makeText(RegistrationActivity.this, "Server rejected submission", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DuplicateCheckResponse> call, Throwable t) {
                root.btnRegister.setEnabled(true);
                Toast.makeText(RegistrationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showFieldErrors(boolean emailExists, boolean userNameExists) {
        if (emailExists) {
            root.layoutEmail.setError("Email already exists");
            root.edtEmail.post(() -> {
                root.edtEmail.requestFocus();
                root.edtEmail.getParent().requestChildFocus(root.edtStudentUsername, root.edtStudentUsername);
            });

        } else {
            root.layoutEmail.setError(null);
        }

        if (userNameExists) {
            root.layoutStudentUsername.setError("Username already exists");
            root.edtStudentUsername.post(() -> {
                root.edtStudentUsername.requestFocus();
                root.edtStudentUsername.getParent().requestChildFocus(root.edtStudentUsername, root.edtStudentUsername);
            });

        } else {
            root.layoutStudentUsername.setError(null);
        }
    }

    private void showTermsAndConditionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_terms_conditions, null);
        
        // Set the terms content with proper formatting
        TextView txtTermsContent = dialogView.findViewById(R.id.txtTermsContent);
        
        // Create properly formatted terms text
        StringBuilder termsBuilder = new StringBuilder();
        termsBuilder.append("TERMS AND CONDITIONS FOR STI TARLAC GUIDANCE APP\n\n");
        termsBuilder.append("By using the STI Tarlac Guidance App, you agree to the following terms and conditions:\n\n");
        
        termsBuilder.append("1. ACCEPTANCE OF TERMS\n\n");
        termsBuilder.append("By creating an account and using this application, you acknowledge that you have read, understood, and agree to be bound by these Terms and Conditions.\n\n");
        
        termsBuilder.append("2. APP PURPOSE AND SERVICES\n\n");
        termsBuilder.append("This application provides the following services to STI Tarlac students:\n\n");
        termsBuilder.append("• Guidance and Counseling Appointments\n");
        termsBuilder.append("• Daily Mood Tracking and Emotional Assessment\n");
        termsBuilder.append("• Private Journal and Personal Reflection Tools\n");
        termsBuilder.append("• Career Planning and Academic Guidance\n");
        termsBuilder.append("• Personal Inventory and Assessment Forms\n");
        termsBuilder.append("• Referral Services to Professional Counselors\n");
        termsBuilder.append("• Exit Interview Management\n");
        termsBuilder.append("• Profile and Settings Management\n\n");
        
        termsBuilder.append("3. USER ACCOUNT AND REGISTRATION\n\n");
        termsBuilder.append("• You must provide accurate and complete information during registration\n");
        termsBuilder.append("• You are responsible for maintaining the confidentiality of your account credentials\n");
        termsBuilder.append("• You must notify the guidance office immediately of any unauthorized use of your account\n");
        termsBuilder.append("• Only current STI Tarlac students are eligible to use this application\n\n");
        
        termsBuilder.append("4. PRIVACY AND CONFIDENTIALITY\n\n");
        termsBuilder.append("• All personal information and data shared through this app is strictly confidential\n");
        termsBuilder.append("• Your counseling sessions, journal entries, and personal assessments are protected\n");
        termsBuilder.append("• Information may only be shared with authorized guidance counselors and staff\n");
        termsBuilder.append("• Data may be used for academic and counseling purposes only\n");
        termsBuilder.append("• We comply with applicable privacy laws and regulations\n\n");
        
        termsBuilder.append("5. DATA COLLECTION AND USAGE\n\n");
        termsBuilder.append("The app collects the following types of data:\n\n");
        termsBuilder.append("• Personal information (name, student number, contact details)\n");
        termsBuilder.append("• Academic information (program, year level)\n");
        termsBuilder.append("• Emotional and psychological assessments\n");
        termsBuilder.append("• Journal entries and personal reflections\n");
        termsBuilder.append("• Appointment scheduling and counseling records\n");
        termsBuilder.append("• Career planning and inventory responses\n\n");
        
        termsBuilder.append("6. USER RESPONSIBILITIES\n\n");
        termsBuilder.append("• Use the app only for legitimate educational and counseling purposes\n");
        termsBuilder.append("• Provide honest and accurate information in all forms and assessments\n");
        termsBuilder.append("• Respect the confidentiality of other users' information\n");
        termsBuilder.append("• Report any technical issues or concerns to the guidance office\n");
        termsBuilder.append("• Maintain appropriate and respectful communication\n\n");
        
        termsBuilder.append("7. PROHIBITED ACTIVITIES\n\n");
        termsBuilder.append("You may not:\n\n");
        termsBuilder.append("• Share your account credentials with others\n");
        termsBuilder.append("• Attempt to access other users' accounts or data\n");
        termsBuilder.append("• Use the app for any illegal or unauthorized purposes\n");
        termsBuilder.append("• Submit false or misleading information\n");
        termsBuilder.append("• Interfere with the app's functionality or security\n\n");
        
        termsBuilder.append("8. MOOD TRACKER SPECIFIC TERMS\n\n");
        termsBuilder.append("• Daily mood tracking is voluntary but encouraged for better counseling support\n");
        termsBuilder.append("• Mood data is used to provide personalized guidance and support\n");
        termsBuilder.append("• A 24-hour cooldown period applies between mood tracking sessions\n");
        termsBuilder.append("• Mood data is confidential and used only by authorized guidance staff\n\n");
        
        termsBuilder.append("9. JOURNAL AND PRIVACY\n\n");
        termsBuilder.append("• Private journal entries are encrypted and stored securely\n");
        termsBuilder.append("• Journal content is accessible only to you and authorized guidance counselors\n");
        termsBuilder.append("• Entries may be discussed during counseling sessions with your consent\n");
        termsBuilder.append("• You can edit or delete your journal entries at any time\n\n");
        
        termsBuilder.append("10. APPOINTMENT SCHEDULING\n\n");
        termsBuilder.append("• Appointments are subject to counselor availability\n");
        termsBuilder.append("• Cancellations should be made at least 24 hours in advance\n");
        termsBuilder.append("• No-show policies may apply as per guidance office procedures\n");
        termsBuilder.append("• Emergency situations may require immediate counselor contact\n\n");
        
        termsBuilder.append("11. DATA RETENTION AND DELETION\n\n");
        termsBuilder.append("• Your data will be retained for the duration of your enrollment at STI Tarlac\n");
        termsBuilder.append("• Upon graduation or withdrawal, data retention policies will apply\n");
        termsBuilder.append("• You may request data deletion subject to legal and administrative requirements\n");
        termsBuilder.append("• Some data may be retained for statistical and research purposes (anonymized)\n\n");
        
        termsBuilder.append("12. TECHNICAL SUPPORT AND AVAILABILITY\n\n");
        termsBuilder.append("• The app is provided \"as is\" without warranties\n");
        termsBuilder.append("• Technical support is available during regular office hours\n");
        termsBuilder.append("• The app may experience downtime for maintenance or updates\n");
        termsBuilder.append("• We strive to maintain 99% uptime but cannot guarantee uninterrupted service\n\n");
        
        termsBuilder.append("13. MODIFICATIONS TO TERMS\n\n");
        termsBuilder.append("• These terms may be updated periodically\n");
        termsBuilder.append("• Users will be notified of significant changes\n");
        termsBuilder.append("• Continued use of the app constitutes acceptance of modified terms\n");
        termsBuilder.append("• Previous versions of terms are available upon request\n\n");
        
        termsBuilder.append("14. TERMINATION\n\n");
        termsBuilder.append("• Your access to the app may be terminated for violation of these terms\n");
        termsBuilder.append("• Termination does not affect the confidentiality of your data\n");
        termsBuilder.append("• You may request account deletion at any time\n");
        termsBuilder.append("• Data retention policies apply even after account termination\n\n");
        
        termsBuilder.append("15. CONTACT INFORMATION\n\n");
        termsBuilder.append("For questions about these Terms and Conditions, contact:\n\n");
        termsBuilder.append("STI Tarlac Guidance Office\n");
        termsBuilder.append("Email: guidance@sti.edu.ph\n");
        termsBuilder.append("Phone: (045) 982-0115\n");
        termsBuilder.append("Office Hours: Monday-Friday, 8:00 AM - 5:00 PM\n\n");
        
        termsBuilder.append("16. GOVERNING LAW\n\n");
        termsBuilder.append("These terms are governed by Philippine law and STI Tarlac institutional policies.\n\n");
        
        termsBuilder.append("By checking the \"I agree to the Terms and Conditions\" checkbox, you confirm that you have read, understood, and agree to be bound by these Terms and Conditions.");
        
        txtTermsContent.setText(termsBuilder.toString());
        
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        Button btnAccept = dialogView.findViewById(R.id.btnAcceptTerms);
        Button btnDecline = dialogView.findViewById(R.id.btnDeclineTerms);
        
        btnAccept.setOnClickListener(v -> {
            root.checkboxTermsConditions.setChecked(true);
            dialog.dismiss();
        });
        
        btnDecline.setOnClickListener(v -> {
            root.checkboxTermsConditions.setChecked(false);
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void registerStudent() {
        root.btnRegister.setEnabled(false);

        String studentNumber = root.edtStudentNumber.getText().toString();
        String fullName = root.edtFullName.getText().toString();
        String program = root.edtProgram.getText().toString();
        String gradeYear = root.edtGradeYear.getText().toString();
        String email = root.edtEmail.getText().toString();
        String username = root.edtStudentUsername.getText().toString();
        String password = root.edtStudentPassword.getText().toString();

        if (studentNumber.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        //Create student object
        Student student = new Student(studentNumber, fullName, program, gradeYear, email, username, password);

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
        Type siblingListType = new TypeToken<List<InventoryForm.Sibling>>() {
        }.getType();
        Type workListType = new TypeToken<List<InventoryForm.WorkExperience>>() {
        }.getType();

        inventoryForm.siblings = gson.fromJson(siblingsJson, siblingListType);
        inventoryForm.workExperience = gson.fromJson(workJson, workListType);

        // ✅ Clean the lists of empty entries
        inventoryForm.siblings.removeIf(s -> s.name == null || s.name.trim().isEmpty());
        inventoryForm.workExperience.removeIf(w -> w.company == null || w.company.trim().isEmpty());


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

        //Toast.makeText(this, "Data collected. Ready to submit.", Toast.LENGTH_SHORT).show();

        gson = new GsonBuilder().setPrettyPrinting().create();
        Log.d("FULL_JSON", gson.toJson(fullRegistration));

        StudentApi api = ApiClient.getClient().create(StudentApi.class);
        Call<Void> call = api.submitFullRegistration(fullRegistration);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("registered", true);
                    startActivity(intent);
                } else {
                    Log.e("SUBMIT_ERROR", "Code: " + response.code() + ", Message: " + response.message());

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("SUBMIT_ERROR_BODY", errorBody); // 🔥 This reveals the real problem

                            Gson gson = new Gson();
                            DuplicateCheckResponse error = gson.fromJson(errorBody, DuplicateCheckResponse.class);

                            boolean emailExists = error.isEmailExists();
                            boolean userNameExists = error.isUserNameExists();

                            // 🔥 Add this line
                            showFieldErrors(emailExists, userNameExists);
                        }
                    } catch (Exception e) {
                        Log.e("SUBMIT_ERROR_BODY", "Error reading error body", e);
                    }
                    root.btnRegister.setEnabled(true);
                    //Toast.makeText(RegistrationActivity.this, "Submission failed: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegistrationActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                root.btnRegister.setEnabled(true);
            }
        });
    }
    public interface ValidationRule {
        String validate(String input);
    }
}
