package com.example.stitarlacguidanceapp;

public class DuplicateCheckResponse {
    private boolean studentNumberExists;
    private boolean emailExists;

    public boolean isStudentNumberExists() {
        return studentNumberExists;
    }

    public boolean isEmailExists() {
        return emailExists;
    }
}

