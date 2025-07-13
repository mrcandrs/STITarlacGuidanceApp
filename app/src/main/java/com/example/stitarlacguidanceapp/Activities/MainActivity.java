package com.example.stitarlacguidanceapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.stitarlacguidanceapp.ApiClient;
import com.example.stitarlacguidanceapp.StudentApi;
import com.example.stitarlacguidanceapp.databinding.ActivityMainBinding;
import com.example.stitarlacguidanceapp.Models.Student;
import com.example.stitarlacguidanceapp.Models.LoginRequest;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        if (getIntent().getBooleanExtra("registered", false)) {
            Toast.makeText(this, "All forms submitted successfully!", Toast.LENGTH_LONG).show();
        }


        root.btnLogin.setOnClickListener(v -> performLogin());
        root.btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, ClientConsentFormActivity.class));
        });
    }

    private void performLogin() {
        String login = root.edtLogin.getText().toString().trim();
        String password = root.edtPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        StudentApi studentApi = ApiClient.getClient().create(StudentApi.class);
        LoginRequest request = new LoginRequest(login, password);

        Call<Student> call = studentApi.login(request);
        call.enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    // TODO: Navigate to another screen (e.g., DashboardActivity)
                    startActivity(new Intent(MainActivity.this, StudentDashboardActivity.class));
                } else {
                    Toast.makeText(MainActivity.this, "Login failed: Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                Log.e("LOGIN", "Error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}