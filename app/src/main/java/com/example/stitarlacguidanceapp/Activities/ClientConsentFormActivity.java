package com.example.stitarlacguidanceapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stitarlacguidanceapp.databinding.ActivityClientConsentFormBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClientConsentFormActivity extends AppCompatActivity {

    private ActivityClientConsentFormBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityClientConsentFormBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        // Disable submit button initially
        root.btnSubmit.setEnabled(false);

        // Enable the submit button when checkbox is checked
        root.checkboxConsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            root.btnSubmit.setEnabled(isChecked);
        });

        // When Submit button is clicked
        root.btnSubmit.setOnClickListener(view -> {
            boolean isAgreed = root.checkboxConsent.isChecked();
            String studentName = root.edtStudentName.getText().toString().trim();
            String parentName = root.edtParentName.getText().toString().trim();

            // Validate inputs
            if (studentName.isEmpty()) {
                root.edtStudentName.setError("Please enter the student's name");
                root.edtStudentName.requestFocus();
                return;
            }

            if (parentName.isEmpty()) {
                root.edtParentName.setError("Please enter the parent/guardian's name");
                root.edtParentName.requestFocus();
                return;
            }


            // Save to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("OnboardingData", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Consent_IsAgreed", isAgreed);
            editor.putString("Consent_ParentName", parentName);

            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            editor.putString("Consent_SignedDate", currentDate);
            editor.apply();

            Toast.makeText(this, "Consent saved.", Toast.LENGTH_SHORT).show();

            // Go to next screen
            startActivity(new Intent(ClientConsentFormActivity.this, InventoryFormActivity.class));
        });

    }
}
