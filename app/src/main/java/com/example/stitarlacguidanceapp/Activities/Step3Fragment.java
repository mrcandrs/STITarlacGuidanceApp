package com.example.stitarlacguidanceapp.Activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.stitarlacguidanceapp.Models.ExitInterviewForm;
import com.example.stitarlacguidanceapp.R;

import org.w3c.dom.Text;

public class Step3Fragment extends Fragment {

    String[] services = {
            "Library Services",
            "Computer Laboratory Services",
            "Records Services (Registrar)",
            "Cashiering Services",
            "Guidance Services",
            "Admissions",
            "Facilities",
            "Faculty Members/Staff",
            "Clinic Services",
            "Canteen Services",
            "Security Services",
            "Other Services"
    };

    String[] activities = {
            "Student Organizations",
            "Sports Fest",
            "Educational Tours",
            "Other Activities"
    };


    public Step3Fragment() {
        super(R.layout.fragment_step3);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //declarations
        ExitFormActivity activity = (ExitFormActivity) getActivity();
        ExitInterviewForm form = activity.getFormData();
        LinearLayout container = view.findViewById(R.id.containerServices);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        Typeface poppinsRegular = ResourcesCompat.getFont(getContext(), R.font.poppins_regular);

        //Services Section
        for (String service : services) {
            View row = inflater.inflate(R.layout.service_toggle, container, false);
            TextView txtService = row.findViewById(R.id.txtServiceName);
            Button btnYes = row.findViewById(R.id.btnYes);
            Button btnNo = row.findViewById(R.id.btnNo);

            txtService.setText(service);
            container.addView(row);

            if (service.equals("Other Services")) {
                EditText edtOtherServices = new EditText(getContext());
                edtOtherServices.setHint("Please specify other services...");
                edtOtherServices.setVisibility(View.GONE);
                edtOtherServices.setTypeface(poppinsRegular);
                container.addView(edtOtherServices);

                btnYes.setOnClickListener(v -> {
                    form.serviceResponses.put(service, "Y");
                    btnYes.setBackgroundColor(Color.parseColor("#008200"));
                    btnNo.setBackgroundColor(Color.LTGRAY);
                    edtOtherServices.setVisibility(View.VISIBLE);
                });

                btnNo.setOnClickListener(v -> {
                    form.serviceResponses.put(service, "N");
                    btnNo.setBackgroundColor(Color.parseColor("#F54236"));
                    btnYes.setBackgroundColor(Color.LTGRAY);
                    edtOtherServices.setVisibility(View.GONE);
                    form.otherServicesDetail = null;
                });

                edtOtherServices.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        form.otherServicesDetail = s.toString();
                    }
                    public void afterTextChanged(Editable s) {}
                });
            } else {
                // Normal Y/N behavior
                btnYes.setOnClickListener(v -> {
                    form.serviceResponses.put(service, "Y");
                    btnYes.setBackgroundColor(Color.parseColor("#008200"));
                    btnNo.setBackgroundColor(Color.LTGRAY);
                });

                btnNo.setOnClickListener(v -> {
                    form.serviceResponses.put(service, "N");
                    btnNo.setBackgroundColor(Color.parseColor("#F54236"));
                    btnYes.setBackgroundColor(Color.LTGRAY);
                });
            }
        }


        //Activities section
        Typeface customFont = ResourcesCompat.getFont(getContext(), R.font.poppins_semibold);

        TextView activityHeader = new TextView(getContext());
        activityHeader.setText("Activities");
        activityHeader.setTextSize(18);
        activityHeader.setTypeface(customFont);
        activityHeader.setPadding(0, 24, 0, 8);
        container.addView(activityHeader);

        for (String activityItem : activities) {
            View row = inflater.inflate(R.layout.service_toggle, container, false);
            TextView txtService = row.findViewById(R.id.txtServiceName);
            Button btnYes = row.findViewById(R.id.btnYes);
            Button btnNo = row.findViewById(R.id.btnNo);

            txtService.setText(activityItem);
            container.addView(row);

            if (activityItem.equals("Other Activities")) {
                EditText edtOtherActivities = new EditText(getContext());
                edtOtherActivities.setHint("Please specify other activities...");
                edtOtherActivities.setVisibility(View.GONE);
                edtOtherActivities.setTypeface(poppinsRegular);
                container.addView(edtOtherActivities); // ✅ Add below

                btnYes.setOnClickListener(v -> {
                    form.serviceResponses.put(activityItem, "Y");
                    btnYes.setBackgroundColor(Color.parseColor("#008200"));
                    btnNo.setBackgroundColor(Color.LTGRAY);
                    edtOtherActivities.setVisibility(View.VISIBLE);
                });

                btnNo.setOnClickListener(v -> {
                    form.serviceResponses.put(activityItem, "N");
                    btnNo.setBackgroundColor(Color.parseColor("#F54236"));
                    btnYes.setBackgroundColor(Color.LTGRAY);
                    edtOtherActivities.setVisibility(View.GONE);
                    form.otherActivitiesDetail = null;
                });

                edtOtherActivities.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        form.otherActivitiesDetail = s.toString();
                    }
                    public void afterTextChanged(Editable s) {}
                });
                } else {
                btnYes.setOnClickListener(v -> {
                    form.serviceResponses.put(activityItem, "Y");
                    btnYes.setBackgroundColor(Color.parseColor("#008200"));
                    btnNo.setBackgroundColor(Color.LTGRAY);
                });

                btnNo.setOnClickListener(v -> {
                    form.serviceResponses.put(activityItem, "N");
                    btnNo.setBackgroundColor(Color.parseColor("#F54236"));
                    btnYes.setBackgroundColor(Color.LTGRAY);
                });
            }
        }
    }
}

