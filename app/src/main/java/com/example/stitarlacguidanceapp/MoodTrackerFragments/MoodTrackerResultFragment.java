package com.example.stitarlacguidanceapp.MoodTrackerFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stitarlacguidanceapp.Models.MoodViewModel;
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

        String resultText;
        if (totalScore <= 5) {
            resultText = "Your result is: Mild";
        } else if (totalScore > 5 && totalScore < 12) {
            resultText = "Your result is: Moderate";
        } else {
            resultText = "Your result is: Severe";
        }

        root.txtResult.setText(resultText);
        return root.getRoot();
    }
}
