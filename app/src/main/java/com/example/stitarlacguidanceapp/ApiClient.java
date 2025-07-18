package com.example.stitarlacguidanceapp;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:5281/"; // Emulator access to localhost
    private static Retrofit retrofit = null;

    //Student
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    //Exit Interview Form
    public static ExitInterviewApi getExitInterviewApi() {
        return retrofit.create(ExitInterviewApi.class);
    }


}
