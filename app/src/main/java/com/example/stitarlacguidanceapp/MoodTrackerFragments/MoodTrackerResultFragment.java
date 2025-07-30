package com.example.stitarlacguidanceapp.MoodTrackerFragments;

import static android.graphics.Color.parseColor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.stitarlacguidanceapp.Activities.StudentDashboardActivity;
import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.Models.MoodTrackerModel;
import com.example.stitarlacguidanceapp.Models.MoodViewModel;
import com.example.stitarlacguidanceapp.MoodTrackerApi;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.FragmentMoodTrackerResultBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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

        //Display score
        root.txtTotalScore.setText("Your Score: " + totalScore);

        //Set mood level info
        root.txtMoodLevel.setText(moodLevel);
        root.txtMoodDescription.setText(description);
        root.imgMoodFace.setImageResource(moodIcon);

        //Optionally set badge-style color for mood level
        int color;
        if (moodLevel.equals("MILD")) {
            color = parseColor("#34C759");
        } else if (moodLevel.equals("MODERATE")) {
            color = parseColor("#009951");
        } else {
            color = parseColor("#1B5E20");
        }
        root.txtMoodLevel.setTextColor(color);

        // Prepare data for API call
        SharedPreferences sessionPrefs = requireActivity().getSharedPreferences("student_session", Context.MODE_PRIVATE);
        int studentId = sessionPrefs.getInt("studentId", -1);

        String entryDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
        MoodTrackerModel mood = new MoodTrackerModel(studentId, moodLevel, entryDate);

        //Call API
        MoodTrackerApi apiService = ApiClient.getClient().create(MoodTrackerApi.class);
        Call<MoodTrackerModel> call = apiService.submitMood(mood);

        call.enqueue(new Callback<MoodTrackerModel>() {
            @Override
            public void onResponse(Call<MoodTrackerModel> call, Response<MoodTrackerModel> response) {
                if (response.isSuccessful()) {
                    Log.d("MoodTracker", "Mood result saved successfully.");
                } else {
                    Log.e("MoodTracker", "Failed to save mood: " + response.code());

                    // Try to log the full error body from server
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("MoodTracker", "Server error body: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e("MoodTracker", "Error reading errorBody: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<MoodTrackerModel> call, Throwable t) {
                Log.e("MoodTracker", "Error: " + t.getMessage());
            }
        });

        //Save current time to SharedPreferences for cooldown
        SharedPreferences moodPrefs = requireActivity().getSharedPreferences("MoodPrefs", Context.MODE_PRIVATE);
        moodPrefs.edit().putLong("lastMoodTimestamp", System.currentTimeMillis()).apply();

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

