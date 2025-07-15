package com.example.stitarlacguidanceapp.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.DuplicateCheckResponse;
import com.example.stitarlacguidanceapp.Models.Quote;
import com.example.stitarlacguidanceapp.Models.StudentUpdateRequest;
import com.example.stitarlacguidanceapp.QuoteAdapter;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.StudentApi;
import com.example.stitarlacguidanceapp.databinding.ActivityRegistrationBinding;
import com.example.stitarlacguidanceapp.databinding.ActivityStudentDashboardBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());


        //load the student's info in profile
        loadStudentInfo();

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

        root.btnEdit.setOnClickListener(v -> editProfile());
    }

    private void editProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile,null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnCancel, btnSave;

        btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnSave = dialogView.findViewById(R.id.btnSave);
        TextInputEditText edtEmail = dialogView.findViewById(R.id.edtEmail);
        TextInputEditText edtStudentUsername = dialogView.findViewById(R.id.edtStudentUsername);
        TextInputEditText edtStudentPassword = dialogView.findViewById(R.id.edtStudentPassword);
        TextInputEditText edtConfirmPassword = dialogView.findViewById(R.id.edtConfirmPassword);
        TextInputLayout layoutEmail = dialogView.findViewById(R.id.layoutEmail);
        TextInputLayout layoutUsername = dialogView.findViewById(R.id.layoutStudentUsername);
        TextInputLayout layoutPassword = dialogView.findViewById(R.id.layoutStudentPassword);
        TextInputLayout layoutConfirmPassword = dialogView.findViewById(R.id.layoutConfirmPassword);

        //live validation for email and username if it exists in the database
        edtEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String email = s.toString().trim();
                String username = edtStudentUsername.getText().toString().trim(); // optional, or pass empty

                checkEmailAndUsername(email, username, layoutEmail, null);
            }
        });

        edtStudentUsername.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String username = s.toString().trim();
                String email = edtEmail.getText().toString().trim(); // optional, or pass empty

                checkEmailAndUsername(email, username, null, layoutUsername);
            }
        });


        //live validation for password is null, removes the setError
        edtConfirmPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String confirmPassword = s.toString().trim();
                String newPassword = edtStudentPassword.getText().toString().trim();

                if (confirmPassword.isEmpty()) {
                    layoutConfirmPassword.setError(null); // clear error if empty
                } else if (!confirmPassword.equals(newPassword)) {
                    layoutConfirmPassword.setError("Passwords do not match");
                } else {
                    layoutConfirmPassword.setError(null); // clear error if matched
                }
            }
        });

        //Live validation for email address
        addLiveValidation(layoutEmail, edtEmail, input -> {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
                    ? null
                    : "Invalid email address";
        });

        //Live validation for new password regex
        addLiveValidation(layoutPassword, edtStudentPassword, input -> {
            if (input == null || input.isEmpty()) {
                return null;
            }

            String passwordPattern = "^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{6,}$";
            return input.matches(passwordPattern)
                    ? null
                    : "Password must be 6+ characters, include 1 uppercase and 1 special character";
        });

        // Pre-fill with existing values if you have them
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newEmail = edtEmail.getText().toString().trim();
            String newUsername = edtStudentUsername.getText().toString().trim();
            String newPassword = edtStudentPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            boolean hasError = false;

            if (newEmail.isEmpty()) {
                layoutEmail.setError("Email is required");
                hasError = true;
            } else {
                layoutEmail.setError(null);
            }

            if (newUsername.isEmpty()) {
                layoutUsername.setError("Username is required");
                hasError = true;
            } else {
                layoutUsername.setError(null);
            }

            if (newPassword.isEmpty()) {
                layoutPassword.setError("Password is required");
                hasError = true;
            } else {
                layoutPassword.setError(null);
            }

            if (confirmPassword.isEmpty()) {
                layoutConfirmPassword.setError("Please confirm your password");
                hasError = true;
            } else if (!confirmPassword.equals(newPassword)) {
                layoutConfirmPassword.setError("Passwords do not match");
                hasError = true;
            } else {
                layoutConfirmPassword.setError(null);
            }

            if (!hasError) {
                updateStudentProfile(newEmail, newUsername, newPassword, dialog);
            }
        });
    }

    private void updateStudentProfile(String email, String username, String password, AlertDialog dialog) {
        int studentId = getSharedPreferences("student_session", MODE_PRIVATE).getInt("studentId", -1);
        StudentUpdateRequest request = new StudentUpdateRequest(studentId, email, username, password);

        Log.d("EDIT_PROFILE", "Request: " + new Gson().toJson(request)); // Optional

        ApiClient.getClient().create(StudentApi.class)
                .updateStudentProfile(request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // success
                            SharedPreferences.Editor editor = getSharedPreferences("student_session", MODE_PRIVATE).edit();
                            editor.putString("email", email);
                            editor.putString("username", username);
                            editor.apply();
                            Toast.makeText(StudentDashboardActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Log.e("EDIT_PROFILE", "Update failed: " + response.code() + ", " + response.message());
                            Toast.makeText(StudentDashboardActivity.this, "Update failed!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("EDIT_PROFILE", "Network error", t);
                        Toast.makeText(StudentDashboardActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkEmailAndUsername(String email, String username, TextInputLayout emailLayout, TextInputLayout usernameLayout) {
        ApiClient.getClient().create(StudentApi.class)
                .checkEmailOrUsername(email, username)
                .enqueue(new Callback<DuplicateCheckResponse>() {
                    @Override
                    public void onResponse(Call<DuplicateCheckResponse> call, Response<DuplicateCheckResponse> response) {
                        if (response.isSuccessful()) {
                            DuplicateCheckResponse result = response.body();
                            if (result != null) {
                                if (emailLayout != null) {
                                    emailLayout.setError(result.isEmailExists() ? "Email already in use" : null);
                                }
                                if (usernameLayout != null) {
                                    usernameLayout.setError(result.isUserNameExists() ? "Username already in use" : null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<DuplicateCheckResponse> call, Throwable t) {
                        Log.e("EDIT_PROFILE", "Duplicate check failed", t);
                    }
                });
    }



    private void loadStudentInfo() {
        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);

        String fullName = prefs.getString("fullName", "Unknown");
        String studentNumber = prefs.getString("studentNumber", "");
        String program = prefs.getString("program", "");
        String yearLevel = prefs.getString("yearLevel", "");

        // Update your UI (example using ViewBinding or findViewById)
        root.txtName.setText(fullName);
        root.txtStudentNo.setText(studentNumber);
        root.txtCourse.setText(program);
        root.txtYear.setText(yearLevel);
    }

    private void addLiveValidation(com.google.android.material.textfield.TextInputLayout layout,
                                   android.widget.EditText editText,
                                   RegistrationActivity.ValidationRule rule) {

        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String input = s.toString().trim();
                String error = rule.validate(input);
                layout.setError(error); // Show or clear error in TextInputLayout
            }
        });
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
    }

}
