package com.example.stitarlacguidanceapp.Models;

import java.util.HashMap;
import java.util.Map;
public class ExitInterviewForm {
    public String mainReason;
    public String[] specificReasons;
    public String otherReason;
    public String plansAfterLeaving;

    public String valuesLearned;
    public String skillsLearned;

    public String otherServicesDetail;
    public String otherActivitiesDetail;


    public Map<String, String> serviceResponses = new HashMap<>();
}
