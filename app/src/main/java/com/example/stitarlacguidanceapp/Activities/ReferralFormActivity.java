package com.example.stitarlacguidanceapp.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivityReferralFormBinding;

public class ReferralFormActivity extends AppCompatActivity {

    private ActivityReferralFormBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityReferralFormBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        LinearLayout layoutFeedbackSection = findViewById(R.id.layoutFeedbackSection);

        root.btnToggleFeedback.setOnClickListener(v -> {
            if (layoutFeedbackSection.getVisibility() == View.GONE) {
                layoutFeedbackSection.setVisibility(View.VISIBLE);
                root.btnToggleFeedback.setText("Hide Feedback");
            } else {
                layoutFeedbackSection.setVisibility(View.GONE);
                root.btnToggleFeedback.setText("View Feedback");
            }
        });
    }
}