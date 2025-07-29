package com.example.stitarlacguidanceapp.MoodTrackerFragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.stitarlacguidanceapp.Activities.StudentDashboardActivity;
import com.example.stitarlacguidanceapp.Models.MoodViewModel;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.FragmentMoodTrackerResultBinding;


public class MoodTrackerResultFragment extends Fragment {

    private FragmentMoodTrackerResultBinding root;

    public MoodTrackerResultFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentMoodTrackerResultBinding.inflate(inflater, container, false);

        MoodViewModel viewModel = new ViewModelProvider(requireActivity()).get(MoodViewModel.class);
        int totalScore = viewModel.getScore();

        String moodLevel;
        String description;
        int moodIcon;

        if (totalScore <= 5) {
            moodLevel = "MILD";
            description = "Based on your responses, your mood level is in the mild range. This suggests you're experiencing relatively good emotional well-being today.";
            moodIcon = R.drawable.mild;
        } else if (totalScore <= 11) {
            moodLevel = "MODERATE";
            description = "Based on your responses, your mood level is moderate. Consider reaching out to someone you trust or practicing self-care activities.";
            moodIcon = R.drawable.moderate;
        } else {
            moodLevel = "HIGH";
            description = "Your responses indicate a higher level of concern. Consider speaking with a counselor, teacher, or trusted adult for additional support.";
            moodIcon = R.drawable.highmood;
        }

        root.txtMoodLevel.setText(moodLevel);
        root.txtMoodDescription.setText(description);
        root.imgMoodFace.setImageResource(moodIcon);

        //Save current time to SharedPreferences for cooldown
        SharedPreferences prefs = requireActivity().getSharedPreferences("MoodPrefs", Context.MODE_PRIVATE);
        prefs.edit().putLong("lastMoodTimestamp", System.currentTimeMillis()).apply();

        root.btnReturnDashboard.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), StudentDashboardActivity.class));
        });

        return root.getRoot();
    }

    //🔒 Disable back button
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //do nothing
            }
        });
    }
}

