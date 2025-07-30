package com.example.stitarlacguidanceapp.Models;

public class MoodTrackerModel {
    private int studentId;
    private String moodLevel;
    private String entryDate; // format: "2025-07-29T13:00:00"

    public MoodTrackerModel(int studentId, String moodLevel, String entryDate) {
        this.studentId = studentId;
        this.moodLevel = moodLevel;
        this.entryDate = entryDate;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getMoodLevel() {
        return moodLevel;
    }

    public void setMoodLevel(String moodLevel) {
        this.moodLevel = moodLevel;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }
}


