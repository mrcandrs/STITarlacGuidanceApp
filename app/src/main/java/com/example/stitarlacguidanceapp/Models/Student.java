package com.example.stitarlacguidanceapp.Models;

public class Student {
    public int studentId; //Will be filled by the backend after saving
    public String studentNumber;
    public String name;
    public String program;
    public String yearLevel;
    public String email;
    public String username;
    public String password;

    public Student() {
    }

    public Student(String studentNumber, String fullName, String program, String gradeYear, String email, String username, String password) {
        this.studentNumber = studentNumber;
        this.name = fullName;
        this.program = program;
        this.yearLevel = gradeYear;
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

    public String getName() {
        return name;
    }

    public void setName(String fullName) {
        this.name = fullName;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String gradeYear) {
        this.yearLevel = gradeYear;
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
