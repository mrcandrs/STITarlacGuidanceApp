package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.JournalEntry;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface JournalEntryApi {
    @GET("api/journalentry/today/{studentId}")
    Call<JournalEntry> getToday(@Path("studentId") int studentId);

    @POST("api/journalentry/submit-entry")
    Call<JournalEntry> submitJournalEntry(@Body JournalEntry entry);

    @GET("api/journalentry/student/{studentId}")
    Call<List<JournalEntry>> getJournalEntriesByStudent(@Path("studentId") int studentId);

    @PUT("api/journalentry/update/{id}")
    Call<ResponseBody> updateJournal(@Path("id") int journalId, @Body JournalEntry entry);

    @PUT("api/journalentry/update-today/{studentId}")
    Call<ResponseBody> updateToday(@Path("studentId") int studentId, @Body JournalEntry entry);

    @DELETE("api/journalentry/delete/{id}")
    Call<ResponseBody> deleteJournalEntry(@Path("id") int journalId);

}
