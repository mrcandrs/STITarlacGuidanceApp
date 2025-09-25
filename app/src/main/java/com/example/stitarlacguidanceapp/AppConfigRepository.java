package com.example.stitarlacguidanceapp;
import android.content.Context;
import android.content.SharedPreferences;
import com.example.stitarlacguidanceapp.Models.DictionariesResponse;
import com.example.stitarlacguidanceapp.Models.MobileConfig;
import com.example.stitarlacguidanceapp.Models.QuoteDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class AppConfigRepository {
    private static final String PREF = "app_config";
    private static final String KEY_DICT = "dict";
    private static final String KEY_CONFIG = "config";
    private static final String KEY_QUOTES = "quotes";
    private final SharedPreferences sp;
    private final Gson gson = new Gson();

    public AppConfigRepository(Context ctx) { sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE); }

    public void saveDictionaries(DictionariesResponse d) { sp.edit().putString(KEY_DICT, gson.toJson(d)).apply(); }
    public void saveMobileConfig(MobileConfig c) { sp.edit().putString(KEY_CONFIG, gson.toJson(c)).apply(); }
    public void saveQuotes(List<QuoteDto> q) { sp.edit().putString(KEY_QUOTES, gson.toJson(q)).apply(); }

    public DictionariesResponse getDictionaries() {
        String s = sp.getString(KEY_DICT, null);
        return s == null ? new DictionariesResponse() : gson.fromJson(s, DictionariesResponse.class);
    }
    public MobileConfig getMobileConfig() {
        String s = sp.getString(KEY_CONFIG, null);
        return s == null ? new MobileConfig() : gson.fromJson(s, MobileConfig.class);
    }
    public List<QuoteDto> getQuotes() {
        String s = sp.getString(KEY_QUOTES, null);
        if (s == null) return Collections.emptyList();
        Type t = new TypeToken<List<QuoteDto>>(){}.getType();
        return gson.fromJson(s, t);
    }
}