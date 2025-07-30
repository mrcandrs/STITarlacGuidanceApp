package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.MoodTrackerModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MoodTrackerApi {
    @POST("api/MoodTracker")
    Call<MoodTrackerModel> submitMood(@Body MoodTrackerModel moodEntry);
}
