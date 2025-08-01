package com.example.stitarlacguidanceapp.Models;

public class GuidanceAppointment {
    private int studentId;  // <-- Add this
    private String studentName;
    private String programSection;
    private String reason;
    private String date;
    private String time;
    private String status;

    public GuidanceAppointment() {
    }

    public GuidanceAppointment(int studentId, String studentName, String programSection, String reason, String date, String time, String status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.programSection = programSection;
        this.reason = reason;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getProgramSection() {
        return programSection;
    }

    public void setProgramSection(String programSection) {
        this.programSection = programSection;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

