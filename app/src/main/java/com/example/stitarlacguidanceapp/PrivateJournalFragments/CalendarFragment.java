package com.example.stitarlacguidanceapp.PrivateJournalFragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.stitarlacguidanceapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private TextView txtMonthYear;
    private GridLayout gridDays;
    private Calendar calendar;
    private Button lastSelectedButton = null;

    public CalendarFragment() {
        calendar = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        txtMonthYear = view.findViewById(R.id.txtMonthYear);
        gridDays = view.findViewById(R.id.gridDays);
        Button btnPrev = view.findViewById(R.id.btnPrev);
        Button btnNext = view.findViewById(R.id.btnNext);

        Typeface customFont = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular);

        updateCalendar(customFont);

        btnPrev.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar(customFont); // 👈 use same font again
        });

        btnNext.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar(customFont);
        });

        return view;
    }

    private void updateCalendar(Typeface customFont) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        txtMonthYear.setText(monthFormat.format(calendar.getTime()));
        gridDays.removeAllViews();

        //Weekday Headers (Sun–Sat)
        String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayHeaders) {
            TextView txtDay = new TextView(getContext());
            txtDay.setText(day);
            txtDay.setGravity(Gravity.CENTER);
            txtDay.setTypeface(customFont, Typeface.BOLD);
            txtDay.setTextColor(Color.DKGRAY);

            GridLayout.LayoutParams headerParams = new GridLayout.LayoutParams();
            headerParams.width = 0;
            headerParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            headerParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            txtDay.setLayoutParams(headerParams);

            gridDays.addView(txtDay);
        }

        //Clone calendar to calculate padding & days
        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK); // 1 (Sun) to 7 (Sat)
        int maxDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH);
        int todayYear = today.get(Calendar.YEAR);


        // Add empty views before day 1 (padding)
        for (int i = 1; i < firstDayOfWeek; i++) {
            TextView empty = new TextView(getContext());
            GridLayout.LayoutParams emptyParams = new GridLayout.LayoutParams();
            emptyParams.width = 0;
            emptyParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            emptyParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            empty.setLayoutParams(emptyParams);
            gridDays.addView(empty);
        }

        // Day buttons
        for (int day = 1; day <= maxDay; day++) {
            Button dayBtn = new Button(getContext());

            dayBtn.setText(String.valueOf(day));
            dayBtn.setTypeface(customFont);
            dayBtn.setTextColor(Color.BLACK);
            if (day == todayDay &&
                    calendar.get(Calendar.MONTH) == todayMonth &&
                    calendar.get(Calendar.YEAR) == todayYear) {

                dayBtn.setBackgroundResource(R.drawable.bg_today);
                dayBtn.setTextColor(Color.WHITE); //White text for contrast
                dayBtn.setTypeface(null, Typeface.BOLD);
            } else {
                //dayBtn.setBackgroundResource(R.drawable.bg_day);
                dayBtn.setBackgroundColor(Color.TRANSPARENT);
                dayBtn.setTextColor(Color.BLACK);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            dayBtn.setLayoutParams(params);

            int finalDay = day;

            dayBtn.setOnClickListener(v -> {
                // 1. Reset previous selection
                if (lastSelectedButton != null) {
                    if (lastSelectedButton.getText().toString().equals(String.valueOf(todayDay)) &&
                            calendar.get(Calendar.MONTH) == todayMonth &&
                            calendar.get(Calendar.YEAR) == todayYear) {

                        lastSelectedButton.setBackgroundResource(R.drawable.bg_today);
                        lastSelectedButton.setTextColor(Color.WHITE);
                    } else {
                        lastSelectedButton.setBackgroundColor(Color.TRANSPARENT);
                        lastSelectedButton.setTextColor(Color.BLACK);
                    }
                }

                // 2. Highlight new selection
                dayBtn.setBackgroundResource(R.drawable.bg_selected_day);
                dayBtn.setTextColor(Color.BLACK);
                lastSelectedButton = dayBtn;
            });

            gridDays.addView(dayBtn);
        }
    }
}

