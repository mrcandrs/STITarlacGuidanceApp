package com.example.stitarlacguidanceapp;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

import com.example.stitarlacguidanceapp.Models.FullRegistration;
import com.example.stitarlacguidanceapp.Models.LoginRequest;
import com.example.stitarlacguidanceapp.Models.Student;
import com.example.stitarlacguidanceapp.Models.StudentUpdateRequest;

public interface StudentApi {

    // GET all students
    @GET("api/Student")
    Call<List<Student>> getAllStudents();

    @GET("api/student/check-duplicate")
    Call<DuplicateCheckResponse> checkDuplicate(
            @Query("studentNumber") String studentNumber,
            @Query("email") String email
    );

    @GET("api/student/check-email-username")
    Call<DuplicateCheckResponse> checkEmailOrUsername(
            @Query("email") String email,
            @Query("username") String username,
            @Query("studentId") int studentId
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

    // UPDATE a new student
    @POST("api/student/update-profile")
    Call<Void> updateStudentProfile(@Body StudentUpdateRequest request);


    // POST profile with image
    @Multipart
    @POST("api/student/update-profile-with-image")
    Call<Void> updateStudentProfileWithImage(
            @Part("studentId") RequestBody studentId,
            @Part("email") RequestBody email,
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part MultipartBody.Part profileImage
    );



}
