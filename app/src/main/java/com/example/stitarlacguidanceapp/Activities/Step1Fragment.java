package com.example.stitarlacguidanceapp.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
                form.specificReasons = selected.toArray(new String[0]);

                // Show/hide edtOtherReason only when chkReason10 is checked
                CheckBox others = checkBoxes[9]; // index 9 = chkReason10
                edtOtherReason.setVisibility(others.isChecked() ? View.VISIBLE : View.GONE);
            });
        }

        edtOtherReason.addTextChangedListener(new SimpleTextWatcher(s -> form.otherReason = s));
        edtPlans.addTextChangedListener(new SimpleTextWatcher(s -> form.plansAfterLeaving = s));
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
