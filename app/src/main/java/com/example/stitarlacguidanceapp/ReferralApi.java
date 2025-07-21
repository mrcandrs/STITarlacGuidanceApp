package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.ReferralForm;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ReferralApi {
    @POST("api/referral/submit-referral")
    Call<ResponseBody> submitReferralForm(@Body ReferralForm referralForm);
}
