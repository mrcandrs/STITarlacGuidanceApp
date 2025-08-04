package com.example.stitarlacguidanceapp;

import com.google.gson.annotations.SerializedName;

public class DuplicateCheckResponse {
    private boolean studentNumberExists;
    private boolean emailExists;

    @SerializedName("email1Exists")
    private boolean email1Exists;

    @SerializedName("email2Exists")
    private boolean email2Exists;

    @SerializedName("userNameExists")
    private boolean userNameExists;

    public boolean isStudentNumberExists() {
        return studentNumberExists;
    }

    public boolean isEmailExists() {
        return emailExists;
    }

    public boolean isEmail1Exists() { return email1Exists; }
    public boolean isEmail2Exists() { return email2Exists; }

    public boolean isUserNameExists() {
        return userNameExists;
    }
}

