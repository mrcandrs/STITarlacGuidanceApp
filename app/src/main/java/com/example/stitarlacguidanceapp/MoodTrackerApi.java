package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.MoodTrackerModel;
import com.example.stitarlacguidanceapp.Models.MoodCooldownResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MoodTrackerApi {
    @POST("api/MoodTracker")
    Call<MoodTrackerModel> submitMood(@Body MoodTrackerModel moodEntry);
    
    @GET("api/MoodTracker/cooldown/{studentId}")
    Call<MoodCooldownResponse> checkCooldown(@Path("studentId") int studentId);
}
