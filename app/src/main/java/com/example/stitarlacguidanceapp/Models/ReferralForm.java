package com.example.stitarlacguidanceapp.Models;

public class ReferralForm {
    private int studentId;
    private int referralId;

    //Basic Info
    private String fullName;
    private String studentNumber;
    private String program;
    private int age;
    private String gender;

    //Academic Level
    private String academicLevel;

    //Chips
    private String referredBy; //Comma-separated, e.g., "Student, Parent"
    private String areasOfConcern;
    private String areasOfConcernOtherDetail;
    private String actionRequested;
    private String actionRequestedOtherDetail;

    private String priorityLevel;
    private String priorityDate; //Optional, format: "yyyy-MM-dd"

    //Text Fields
    private String actionsTakenBefore;
    private String referralReasons;
    private String counselorInitialAction;

    //Footer Info
    private String personWhoReferred;
    private String dateReferred; // Format: "yyyy-MM-dd"

    //Counselor Feedback (Optional, to be filled in by counselor)
    private String counselorFeedbackStudentName;
    private String counselorFeedbackDateReferred;
    private String counselorSessionDate;
    private String counselorActionsTaken;
    private String counselorName;

    //Submission timestamp
    private String submissionDate;


    public ReferralForm() {
    }

    public ReferralForm(int studentId, String fullName, String studentNumber, String program, int age, String gender, String academicLevel, String referredBy, String areasOfConcern, String areasOfConcernOtherDetail, String actionRequested, String actionRequestedOtherDetail, String priorityLevel, String priorityDate, String actionsTakenBefore, String referralReasons, String counselorInitialAction, String personWhoReferred, String dateReferred, String counselorFeedbackStudentName, String counselorFeedbackDateReferred, String counselorSessionDate, String counselorActionsTaken, String counselorName, String submissionDate) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.studentNumber = studentNumber;
        this.program = program;
        this.age = age;
        this.gender = gender;
        this.academicLevel = academicLevel;
        this.referredBy = referredBy;
        this.areasOfConcern = areasOfConcern;
        this.areasOfConcernOtherDetail = areasOfConcernOtherDetail;
        this.actionRequested = actionRequested;
        this.actionRequestedOtherDetail = actionRequestedOtherDetail;
        this.priorityLevel = priorityLevel;
        this.priorityDate = priorityDate;
        this.actionsTakenBefore = actionsTakenBefore;
        this.referralReasons = referralReasons;
        this.counselorInitialAction = counselorInitialAction;
        this.personWhoReferred = personWhoReferred;
        this.dateReferred = dateReferred;
        this.counselorFeedbackStudentName = counselorFeedbackStudentName;
        this.counselorFeedbackDateReferred = counselorFeedbackDateReferred;
        this.counselorSessionDate = counselorSessionDate;
        this.counselorActionsTaken = counselorActionsTaken;
        this.counselorName = counselorName;
        this.submissionDate = submissionDate;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getReferralId() { return referralId; }
    public void setReferralId(int referralId) { this.referralId = referralId; }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAcademicLevel() {
        return academicLevel;
    }

    public void setAcademicLevel(String academicLevel) {
        this.academicLevel = academicLevel;
    }

    public String getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
    }

    public String getAreasOfConcern() {
        return areasOfConcern;
    }

    public void setAreasOfConcern(String areasOfConcern) {
        this.areasOfConcern = areasOfConcern;
    }

    public String getAreasOfConcernOtherDetail() {
        return areasOfConcernOtherDetail;
    }

    public void setAreasOfConcernOtherDetail(String areasOfConcernOtherDetail) {
        this.areasOfConcernOtherDetail = areasOfConcernOtherDetail;
    }

    public String getActionRequested() {
        return actionRequested;
    }

    public void setActionRequested(String actionRequested) {
        this.actionRequested = actionRequested;
    }

    public String getActionRequestedOtherDetail() {
        return actionRequestedOtherDetail;
    }

    public void setActionRequestedOtherDetail(String actionRequestedOtherDetail) {
        this.actionRequestedOtherDetail = actionRequestedOtherDetail;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getPriorityDate() {
        return priorityDate;
    }

    public void setPriorityDate(String priorityDate) {
        this.priorityDate = priorityDate;
    }

    public String getActionsTakenBefore() {
        return actionsTakenBefore;
    }

    public void setActionsTakenBefore(String actionsTakenBefore) {
        this.actionsTakenBefore = actionsTakenBefore;
    }

    public String getReferralReasons() {
        return referralReasons;
    }

    public void setReferralReasons(String referralReasons) {
        this.referralReasons = referralReasons;
    }

    public String getCounselorInitialAction() {
        return counselorInitialAction;
    }

    public void setCounselorInitialAction(String counselorInitialAction) {
        this.counselorInitialAction = counselorInitialAction;
    }

    public String getPersonWhoReferred() {
        return personWhoReferred;
    }

    public void setPersonWhoReferred(String personWhoReferred) {
        this.personWhoReferred = personWhoReferred;
    }

    public String getDateReferred() {
        return dateReferred;
    }

    public void setDateReferred(String dateReferred) {
        this.dateReferred = dateReferred;
    }

    public String getCounselorFeedbackStudentName() {
        return counselorFeedbackStudentName;
    }

    public void setCounselorFeedbackStudentName(String counselorFeedbackStudentName) {
        this.counselorFeedbackStudentName = counselorFeedbackStudentName;
    }

    public String getCounselorFeedbackDateReferred() {
        return counselorFeedbackDateReferred;
    }

    public void setCounselorFeedbackDateReferred(String counselorFeedbackDateReferred) {
        this.counselorFeedbackDateReferred = counselorFeedbackDateReferred;
    }

    public String getCounselorSessionDate() {
        return counselorSessionDate;
    }

    public void setCounselorSessionDate(String counselorSessionDate) {
        this.counselorSessionDate = counselorSessionDate;
    }

    public String getCounselorActionsTaken() {
        return counselorActionsTaken;
    }

    public void setCounselorActionsTaken(String counselorActionsTaken) {
        this.counselorActionsTaken = counselorActionsTaken;
    }

    public String getCounselorName() {
        return counselorName;
    }

    public void setCounselorName(String counselorName) {
        this.counselorName = counselorName;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }
}


