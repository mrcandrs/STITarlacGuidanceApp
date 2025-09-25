package com.example.stitarlacguidanceapp;
import com.example.stitarlacguidanceapp.Models.DictionariesResponse;
import com.example.stitarlacguidanceapp.Models.MobileConfig;
import com.example.stitarlacguidanceapp.Models.QuoteDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MaintenanceApi {
    @GET("api/maintenance/dictionaries")
    Call<DictionariesResponse> getDictionaries();

    @GET("api/maintenance/mobile-config")
    Call<MobileConfig> getMobileConfig();

    @GET("api/maintenance/quotes")
    Call<List<QuoteDto>> getQuotes();
}
