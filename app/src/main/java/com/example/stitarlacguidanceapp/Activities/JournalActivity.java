package com.example.stitarlacguidanceapp.Activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.stitarlacguidanceapp.PrivateJournalFragments.CalendarFragment;
import com.example.stitarlacguidanceapp.PrivateJournalFragments.JournalFragment;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivityJournalBinding;

public class JournalActivity extends AppCompatActivity {

    private ActivityJournalBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityJournalBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        //Showing calendar by default
        showFragment(new CalendarFragment());

        root.btnCalendarView.setOnClickListener(v -> showFragment(new CalendarFragment()));
        root.btnJournalView.setOnClickListener(v -> showFragment(new JournalFragment()));

        root.fabAddEntry.setOnClickListener(v -> {
            Toast.makeText(this, "Add Journal Entry tapped!", Toast.LENGTH_SHORT).show();
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fabContainer, fragment)
                .commit();
    }
}