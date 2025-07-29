package com.example.stitarlacguidanceapp.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        // Only load fragments on first launch
        if (savedInstanceState == null) {

            SharedPreferences prefs = getSharedPreferences("MoodPrefs", MODE_PRIVATE);
            long lastTime = prefs.getLong("lastMoodTimestamp", 0);
            long now = System.currentTimeMillis();

            //Allowed to proceed: Show welcome screen
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MTWelcomeFragment())
                    .commit();

            /*if (now - lastTime < 24 * 60 * 60 * 1000) {
                long hoursLeft = (24 * 60 * 60 * 1000 - (now - lastTime)) / (1000 * 60 * 60);
                Toast.makeText(this, "You can take the Mood Tracker again in " + hoursLeft + " hours.", Toast.LENGTH_LONG).show();

                // Optional: finish() if you don't want to proceed
                finish();

            } else {
                //Allowed to proceed: Show welcome screen
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MTWelcomeFragment())
                        .commit();
            }*/
        }
    }
}
