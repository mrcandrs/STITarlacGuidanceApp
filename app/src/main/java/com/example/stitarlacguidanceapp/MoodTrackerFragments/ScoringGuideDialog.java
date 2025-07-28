package com.example.stitarlacguidanceapp.MoodTrackerFragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ScoringGuideDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle("Scoring Guide")
                .setMessage("0 = Not True\n1 = Sometimes\n2 = True\n\nTotal Score:\n0–5 = Mild\n6–11 = Moderate\n12+ = High")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}

