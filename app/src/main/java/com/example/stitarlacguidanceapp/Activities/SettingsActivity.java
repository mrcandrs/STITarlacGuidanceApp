package com.example.stitarlacguidanceapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.DuplicateCheckResponse;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.StudentApi;
import okhttp3.ResponseBody;
import com.example.stitarlacguidanceapp.ValidationRule;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.example.stitarlacguidanceapp.databinding.ActivitySettingsBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding root;

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
        root = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        root.btnLogout.setOnClickListener(v -> logout());
        root.cvEditProfile.setOnClickListener(v -> editProfile());
        root.cvChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password,null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnCancel, btnSave;
        btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnSave = dialogView.findViewById(R.id.btnSave);
        TextInputEditText edtStudentPassword = dialogView.findViewById(R.id.edtStudentPassword);
        TextInputEditText edtConfirmPassword = dialogView.findViewById(R.id.edtConfirmPassword);
        TextInputLayout layoutPassword = dialogView.findViewById(R.id.layoutStudentPassword);
        TextInputLayout layoutConfirmPassword = dialogView.findViewById(R.id.layoutConfirmPassword);


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

        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        //Pre-fill with existing values if you have them
        int studentId = prefs.getInt("studentId", -1);
        String savedEmail = prefs.getString("email_" + studentId, "");
        String savedUsername = prefs.getString("username_" + studentId, "");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newPassword = edtStudentPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            boolean hasError = false;

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

            if (hasError) return;

            updateStudentProfile(savedEmail, savedUsername, newPassword, dialog);
        });
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    // 🔑 Clear student session (if you're using one)
                    SharedPreferences sessionPrefs = getSharedPreferences("student_session", Context.MODE_PRIVATE);
                    sessionPrefs.edit().clear().apply();

                    // 🧹 Clear journal entries saved in SharedPreferences
                    SharedPreferences journalPrefs = getSharedPreferences("journal_prefs", Context.MODE_PRIVATE);
                    journalPrefs.edit().clear().apply();

                    // 🚪 Go back to main login screen
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
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
        TextInputLayout layoutEmail = dialogView.findViewById(R.id.layoutEmail);
        TextInputLayout layoutUsername = dialogView.findViewById(R.id.layoutStudentUsername);
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


        //Live validation for email address
        addLiveValidation(layoutEmail, edtEmail, input -> {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
                    ? null
                    : "Invalid email address";
        });

        SharedPreferences prefs = getSharedPreferences("student_session", MODE_PRIVATE);
        //Pre-fill with existing values if you have them
        int studentId = prefs.getInt("studentId", -1);
        edtEmail.setText(prefs.getString("email_" + studentId, ""));
        edtStudentUsername.setText(prefs.getString("username_" + studentId, ""));
        String savedPassword = prefs.getString("password_" + studentId, "");

        // Load profile photo from server first, then fallback to local
        loadProfilePhoto(studentId);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newEmail = edtEmail.getText().toString().trim();
            String newUsername = edtStudentUsername.getText().toString().trim();

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
            // Skip network check if local validation fails
            if (hasError) return;

            // 🔐 Check for duplicate email/username before updating
            checkEmailAndUsername(newEmail, newUsername, layoutEmail, layoutUsername,
                    () -> {
                        // No duplicates → proceed to update
                        String passwordToSend = (savedPassword != null) ? savedPassword : "default123";
                        updateStudentProfile(newEmail, newUsername, passwordToSend, dialog);

                    },
                    () -> {
                        // Duplicates found → prevent save
                        Toast.makeText(SettingsActivity.this, "Email or username already exists", Toast.LENGTH_SHORT).show();
                    }
            );
        });
    }

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

        RequestBody passwordBody;
        if (password != null && !password.equals("skip")) {
            passwordBody = RequestBody.create(MediaType.parse("text/plain"), password);
        } else {
            // Send placeholder or empty string so backend can skip updating password
            passwordBody = RequestBody.create(MediaType.parse("text/plain"), "");
        }

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

        Log.d("UPDATE_DEBUG", "StudentId: " + studentId);
        Log.d("UPDATE_DEBUG", "Email: " + email);
        Log.d("UPDATE_DEBUG", "Username: " + username);
        Log.d("UPDATE_DEBUG", "Password: " + password);
        Log.d("UPDATE_DEBUG", "ImageUri: " + (selectedImageUri != null ? selectedImageUri.toString() : "null"));


        ApiClient.getClient().create(StudentApi.class)
                .updateStudentProfileWithImage(studentIdBody, emailBody, usernameBody, passwordBody, imagePart)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            SharedPreferences.Editor editor = getSharedPreferences("student_session", MODE_PRIVATE).edit();
                            editor.putString("email_" + studentId, email);
                            editor.putString("username_" + studentId, username);

                            if (selectedImageUri != null) {
                                String profileKey = "profileUri_" + studentId;
                                editor.putString(profileKey, selectedImageUri.toString()); // ✅ Save per-student
                            }
                            editor.apply();

                            Snackbar.make(root.getRoot(), "Successfully updated.", Snackbar.LENGTH_LONG).show();
                            dialog.dismiss();

                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("UPDATE_FAIL", "Error 400: " + errorBody); // 👈 Show reason in Logcat
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(SettingsActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(SettingsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addLiveValidation(com.google.android.material.textfield.TextInputLayout layout,
                                   android.widget.EditText editText,
                                   ValidationRule rule) {

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

    private String getRealPathFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            if (index == -1) {
                cursor.close();
                return uri.getPath(); // fallback
            }

            String path = cursor.getString(index);
            cursor.close();
            return path;
        }
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
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .into(imgProfile);
                    
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
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .into(imgProfile);
        }
    }
}