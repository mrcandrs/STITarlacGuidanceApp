package com.example.stitarlacguidanceapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import com.example.stitarlacguidanceapp.Models.FullRegistration;
import com.example.stitarlacguidanceapp.Models.LoginRequest;
import com.example.stitarlacguidanceapp.Models.Student; // make sure the model path is correct

public interface StudentApi {

    // GET all students
    @GET("api/Student")
    Call<List<Student>> getAllStudents();

    @GET("api/student/check-duplicate")
    Call<DuplicateCheckResponse> checkDuplicate(
            @Query("studentNumber") String studentNumber,
            @Query("email") String email
    );

    // POST a new student
    @POST("api/Student")
    Call<Student> createStudent(@Body Student student);

    @POST("api/student/register")
    Call<Student> register(@Body Student student);

    @POST("api/student/login")
    Call<Student> login(@Body LoginRequest request);

    @POST("api/student/submit-full-registration")
    Call<Void> submitFullRegistration(@Body FullRegistration data);


}
