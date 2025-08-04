package com.example.stitarlacguidanceapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        // Position the splash image
        root.image.setX(1300);

        // Animate the splash
        root.image.animate()
                .translationXBy(-1300)
                .setDuration(100)
                .setStartDelay(500)
                .withEndAction(() -> root.image.animate()
                        .rotationBy(-360)
                        .setDuration(250)
                        .setStartDelay(250)
                        .withEndAction(() -> new Handler().postDelayed(() -> {
                                    SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
                                    int studentId = prefs.getInt("studentId", -1);

                                    Intent intent;
                                    if (studentId != -1) {
                                        intent = new Intent(SplashActivity.this, StudentDashboardActivity.class);
                                    } else {
                                        intent = new Intent(SplashActivity.this, MainActivity.class);
                                    }

                                    startActivity(intent);
                                    finish();

                                }, 1000) // 1 second delay
                        ));
    }

}