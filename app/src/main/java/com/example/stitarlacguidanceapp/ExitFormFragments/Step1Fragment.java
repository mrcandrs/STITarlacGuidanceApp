package com.example.stitarlacguidanceapp.ExitFormFragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stitarlacguidanceapp.Activities.ExitFormActivity;
import com.example.stitarlacguidanceapp.Models.ExitInterviewForm;
import com.example.stitarlacguidanceapp.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Step1Fragment extends Fragment {

    public Step1Fragment() {
        super(R.layout.fragment_step1);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ExitFormActivity activity = (ExitFormActivity) getActivity();
        ExitInterviewForm form = activity.getFormData();

        RadioGroup radioGroup = view.findViewById(R.id.radioMainReason);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selected = view.findViewById(checkedId);
            form.mainReason = selected.getText().toString();
        });

        CheckBox[] checkBoxes = {
                view.findViewById(R.id.chkReason1),
                view.findViewById(R.id.chkReason2),
                view.findViewById(R.id.chkReason3),
                view.findViewById(R.id.chkReason4),
                view.findViewById(R.id.chkReason5),
                view.findViewById(R.id.chkReason6),
                view.findViewById(R.id.chkReason7),
                view.findViewById(R.id.chkReason8),
                view.findViewById(R.id.chkReason9),
                view.findViewById(R.id.chkReason10)
        };

        TextInputEditText edtOtherReason = view.findViewById(R.id.edtOtherReason);
        TextInputEditText edtPlans = view.findViewById(R.id.edtPlans);

        for (CheckBox cb : checkBoxes) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                List<String> selected = new ArrayList<>();
                for (CheckBox c : checkBoxes) {
                    if (c.isChecked()) selected.add(c.getText().toString());
                }
                form.specificReasonsArray = selected.toArray(new String[0]);
                form.specificReasons = android.text.TextUtils.join(",", form.specificReasonsArray);

                // Show/hide edtOtherReason only when chkReason10 is checked
                CheckBox others = checkBoxes[9]; // index 9 = chkReason10
                edtOtherReason.setVisibility(others.isChecked() ? View.VISIBLE : View.GONE);
            });
        }

        edtOtherReason.addTextChangedListener(new SimpleTextWatcher(s -> form.otherReason = s));
        edtPlans.addTextChangedListener(new SimpleTextWatcher(s -> form.plansAfterLeaving = s));
    }

    public boolean isValidStep() {
        ExitFormActivity activity = (ExitFormActivity) getActivity();
        ExitInterviewForm form = activity.getFormData();

        View view = getView();
        if (view == null) return false;

        RadioGroup radioGroup = view.findViewById(R.id.radioMainReason);
        CheckBox chkOthers = view.findViewById(R.id.chkReason10);
        TextInputEditText edtOtherReason = view.findViewById(R.id.edtOtherReason);
        TextInputEditText edtPlans = view.findViewById(R.id.edtPlans);

        //Checking if a radio button is selected
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            activity.showSnackbar("Please select a primary reason for leaving.");
            return false;
        }

        //Checking if at least one checkbox is selected
        boolean atLeastOneChecked = false;
        for (int id : new int[]{
                R.id.chkReason1, R.id.chkReason2, R.id.chkReason3, R.id.chkReason4, R.id.chkReason5,
                R.id.chkReason6, R.id.chkReason7, R.id.chkReason8, R.id.chkReason9, R.id.chkReason10}) {

            CheckBox cb = view.findViewById(id);
            if (cb.isChecked()) {
                atLeastOneChecked = true;
                break;
            }
        }

        if (!atLeastOneChecked) {
            activity.showSnackbar("Please check at least one specific reason.");
            return false;
        }

        //If "Others" is checked, make sure text is provided
        if (chkOthers.isChecked() && edtOtherReason.getText().toString().trim().isEmpty()) {
            edtOtherReason.setError("Please specify your other reason.");
            edtOtherReason.requestFocus();
            return false;
        }

        //Plans after leaving is required
        if (edtPlans.getText().toString().trim().isEmpty()) {
            edtPlans.setError("Please provide your plans after leaving STI.");
            edtPlans.requestFocus();
            return false;
        }

        return true;
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
