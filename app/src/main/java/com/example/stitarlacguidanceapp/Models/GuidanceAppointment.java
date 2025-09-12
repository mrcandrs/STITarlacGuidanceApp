package com.example.stitarlacguidanceapp.Models;

public class GuidanceAppointment {
    private int appointmentId;
    private int studentId;
    private String studentName;
    private String programSection;
    private String reason;
    private String date;        // Scheduled appointment date
    private String time;        // Scheduled appointment time
    private String status;
    private String rejectionReason;
    private String createdAt;   // When appointment was submitted (from server)
    private String updatedAt;   // When status was last updated (from server)

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

    // Getters and Setters
    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
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

    // Add getter and setter
    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}