package com.example.stitarlacguidanceapp.Models;

public class JournalEntry {
    private String title;
    private String content;
    private String date;
    private String mood;

    public JournalEntry(String title, String content, String date, String mood) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.mood = mood;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public String getMood() { return mood; }
}
