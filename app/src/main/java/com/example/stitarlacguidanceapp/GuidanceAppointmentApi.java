package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.GuidanceAppointment;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GuidanceAppointmentApi {
    @POST("api/GuidanceAppointment")
    Call<ResponseBody> submitAppointment(@Body GuidanceAppointment appointment);
}
