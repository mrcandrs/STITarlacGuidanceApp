package com.example.stitarlacguidanceapp.PrivateJournalFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.JournalEntryApi;
import com.example.stitarlacguidanceapp.Models.JournalEntry;
import com.example.stitarlacguidanceapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarFragment extends Fragment {

    private TextView txtMonthYear;
    private GridLayout gridDays;
    private Calendar calendar;
    private View lastSelectedButton = null;
    private CardView cardSelectedEntry;
    private TextView txtSelectedDate, txtSelectedTitle, txtSelectedContent, txtSelectedMood;

    private List<JournalEntry> journalEntries = new ArrayList<>();
    private boolean isLoading = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    public CalendarFragment() {
        calendar = Calendar.getInstance();
        // Set to current date instead of July
        calendar.setTime(new Date());
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
        
        // Initialize entry display views
        cardSelectedEntry = view.findViewById(R.id.cardSelectedEntry);
        txtSelectedDate = view.findViewById(R.id.txtSelectedDate);
        txtSelectedTitle = view.findViewById(R.id.txtSelectedTitle);
        txtSelectedContent = view.findViewById(R.id.txtSelectedContent);
        txtSelectedMood = view.findViewById(R.id.txtSelectedMood);

        Typeface customFont = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular);

        // Load journal entries once on startup
        loadJournalEntriesFromApi(customFont);

        btnPrev.setOnClickListener(v -> {
            if (!isLoading) {
                calendar.add(Calendar.MONTH, -1);
                updateCalendarDisplay(customFont);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (!isLoading) {
                calendar.add(Calendar.MONTH, 1);
                updateCalendarDisplay(customFont);
            }
        });

        return view;
    }

    private void updateCalendarDisplay(Typeface customFont) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        txtMonthYear.setText(monthFormat.format(calendar.getTime()));
        
        // Clear previous selection and hide entry display
        lastSelectedButton = null;
        cardSelectedEntry.setVisibility(View.GONE);
        
        // Clear and rebuild calendar grid
        gridDays.removeAllViews();
        buildCalendarGrid(customFont);
    }

    private void buildCalendarGrid(Typeface customFont) {
        // Weekday Headers (Sun–Sat)
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

        // Clone calendar to calculate padding & days
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
            View dayView = LayoutInflater.from(getContext()).inflate(R.layout.item_calendar_day, null);
            TextView txtDayNumber = dayView.findViewById(R.id.txtDayNumber);
            View dotIndicator = dayView.findViewById(R.id.dotIndicator);

            txtDayNumber.setText(String.valueOf(day));
            txtDayNumber.setTypeface(customFont);
            txtDayNumber.setTextColor(Color.BLACK);

            // Highlight today
            if (day == todayDay &&
                    calendar.get(Calendar.MONTH) == todayMonth &&
                    calendar.get(Calendar.YEAR) == todayYear) {
                dayView.setBackgroundResource(R.drawable.bg_today);
                txtDayNumber.setTextColor(Color.WHITE);
                txtDayNumber.setTypeface(null, Typeface.BOLD);
            }

            // Check if day has journal entry
            Calendar thisDay = (Calendar) calendar.clone();
            thisDay.set(Calendar.DAY_OF_MONTH, day);
            String formattedDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(thisDay.getTime());

            boolean hasEntry = false;
            for (JournalEntry entry : journalEntries) {
                if (entry.getDate().equals(formattedDate)) {
                    hasEntry = true;
                    break;
                }
            }
            dotIndicator.setVisibility(hasEntry ? View.VISIBLE : View.GONE);

            // Layout params
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            dayView.setLayoutParams(params);

            // Click listener
            dayView.setOnClickListener(v -> {
                // Restore previous selection appearance
                if (lastSelectedButton != null) {
                    TextView prevTxt = lastSelectedButton.findViewById(R.id.txtDayNumber);
                    int prevDay = Integer.parseInt(prevTxt.getText().toString());

                    if (prevDay == todayDay &&
                            calendar.get(Calendar.MONTH) == todayMonth &&
                            calendar.get(Calendar.YEAR) == todayYear) {
                        lastSelectedButton.setBackgroundResource(R.drawable.bg_today);
                        prevTxt.setTextColor(Color.WHITE);
                    } else {
                        lastSelectedButton.setBackgroundColor(Color.TRANSPARENT);
                        prevTxt.setTextColor(Color.BLACK);
                    }
                }

                // Highlight the newly selected day
                dayView.setBackgroundResource(R.drawable.bg_selected_day);
                txtDayNumber.setTextColor(Color.BLACK);
                lastSelectedButton = dayView;

                // Show journal entry if available
                showJournalEntryForDate(formattedDate);
            });

            gridDays.addView(dayView);
        }
    }

    private void showJournalEntryForDate(String formattedDate) {
        for (JournalEntry entry : journalEntries) {
            if (entry.getDate().equals(formattedDate)) {
                // Show entry in the card below calendar
                txtSelectedDate.setText(formattedDate);
                txtSelectedTitle.setText(entry.getTitle().isEmpty() ? "(No Title)" : entry.getTitle());
                txtSelectedContent.setText(entry.getContent());
                txtSelectedMood.setText("Mood: " + entry.getMood());
                cardSelectedEntry.setVisibility(View.VISIBLE);
                return;
            }
        }
        
        // No entry found for this date
        cardSelectedEntry.setVisibility(View.GONE);
        Toast.makeText(getContext(), "No journal entry for this day", Toast.LENGTH_SHORT).show();
    }

    private void loadJournalEntriesFromApi(Typeface customFont) {
        if (isLoading) return; // Prevent multiple simultaneous calls
        
        isLoading = true;
        SharedPreferences sessionPrefs = requireContext().getSharedPreferences("student_session", Context.MODE_PRIVATE);
        int studentId = sessionPrefs.getInt("studentId", -1);

        if (studentId == -1) {
            Toast.makeText(getContext(), "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            isLoading = false;
            return;
        }

        JournalEntryApi api = ApiClient.getClient().create(JournalEntryApi.class);
        Call<List<JournalEntry>> call = api.getJournalEntriesByStudent(studentId);

        call.enqueue(new Callback<List<JournalEntry>>() {
            @Override
            public void onResponse(Call<List<JournalEntry>> call, Response<List<JournalEntry>> response) {
                isLoading = false;
                if (!isAdded()) return; // Prevents crash if fragment is detached

                if (response.isSuccessful() && response.body() != null) {
                    journalEntries = response.body();
                    // Normalize dates from ISO to display format to match calendar comparison
                    for (int i = 0; i < journalEntries.size(); i++) {
                        JournalEntry e = journalEntries.get(i);
                        e.setDate(toDisplayDate(e.getDate()));
                    }
                    // Update calendar display after data is loaded
                    updateCalendarDisplay(customFont);
                } else {
                    Toast.makeText(getContext(), "Failed to load journal entries.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JournalEntry>> call, Throwable t) {
                isLoading = false;
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Network error loading journal entries.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isIsoDate(String value) {
        return value != null && value.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private String toDisplayDate(String value) {
        try {
            if (isIsoDate(value)) {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date d = iso.parse(value);
                if (d != null) {
                    SimpleDateFormat out = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    out.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
                    return out.format(d);
                }
            }
        } catch (Exception ignored) {}
        return value;
    }
}