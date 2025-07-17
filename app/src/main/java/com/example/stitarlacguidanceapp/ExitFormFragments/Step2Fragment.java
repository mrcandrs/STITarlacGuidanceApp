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

import java.util.function.Consumer;

public class Step2Fragment extends Fragment {

    public Step2Fragment() {
        super(R.layout.fragment_step2);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ExitFormActivity activity = (ExitFormActivity) getActivity();
        ExitInterviewForm form = activity.getFormData();

        TextInputEditText edtValues = view.findViewById(R.id.edtValues);
        TextInputEditText edtSkills = view.findViewById(R.id.edtSkills);

        edtValues.addTextChangedListener(new SimpleTextWatcher(s -> form.valuesLearned = s));
        edtSkills.addTextChangedListener(new SimpleTextWatcher(s -> form.skillsLearned = s));
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

