package com.example.stitarlacguidanceapp.Models;

public class MoodCooldownResponse {
    private boolean canTakeMoodTracker;
    private long lastMoodTimestamp;
    private long timeRemaining; // in milliseconds
    private String message;

    public MoodCooldownResponse() {
    }

    public MoodCooldownResponse(boolean canTakeMoodTracker, long lastMoodTimestamp, long timeRemaining, String message) {
        this.canTakeMoodTracker = canTakeMoodTracker;
        this.lastMoodTimestamp = lastMoodTimestamp;
        this.timeRemaining = timeRemaining;
        this.message = message;
    }

    public boolean isCanTakeMoodTracker() {
        return canTakeMoodTracker;
    }

    public void setCanTakeMoodTracker(boolean canTakeMoodTracker) {
        this.canTakeMoodTracker = canTakeMoodTracker;
    }

    public long getLastMoodTimestamp() {
        return lastMoodTimestamp;
    }

    public void setLastMoodTimestamp(long lastMoodTimestamp) {
        this.lastMoodTimestamp = lastMoodTimestamp;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
