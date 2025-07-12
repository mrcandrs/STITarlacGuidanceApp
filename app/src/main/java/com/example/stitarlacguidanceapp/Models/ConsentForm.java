package com.example.stitarlacguidanceapp.Models;

public class ConsentForm {
    public String parentName;
    public boolean isAgreed;
    public String signedDate;
    public int studentId;  //This will be filled by the API before saving to the database
    public int counselorId;

    public ConsentForm(int counselorId) {
        this.counselorId = counselorId;
    }

    public ConsentForm() {
    }

    public ConsentForm(String parentName, boolean isAgreed, String signedDate) {
        this.parentName = parentName;
        this.isAgreed = isAgreed;
        this.signedDate = signedDate;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public boolean isAgreed() {
        return isAgreed;
    }

    public void setAgreed(boolean agreed) {
        isAgreed = agreed;
    }

    public String getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(String signedDate) {
        this.signedDate = signedDate;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(int counselorId) {
        this.counselorId = counselorId;
    }
}
