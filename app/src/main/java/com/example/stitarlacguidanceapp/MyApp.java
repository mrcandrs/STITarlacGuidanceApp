package com.example.stitarlacguidanceapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.stitarlacguidanceapp.Models.ReferralForm;
import com.example.stitarlacguidanceapp.ReferralApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApp extends Application {

    private final Handler handler = new Handler();
    private final Runnable poll = new Runnable() {
        @Override public void run() {
            try {
                SharedPreferences sessionPrefs = getSharedPreferences("student_session", MODE_PRIVATE);
                int studentId = sessionPrefs.getInt("studentId", -1);
                String loggedInStudentName = sessionPrefs.getString("fullName", "");
                if (studentId > 0) {
                    ReferralApi api = ApiClient.getReferralFormApi();
                    api.getLatestReferral(studentId).enqueue(new Callback<ReferralForm>() {
                        @Override public void onResponse(Call<ReferralForm> call, Response<ReferralForm> resp) {
                            if (!resp.isSuccessful() || resp.body() == null) return;
                            // inside onResponse of the poll:
                            ReferralForm r = resp.body();

                            String actions  = safe(r.getCounselorActionsTaken());
                            String fbDate   = safe(r.getCounselorFeedbackDateReferred());
                            String session  = safe(r.getCounselorSessionDate());
                            String counselor= safe(r.getCounselorName());

                            boolean hasFeedback = !actions.isEmpty() || !fbDate.isEmpty() || !session.isEmpty() || !counselor.isEmpty();
                            if (!hasFeedback) return;

                            String feedbackHash = Integer.toHexString((actions + "|" + fbDate + "|" + session + "|" + counselor).hashCode());

                            // include referralId so a new referral with same feedback still notifies
                            String newKey = r.getReferralId() + "|" + feedbackHash;

                            SharedPreferences sp = getSharedPreferences("referral_prefs", MODE_PRIVATE);
                            String lastKey = sp.getString("last_feedback_key", "");

                            if (!newKey.equals(lastKey)) {
                                // Use logged-in student's name instead of referred student's name
                                String name = (!loggedInStudentName.trim().isEmpty()) ? loggedInStudentName : "Student";
                                new NotificationHelper(getApplicationContext()).showReferralFeedbackNotification(name);
                                sp.edit().putString("last_feedback_key", newKey).apply();
                            }
                        }
                        @Override public void onFailure(Call<ReferralForm> call, Throwable t) {
                            Log.w("MyApp", "Referral poll failed: " + t.getMessage());
                        }
                    });
                }
            } finally {
                handler.postDelayed(this, 2000); // every 2s
            }
        }
        private String safe(String s) { return s == null ? "" : s.trim(); }
    };

    @Override public void onCreate() {
        super.onCreate();

        MaintenanceApi api = ApiClient.getClient().create(MaintenanceApi.class);
        AppConfigRepository repo = new AppConfigRepository(this);

        api.getDictionaries().enqueue(new retrofit2.Callback<com.example.stitarlacguidanceapp.Models.DictionariesResponse>() {
            @Override public void onResponse(retrofit2.Call<com.example.stitarlacguidanceapp.Models.DictionariesResponse> call,
                                             retrofit2.Response<com.example.stitarlacguidanceapp.Models.DictionariesResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) repo.saveDictionaries(resp.body());
            }
            @Override public void onFailure(retrofit2.Call<com.example.stitarlacguidanceapp.Models.DictionariesResponse> call, Throwable t) {}
        });

        api.getMobileConfig().enqueue(new retrofit2.Callback<com.example.stitarlacguidanceapp.Models.MobileConfig>() {
            @Override public void onResponse(retrofit2.Call<com.example.stitarlacguidanceapp.Models.MobileConfig> call,
                                             retrofit2.Response<com.example.stitarlacguidanceapp.Models.MobileConfig> resp) {
                if (resp.isSuccessful() && resp.body() != null) repo.saveMobileConfig(resp.body());
            }
            @Override public void onFailure(retrofit2.Call<com.example.stitarlacguidanceapp.Models.MobileConfig> call, Throwable t) {}
        });

        api.getQuotes().enqueue(new retrofit2.Callback<java.util.List<com.example.stitarlacguidanceapp.Models.QuoteDto>>() {
            @Override public void onResponse(retrofit2.Call<java.util.List<com.example.stitarlacguidanceapp.Models.QuoteDto>> call,
                                             retrofit2.Response<java.util.List<com.example.stitarlacguidanceapp.Models.QuoteDto>> resp) {
                if (resp.isSuccessful() && resp.body() != null) repo.saveQuotes(resp.body());
            }
            @Override public void onFailure(retrofit2.Call<java.util.List<com.example.stitarlacguidanceapp.Models.QuoteDto>> call, Throwable t) {}
        });

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override public void onStart(LifecycleOwner owner) { handler.post(poll); }
            @Override public void onStop(LifecycleOwner owner) { handler.removeCallbacksAndMessages(null); }
        });
    }
}
