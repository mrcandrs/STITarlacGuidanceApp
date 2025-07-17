package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.ExitInterviewForm;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ExitInterviewApi {
    @POST("api/exitinterview/submit")
    Call<Void> submitExitForm(@Body ExitInterviewForm form);
}

