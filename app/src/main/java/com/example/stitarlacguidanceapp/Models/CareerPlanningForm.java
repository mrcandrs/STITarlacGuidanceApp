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

    public CareerPlanningForm(String studentNo, String program, String fullName, String gradeYear, String gender, String contactNumber, String birthday, String topValue1, String topValue2, String topValue3, String topStrength1, String topStrength2, String topStrength3, String topSkill1, String topSkill2, String topSkill3, String topInterest1, String topInterest2, String topInterest3, String programChoice, String firstChoice, String originalChoice, String programExpectation, String enrollmentReason, String futureVision, String mainPlan, boolean anotherCourse, boolean mastersProgram, String courseField, boolean localEmployment, boolean workAbroad, String natureJob1, boolean aimPromotion, boolean currentWorkAbroad, String natureJob2, String businessNature, int studentId, String section, String programChoiceReason, String currentWorkNature, String employmentNature) {
        this.studentNo = studentNo;
        this.program = program;
        this.fullName = fullName;
        this.gradeYear = gradeYear;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.birthday = birthday;
        this.topValue1 = topValue1;
        this.topValue2 = topValue2;
        this.topValue3 = topValue3;
        this.topStrength1 = topStrength1;
        this.topStrength2 = topStrength2;
        this.topStrength3 = topStrength3;
        this.topSkill1 = topSkill1;
        this.topSkill2 = topSkill2;
        this.topSkill3 = topSkill3;
        this.topInterest1 = topInterest1;
        this.topInterest2 = topInterest2;
        this.topInterest3 = topInterest3;
        this.programChoice = programChoice;
        this.firstChoice = firstChoice;
        this.originalChoice = originalChoice;
        this.programExpectation = programExpectation;
        this.enrollmentReason = enrollmentReason;
        this.futureVision = futureVision;
        this.mainPlan = mainPlan;
        this.anotherCourse = anotherCourse;
        this.mastersProgram = mastersProgram;
        this.courseField = courseField;
        this.localEmployment = localEmployment;
        this.workAbroad = workAbroad;
        this.natureJob1 = natureJob1;
        this.aimPromotion = aimPromotion;
        this.currentWorkAbroad = currentWorkAbroad;
        this.natureJob2 = natureJob2;
        this.businessNature = businessNature;
        this.studentId = studentId;
        this.section = section;
        this.programChoiceReason = programChoiceReason;
        this.currentWorkNature = currentWorkNature;
        this.employmentNature = employmentNature;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGradeYear() {
        return gradeYear;
    }

    public void setGradeYear(String gradeYear) {
        this.gradeYear = gradeYear;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getTopValue1() {
        return topValue1;
    }

    public void setTopValue1(String topValue1) {
        this.topValue1 = topValue1;
    }

    public String getTopValue2() {
        return topValue2;
    }

    public void setTopValue2(String topValue2) {
        this.topValue2 = topValue2;
    }

    public String getTopValue3() {
        return topValue3;
    }

    public void setTopValue3(String topValue3) {
        this.topValue3 = topValue3;
    }

    public String getTopStrength1() {
        return topStrength1;
    }

    public void setTopStrength1(String topStrength1) {
        this.topStrength1 = topStrength1;
    }

    public String getTopStrength2() {
        return topStrength2;
    }

    public void setTopStrength2(String topStrength2) {
        this.topStrength2 = topStrength2;
    }

    public String getTopStrength3() {
        return topStrength3;
    }

    public void setTopStrength3(String topStrength3) {
        this.topStrength3 = topStrength3;
    }

    public String getTopSkill1() {
        return topSkill1;
    }

    public void setTopSkill1(String topSkill1) {
        this.topSkill1 = topSkill1;
    }

    public String getTopSkill2() {
        return topSkill2;
    }

    public void setTopSkill2(String topSkill2) {
        this.topSkill2 = topSkill2;
    }

    public String getTopSkill3() {
        return topSkill3;
    }

    public void setTopSkill3(String topSkill3) {
        this.topSkill3 = topSkill3;
    }

    public String getTopInterest1() {
        return topInterest1;
    }

    public void setTopInterest1(String topInterest1) {
        this.topInterest1 = topInterest1;
    }

    public String getTopInterest2() {
        return topInterest2;
    }

    public void setTopInterest2(String topInterest2) {
        this.topInterest2 = topInterest2;
    }

    public String getTopInterest3() {
        return topInterest3;
    }

    public void setTopInterest3(String topInterest3) {
        this.topInterest3 = topInterest3;
    }

    public String getProgramChoice() {
        return programChoice;
    }

    public void setProgramChoice(String programChoice) {
        this.programChoice = programChoice;
    }

    public String getFirstChoice() {
        return firstChoice;
    }

    public void setFirstChoice(String firstChoice) {
        this.firstChoice = firstChoice;
    }

    public String getOriginalChoice() {
        return originalChoice;
    }

    public void setOriginalChoice(String originalChoice) {
        this.originalChoice = originalChoice;
    }

    public String getProgramExpectation() {
        return programExpectation;
    }

    public void setProgramExpectation(String programExpectation) {
        this.programExpectation = programExpectation;
    }

    public String getEnrollmentReason() {
        return enrollmentReason;
    }

    public void setEnrollmentReason(String enrollmentReason) {
        this.enrollmentReason = enrollmentReason;
    }

    public String getFutureVision() {
        return futureVision;
    }

    public void setFutureVision(String futureVision) {
        this.futureVision = futureVision;
    }

    public String getMainPlan() {
        return mainPlan;
    }

    public void setMainPlan(String mainPlan) {
        this.mainPlan = mainPlan;
    }

    public boolean isAnotherCourse() {
        return anotherCourse;
    }

    public void setAnotherCourse(boolean anotherCourse) {
        this.anotherCourse = anotherCourse;
    }

    public boolean isMastersProgram() {
        return mastersProgram;
    }

    public void setMastersProgram(boolean mastersProgram) {
        this.mastersProgram = mastersProgram;
    }

    public String getCourseField() {
        return courseField;
    }

    public void setCourseField(String courseField) {
        this.courseField = courseField;
    }

    public boolean isLocalEmployment() {
        return localEmployment;
    }

    public void setLocalEmployment(boolean localEmployment) {
        this.localEmployment = localEmployment;
    }

    public boolean isWorkAbroad() {
        return workAbroad;
    }

    public void setWorkAbroad(boolean workAbroad) {
        this.workAbroad = workAbroad;
    }

    public String getNatureJob1() {
        return natureJob1;
    }

    public void setNatureJob1(String natureJob1) {
        this.natureJob1 = natureJob1;
    }

    public boolean isAimPromotion() {
        return aimPromotion;
    }

    public void setAimPromotion(boolean aimPromotion) {
        this.aimPromotion = aimPromotion;
    }

    public boolean isCurrentWorkAbroad() {
        return currentWorkAbroad;
    }

    public void setCurrentWorkAbroad(boolean currentWorkAbroad) {
        this.currentWorkAbroad = currentWorkAbroad;
    }

    public String getNatureJob2() {
        return natureJob2;
    }

    public void setNatureJob2(String natureJob2) {
        this.natureJob2 = natureJob2;
    }

    public String getBusinessNature() {
        return businessNature;
    }

    public void setBusinessNature(String businessNature) {
        this.businessNature = businessNature;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getProgramChoiceReason() {
        return programChoiceReason;
    }

    public void setProgramChoiceReason(String programChoiceReason) {
        this.programChoiceReason = programChoiceReason;
    }

    public String getCurrentWorkNature() {
        return currentWorkNature;
    }

    public void setCurrentWorkNature(String currentWorkNature) {
        this.currentWorkNature = currentWorkNature;
    }

    public String getEmploymentNature() {
        return employmentNature;
    }

    public void setEmploymentNature(String employmentNature) {
        this.employmentNature = employmentNature;
    }
}
