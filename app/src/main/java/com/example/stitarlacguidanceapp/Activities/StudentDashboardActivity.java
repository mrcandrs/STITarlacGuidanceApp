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

        //edit profile button
        root.btnEdit.setOnClickListener(v -> editProfile());

        //exit interview form button
        root.cvExitInterview.setOnClickListener(v -> exitInterview());


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

    //for exit interview form
    private void exitInterview() {
        startActivity(new Intent(this, ExitFormActivity.class));
    }


    //for edit profile
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
        imgProfile = dialogView.findViewById(R.id.imgProfile);
        Button btnChangePhoto = dialogView.findViewById(R.id.btnChangePhoto);

        btnChangePhoto.setOnClickListener(v -> showImageSourceDialog());

        //live validation for email and username if it exists in the database
        edtEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String email = s.toString().trim();
                String username = edtStudentUsername.getText().toString().trim();

                checkEmailAndUsername(email, username, layoutEmail, null, () -> {}, () -> {});
            }
        });

        edtStudentUsername.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String username = s.toString().trim();
                String email = edtEmail.getText().toString().trim();

                checkEmailAndUsername(email, username, null, layoutUsername, () -> {}, () -> {});
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

            // Skip network check if local validation fails
            if (hasError) return;

            // 🔐 Check for duplicate email/username before updating
            checkEmailAndUsername(newEmail, newUsername, layoutEmail, layoutUsername,
                    () -> {
                        // No duplicates → proceed to update
                        updateStudentProfile(newEmail, newUsername, newPassword, dialog);
                    },
                    () -> {
                        // Duplicates found → prevent save
                        Toast.makeText(StudentDashboardActivity.this, "Email or username already exists", Toast.LENGTH_SHORT).show();
                    }
            );
        });

    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Photo Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        //Camera
                        ImagePicker.with(this)
                                .cropSquare()
                                .cameraOnly()
                                .compress(1024)
                                .maxResultSize(512, 512)
                                .createIntent(intent -> {
                                    imagePickerLauncher.launch(intent);
                                    return null;
                                });
                    } else {
                        //Gallery
                        ImagePicker.with(this)
                                .cropSquare()
                                .galleryOnly()
                                .compress(1024)
                                .maxResultSize(512, 512)
                                .createIntent(intent -> {
                                    imagePickerLauncher.launch(intent);
                                    return null;
                                });
                    }
                }).show();
    }



    private void updateStudentProfile(String email, String username, String password, AlertDialog dialog) {
        int studentId = getSharedPreferences("student_session", MODE_PRIVATE).getInt("studentId", -1);

        RequestBody studentIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(studentId));
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), username);
        RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), password);

        MultipartBody.Part imagePart = null;

        if (selectedImageUri != null) {
            try {
                File file = new File(getRealPathFromUri(selectedImageUri));
                RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                imagePart = MultipartBody.Part.createFormData("ProfileImage", file.getName(), reqFile);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Pass an empty part so the image isn't required
            imagePart = MultipartBody.Part.createFormData("ProfileImage", "", RequestBody.create(null, new byte[0]));
        }

        ApiClient.getClient().create(StudentApi.class)
                .updateStudentProfileWithImage(studentIdBody, emailBody, usernameBody, passwordBody, imagePart)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            SharedPreferences.Editor editor = getSharedPreferences("student_session", MODE_PRIVATE).edit();
                            editor.putString("email", email);
                            editor.putString("username", username);

                            int studentId = getSharedPreferences("student_session", MODE_PRIVATE).getInt("studentId", -1);
                            if (selectedImageUri != null) {
                                String profileKey = "profileUri_" + studentId;
                                editor.putString(profileKey, selectedImageUri.toString()); // ✅ Save per-student
                            }
                            editor.apply();


                            Toast.makeText(StudentDashboardActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            // Update the dashboard profile image
                            ImageView imgDashboardProfile = root.imgProfile;
                            if (selectedImageUri != null) {
                                Glide.with(StudentDashboardActivity.this)
                                        .load(selectedImageUri)
                                        .circleCrop()
                                        .into(imgDashboardProfile);
                            }

                        } else {
                            Toast.makeText(StudentDashboardActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(StudentDashboardActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //✅ 6-parameter version
    private void checkEmailAndUsername(String email, String username, TextInputLayout emailLayout, TextInputLayout usernameLayout, Runnable onValid, Runnable onInvalid) {
        int studentId = getSharedPreferences("student_session", MODE_PRIVATE).getInt("studentId", -1);

        Log.d("CHECK", "Checking duplicates for: " + email + " | " + username);

        ApiClient.getClient().create(StudentApi.class)
                .checkEmailOrUsername(email, username, studentId)
                .enqueue(new Callback<DuplicateCheckResponse>() {
                    @Override
                    public void onResponse(Call<DuplicateCheckResponse> call, Response<DuplicateCheckResponse> response) {
                        if (response.isSuccessful()) {
                            DuplicateCheckResponse result = response.body();
                            boolean valid = true;

                            if (emailLayout != null) {
                                boolean emailTaken = result.isEmailExists();
                                emailLayout.setError(emailTaken ? "Email already in use" : null);
                                if (emailTaken) valid = false;
                            }

                            if (usernameLayout != null) {
                                boolean usernameTaken = result.isUserNameExists();
                                usernameLayout.setError(usernameTaken ? "Username already in use" : null);
                                if (usernameTaken) valid = false;
                            }

                            if (valid) {
                                if (onValid != null) onValid.run();
                            } else {
                                if (onInvalid != null) onInvalid.run();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<DuplicateCheckResponse> call, Throwable t) {
                        Log.e("EDIT_PROFILE", "Duplicate check failed", t);
                        onInvalid.run();
                    }
                });
    }

    //✅ 4-parameter overload for live validation
    private void checkEmailAndUsername(String email, String username, TextInputLayout emailLayout, TextInputLayout usernameLayout) {
        checkEmailAndUsername(email, username, emailLayout, usernameLayout,
                () -> {}, // onValid: do nothing
                () -> {}  // onInvalid: do nothing
        );
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

        // Update your UI (example using ViewBinding or findViewById)
        root.txtName.setText(fullName);
        root.txtStudentNo.setText(studentNumber);
        root.txtCourse.setText(program);
        root.txtYear.setText(yearLevel);

        // ✅ Load and display profile image if saved
        if (profileUriString != null) {
            Uri profileUri = Uri.parse(profileUriString);
            root.imgProfile.setImageURI(profileUri);
        }
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
