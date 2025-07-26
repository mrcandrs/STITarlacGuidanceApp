package com.example.stitarlacguidanceapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.DuplicateCheckResponse;
import com.example.stitarlacguidanceapp.Models.Quote;
import com.example.stitarlacguidanceapp.Models.StudentUpdateRequest;
import com.example.stitarlacguidanceapp.QuoteAdapter;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.StudentApi;
import com.example.stitarlacguidanceapp.databinding.ActivityRegistrationBinding;
import com.example.stitarlacguidanceapp.databinding.ActivityStudentDashboardBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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
    private final long SLIDE_DELAY = 4000; // 4 seconds
    private List<Quote> quoteList;
    private Uri selectedImageUri;
    private ImageView imgProfile;

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
        int studentId = prefs.getInt("studentId", -1);
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
