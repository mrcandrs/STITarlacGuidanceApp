package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.JournalEntry;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface JournalEntryApi {
    @POST("api/journalentry/submit-entry")
    Call<Void> submitJournalEntry(@Body JournalEntry entry);
}
