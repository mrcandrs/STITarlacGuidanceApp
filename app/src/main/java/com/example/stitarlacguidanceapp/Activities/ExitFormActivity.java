package com.example.stitarlacguidanceapp.Activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.Adapters.ExitFormAdapter;
import com.example.stitarlacguidanceapp.Models.ExitInterviewForm;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivityExitFormBinding;

public class ExitFormActivity extends AppCompatActivity {

    private ActivityExitFormBinding root;

    //logic for progress
    private final int totalSteps = 4;
    private int currentStep = 0;

    public ExitInterviewForm formData = new ExitInterviewForm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityExitFormBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());


        root.viewPager.setAdapter(new ExitFormAdapter(this));
        root.viewPager.setUserInputEnabled(false); //prevents swiping

        updateStepUI();

        //logic for previous and next buttons
        root.btnNext.setOnClickListener(v -> {
            if (currentStep < totalSteps - 1) {
                currentStep++;
                root.viewPager.setCurrentItem(currentStep);
                updateStepUI();
            } else {
                submitForm();
            }
        });

        root.btnPrevious.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                root.viewPager.setCurrentItem(currentStep);
                updateStepUI();
            }
        });
    }

    private void updateStepUI() {
        root.txtStep.setText("Step " + (currentStep + 1) + " of " + totalSteps);
        root.progressBar.setProgress((currentStep + 1) * 100 / totalSteps);
        root.btnPrevious.setEnabled(currentStep > 0);
        root.btnNext.setText(currentStep == totalSteps - 1 ? "Submit" : "Next");
    }

    public void goToNextStep() {
        if (currentStep < totalSteps - 1) {
            currentStep++;
            root.viewPager.setCurrentItem(currentStep);
            updateStepUI();
        }
    }

    public ExitInterviewForm getFormData() {
        return formData;
    }

    private void submitForm() {
        //TODO: Replace with Retrofit API call later
        Toast.makeText(this, "Submitted Successfully", Toast.LENGTH_SHORT).show();
    }
}