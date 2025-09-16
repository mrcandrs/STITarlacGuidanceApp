package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.ReferralForm;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReferralApi {
    @POST("api/referral/submit-referral")
    Call<ResponseBody> submitReferralForm(@Body ReferralForm referralForm);

    @GET("api/referral/student/{studentId}/latest")
    Call<ReferralForm> getLatestReferral(@Path("studentId") int studentId);
}
