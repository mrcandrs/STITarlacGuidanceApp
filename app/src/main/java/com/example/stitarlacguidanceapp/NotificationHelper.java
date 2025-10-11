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

import com.example.stitarlacguidanceapp.Activities.ReferralFormActivity;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.Activities.GuidanceAppointmentSlipActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "appointment_notifications";
    private static final String CHANNEL_NAME = "Appointment Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for appointment status updates";
    private static final String TAG = "NotificationHelper";

    private Context context;
    private NotificationManagerCompat notificationManager;

    // Track notifications by type, not by status
    private String lastNotificationType = "";
    private long lastNotificationTime = 0;
    private static final long NOTIFICATION_COOLDOWN = 10000; // Reduced to 10 seconds

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
        Log.d(TAG, "NotificationHelper initialized");
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
                Log.d(TAG, "Notification channel created");
            }
        }
    }

    // Helper method to check if notifications are permitted
    private boolean areNotificationsEnabled() {
        boolean enabled;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            enabled = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            enabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
        Log.d(TAG, "Notifications enabled: " + enabled);
        return enabled;
    }

    // Helper method to check if we should show notification
    private boolean shouldShowNotification(String notificationType) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastNotification = currentTime - lastNotificationTime;

        Log.d(TAG, "Checking if should show notification for type: " + notificationType);
        Log.d(TAG, "Last notification type: " + lastNotificationType);
        Log.d(TAG, "Time since last notification: " + timeSinceLastNotification + "ms");
        Log.d(TAG, "Cooldown period: " + NOTIFICATION_COOLDOWN + "ms");

        // Don't show if same notification type was shown recently
        if (lastNotificationType.equals(notificationType) && timeSinceLastNotification < NOTIFICATION_COOLDOWN) {
            Log.d(TAG, "Skipping duplicate notification for type: " + notificationType);
            return false;
        }

        Log.d(TAG, "Notification should be shown");
        return true;
    }

    // Helper method to safely show notification with deduplication
    private void showNotificationSafely(int notificationId, NotificationCompat.Builder builder, String notificationType) {
        Log.d(TAG, "Attempting to show notification with ID: " + notificationId + " for type: " + notificationType);

        if (!areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are not enabled or permission denied");
            return;
        }

        if (!shouldShowNotification(notificationType)) {
            Log.d(TAG, "Notification blocked by deduplication logic");
            return;
        }

        try {
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification shown successfully with ID: " + notificationId);

            // Update tracking
            lastNotificationType = notificationType;
            lastNotificationTime = System.currentTimeMillis();
            Log.d(TAG, "Updated tracking - Type: " + notificationType + ", Time: " + lastNotificationTime);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException when showing notification: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage());
        }
    }

    public void showAppointmentApprovedNotification(String studentName, String appointmentDate, String appointmentTime) {
        Log.d(TAG, "showAppointmentApprovedNotification called for: " + studentName);

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

        showNotificationSafely(1, builder, "approved");
    }

    public void showAppointmentRejectedNotification(String studentName, String appointmentDate, String appointmentTime) {
        Log.d(TAG, "showAppointmentRejectedNotification called for: " + studentName);

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

        showNotificationSafely(2, builder, "rejected");
    }

    public void showReferralFeedbackNotification(String studentName) {
        Intent intent = new Intent(context, ReferralFormActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.feedback) // your feedback icon
                .setContentTitle("New Feedback from Counselor")
                .setContentText("Your referral has new feedback. Tap to view.")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Hi " + studentName + ", your counselor posted feedback on your referral."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        showNotificationSafely(100, b, "referral_feedback");
    }

    public void showAppointmentReminderNotification(String studentName, String appointmentDate, String appointmentTime) {
        Log.d(TAG, "showAppointmentReminderNotification called for: " + studentName);

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

        showNotificationSafely(3, builder, "reminder");
    }

    public void showMoodTrackerReadyNotification(String studentName) {
        Log.d(TAG, "showMoodTrackerReadyNotification called for: " + studentName);

        Intent intent = new Intent(context, com.example.stitarlacguidanceapp.Activities.MoodTrackerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.smile)
                .setContentTitle("🎯 Mood Tracker Ready!")
                .setContentText("Track your daily emotions - it's time for your mood check-in")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Hello " + studentName + "! Your Mood Tracker is ready for today. " +
                                "Take a moment to track your emotions and help us understand how you're feeling."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 300, 200, 300})
                .setLights(0xFF2196F3, 1000, 1000);

        showNotificationSafely(4, builder, "mood_tracker_ready");
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

    // Add this method to reset the deduplication tracking (useful for testing)
    public void resetNotificationTracking() {
        lastNotificationType = "";
        lastNotificationTime = 0;
        Log.d(TAG, "Notification tracking reset");
    }
}