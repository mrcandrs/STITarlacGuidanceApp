package com.example.stitarlacguidanceapp.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.Adapters.ExitFormAdapter;
import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.Models.ExitInterviewForm;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivityExitFormBinding;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        int studentId = getSharedPreferences("student_session", MODE_PRIVATE)
                .getInt("studentId", 0);
        formData.studentId = studentId;

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
        ExitInterviewForm form = getFormData();

        // ✅ Convert specificReasonsArray to comma-separated string
        form.specificReasons = TextUtils.join(",", form.specificReasonsArray);

        // ✅ Convert serviceResponses Map to JSON
        Gson gson = new Gson();
        form.serviceResponsesJson = gson.toJson(form.serviceResponses);

        Log.d("ExitFormPayload", gson.toJson(form));
        // ✅ Call Retrofit
        ApiClient.getExitInterviewApi().submitExitForm(form).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ExitFormActivity.this, "Form submitted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("ExitFormError", errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ExitFormActivity.this, "Submission failed: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ExitFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}