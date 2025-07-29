package com.example.stitarlacguidanceapp.MoodTrackerFragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.stitarlacguidanceapp.R;

public class ScoringGuideDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_scoring_guide, null);

        builder.setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}


