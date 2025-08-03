package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.CareerPlanningForm;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CareerPlanningApi {
    @GET("api/careerplanning/{studentId}")
    Call<CareerPlanningForm> getCareerPlanningForm(@Path("studentId") int studentId);

    @PUT("api/careerplanning/{studentId}")
    Call<Void> updateCareerPlanningForm(@Path("studentId") int studentId, @Body CareerPlanningForm form);


}
