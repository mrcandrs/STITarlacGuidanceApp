package com.example.stitarlacguidanceapp.Models;

public class GuidancePass {
    private int passId;
    private int appointmentId;
    private String issuedDate;
    private String notes;
    private int counselorId;
    private GuidanceAppointment appointment;
    private Counselor counselor;

    // Getters and setters
    public int getPassId() { return passId; }
    public void setPassId(int passId) { this.passId = passId; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public String getIssuedDate() { return issuedDate; }
    public void setIssuedDate(String issuedDate) { this.issuedDate = issuedDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getCounselorId() { return counselorId; }
    public void setCounselorId(int counselorId) { this.counselorId = counselorId; }

    public GuidanceAppointment getAppointment() { return appointment; }
    public void setAppointment(GuidanceAppointment appointment) { this.appointment = appointment; }

    public Counselor getCounselor() { return counselor; }
    public void setCounselor(Counselor counselor) { this.counselor = counselor; }
}
