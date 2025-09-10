// Models/AvailableTimeSlot.java
package com.example.stitarlacguidanceapp.Models;

public class AvailableTimeSlot {
    private int slotId;
    private String date;
    private String time;
    private int maxAppointments;
    private int currentAppointmentCount;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;

    public AvailableTimeSlot() {
    }

    // Getters and Setters
    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
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

    public int getMaxAppointments() {
        return maxAppointments;
    }

    public void setMaxAppointments(int maxAppointments) {
        this.maxAppointments = maxAppointments;
    }

    public int getCurrentAppointmentCount() {
        return currentAppointmentCount;
    }

    public void setCurrentAppointmentCount(int currentAppointmentCount) {
        this.currentAppointmentCount = currentAppointmentCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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