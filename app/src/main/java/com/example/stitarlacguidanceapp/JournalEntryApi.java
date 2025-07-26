package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.JournalEntry;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface JournalEntryApi {
    @POST("api/journalentry/submit-entry")
    Call<JournalEntry> submitJournalEntry(@Body JournalEntry entry);

    @PUT("api/journalentry/update/{id}")
    Call<ResponseBody> updateJournal(@Path("id") int journalId, @Body JournalEntry entry);

    @DELETE("api/journalentry/delete/{id}")
    Call<ResponseBody> deleteJournalEntry(@Path("id") int journalId);

}
