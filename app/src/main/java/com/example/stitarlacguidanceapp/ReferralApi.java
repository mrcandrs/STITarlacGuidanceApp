package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.ReferralForm;

import java.util.List;

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

    @GET("api/referral/student/{studentId}/all")
    Call<List<ReferralForm>> getAllReferrals(@Path("studentId") int studentId);
}
