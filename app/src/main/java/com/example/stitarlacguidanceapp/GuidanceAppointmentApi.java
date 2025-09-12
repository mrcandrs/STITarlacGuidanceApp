package com.example.stitarlacguidanceapp;

import com.example.stitarlacguidanceapp.Models.AvailableSlotForStudent;
import com.example.stitarlacguidanceapp.Models.AvailableTimeSlot;
import com.example.stitarlacguidanceapp.Models.GuidanceAppointment;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface GuidanceAppointmentApi {

    // Submit a new appointment
    @POST("api/GuidanceAppointment")
    Call<ResponseBody> submitAppointment(@Body GuidanceAppointment appointment);

    // Get all appointments for a specific student
    @GET("api/GuidanceAppointment/student/{studentId}")
    Call<List<GuidanceAppointment>> getStudentAppointments(@Path("studentId") int studentId);

    // Get a specific appointment by ID
    @GET("api/GuidanceAppointment/{id}")
    Call<GuidanceAppointment> getAppointmentById(@Path("id") int id);

    // Get all pending appointments (for counselor)
    @GET("api/GuidanceAppointment/pending-appointments")
    Call<List<GuidanceAppointment>> getPendingAppointments();

    // Get all appointments (for counselor)
    @GET("api/GuidanceAppointment/all-appointments")
    Call<List<GuidanceAppointment>> getAllAppointments();

    // Add these methods to GuidanceAppointmentApi
    @GET("api/availabletimeslot")
    Call<List<AvailableTimeSlot>> getAvailableTimeSlots();

    @GET("api/availabletimeslot/with-counts")
    Call<List<AvailableTimeSlot>> getAvailableTimeSlotsWithCounts();

    @GET("api/availabletimeslot/date/{date}")
    Call<List<AvailableTimeSlot>> getAvailableTimeSlotsByDate(@Path("date") String date);

    // Add these methods to GuidanceAppointmentApi.java
    @GET("api/availabletimeslot/available-for-students")
    Call<List<AvailableSlotForStudent>> getAvailableSlotsForStudents();

    @GET("api/availabletimeslot/available-for-students/date/{date}")
    Call<List<AvailableSlotForStudent>> getAvailableSlotsForStudentsByDate(@Path("date") String date);

    // Approve an appointment
    @PUT("api/GuidanceAppointment/{id}/approve")
    Call<ResponseBody> approveAppointment(@Path("id") int id);

    // Reject an appointment
    @PUT("api/GuidanceAppointment/{id}/reject")
    Call<ResponseBody> rejectAppointment(@Path("id") int id, @Body RejectAppointmentRequest request);

    // Update appointment status (generic)
    @PUT("api/GuidanceAppointment/{id}/status")
    Call<ResponseBody> updateAppointmentStatus(@Path("id") int id, @Body StatusUpdateRequest request);

    // Helper class for status updates
    class StatusUpdateRequest {
        private String status;

        public StatusUpdateRequest(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    // Add the request class
    class RejectAppointmentRequest {
        private String rejectionReason;

        public RejectAppointmentRequest(String rejectionReason) {
            this.rejectionReason = rejectionReason;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        public void setRejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
        }
    }
}