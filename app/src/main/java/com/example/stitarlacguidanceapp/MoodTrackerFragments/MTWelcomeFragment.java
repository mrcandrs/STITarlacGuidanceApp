package com.example.stitarlacguidanceapp.MoodTrackerFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.stitarlacguidanceapp.Activities.MoodTrackerActivity;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.FragmentMtwelcomeBinding;

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
        SharedPreferences prefs = activity.getSharedPreferences("student_session", Context.MODE_PRIVATE); //Shared Preferences from login

        String studentNumber = prefs.getString("studentNumber", "N/A");

        //Setting the student number in the TextView
        root.txtStudentNumber.setText(studentNumber);

        root.btnStart.setOnClickListener(v -> {
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
        });

        return root.getRoot();
    }
}
