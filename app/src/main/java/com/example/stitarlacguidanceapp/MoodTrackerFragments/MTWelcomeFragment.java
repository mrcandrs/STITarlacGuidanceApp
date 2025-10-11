package com.example.stitarlacguidanceapp.MoodTrackerFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.stitarlacguidanceapp.Activities.MoodTrackerActivity;
import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.MoodTrackerApi;
import com.example.stitarlacguidanceapp.Models.MoodCooldownResponse;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.FragmentMtwelcomeBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MTWelcomeFragment extends Fragment {

    private FragmentMtwelcomeBinding root;

    //Empty constructor
    public MTWelcomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentMtwelcomeBinding.inflate(inflater, container, false);

        //Getting student number from SharedPreferences
        MoodTrackerActivity activity = (MoodTrackerActivity) getActivity();
        SharedPreferences sessionPrefs = requireActivity().getSharedPreferences("student_session", Context.MODE_PRIVATE);
        int studentId = sessionPrefs.getInt("studentId", -1);

        // Check server-side cooldown status first
        checkServerCooldownStatus(studentId);

        SharedPreferences prefs = activity.getSharedPreferences("student_session", Context.MODE_PRIVATE); //Shared Preferences from login

        String studentNumber = prefs.getString("studentNumber", "N/A");

        //Setting the student number in the TextView
        root.txtStudentNumber.setText(studentNumber);

        root.btnStart.setOnClickListener(v -> {
            // Check cooldown again before allowing start
            checkCooldownBeforeStart(studentId);
        });

        return root.getRoot();
    }

    private void checkServerCooldownStatus(int studentId) {
        MoodTrackerApi apiService = ApiClient.getClient().create(MoodTrackerApi.class);
        Call<MoodCooldownResponse> call = apiService.checkCooldown(studentId);

        call.enqueue(new Callback<MoodCooldownResponse>() {
            @Override
            public void onResponse(Call<MoodCooldownResponse> call, Response<MoodCooldownResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MoodCooldownResponse cooldownResponse = response.body();
                    updateLastTakenDisplay(cooldownResponse.getLastMoodTimestamp());
                } else {
                    // Fallback to local check if server fails
                    Log.e("MoodTracker", "Server cooldown check failed, falling back to local check");
                    fallbackToLocalLastTaken(studentId);
                }
            }

            @Override
            public void onFailure(Call<MoodCooldownResponse> call, Throwable t) {
                Log.e("MoodTracker", "Network error checking cooldown: " + t.getMessage());
                // Fallback to local check if network fails
                fallbackToLocalLastTaken(studentId);
            }
        });
    }

    private void updateLastTakenDisplay(long lastMoodTimestamp) {
        if (lastMoodTimestamp != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            root.txtLastTaken.setText("Last taken: " + sdf.format(new Date(lastMoodTimestamp)));
        } else {
            root.txtLastTaken.setText("Last taken: Not yet taken");
        }
    }

    private void fallbackToLocalLastTaken(int studentId) {
        SharedPreferences moodprefs = requireActivity().getSharedPreferences("MoodPrefs_" + studentId, Context.MODE_PRIVATE);
        long lastMillis = moodprefs.getLong("lastMoodTimestamp", 0);
        updateLastTakenDisplay(lastMillis);
    }

    private void checkCooldownBeforeStart(int studentId) {
        MoodTrackerApi apiService = ApiClient.getClient().create(MoodTrackerApi.class);
        Call<MoodCooldownResponse> call = apiService.checkCooldown(studentId);

        call.enqueue(new Callback<MoodCooldownResponse>() {
            @Override
            public void onResponse(Call<MoodCooldownResponse> call, Response<MoodCooldownResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MoodCooldownResponse cooldownResponse = response.body();
                    if (cooldownResponse.isCanTakeMoodTracker()) {
                        startMoodTracker();
                    } else {
                        // Show cooldown message
                        long timeRemaining = cooldownResponse.getTimeRemaining();
                        int hrs = (int) (timeRemaining / (1000 * 60 * 60));
                        int mins = (int) ((timeRemaining / (1000 * 60)) % 60);
                        Toast.makeText(requireContext(), 
                            String.format("Mood Tracker available in: %02d:%02d", hrs, mins), 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Fallback to local check if server fails
                    fallbackToLocalCooldownCheck(studentId);
                }
            }

            @Override
            public void onFailure(Call<MoodCooldownResponse> call, Throwable t) {
                Log.e("MoodTracker", "Network error checking cooldown: " + t.getMessage());
                // Fallback to local check if network fails
                fallbackToLocalCooldownCheck(studentId);
            }
        });
    }

    private void fallbackToLocalCooldownCheck(int studentId) {
        SharedPreferences moodprefs = requireActivity().getSharedPreferences("MoodPrefs_" + studentId, Context.MODE_PRIVATE);
        long lastTakenMillis = moodprefs.getLong("lastMoodTimestamp", 0);
        long now = System.currentTimeMillis();
        long timeLeft = (24 * 60 * 60 * 1000) - (now - lastTakenMillis); // 24 hours

        if (timeLeft > 0) {
            int hrs = (int) (timeLeft / (1000 * 60 * 60));
            int mins = (int) ((timeLeft / (1000 * 60)) % 60);
            Toast.makeText(requireContext(), 
                String.format("Mood Tracker available in: %02d:%02d", hrs, mins), 
                Toast.LENGTH_LONG).show();
        } else {
            startMoodTracker();
        }
    }

    private void startMoodTracker() {
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

        //Setting custom animations
        ft.setCustomAnimations(
                R.anim.slide_in_right,  //Enter
                R.anim.slide_out_left,  //Exit
                R.anim.slide_in_left,   //PopEnter (when back)
                R.anim.slide_out_right  //PopExit (when back)
        );

        ft.replace(R.id.fragment_container, new DisclaimerFragment());
        ft.addToBackStack(null);
        ft.commit();
    }
}
