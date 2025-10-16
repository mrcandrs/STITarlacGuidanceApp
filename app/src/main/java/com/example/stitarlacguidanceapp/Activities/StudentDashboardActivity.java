package com.example.stitarlacguidanceapp.Activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import com.example.stitarlacguidanceapp.Models.QuoteDto;
import com.example.stitarlacguidanceapp.Models.StudentUpdateRequest;
import com.example.stitarlacguidanceapp.Models.MoodCooldownResponse;
import com.example.stitarlacguidanceapp.NotificationHelper;
import com.example.stitarlacguidanceapp.QuoteAdapter;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.StudentApi;
import com.example.stitarlacguidanceapp.MoodTrackerApi;
import com.example.stitarlacguidanceapp.MaintenanceApi;
import com.example.stitarlacguidanceapp.AppConfigRepository;
import okhttp3.ResponseBody;
import com.example.stitarlacguidanceapp.databinding.ActivityRegistrationBinding;
import com.example.stitarlacguidanceapp.databinding.ActivityStudentDashboardBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;


import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


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
    
    // Notification constants
    private static final String CHANNEL_ID = "mood_tracker_notifications";
    private static final int NOTIFICATION_ID = 1001;
    
    // NotificationHelper instance
    private NotificationHelper notificationHelper;
    
    // AppConfigRepository for caching quotes
    private AppConfigRepository appConfigRepository;


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

        // Initialize NotificationHelper
        notificationHelper = new NotificationHelper(this);
        
        // Initialize AppConfigRepository
        appConfigRepository = new AppConfigRepository(this);

        //load the student's info in profile
        loadStudentInfo();

        //Mood Tracker cooldown
        setupMoodTrackerCooldown();
        
        // Check if Mood Tracker is available and highlight it
        checkAndHighlightMoodTracker();

        // For testing Mood Tracker when available
        //simulateMoodTrackerAvailable();

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


        //viewPager for inspirational quotes
        viewPager = findViewById(R.id.viewPager);
        
        // Load quotes from API
        loadQuotes();
    }
    
    private void loadQuotes() {
        // First try to load from cache
        List<QuoteDto> cachedQuotes = appConfigRepository.getQuotes();
        if (!cachedQuotes.isEmpty()) {
            setupQuotesViewPager(convertQuoteDtosToQuotes(cachedQuotes));
        }
        
        // Then fetch from API
        MaintenanceApi maintenanceApi = ApiClient.getClient().create(MaintenanceApi.class);
        Call<List<QuoteDto>> call = maintenanceApi.getQuotes();
        
        call.enqueue(new Callback<List<QuoteDto>>() {
            @Override
            public void onResponse(Call<List<QuoteDto>> call, Response<List<QuoteDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Save quotes to cache
                    appConfigRepository.saveQuotes(response.body());
                    // Update the viewpager with new quotes
                    setupQuotesViewPager(convertQuoteDtosToQuotes(response.body()));
                } else {
                    // If API fails and no cached quotes, use hardcoded fallback
                    if (cachedQuotes.isEmpty()) {
                        setupQuotesViewPager(getHardcodedQuotes());
                    }
                }
            }
            
            @Override
            public void onFailure(Call<List<QuoteDto>> call, Throwable t) {
                Log.e("Quotes", "Failed to load quotes from API: " + t.getMessage());
                // If API fails and no cached quotes, use hardcoded fallback
                if (cachedQuotes.isEmpty()) {
                    setupQuotesViewPager(getHardcodedQuotes());
                }
            }
        });
    }
    
    private void setupQuotesViewPager(List<Quote> quotes) {
        if (quotes.isEmpty()) {
            quotes = getHardcodedQuotes();
        }
        
        quoteList = quotes;
        QuoteAdapter adapter = new QuoteAdapter(quoteList);
        viewPager.setAdapter(adapter);
        sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY);
    }
    
    private List<Quote> convertQuoteDtosToQuotes(List<QuoteDto> quoteDtos) {
        List<Quote> quotes = new ArrayList<>();
        for (QuoteDto dto : quoteDtos) {
            quotes.add(new Quote(dto.text, dto.author));
        }
        return quotes;
    }
    
    private List<Quote> getHardcodedQuotes() {
        return Arrays.asList(
                new Quote("Believe you can and you're halfway there.", "Theodore Roosevelt"),
                new Quote("Push yourself, because no one else is going to do it for you.", "Unknown"),
                new Quote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson"),
                new Quote("Wake up with determination. Go to bed with satisfaction.", "George Lorimer"),
                new Quote("Success is not for the lazy.", "Unknown")
        );
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
        String lastLogin = prefs.getString("lastLogin", "Not available");

        // Update your UI (example using ViewBinding or findViewById)
        root.txtName.setText(fullName);
        root.txtStudentNo.setText(studentNumber);
        root.txtCourse.setText(program);
        root.txtYear.setText(yearLevel);
        root.txtLastLogin.setText("Last login: " + lastLogin);

        // Load profile photo from server first, then fallback to local
        loadProfilePhoto(studentId);
    }

    private void loadProfilePhoto(int studentId) {
        // First try to load from server
        StudentApi apiService = ApiClient.getClient().create(StudentApi.class);
        Call<ResponseBody> call = apiService.getProfilePhoto(studentId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save the photo locally and display it
                    saveAndDisplayProfilePhoto(response.body(), studentId);
                } else {
                    // Server doesn't have photo, fallback to local storage
                    fallbackToLocalProfilePhoto(studentId);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ProfilePhoto", "Failed to load profile photo from server: " + t.getMessage());
                // Network error, fallback to local storage
                fallbackToLocalProfilePhoto(studentId);
            }
        });
    }

    private void saveAndDisplayProfilePhoto(ResponseBody responseBody, int studentId) {
        try {
            // Create a temporary file to save the photo
            File tempFile = File.createTempFile("profile_" + studentId, ".jpg", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            
            InputStream inputStream = responseBody.byteStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            
            fos.close();
            inputStream.close();
            
            // Save the file path to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
            String profileKey = "profileUri_" + studentId;
            prefs.edit().putString(profileKey, Uri.fromFile(tempFile).toString()).apply();
            
            // Display the photo
            Glide.with(this)
                    .load(tempFile)
                    .circleCrop()
                    .into(root.imgProfile);
                    
        } catch (IOException e) {
            Log.e("ProfilePhoto", "Failed to save profile photo: " + e.getMessage());
            fallbackToLocalProfilePhoto(studentId);
        }
    }

    private void fallbackToLocalProfilePhoto(int studentId) {
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        String profileKey = "profileUri_" + studentId;
        String profileUriString = prefs.getString(profileKey, null);
        
        if (profileUriString != null) {
            Uri profileUri = Uri.parse(profileUriString);
            Glide.with(this)
                    .load(profileUri)
                    .circleCrop()
                    .into(root.imgProfile);
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

    private void checkAndHighlightMoodTracker() {
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1);
        
        // Check if Mood Tracker has been used since last highlighting
        String highlightKey = "mood_tracker_highlighted_" + studentId;
        boolean hasBeenHighlighted = prefs.getBoolean(highlightKey, false);
        
        // Use server-side cooldown check instead of local
        checkServerCooldownForHighlighting(studentId, hasBeenHighlighted);
    }
    
    private void checkServerCooldownForHighlighting(int studentId, boolean hasBeenHighlighted) {
        MoodTrackerApi apiService = ApiClient.getClient().create(MoodTrackerApi.class);
        Call<MoodCooldownResponse> call = apiService.checkCooldown(studentId);

        call.enqueue(new Callback<MoodCooldownResponse>() {
            @Override
            public void onResponse(Call<MoodCooldownResponse> call, Response<MoodCooldownResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MoodCooldownResponse cooldownResponse = response.body();
                    
                    // If Mood Tracker is available and hasn't been highlighted yet, highlight it
                    if (cooldownResponse.isCanTakeMoodTracker() && !hasBeenHighlighted) {
                        highlightMoodTrackerButton();
                    }
                } else {
                    // Fallback to local check if server fails
                    Log.e("MoodTracker", "Server cooldown check failed for highlighting, falling back to local check");
                    fallbackToLocalHighlightingCheck(studentId, hasBeenHighlighted);
                }
            }

            @Override
            public void onFailure(Call<MoodCooldownResponse> call, Throwable t) {
                Log.e("MoodTracker", "Network error checking cooldown for highlighting: " + t.getMessage());
                // Fallback to local check if network fails
                fallbackToLocalHighlightingCheck(studentId, hasBeenHighlighted);
            }
        });
    }
    
    private void fallbackToLocalHighlightingCheck(int studentId, boolean hasBeenHighlighted) {
        SharedPreferences moodPrefs = getSharedPreferences("MoodPrefs_" + studentId, MODE_PRIVATE);
        long lastTakenMillis = moodPrefs.getLong("lastMoodTimestamp", 0);
        long now = System.currentTimeMillis();
        long timeLeft = COOLDOWN_PERIOD - (now - lastTakenMillis);
        
        // If Mood Tracker is available and hasn't been highlighted yet, highlight it
        if (timeLeft <= 0 && !hasBeenHighlighted) {
            highlightMoodTrackerButton();
        }
    }
    
    private void highlightMoodTrackerButton() {
        CardView moodCard = findViewById(R.id.cv_MoodTracker);
        TextView newBadge = findViewById(R.id.txtNewBadge);
        
        if (moodCard != null) {
            // Set the highlighted background
            moodCard.setCardBackgroundColor(getResources().getColor(R.color.blue_light));
            
            // Show the "READY" badge instead of "NEW"
            if (newBadge != null) {
                newBadge.setText("READY");
                newBadge.setVisibility(View.VISIBLE);
            }
            
            // Add a pulsing animation that repeats
            startPulsingAnimation(moodCard);
            
            // Show notification using NotificationHelper
            SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
            String studentName = prefs.getString("fullName", "Student");
            notificationHelper.showMoodTrackerReadyNotification(studentName);
            
            // Mark as highlighted so it persists until used
            int studentId = prefs.getInt("studentId", -1);
            String highlightKey = "mood_tracker_highlighted_" + studentId;
            prefs.edit().putBoolean(highlightKey, true).apply();
        }
    }
    
    private void startPulsingAnimation(CardView cardView) {
        // Create a continuous pulsing animation that repeats every 3 seconds
        Runnable pulseRunnable = new Runnable() {
            @Override
            public void run() {
                cardView.animate()
                        .scaleX(1.15f)
                        .scaleY(1.15f)
                        .setDuration(500)
                        .withEndAction(() -> {
                            cardView.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(500)
                                    .withEndAction(() -> {
                                        // Schedule next pulse in 3 seconds
                                        new Handler(Looper.getMainLooper()).postDelayed(this, 3000);
                                    })
                                    .start();
                        })
                        .start();
            }
        };
        
        // Start the first pulse after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(pulseRunnable, 1000);
    }
    
    // Restore highlighting state when dashboard resumes
    private void restoreMoodTrackerHighlighting() {
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1);
        
        // Check if Mood Tracker should be highlighted
        String highlightKey = "mood_tracker_highlighted_" + studentId;
        boolean shouldBeHighlighted = prefs.getBoolean(highlightKey, false);
        
        if (shouldBeHighlighted) {
            // Check if Mood Tracker is still available (not on cooldown)
            SharedPreferences moodPrefs = getSharedPreferences("MoodPrefs_" + studentId, MODE_PRIVATE);
            long lastTakenMillis = moodPrefs.getLong("lastMoodTimestamp", 0);
            long now = System.currentTimeMillis();
            long timeLeft = COOLDOWN_PERIOD - (now - lastTakenMillis);
            
            // If still available, restore the highlighting
            if (timeLeft <= 0) {
                CardView moodCard = findViewById(R.id.cv_MoodTracker);
                TextView readyBadge = findViewById(R.id.txtNewBadge);
                
                if (moodCard != null) {
                    // Restore visual highlighting
                    moodCard.setCardBackgroundColor(getResources().getColor(R.color.blue_light));
                    
                    if (readyBadge != null) {
                        readyBadge.setText("READY");
                        readyBadge.setVisibility(View.VISIBLE);
                    }
                    
                    // Restart pulsing animation
                    startPulsingAnimation(moodCard);
                }
            } else {
                // Cooldown is active, clear highlighting flag
                prefs.edit().putBoolean(highlightKey, false).apply();
            }
        }
    }
    
    
    // Clear highlighting when user uses the Mood Tracker
    private void clearMoodTrackerHighlighting() {
        CardView moodCard = findViewById(R.id.cv_MoodTracker);
        TextView readyBadge = findViewById(R.id.txtNewBadge);
        
        if (moodCard != null) {
            // Reset background color
            moodCard.setCardBackgroundColor(getResources().getColor(R.color.white));
            moodCard.clearAnimation();
            
            // Hide the READY badge
            if (readyBadge != null) {
                readyBadge.setVisibility(View.GONE);
            }
            
            // Mark as not highlighted so it can be highlighted again next time
            SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
            int studentId = prefs.getInt("studentId", -1);
            String highlightKey = "mood_tracker_highlighted_" + studentId;
            prefs.edit().putBoolean(highlightKey, false).apply();
        }
    }
    
    // Static method to clear highlighting from other activities/fragments
    public static void clearMoodTrackerHighlighting(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("student_session", Context.MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1);
        String highlightKey = "mood_tracker_highlighted_" + studentId;
        prefs.edit().putBoolean(highlightKey, false).apply();
    }
    
    // Static method to check and show notification from anywhere
    public static void checkAndShowMoodTrackerNotification(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("student_session", Context.MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1);
        
        if (studentId == -1) return; // No student logged in
        
        // Check if Mood Tracker has been highlighted since last use
        String highlightKey = "mood_tracker_highlighted_" + studentId;
        boolean hasBeenHighlighted = prefs.getBoolean(highlightKey, false);
        
        // Use server-side cooldown check instead of local
        checkServerCooldownForNotification(context, studentId, hasBeenHighlighted);
    }
    
    private static void checkServerCooldownForNotification(Context context, int studentId, boolean hasBeenHighlighted) {
        MoodTrackerApi apiService = ApiClient.getClient().create(MoodTrackerApi.class);
        Call<MoodCooldownResponse> call = apiService.checkCooldown(studentId);

        call.enqueue(new Callback<MoodCooldownResponse>() {
            @Override
            public void onResponse(Call<MoodCooldownResponse> call, Response<MoodCooldownResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MoodCooldownResponse cooldownResponse = response.body();
                    
                    // If Mood Tracker is available and hasn't been highlighted yet, show notification
                    if (cooldownResponse.isCanTakeMoodTracker() && !hasBeenHighlighted) {
                        // Get student name for notification
                        SharedPreferences prefs = context.getSharedPreferences("student_session", Context.MODE_PRIVATE);
                        String studentName = prefs.getString("fullName", "Student");
                        
                        // Show notification using NotificationHelper
                        NotificationHelper notificationHelper = new NotificationHelper(context);
                        notificationHelper.showMoodTrackerReadyNotification(studentName);
                        
                        // Mark as highlighted so it doesn't show again until used
                        prefs.edit().putBoolean("mood_tracker_highlighted_" + studentId, true).apply();
                    }
                } else {
                    // Fallback to local check if server fails
                    Log.e("MoodTracker", "Server cooldown check failed for notification, falling back to local check");
                    fallbackToLocalNotificationCheck(context, studentId, hasBeenHighlighted);
                }
            }

            @Override
            public void onFailure(Call<MoodCooldownResponse> call, Throwable t) {
                Log.e("MoodTracker", "Network error checking cooldown for notification: " + t.getMessage());
                // Fallback to local check if network fails
                fallbackToLocalNotificationCheck(context, studentId, hasBeenHighlighted);
            }
        });
    }
    
    private static void fallbackToLocalNotificationCheck(Context context, int studentId, boolean hasBeenHighlighted) {
        SharedPreferences moodPrefs = context.getSharedPreferences("MoodPrefs_" + studentId, Context.MODE_PRIVATE);
        long lastTakenMillis = moodPrefs.getLong("lastMoodTimestamp", 0);
        long now = System.currentTimeMillis();
        long timeLeft = (24 * 60 * 60 * 1000) - (now - lastTakenMillis); // 24 hours
        
        // If Mood Tracker is available and hasn't been highlighted yet, show notification
        if (timeLeft <= 0 && !hasBeenHighlighted) {
            // Get student name for notification
            SharedPreferences prefs = context.getSharedPreferences("student_session", Context.MODE_PRIVATE);
            String studentName = prefs.getString("fullName", "Student");
            
            // Show notification using NotificationHelper
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showMoodTrackerReadyNotification(studentName);
            
            // Mark as highlighted so it doesn't show again until used
            prefs.edit().putBoolean("mood_tracker_highlighted_" + studentId, true).apply();
        }
    }
    
    
    // Method for testing - simulates Mood Tracker being available
    private void simulateMoodTrackerAvailable() {
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        int studentId = prefs.getInt("studentId", -1);
        
        // Reset the highlighting flag to simulate availability
        String highlightKey = "mood_tracker_highlighted_" + studentId;
        prefs.edit().putBoolean(highlightKey, false).apply();
        
        // Clear any existing cooldown to make it available
        SharedPreferences moodPrefs = getSharedPreferences("MoodPrefs_" + studentId, MODE_PRIVATE);
        moodPrefs.edit().remove("lastMoodTimestamp").apply();
        
        // Trigger the highlighting
        highlightMoodTrackerButton();
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
        
        // Refresh quotes to get latest from server
        loadQuotes();
        
        // Restore highlighting state if it should be highlighted
        restoreMoodTrackerHighlighting();
        
        // Check for Mood Tracker notification when app comes to foreground
        checkAndShowMoodTrackerNotification(this);
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
