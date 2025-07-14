package com.example.stitarlacguidanceapp;

import com.google.gson.annotations.SerializedName;

public class DuplicateCheckResponse {
    private boolean studentNumberExists;
    private boolean emailExists;

    @SerializedName("userNameExists")
    private boolean userNameExists;

    public boolean isStudentNumberExists() {
        return studentNumberExists;
    }

    public boolean isEmailExists() {
        return emailExists;
    }

    public boolean isUserNameExists() {
        return userNameExists;
    }
}

