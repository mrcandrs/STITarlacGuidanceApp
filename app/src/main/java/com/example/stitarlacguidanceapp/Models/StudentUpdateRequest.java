package com.example.stitarlacguidanceapp.Models;

public class StudentUpdateRequest {
    private int studentId;
    private String email;
    private String username;
    private String password;

    public StudentUpdateRequest(int studentId, String email, String username, String password) {
        this.studentId = studentId;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getters and setters (if needed)
}
