package com.example.stitarlacguidanceapp.MoodTrackerFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.stitarlacguidanceapp.Models.MoodViewModel;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.FragmentQuestion1Binding;
import com.example.stitarlacguidanceapp.databinding.FragmentQuestion2Binding;

public class Question2Fragment extends Fragment {

    private FragmentQuestion2Binding root;
    private int selectedScore = -1;

    public Question2Fragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentQuestion2Binding.inflate(inflater, container, false);


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
            viewModel.setScoreForQuestion(1, selectedScore); //Question 2 = index 1
            Log.d("MoodTracker", "Total score after Q2: " + viewModel.getScore());


            //Go to Question 3
            FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

            //Setting custom animations
            ft.setCustomAnimations(
                    R.anim.slide_in_right,  //Enter
                    R.anim.slide_out_left,  //Exit
                    R.anim.slide_in_left,   //PopEnter (when back)
                    R.anim.slide_out_right  //PopExit (when back)
            );

            ft.replace(R.id.fragment_container, new Question3Fragment());
            ft.addToBackStack(null);
            ft.commit();
        });

        root.btnViewGuide.setOnClickListener(v -> {
            new ScoringGuideDialog().show(getParentFragmentManager(), "scoring_guide");
        });

        return root.getRoot();
    }
}

