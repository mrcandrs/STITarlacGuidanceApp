package com.example.stitarlacguidanceapp.Models;

import java.util.HashMap;
import java.util.Map;
public class ExitInterviewForm {
    public int studentId;
    public String mainReason;
    public String specificReasons; // ✅ new field (comma-separated)
    public String otherReason;
    public String plansAfterLeaving;

    public String valuesLearned;
    public String skillsLearned;

    public String serviceResponsesJson; // ✅ new field (Map as JSON)
    public String otherServicesDetail;
    public String otherActivitiesDetail;

    public String comments;

    // internal-use only (don't send directly)
    public transient String[] specificReasonsArray;
    public transient Map<String, String> serviceResponses = new HashMap<>();
}

