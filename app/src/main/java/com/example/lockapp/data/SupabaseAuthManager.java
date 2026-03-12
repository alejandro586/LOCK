package com.example.lockapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
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

    // DATOS DEL PROYECTO NUEVO (de prueba)
    private static final String SUPABASE_URL = "https://axnnrbmyztiudesberfo.supabase.co";
    private static final String ANON_KEY  = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF4bm5yYm15enRpdWRlc2JlcmZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzMyODg4OTMsImV4cCI6MjA4ODg2NDg5M30.msD7B-7IqVT5zyCaFaixOfPXPwRfZzh7ZY7r9w9HdHM";

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final MediaType IMAGE_MEDIA_TYPE = MediaType.get("image/jpeg");

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Context context;

    // Bucket del proyecto nuevo (cámbialo si usaste otro nombre)
    private static final String BUCKET_NAME = "fotos_perfil_test";

    public SupabaseAuthManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // LOGIN
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

                Log.d(TAG, "Login respuesta: Código=" + response.code() + " → " + responseBody);

                if (response.isSuccessful()) {
                    JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                    String token = json.get("access_token").getAsString();
                    String userId = json.getAsJsonObject("user").get("id").getAsString();

                    SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit().putString("access_token", token).putString("user_id", userId).apply();

                    callback.onSuccess(userId, token);
                } else {
                    callback.onError("Login falló: " + response.code() + " - " + responseBody);
                }
            } catch (Exception e) {
                callback.onError("Excepción en login: " + e.getMessage());
            }
        }).start();
    }

    // SIGNUP
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

                Log.d(TAG, "Signup respuesta: Código=" + response.code() + " → " + responseBody);

                if (response.isSuccessful()) {
                    JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                    String userId = json.getAsJsonObject("user").get("id").getAsString();
                    String token = json.has("access_token") ? json.get("access_token").getAsString() : null;

                    SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit().putString("user_id", userId).apply();
                    if (token != null) {
                        prefs.edit().putString("access_token", token).apply();
                    }

                    callback.onSuccess(userId, token);
                } else {
                    callback.onError("Signup falló: " + response.code() + " - " + responseBody);
                }
            } catch (Exception e) {
                callback.onError("Excepción en signup: " + e.getMessage());
            }
        }).start();
    }

    // SUBIR FOTO DE PERFIL - ADAPTADO PARA PROYECTO NUEVO
    public void uploadProfilePhoto(String userId, File photoFile, DataCallback callback) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", null);
                if (token == null) {
                    callback.onError("No hay token de autenticación");
                    Log.e(TAG, "No hay token para subir foto");
                    return;
                }

                if (photoFile == null || !photoFile.exists()) {
                    callback.onError("Archivo de foto no encontrado o inválido");
                    Log.e(TAG, "Archivo no existe: " + photoFile.getAbsolutePath());
                    return;
                }

                Log.d(TAG, "Intentando subir foto al proyecto nuevo → userId: " + userId + " | Bucket: " + BUCKET_NAME);

                RequestBody body = RequestBody.create(photoFile, IMAGE_MEDIA_TYPE);

                // Path simple para test (sin carpetas para evitar cualquier problema)
                String filePath = userId + "_perfil.jpg";
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filePath;

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .put(body)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "image/jpeg")
                        .header("x-upsert", "true")
                        .header("x-client-info", "android-okhttp-test")
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                String bodyStr = response.body() != null ? response.body().string() : "";

                Log.d(TAG, "Upload respuesta (proyecto nuevo): Código=" + response.code() + " → " + bodyStr);

                if (response.isSuccessful()) {
                    String photoUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + filePath;
                    Log.d(TAG, "Foto subida OK en proyecto nuevo. URL pública: " + photoUrl);
                    callback.onSuccess(photoUrl);
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        callback.onError("Token inválido o expirado. Inicia sesión nuevamente.");
                    } else {
                        callback.onError("Error al subir foto en proyecto nuevo: " + response.code() + " - " + bodyStr);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Excepción grave al subir foto", e);
                callback.onError("Error inesperado al subir foto: " + e.getMessage());
            }
        }).start();
    }

    // FETCH PROFILE
    public void fetchProfile(String userId, DataCallback callback) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", null);
                if (token == null) {
                    callback.onError("No hay token");
                    return;
                }
                String authHeader = "Bearer " + token;

                String url = SUPABASE_URL + "/rest/v1/profiles?select=nombre,telefono,foto_url&user_id=eq." + userId;

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .header("apikey", ANON_KEY)
                        .header("Authorization", authHeader)
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                String bodyStr = response.body() != null ? response.body().string() : "";

                Log.d(TAG, "Fetch profile respuesta: Código=" + response.code() + " → " + bodyStr);

                if (response.isSuccessful()) {
                    callback.onSuccess(bodyStr);
                } else {
                    callback.onError("Error al cargar perfil: " + response.code() + " - " + bodyStr);
                }
            } catch (Exception e) {
                callback.onError("Excepción en fetchProfile: " + e.getMessage());
            }
        }).start();
    }

    // UPDATE PROFILE
    public void updateProfile(String userId, JsonObject data, DataCallback callback) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", null);
                if (token == null) {
                    callback.onError("No hay token");
                    return;
                }
                String authHeader = "Bearer " + token;

                RequestBody body = RequestBody.create(gson.toJson(data), JSON_MEDIA_TYPE);

                String url = SUPABASE_URL + "/rest/v1/profiles?user_id=eq." + userId;

                Request request = new Request.Builder()
                        .url(url)
                        .patch(body)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", authHeader)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal")
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                String bodyStr = response.body() != null ? response.body().string() : "";

                Log.d(TAG, "Update profile respuesta: Código=" + response.code() + " → " + bodyStr);

                if (response.isSuccessful()) {
                    callback.onSuccess("Perfil actualizado correctamente");
                } else {
                    callback.onError("Error al actualizar perfil: " + response.code() + " - " + bodyStr);
                }
            } catch (Exception e) {
                callback.onError("Excepción en updateProfile: " + e.getMessage());
            }
        }).start();
    }

    // INSERT DATA
    public void insertData(String tableName, JsonObject data, DataCallback callback) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", null);
                if (token == null) {
                    callback.onError("No hay token");
                    return;
                }
                String authHeader = "Bearer " + token;

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

                Log.d(TAG, "Insert en " + tableName + " respuesta: Código=" + response.code() + " → " + bodyStr);

                if (response.isSuccessful()) {
                    callback.onSuccess("Datos insertados en " + tableName);
                } else {
                    callback.onError("Error al insertar en " + tableName + ": " + response.code() + " - " + bodyStr);
                }
            } catch (Exception e) {
                callback.onError("Excepción en insertData: " + e.getMessage());
            }
        }).start();
    }

    // CLEAN PROFILE AND DELETE PHOTO
    public void cleanUserProfileAndDeletePhoto(String userId, DataCallback callback) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", null);
                if (token == null) {
                    callback.onError("No hay token");
                    return;
                }
                String authHeader = "Bearer " + token;

                // Borrar foto (si existe)
                String filePath = userId + "_perfil.jpg";
                String deletePhotoUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filePath;

                Request deletePhotoRequest = new Request.Builder()
                        .url(deletePhotoUrl)
                        .delete()
                        .header("apikey", ANON_KEY)
                        .header("Authorization", authHeader)
                        .build();

                Response photoResponse = okHttpClient.newCall(deletePhotoRequest).execute();
                String photoBody = photoResponse.body() != null ? photoResponse.body().string() : "";
                Log.d(TAG, "Delete foto respuesta: Código=" + photoResponse.code() + " → " + photoBody);

                // Limpiar campos en profiles
                JsonObject cleanData = new JsonObject();
                cleanData.addProperty("nombre", (String) null);
                cleanData.addProperty("telefono", (String) null);
                cleanData.addProperty("foto_url", (String) null);

                RequestBody cleanBody = RequestBody.create(gson.toJson(cleanData), JSON_MEDIA_TYPE);

                String updateUrl = SUPABASE_URL + "/rest/v1/profiles?user_id=eq." + userId;

                Request cleanRequest = new Request.Builder()
                        .url(updateUrl)
                        .patch(cleanBody)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", authHeader)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal")
                        .build();

                Response cleanResponse = okHttpClient.newCall(cleanRequest).execute();
                String cleanBodyStr = cleanResponse.body() != null ? cleanResponse.body().string() : "";
                Log.d(TAG, "Limpieza rasgos respuesta: Código=" + cleanResponse.code() + " → " + cleanBodyStr);

                if ((photoResponse.isSuccessful() || photoResponse.code() == 404) && cleanResponse.isSuccessful()) {
                    callback.onSuccess("Foto eliminada (o no existía) y perfil limpiado");
                } else {
                    callback.onError("Error al limpiar: Foto=" + photoResponse.code() + ", Rasgos=" + cleanResponse.code());
                }
            } catch (Exception e) {
                callback.onError("Excepción en cleanUserProfileAndDeletePhoto: " + e.getMessage());
            }
        }).start();
    }

    // INTERFACES
    public interface AuthCallback {
        void onSuccess(String userId, String accessToken);
        void onError(String errorMessage);
    }

    public interface DataCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }
}