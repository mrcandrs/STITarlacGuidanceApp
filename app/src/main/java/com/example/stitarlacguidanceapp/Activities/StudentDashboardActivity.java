package com.example.stitarlacguidanceapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.DuplicateCheckResponse;
import com.example.stitarlacguidanceapp.Models.Quote;
import com.example.stitarlacguidanceapp.Models.StudentUpdateRequest;
import com.example.stitarlacguidanceapp.Models.MoodCooldownResponse;
import com.example.stitarlacguidanceapp.QuoteAdapter;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.StudentApi;
import com.example.stitarlacguidanceapp.MoodTrackerApi;
import com.example.stitarlacguidanceapp.databinding.ActivityRegistrationBinding;
import com.example.stitarlacguidanceapp.databinding.ActivityStudentDashboardBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;


import java.util.Arrays;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDashboardActivity extends AppCompatActivity {

    private ActivityStudentDashboardBinding root;
    private ViewPager2 viewPager;
    private Handler sliderHandler = new Handler();
    private int currentPage = 0;
    private int studentId;
    private final long SLIDE_DELAY = 4000; // 4 seconds
    private List<Quote> quoteList;
    private Uri selectedImageUri;
    private ImageView imgProfile;

    private final long COOLDOWN_PERIOD = 24 * 60 * 60 * 1000; //24 hours
    //private final long COOLDOWN_PERIOD = 30 * 1000; //30 seconds for testing

    private final Handler cooldownHandler = new Handler(Looper.getMainLooper());
    private Runnable cooldownRunnable;


    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    selectedImageUri = uri;
                    if (imgProfile != null) {
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(imgProfile); // Show as circular
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        //load the student's info in profile
        loadStudentInfo();

        //Mood Tracker cooldown
        setupMoodTrackerCooldown();

        //Edit Inventory Form button
        root.btnEditInventoryForm.setOnClickListener(v -> editInventoryForm());

        //Career Planning Form button
        root.btnEditCareerPlanningForm.setOnClickListener(v -> editCareerPlanningForm());

        //Guidance Appointment Slip button
        root.cvGuidanceAppointment.setOnClickListener(v -> guidanceSlip());

        //exit interview form button
        root.cvExitInterview.setOnClickListener(v -> exitInterview());

        //referral form button
        root.cvReferralForm.setOnClickListener(v -> referralForm());

        //private journal button
        root.cvPrivateJournal.setOnClickListener(v -> privateJournal());

        //mood tracker button
        root.cvMoodTracker.setOnClickListener(v -> moodTracker());

        //setting button
        root.cvSettings.setOnClickListener(v -> settings());


        //viewPager for insiprational quotes
        viewPager = findViewById(R.id.viewPager);

        quoteList = Arrays.asList(
                new Quote("Believe you can and you're halfway there.", "Theodore Roosevelt"),
                new Quote("Push yourself, because no one else is going to do it for you.", "Unknown"),
                new Quote("Don’t watch the clock; do what it does. Keep going.", "Sam Levenson"),
                new Quote("Wake up with determination. Go to bed with satisfaction.", "George Lorimer"),
                new Quote("Success is not for the lazy.", "Unknown")
        );
        QuoteAdapter adapter = new QuoteAdapter(quoteList);
        viewPager.setAdapter(adapter);
        sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY);
    }

    //for edit inventory form
    private void editInventoryForm() {
        Intent intent = new Intent(StudentDashboardActivity.this, EditIndividualInventory.class);
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1); // <- fetch studentId here
        intent.putExtra("studentId", studentId);
        startActivity(intent);
    }

    //for career planning form
    private void editCareerPlanningForm() {
        Intent intent = new Intent(StudentDashboardActivity.this, EditCareerPlanningActivity.class);
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1); // <- fetch studentId here
        intent.putExtra("studentId", studentId);
        startActivity(intent);
    }

    //for guidance appointment slip
    private void guidanceSlip() {
        startActivity(new Intent(this, GuidanceAppointmentSlipActivity.class));
    }

    //for referral form
    private void referralForm() {
        startActivity(new Intent(this, ReferralFormActivity.class));
    }

    //for private journal
    private void privateJournal() {
        startActivity(new Intent(this, JournalActivity.class));
    }

    //for exit interview form
    private void exitInterview() {
        startActivity(new Intent(this, ExitFormActivity.class));
    }

    //for mood tracker
    private void moodTracker() {
        startActivity(new Intent(this, MoodTrackerActivity.class));
    }

    //for settings cardview button
    private void settings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }


    private void loadStudentInfo() {
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);

        String fullName = prefs.getString("fullName", "Unknown");
        String studentNumber = prefs.getString("studentNumber", "");
        String program = prefs.getString("program", "");
        String yearLevel = prefs.getString("yearLevel", "");
        studentId = prefs.getInt("studentId", -1);
        String profileKey = "profileUri_" + studentId;
        String profileUriString = prefs.getString(profileKey, null);
        String lastLogin = prefs.getString("lastLogin", "Not available");

        // Update your UI (example using ViewBinding or findViewById)
        root.txtName.setText(fullName);
        root.txtStudentNo.setText(studentNumber);
        root.txtCourse.setText(program);
        root.txtYear.setText(yearLevel);
        root.txtLastLogin.setText("Last login: " + lastLogin);

        // ✅ Load and display profile image if saved
        if (profileUriString != null) {
            Uri profileUri = Uri.parse(profileUriString);
            root.imgProfile.setImageURI(profileUri);
        }
    }

    private void setupMoodTrackerCooldown() {
        TextView txtCooldown = findViewById(R.id.txtMoodTrackerCooldown);
        CardView moodCard = findViewById(R.id.cv_MoodTracker);

        SharedPreferences sessionPrefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = sessionPrefs.getInt("studentId", -1);

        // First check server-side cooldown status
        checkServerCooldownStatus(studentId, txtCooldown, moodCard);
    }

    private void checkServerCooldownStatus(int studentId, TextView txtCooldown, CardView moodCard) {
        MoodTrackerApi apiService = ApiClient.getClient().create(MoodTrackerApi.class);
        Call<MoodCooldownResponse> call = apiService.checkCooldown(studentId);

        call.enqueue(new Callback<MoodCooldownResponse>() {
            @Override
            public void onResponse(Call<MoodCooldownResponse> call, Response<MoodCooldownResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MoodCooldownResponse cooldownResponse = response.body();
                    handleCooldownResponse(cooldownResponse, txtCooldown, moodCard);
                } else {
                    // Fallback to local check if server fails
                    Log.e("MoodTracker", "Server cooldown check failed, falling back to local check");
                    fallbackToLocalCooldown(studentId, txtCooldown, moodCard);
                }
            }

            @Override
            public void onFailure(Call<MoodCooldownResponse> call, Throwable t) {
                Log.e("MoodTracker", "Network error checking cooldown: " + t.getMessage());
                // Fallback to local check if network fails
                fallbackToLocalCooldown(studentId, txtCooldown, moodCard);
            }
        });
    }

    private void handleCooldownResponse(MoodCooldownResponse response, TextView txtCooldown, CardView moodCard) {
        if (response.isCanTakeMoodTracker()) {
            txtCooldown.setText("Mood Tracker available");
            txtCooldown.setTextColor(getResources().getColor(R.color.darkgreen));
            moodCard.setEnabled(true);
        } else {
            moodCard.setEnabled(false);
            txtCooldown.setTextColor(getResources().getColor(R.color.darkred));
            
            // Start countdown timer
            startCooldownCountdown(response.getTimeRemaining(), txtCooldown, moodCard);
        }
    }

    private void startCooldownCountdown(long timeRemaining, TextView txtCooldown, CardView moodCard) {
        final long startTime = System.currentTimeMillis();
        
        cooldownRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = timeRemaining - elapsed;

                if (remaining <= 0) {
                    txtCooldown.setText("Mood Tracker available");
                    txtCooldown.setTextColor(getResources().getColor(R.color.darkgreen));
                    moodCard.setEnabled(true);
                    cooldownHandler.removeCallbacks(this);
                } else {
                    int hrs = (int) (remaining / (1000 * 60 * 60));
                    int mins = (int) ((remaining / (1000 * 60)) % 60);
                    int secs = (int) ((remaining / 1000) % 60);
                    txtCooldown.setText(String.format("Available in: %02d:%02d:%02d", hrs, mins, secs));

                    //Schedule next update in 1 second
                    cooldownHandler.postDelayed(this, 1000);
                }
            }
        };
        cooldownHandler.post(cooldownRunnable);
    }

    private void fallbackToLocalCooldown(int studentId, TextView txtCooldown, CardView moodCard) {
        SharedPreferences prefs = getSharedPreferences("MoodPrefs_" + studentId, MODE_PRIVATE);
        long lastTakenMillis = prefs.getLong("lastMoodTimestamp", 0);
        long now = System.currentTimeMillis();
        long timeLeft = COOLDOWN_PERIOD - (now - lastTakenMillis);

        if (timeLeft > 0) {
            moodCard.setEnabled(false);
            txtCooldown.setTextColor(getResources().getColor(R.color.darkred));

            cooldownRunnable = new Runnable() {
                @Override
                public void run() {
                    long remaining = lastTakenMillis + COOLDOWN_PERIOD - System.currentTimeMillis();

                    if (remaining <= 0) {
                        txtCooldown.setText("Mood Tracker available");
                        txtCooldown.setTextColor(getResources().getColor(R.color.darkgreen));
                        moodCard.setEnabled(true);
                        cooldownHandler.removeCallbacks(this);
                    } else {
                        int hrs = (int) (remaining / (1000 * 60 * 60));
                        int mins = (int) ((remaining / (1000 * 60)) % 60);
                        int secs = (int) ((remaining / 1000) % 60);
                        txtCooldown.setText(String.format("Available in: %02d:%02d:%02d", hrs, mins, secs));

                        //Schedule next update in 1 second
                        cooldownHandler.postDelayed(this, 1000);
                    }
                }
            };
            cooldownHandler.post(cooldownRunnable);
        } else {
            txtCooldown.setText("Mood Tracker available");
            txtCooldown.setTextColor(getResources().getColor(R.color.darkgreen));
            moodCard.setEnabled(true);
        }
    }


    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager.getAdapter() != null) {
                int itemCount = viewPager.getAdapter().getItemCount();
                currentPage = (currentPage + 1) % itemCount;
                viewPager.setCurrentItem(currentPage, true);
                sliderHandler.postDelayed(this, SLIDE_DELAY);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY);
        loadStudentInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Clean up the handler to avoid memory leaks
        if (cooldownRunnable != null) {
            cooldownHandler.removeCallbacks(cooldownRunnable);
        }
    }

    private String getRealPathFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String path = cursor.getString(index);
            cursor.close();
            return path;
        }
    }

}
