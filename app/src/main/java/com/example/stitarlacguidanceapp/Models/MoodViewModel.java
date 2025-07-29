package com.example.stitarlacguidanceapp.Models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MoodViewModel extends ViewModel {
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);

    public void addScore(int value) {
        Integer current = score.getValue();
        score.setValue((current != null ? current : 0) + value);
    }

    public int getScore() {
        return score.getValue() != null ? score.getValue() : 0;
    }

    public void resetScore() {
        score.setValue(0);
    }
}

