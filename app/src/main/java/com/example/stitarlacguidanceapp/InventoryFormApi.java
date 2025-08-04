package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.InventoryForm;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface InventoryFormApi {
    @GET("api/inventory/{studentId}")
    Call<InventoryForm> getIndividualInventoryForm(@Path("studentId") int studentId);

    @PUT("api/inventory/{studentId}")
    Call<Void> updateInventory(@Path("studentId") int studentId, @Body InventoryForm form);
}
