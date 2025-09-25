package com.example.stitarlacguidanceapp;

import android.content.Context;
import android.util.Log;

import com.example.stitarlacguidanceapp.Models.DictionariesResponse;
import com.example.stitarlacguidanceapp.Models.MobileConfig;
import com.example.stitarlacguidanceapp.Models.QuoteDto;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfigSync {
    public interface SyncCallback {
        void onComplete();
        void onError(Throwable t);
    }

    // Fetch dictionaries + mobile-config + quotes in parallel; save all; then callback
    public static void sync(Context ctx, SyncCallback cb) {
        MaintenanceApi api = ApiClient.getClient().create(MaintenanceApi.class);
        AppConfigRepository repo = new AppConfigRepository(ctx);

        AtomicInteger remaining = new AtomicInteger(3);
        final Throwable[] errorHolder = new Throwable[1];

        Callback<Object> done = new Callback<Object>() {
            @Override public void onResponse(Call<Object> call, Response<Object> response) {
                if (!response.isSuccessful()) {
                    errorHolder[0] = new RuntimeException("HTTP " + response.code());
                }
                if (remaining.decrementAndGet() == 0) finish();
            }
            @Override public void onFailure(Call<Object> call, Throwable t) {
                errorHolder[0] = t;
                if (remaining.decrementAndGet() == 0) finish();
            }
            private void finish() {
                if (errorHolder[0] != null) cb.onError(errorHolder[0]); else cb.onComplete();
            }
        };

        api.getDictionaries().enqueue(new Callback<DictionariesResponse>() {
            @Override public void onResponse(Call<DictionariesResponse> call, Response<DictionariesResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    repo.saveDictionaries(resp.body());
                } else {
                    Log.w("ConfigSync", "dictionaries not successful: " + resp.code());
                }
                done.onResponse((Call<Object>) (Object) call, (Response<Object>) (Object) resp);
            }
            @Override public void onFailure(Call<DictionariesResponse> call, Throwable t) {
                Log.e("ConfigSync", "dictionaries failed", t);
                done.onFailure((Call<Object>) (Object) call, t);
            }
        });

        api.getMobileConfig().enqueue(new Callback<MobileConfig>() {
            @Override public void onResponse(Call<MobileConfig> call, Response<MobileConfig> resp) {
                if (resp.isSuccessful() && resp.body() != null) repo.saveMobileConfig(resp.body());
                done.onResponse((Call<Object>) (Object) call, (Response<Object>) (Object) resp);
            }
            @Override public void onFailure(Call<MobileConfig> call, Throwable t) {
                done.onFailure((Call<Object>) (Object) call, t);
            }
        });

        api.getQuotes().enqueue(new Callback<List<QuoteDto>>() {
            @Override public void onResponse(Call<List<QuoteDto>> call, Response<List<QuoteDto>> resp) {
                if (resp.isSuccessful() && resp.body() != null) repo.saveQuotes(resp.body());
                done.onResponse((Call<Object>) (Object) call, (Response<Object>) (Object) resp);
            }
            @Override public void onFailure(Call<List<QuoteDto>> call, Throwable t) {
                done.onFailure((Call<Object>) (Object) call, t);
            }
        });
    }
}