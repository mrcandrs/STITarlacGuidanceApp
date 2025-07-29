package com.example.stitarlacguidanceapp.Models;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class MoodViewModel extends ViewModel {

    private final ArrayList<Integer> scores = new ArrayList<>();

    //Add or update score for a specific question index
    public void setScoreForQuestion(int questionIndex, int score) {
        while (scores.size() <= questionIndex) {
            scores.add(0); // default fill
        }
        scores.set(questionIndex, score);
    }

    public int getScore() {
        int total = 0;
        for (int s : scores) {
            total += s;
        }
        return total;
    }

    public int getScoreForQuestion(int questionIndex) {
        if (questionIndex >= 0 && questionIndex < scores.size()) {
            return scores.get(questionIndex);
        }
        return -1;
    }

    //Reset all scores (e.g., when restarting at Question 1)
    public void resetScores() {
        scores.clear(); //Remove all previous scores
    }
}
