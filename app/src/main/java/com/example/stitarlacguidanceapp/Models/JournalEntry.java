package com.example.stitarlacguidanceapp.Models;

public class JournalEntry {
    private int journalId;
    private int studentId;
    private String date;
    private String title;
    private String content;
    private String mood;

    public JournalEntry() {
    }

    public JournalEntry(int studentId, String date, String title, String content, String mood) {
        this.studentId = studentId;
        this.date = date;
        this.title = title;
        this.content = content;
        this.mood = mood;
    }

    public int getJournalId() {
        return journalId;
    }

    public void setJournalId(int journalId) {
        this.journalId = journalId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
}
