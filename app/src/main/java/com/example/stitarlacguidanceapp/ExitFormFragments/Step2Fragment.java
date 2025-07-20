package com.example.stitarlacguidanceapp.ExitFormFragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stitarlacguidanceapp.Activities.ExitFormActivity;
import com.example.stitarlacguidanceapp.Models.ExitInterviewForm;
import com.example.stitarlacguidanceapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.function.Consumer;

public class Step2Fragment extends Fragment {

    private TextInputLayout layoutValues, layoutSkills;
    private TextInputEditText edtValues, edtSkills;

    public Step2Fragment() {
        super(R.layout.fragment_step2);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ExitFormActivity activity = (ExitFormActivity) getActivity();
        ExitInterviewForm form = activity.getFormData();

        layoutValues = view.findViewById(R.id.layoutedtValues);
        layoutSkills = view.findViewById(R.id.layoutedtSkills);
        edtValues = view.findViewById(R.id.edtValues);
        edtSkills = view.findViewById(R.id.edtSkills);

        //validate both fields
        edtValues.addTextChangedListener(new SimpleTextWatcher(s -> {
            layoutValues.setError(null);
            form.valuesLearned = s;
        }));

        edtSkills.addTextChangedListener(new SimpleTextWatcher(s -> {
            layoutSkills.setError(null);
            form.skillsLearned = s;
        }));
    }

    //validation logic
    public boolean isValidStep() {
        boolean isValid = true;

        if (edtValues.getText() == null || edtValues.getText().toString().trim().isEmpty()) {
            layoutValues.setError("Please enter the values you learned.");
            isValid = false;
        } else {
            layoutValues.setError(null);
        }

        if (edtSkills.getText() == null || edtSkills.getText().toString().trim().isEmpty()) {
            layoutSkills.setError("Please enter the skills you learned.");
            isValid = false;
        } else {
            layoutSkills.setError(null);
        }

        return isValid;
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Consumer<String> onTextChanged;
        SimpleTextWatcher(Consumer<String> callback) { this.onTextChanged = callback; }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onTextChanged.accept(s.toString());
        }
        public void afterTextChanged(Editable s) {}
    }
}

