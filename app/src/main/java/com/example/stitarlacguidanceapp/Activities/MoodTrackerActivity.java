package com.example.stitarlacguidanceapp.Activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stitarlacguidanceapp.ExitFormFragments.WelcomeFragment;
import com.example.stitarlacguidanceapp.MoodTrackerFragments.MTWelcomeFragment;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivityMoodTrackerBinding;

public class MoodTrackerActivity extends AppCompatActivity {

    private ActivityMoodTrackerBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityMoodTrackerBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        //Loading Welcome Fragment only once
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MTWelcomeFragment())
                    .commit();
        }
    }
}