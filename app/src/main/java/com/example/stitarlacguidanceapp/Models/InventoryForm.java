package com.example.stitarlacguidanceapp.Models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InventoryForm {

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("studentNumber")
    public String studentNumber;

    @SerializedName("program")
    public String program;

    @SerializedName("nickname")
    public String nickname;

    @SerializedName("nationality")
    public String nationality;

    @SerializedName("gender")
    public String gender;

    @SerializedName("civilStatus")
    public String civilStatus;

    @SerializedName("religion")
    public String religion;

    @SerializedName("birthday")
    public String birthday; // ISO string e.g., "yyyy-MM-dd"

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("email1")
    public String email1;

    @SerializedName("email2")
    public String email2;

    @SerializedName("presentAddress")
    public String presentAddress;

    @SerializedName("permanentAddress")
    public String permanentAddress;

    @SerializedName("provincialAddress")
    public String provincialAddress;

    // Spouse Info
    @SerializedName("spouseName")
    public String spouseName;

    @SerializedName("spouseAge")
    public Integer spouseAge;

    @SerializedName("spouseOccupation")
    public String spouseOccupation;

    @SerializedName("spouseContact")
    public String spouseContact;

    // Family Background
    @SerializedName("fatherName")
    public String fatherName;

    @SerializedName("fatherOccupation")
    public String fatherOccupation;

    @SerializedName("fatherContact")
    public String fatherContact;

    @SerializedName("fatherIncome")
    public String fatherIncome;

    @SerializedName("motherName")
    public String motherName;

    @SerializedName("motherOccupation")
    public String motherOccupation;

    @SerializedName("motherContact")
    public String motherContact;

    @SerializedName("motherIncome")
    public String motherIncome;

    @SerializedName("fatherStatus")
    public String fatherStatus;

    @SerializedName("motherStatus")
    public String motherStatus;

    @SerializedName("guardianName")
    public String guardianName;

    @SerializedName("guardianContact")
    public String guardianContact;

    // Education
    @SerializedName("elementary")
    public String elementary;

    @SerializedName("juniorHigh")
    public String juniorHigh;

    @SerializedName("seniorHigh")
    public String seniorHigh;

    @SerializedName("college")
    public String college;

    // Interests
    @SerializedName("sports")
    public String sports;

    @SerializedName("hobbies")
    public String hobbies;

    @SerializedName("talents")
    public String talents;

    @SerializedName("socioCivic")
    public String socioCivic;

    @SerializedName("schoolOrg")
    public String schoolOrg;

    // Health History
    @SerializedName("wasHospitalized")
    public Boolean wasHospitalized;

    @SerializedName("hospitalizedReason")
    public String hospitalizedReason;

    @SerializedName("hadOperation")
    public Boolean hadOperation;

    @SerializedName("operationReason")
    public String operationReason;

    @SerializedName("hasIllness")
    public Boolean hasIllness;

    @SerializedName("illnessDetails")
    public String illnessDetails;

    @SerializedName("takesMedication")
    public Boolean takesMedication;

    @SerializedName("medicationDetails")
    public String medicationDetails;

    @SerializedName("hasMedicalCertificate")
    public Boolean hasMedicalCertificate;

    @SerializedName("familyIllness")
    public String familyIllness;

    @SerializedName("lastDoctorVisit")
    public String lastDoctorVisit; // ISO string

    @SerializedName("visitReason")
    public String visitReason;

    // Life Circumstances
    @SerializedName("lossExperience")
    public String lossExperience;

    @SerializedName("problems")
    public String problems;

    @SerializedName("relationshipConcerns")
    public String relationshipConcerns;

    // Sibling and Work Experience
    @SerializedName("siblings")
    public List<Sibling> siblings;

    @SerializedName("workExperience")
    public List<WorkExperience> workExperience;

    @SerializedName("studentId")
    public int studentId;

    public static class Sibling {
        @SerializedName("name")
        public String name;

        @SerializedName("age")
        public Integer age;

        @SerializedName("gender")
        public String gender;

        @SerializedName("programOrOccupation")
        public String programOrOccupation;

        @SerializedName("schoolOrCompany")
        public String schoolOrCompany;
    }

    public static class WorkExperience {
        @SerializedName("company")
        public String company;

        @SerializedName("position")
        public String position;

        @SerializedName("duration")
        public String duration;
    }
}