package com.example.stitarlacguidanceapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.Activities.GuidanceAppointmentSlipActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "appointment_notifications";
    private static final String CHANNEL_NAME = "Appointment Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for appointment status updates";
    private static final String TAG = "NotificationHelper";

    private Context context;
    private NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // Helper method to check if notifications are permitted
    private boolean areNotificationsEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }

    // Helper method to safely show notification
    private void showNotificationSafely(int notificationId, NotificationCompat.Builder builder) {
        if (!areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are not enabled or permission denied");
            return;
        }

        try {
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification shown successfully with ID: " + notificationId);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException when showing notification: " + e.getMessage());
            // Handle the case where permission was revoked at runtime
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage());
        }
    }

    public void showAppointmentApprovedNotification(String studentName, String appointmentDate, String appointmentTime) {
        Intent intent = new Intent(context, GuidanceAppointmentSlipActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_approved)
                .setContentTitle("Appointment Approved! ✅")
                .setContentText("Your appointment for " + appointmentDate + " at " + appointmentTime + " has been approved.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Hello " + studentName + "! Your guidance appointment for " +
                                appointmentDate + " at " + appointmentTime + " has been approved. " +
                                "Please arrive on time for your scheduled appointment."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 300, 200, 300})
                .setLights(0xFF4CAF50, 1000, 1000);

        showNotificationSafely(1, builder);
    }

    public void showAppointmentRejectedNotification(String studentName, String appointmentDate, String appointmentTime) {
        Intent intent = new Intent(context, GuidanceAppointmentSlipActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_rejected)
                .setContentTitle("Appointment Rejected ❌")
                .setContentText("Your appointment for " + appointmentDate + " at " + appointmentTime + " has been rejected.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Hello " + studentName + ", your guidance appointment for " +
                                appointmentDate + " at " + appointmentTime + " has been rejected. " +
                                "You may submit a new appointment request."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 300, 200, 300})
                .setLights(0xFFF44336, 1000, 1000);

        showNotificationSafely(2, builder);
    }

    public void showAppointmentReminderNotification(String studentName, String appointmentDate, String appointmentTime) {
        Intent intent = new Intent(context, GuidanceAppointmentSlipActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pending)
                .setContentTitle("Appointment Reminder 🔔")
                .setContentText("Your appointment is tomorrow: " + appointmentDate + " at " + appointmentTime)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Hello " + studentName + "! This is a reminder that you have a guidance appointment " +
                                "scheduled for " + appointmentDate + " at " + appointmentTime + ". " +
                                "Please arrive on time."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        showNotificationSafely(3, builder);
    }

    // Method to check if notifications are available
    public boolean canShowNotifications() {
        return areNotificationsEnabled();
    }

    // Method to get notification permission status
    public String getNotificationPermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS);
            switch (permission) {
                case PackageManager.PERMISSION_GRANTED:
                    return "Granted";
                case PackageManager.PERMISSION_DENIED:
                    return "Denied";
                default:
                    return "Unknown";
            }
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled() ? "Enabled" : "Disabled";
        }
    }
}