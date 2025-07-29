package com.example.stitarlacguidanceapp.MoodTrackerFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.stitarlacguidanceapp.Models.MoodViewModel;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.FragmentQuestion1Binding;

public class Question1Fragment extends Fragment {

    private FragmentQuestion1Binding root;
    private int selectedScore = -1;

    public Question1Fragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentQuestion1Binding.inflate(inflater, container, false);


        root.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio0) {
                selectedScore = 0;
            } else if (checkedId == R.id.radio1) {
                selectedScore = 1;
            } else if (checkedId == R.id.radio2) {
                selectedScore = 2;
            }
        });

        root.btnNext.setOnClickListener(v -> {
            if (selectedScore == -1) {
                Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            MoodViewModel viewModel = new ViewModelProvider(requireActivity()).get(MoodViewModel.class);
            viewModel.addScore(selectedScore);

            Log.d("MoodTracker", "Current total score: " + viewModel.getScore());

            //Go to Question 2 or final result screen
            FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new MoodTrackerResultFragment());
            ft.addToBackStack(null);
            ft.commit();
        });

        root.btnViewGuide.setOnClickListener(v -> {
            new ScoringGuideDialog().show(getParentFragmentManager(), "scoring_guide");
        });

        return root.getRoot();
    }
}

