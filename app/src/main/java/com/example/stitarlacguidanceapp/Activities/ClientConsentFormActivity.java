package com.example.stitarlacguidanceapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stitarlacguidanceapp.databinding.ActivityClientConsentFormBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class ClientConsentFormActivity extends AppCompatActivity {

    private ActivityClientConsentFormBinding root;
    private boolean isSubmitting = false;
    
    // Validation patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s\\-\\.']+$");
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityClientConsentFormBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        // Disable submit button initially
        root.btnSubmit.setEnabled(false);

        // Set up real-time validation
        setupRealTimeValidation();

        // Enable the submit button when checkbox is checked and form is valid
        root.checkboxConsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkFormValidity();
        });

        // When Submit button is clicked
        root.btnSubmit.setOnClickListener(view -> {
            if (isSubmitting) {
                return; // Prevent double submission
            }
            
            if (!validateForm()) {
                return; // Form validation failed
            }
            
            submitForm();
        });

    }
    
    /**
     * Set up real-time validation for all input fields
     */
    private void setupRealTimeValidation() {
        // Student name validation
        root.edtStudentName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateStudentName();
                checkFormValidity();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Parent name validation
        root.edtParentName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateParentName();
                checkFormValidity();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    /**
     * Validate student name with comprehensive checks
     */
    private boolean validateStudentName() {
        String name = root.edtStudentName.getText().toString().trim();
        
        if (name.isEmpty()) {
            root.edtStudentName.setError("Student name is required");
            return false;
        }
        
        if (name.length() < MIN_NAME_LENGTH) {
            root.edtStudentName.setError("Name must be at least " + MIN_NAME_LENGTH + " characters");
            return false;
        }
        
        if (name.length() > MAX_NAME_LENGTH) {
            root.edtStudentName.setError("Name must not exceed " + MAX_NAME_LENGTH + " characters");
            return false;
        }
        
        if (!NAME_PATTERN.matcher(name).matches()) {
            root.edtStudentName.setError("Name contains invalid characters. Only letters, spaces, hyphens, periods, and apostrophes are allowed");
            return false;
        }
        
        // Check for multiple consecutive spaces
        if (name.contains("  ")) {
            root.edtStudentName.setError("Name cannot contain multiple consecutive spaces");
            return false;
        }
        
        root.edtStudentName.setError(null);
        return true;
    }
    
    /**
     * Validate parent/guardian name with comprehensive checks
     */
    private boolean validateParentName() {
        String name = root.edtParentName.getText().toString().trim();
        
        if (name.isEmpty()) {
            root.edtParentName.setError("Parent/Guardian name is required");
            return false;
        }
        
        if (name.length() < MIN_NAME_LENGTH) {
            root.edtParentName.setError("Name must be at least " + MIN_NAME_LENGTH + " characters");
            return false;
        }
        
        if (name.length() > MAX_NAME_LENGTH) {
            root.edtParentName.setError("Name must not exceed " + MAX_NAME_LENGTH + " characters");
            return false;
        }
        
        if (!NAME_PATTERN.matcher(name).matches()) {
            root.edtParentName.setError("Name contains invalid characters. Only letters, spaces, hyphens, periods, and apostrophes are allowed");
            return false;
        }
        
        // Check for multiple consecutive spaces
        if (name.contains("  ")) {
            root.edtParentName.setError("Name cannot contain multiple consecutive spaces");
            return false;
        }
        
        root.edtParentName.setError(null);
        return true;
    }
    
    /**
     * Check if the entire form is valid and enable/disable submit button accordingly
     */
    private void checkFormValidity() {
        boolean isFormValid = root.checkboxConsent.isChecked() && 
                             validateStudentName() && 
                             validateParentName();
        
        root.btnSubmit.setEnabled(isFormValid);
    }
    
    /**
     * Comprehensive form validation before submission
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate consent checkbox
        if (!root.checkboxConsent.isChecked()) {
            Toast.makeText(this, "You must agree to the terms before submitting", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Validate student name
        if (!validateStudentName()) {
            root.edtStudentName.requestFocus();
            isValid = false;
        }
        
        // Validate parent name
        if (!validateParentName()) {
            root.edtParentName.requestFocus();
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Submit the form with proper state management
     */
    private void submitForm() {
        isSubmitting = true;
        root.btnSubmit.setEnabled(false);
        root.btnSubmit.setText("Submitting...");
        
        try {
            boolean isAgreed = root.checkboxConsent.isChecked();
            String studentName = root.edtStudentName.getText().toString().trim();
            String parentName = root.edtParentName.getText().toString().trim();

            // Save to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("OnboardingData", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Consent_IsAgreed", isAgreed);
            editor.putString("Consent_StudentName", studentName);
            editor.putString("Consent_ParentName", parentName);

            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            editor.putString("Consent_SignedDate", currentDate);
            editor.apply();

            Toast.makeText(this, "Consent form submitted successfully!", Toast.LENGTH_SHORT).show();

            // Go to next screen
            startActivity(new Intent(ClientConsentFormActivity.this, InventoryFormActivity.class));
            finish(); // Prevent going back to this form
            
        } catch (Exception e) {
            Toast.makeText(this, "Error submitting form. Please try again.", Toast.LENGTH_SHORT).show();
            resetSubmitButton();
        }
    }
    
    /**
     * Reset submit button state in case of error
     */
    private void resetSubmitButton() {
        isSubmitting = false;
        root.btnSubmit.setText("Submit");
        checkFormValidity();
    }
}
