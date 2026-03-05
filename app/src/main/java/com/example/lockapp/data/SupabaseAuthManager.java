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
 * 100% Java puro. Incluye login y ahora también insertData.
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

    // Prueba conexión básica
    public void testConnection() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/")
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + ANON_KEY)
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    Log.d(TAG, "Conexión OK! Código: " + response.code());
                } else {
                    Log.e(TAG, "Error conexión: Código " + response.code() + " - " + response.message());
                }
            } catch (IOException e) {
                Log.e(TAG, "Excepción en testConnection: " + e.getMessage(), e);
            }
        }).start();
    }

    // Login con email y contraseña
    public void loginWithEmail(String email, String password, AuthCallback callback) {
        new Thread(() -> {
            try {
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("email", email);
                jsonBody.addProperty("password", password);

                String jsonString = gson.toJson(jsonBody);
                Log.d(TAG, "Enviando body JSON para login: " + jsonString);

                RequestBody body = RequestBody.create(jsonString, JSON_MEDIA_TYPE);

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/auth/v1/signinwithpassword")
                        .post(body)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + ANON_KEY)
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                Log.d(TAG, "Respuesta login: Código " + response.code() + " - " + responseBody);

                if (response.isSuccessful()) {
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    String accessToken = jsonResponse.get("access_token").getAsString();

                    SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit().putString("access_token", accessToken).apply();

                    Log.d(TAG, "Login exitoso - Token guardado: " + accessToken);
                    callback.onSuccess(accessToken);
                } else {
                    Log.e(TAG, "Login fallido: Código " + response.code() + " - " + responseBody);
                    callback.onError(response.code() + ": " + responseBody);
                }
            } catch (IOException e) {
                Log.e(TAG, "Excepción en login: " + e.getMessage(), e);
                callback.onError("Error de red: " + e.getMessage());
            }
        }).start();
    }

    // Insertar datos en cualquier tabla (ej: "reports")
    public void insertData(String tableName, JsonObject data, DataCallback callback) {
        new Thread(() -> {
            try {
                RequestBody body = RequestBody.create(gson.toJson(data), JSON_MEDIA_TYPE);

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/" + tableName)
                        .post(body)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + ANON_KEY)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal") // No devuelve el registro creado
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    Log.d(TAG, "Insert OK en tabla " + tableName);
                    callback.onSuccess(responseBody);
                } else {
                    Log.e(TAG, "Insert fallido en " + tableName + ": " + response.code() + " - " + responseBody);
                    callback.onError(response.code() + ": " + responseBody);
                }
            } catch (IOException e) {
                Log.e(TAG, "Excepción en insertData: " + e.getMessage(), e);
                callback.onError("Error de red: " + e.getMessage());
            }
        }).start();
    }

    // Interfaz para login
    public interface AuthCallback {
        void onSuccess(String accessToken);
        void onError(String errorMessage);
    }

    // Interfaz para operaciones de datos (INSERT, UPDATE, etc.)
    public interface DataCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }
}