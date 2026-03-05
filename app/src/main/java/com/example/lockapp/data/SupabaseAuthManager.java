package com.example.lockapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Manager de autenticación y operaciones con Supabase usando REST puro (OkHttp + Gson).
 */
public class SupabaseAuthManager {

    private static final String TAG = "SupabaseAuth";

    private static final String SUPABASE_URL = "https://fyvlikksdzkcwppxtmaj.supabase.co";
    private static final String ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZ5dmxpa2tzZHprY3dwcHh0bWFqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI2NjE4NzgsImV4cCI6MjA4ODIzNzg3OH0.0WYiMuxdNLHU3sS21Rfp6Mf6FnSSCR7iQCx4qUuqBN8";

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Context context;

    public SupabaseAuthManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void testConnection() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/")
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + ANON_KEY)
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                Log.d(TAG, "Conexión test: " + response.code());
            } catch (IOException e) {
                Log.e(TAG, "Error test conexión", e);
            }
        }).start();
    }

    // ────────────────────────────────────────────────
    // LOGIN
    // ────────────────────────────────────────────────
    public void loginWithEmail(String email, String password, AuthCallback callback) {
        new Thread(() -> {
            try {
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("email", email);
                jsonBody.addProperty("password", password);

                RequestBody body = RequestBody.create(gson.toJson(jsonBody), JSON_MEDIA_TYPE);

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                        .post(body)
                        .header("apikey", ANON_KEY)
                        .header("Content-Type", "application/json")
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                Log.d(TAG, "Login respuesta: " + response.code() + " → " + responseBody);

                if (response.isSuccessful()) {
                    JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                    String token = json.get("access_token").getAsString();

                    SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit().putString("access_token", token).apply();

                    callback.onSuccess(token);
                } else {
                    callback.onError(response.code() + " - " + responseBody);
                }
            } catch (Exception e) {
                callback.onError("Excepción: " + e.getMessage());
            }
        }).start();
    }

    // ────────────────────────────────────────────────
    // SIGNUP (REGISTRO)  ← NUEVO MÉTODO
    // ────────────────────────────────────────────────
    public void signUpWithEmail(String email, String password, AuthCallback callback) {
        new Thread(() -> {
            try {
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("email", email);
                jsonBody.addProperty("password", password);

                RequestBody body = RequestBody.create(gson.toJson(jsonBody), JSON_MEDIA_TYPE);

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/auth/v1/signup")
                        .post(body)
                        .header("apikey", ANON_KEY)
                        .header("Content-Type", "application/json")
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                Log.d(TAG, "Signup respuesta: " + response.code() + " → " + responseBody);

                if (response.isSuccessful()) {
                    JsonObject json = gson.fromJson(responseBody, JsonObject.class);

                    // Si confirm email está desactivado → viene token directo
                    if (json.has("access_token")) {
                        String token = json.get("access_token").getAsString();
                        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                        prefs.edit().putString("access_token", token).apply();
                        callback.onSuccess(token);
                    } else {
                        // Caso común: necesita confirmación por email
                        callback.onSuccess(null); // null = "revisa tu correo"
                    }
                } else {
                    callback.onError(response.code() + " - " + responseBody);
                }
            } catch (Exception e) {
                callback.onError("Excepción: " + e.getMessage());
            }
        }).start();
    }

    // ────────────────────────────────────────────────
    // INSERTAR DATOS (reports, etc.)
    // ────────────────────────────────────────────────
    public void insertData(String tableName, JsonObject data, DataCallback callback) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", null);
                String authHeader = token != null ? "Bearer " + token : "Bearer " + ANON_KEY;

                RequestBody body = RequestBody.create(gson.toJson(data), JSON_MEDIA_TYPE);

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/" + tableName)
                        .post(body)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", authHeader)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal")
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                String bodyStr = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    callback.onSuccess(bodyStr);
                } else {
                    callback.onError(response.code() + " - " + bodyStr);
                }
            } catch (Exception e) {
                callback.onError("Excepción: " + e.getMessage());
            }
        }).start();
    }

    // Interfaces
    public interface AuthCallback {
        void onSuccess(String accessToken);   // null si necesita confirm email
        void onError(String errorMessage);
    }

    public interface DataCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }
}