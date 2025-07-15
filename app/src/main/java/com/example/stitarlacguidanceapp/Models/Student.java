package com.example.stitarlacguidanceapp.Models;

import com.google.gson.annotations.SerializedName;

public class Student {
    public int studentId; //Will be filled by the backend after saving
    public String studentNumber;

    @SerializedName("fullName")
    public String fullName;
    public String program;
    @SerializedName("gradeYear")
    public String gradeYear;
    public String email;
    public String username;
    public String password;

    public Student() {
    }

    public Student(String studentNumber, String fullName, String program, String gradeYear, String email, String username, String password) {
        this.studentNumber = studentNumber;
        this.fullName = fullName;
        this.program = program;
        this.gradeYear = gradeYear;
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

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getYearLevel() {
        return gradeYear;
    }

    public void setYearLevel(String gradeYear) {
        this.gradeYear = gradeYear;
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
}
