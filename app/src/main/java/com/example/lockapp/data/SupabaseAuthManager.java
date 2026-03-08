package com.example.lockapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseAuthManager {

    private static final String TAG = "SupabaseAuth";

    private static final String SUPABASE_URL = "https://fyvlikksdzkcwppxtmaj.supabase.co";
    private static final String ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZ5dmxpa2tzZHprY3dwcHh0bWFqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI2NjE4NzgsImV4cCI6MjA4ODIzNzg3OH0.0WYiMuxdNLHU3sS21Rfp6Mf6FnSSCR7iQCx4qUuqBN8";

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final MediaType IMAGE_MEDIA_TYPE = MediaType.get("image/jpeg");

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Context context;

    public SupabaseAuthManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // LOGIN (Modificado para extraer userId como en signup)
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
                    String userId = json.getAsJsonObject("user").get("id").getAsString(); // Extrae userId

                    SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit().putString("access_token", token).putString("user_id", userId).apply();

                    callback.onSuccess(userId, token);
                } else {
                    callback.onError(response.code() + " - " + responseBody);
                }
            } catch (Exception e) {
                callback.onError("Excepción: " + e.getMessage());
            }
        }).start();
    }

    // SIGNUP (Ya correcto, pero aseguro prefs para user_id)
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
                    String userId = json.getAsJsonObject("user").get("id").getAsString();
                    String token = json.has("access_token") ? json.get("access_token").getAsString() : null;

                    SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit().putString("user_id", userId);
                    if (token != null) {
                        prefs.edit().putString("access_token", token).apply();
                    }

                    callback.onSuccess(userId, token);
                } else {
                    callback.onError(response.code() + " - " + responseBody);
                }
            } catch (Exception e) {
                callback.onError("Excepción: " + e.getMessage());
            }
        }).start();
    }

    // INSERTAR DATOS
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

    // UPLOAD FOTO
    public void uploadProfilePhoto(String userId, File photoFile, DataCallback callback) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", ANON_KEY);
                String authHeader = "Bearer " + token;

                RequestBody body = RequestBody.create(photoFile, IMAGE_MEDIA_TYPE);

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/storage/v1/object/profiles/" + userId + ".jpg")
                        .post(body)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", authHeader)
                        .header("Content-Type", "image/jpeg")
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                String bodyStr = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    JsonObject json = gson.fromJson(bodyStr, JsonObject.class);
                    String photoUrl = json.get("Key").getAsString(); // Ajustado para URL pública
                    callback.onSuccess(photoUrl);
                } else {
                    callback.onError(response.code() + " - " + bodyStr);
                }
            } catch (Exception e) {
                callback.onError("Excepción: " + e.getMessage());
            }
        }).start();
    }

    // FETCH PERFIL
    public void fetchProfile(String userId, DataCallback callback) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", ANON_KEY);
                String authHeader = "Bearer " + token;

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/profiles?select=*&user_id=eq." + userId)
                        .get()
                        .header("apikey", ANON_KEY)
                        .header("Authorization", authHeader)
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

    // UPDATE PERFIL
    public void updateProfile(String userId, JsonObject data, DataCallback callback) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", ANON_KEY);
                String authHeader = "Bearer " + token;

                RequestBody body = RequestBody.create(gson.toJson(data), JSON_MEDIA_TYPE);

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/profiles?user_id=eq." + userId)
                        .patch(body)
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
        void onSuccess(String userId, String accessToken);
        void onError(String errorMessage);
    }

    public interface DataCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }
}