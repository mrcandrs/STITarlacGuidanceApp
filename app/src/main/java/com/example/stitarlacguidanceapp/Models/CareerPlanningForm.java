package com.example.stitarlacguidanceapp.Models;

public class CareerPlanningForm {
    // Personal Info
    public String studentNo;
    public String program;
    public String fullName;
    public String gradeYear;
    public String gender;
    public String contactNumber;
    public String birthday;

    // Self-Assessment
    public String topValue1;
    public String topValue2;
    public String topValue3;

    public String topStrength1;
    public String topStrength2;
    public String topStrength3;

    public String topSkill1;
    public String topSkill2;
    public String topSkill3;

    public String topInterest1;
    public String topInterest2;
    public String topInterest3;

    // Career Choices
    public String programChoice;
    public String firstChoice;
    public String originalChoice;
    public String programExpectation;
    public String enrollmentReason;
    public String futureVision;

    // Plans After Graduation
    public String mainPlan;

    public boolean anotherCourse;
    public boolean mastersProgram;
    public String courseField;

    public boolean localEmployment;
    public boolean workAbroad;
    public String natureJob1;

    public boolean aimPromotion;
    public boolean currentWorkAbroad;
    public String natureJob2;

    public String businessNature;

    public int studentId;  // Will be filled automatically by the backend after registration
    public String section;
    public String programChoiceReason;
    public String currentWorkNature;
    public String employmentNature;

    public CareerPlanningForm() {
    }
}
