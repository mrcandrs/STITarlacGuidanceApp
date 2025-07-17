package com.example.stitarlacguidanceapp.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stitarlacguidanceapp.R;

public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {
        super(R.layout.fragment_step0_welcome);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ExitFormActivity activity = (ExitFormActivity) getActivity();
        SharedPreferences prefs = activity.getSharedPreferences("student_session", Context.MODE_PRIVATE);

        ((TextView)view.findViewById(R.id.txtName)).setText("Name: " + prefs.getString("fullName", "Unknown"));
        ((TextView)view.findViewById(R.id.txtStudentNumber)).setText("Student Number: " + prefs.getString("studentNumber", "N/A"));
        ((TextView)view.findViewById(R.id.txtProgram)).setText("Program: " + prefs.getString("program", "N/A"));
        ((TextView)view.findViewById(R.id.txtYear)).setText("Year Level: " + prefs.getString("yearLevel", "N/A"));
    }
}
