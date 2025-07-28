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

public class MTWelcomeFragment extends Fragment {

    //Empty constructor
    public MTWelcomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mtwelcome, container, false);
        MoodTrackerActivity activity = (MoodTrackerActivity) getActivity();
        SharedPreferences prefs = activity.getSharedPreferences("student_session", Context.MODE_PRIVATE); //Shared Preferences from login

        ((TextView)view.findViewById(R.id.txtStudentNumber)).setText(prefs.getString("studentNumber", "N/A"));

        Button btnStart = view.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
            //ft.replace(R.id.fragment_container, new DisclaimerFragment());
            ft.addToBackStack(null);
            ft.commit();
        });

        return view;
    }
}
