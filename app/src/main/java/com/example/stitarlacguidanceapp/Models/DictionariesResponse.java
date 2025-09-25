package com.example.stitarlacguidanceapp.Models;

import java.util.List;
import java.util.Map;

public class DictionariesResponse {
    public List<String> gradeYears;
    public List<String> genders;
    public List<String> academicLevels;
    public List<String> referredBy;
    public List<String> areasOfConcern;
    public List<String> actionRequested;
    public List<String> referralPriorities;
    public List<String> appointmentReasons;
    public List<String> moodLevels;

    public List<ProgramItem> programs;                          // [{ code, name }]
    public Map<String, List<String>> sectionsByProgram;         // { "BSIT": ["4A","4B"], ... }

    public static class ProgramItem {
        public String code;
        public String name;
    }
}
