package com.example.lockapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lockapp.data.SupabaseAuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Perfil extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etAge, etDni, etEmail, etPassword;
    private com.google.android.material.imageview.ShapeableImageView ivProfilePhoto;
    private MaterialButton btnEditProfile;
    private ImageButton btnBack, btnAgePlus, btnAgeMinus;
    private SupabaseAuthManager authManager;
    private String userId;
    private Uri photoUri;
    private File photoFile;
    private String currentPhotoUrl;

    private final ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    photoUri = result.getData().getData();
                    Glide.with(this).load(photoUri).into(ivProfilePhoto);
                    photoFile = uriToFile(photoUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        authManager = new SupabaseAuthManager(this);

        userId = getSharedPreferences("auth", MODE_PRIVATE).getString("user_id", null);
        if (userId == null) {
            Toast.makeText(this, "Error: No user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnBack = findViewById(R.id.btn_back);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etAge = findViewById(R.id.et_age);
        btnAgePlus = findViewById(R.id.btn_age_plus);
        btnAgeMinus = findViewById(R.id.btn_age_minus);
        etDni = findViewById(R.id.et_dni);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        ivProfilePhoto = findViewById(R.id.profile_image);
        btnEditProfile = findViewById(R.id.btn_edit_profile);

        btnBack.setOnClickListener(v -> finish());

        btnAgePlus.setOnClickListener(v -> {
            int currentAge = Integer.parseInt(etAge.getText().toString().isEmpty() ? "0" : etAge.getText().toString());
            etAge.setText(String.valueOf(currentAge + 1));
        });

        btnAgeMinus.setOnClickListener(v -> {
            int currentAge = Integer.parseInt(etAge.getText().toString().isEmpty() ? "0" : etAge.getText().toString());
            if (currentAge > 0) {
                etAge.setText(String.valueOf(currentAge - 1));
            }
        });

        ivProfilePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            photoPickerLauncher.launch(intent);
        });

        btnEditProfile.setOnClickListener(v -> updateProfile());

        loadProfile();
    }

    private void loadProfile() {
        authManager.fetchProfile(userId, new SupabaseAuthManager.DataCallback() {
            @Override
            public void onSuccess(String response) {
                JsonArray jsonArray = new Gson().fromJson(response, JsonArray.class);
                if (!jsonArray.isEmpty()) {
                    JsonObject profile = jsonArray.get(0).getAsJsonObject();
                    runOnUiThread(() -> {
                        etFirstName.setText(profile.get("first_name").getAsString());
                        etLastName.setText(profile.get("last_name").getAsString());
                        etAge.setText(profile.get("age").getAsString());
                        etDni.setText(profile.get("dni").getAsString());
                        etEmail.setText("..."); // Fetch separado si necesitas
                        etPassword.setText("********");
                        currentPhotoUrl = profile.get("profile_photo_url").getAsString();
                        Glide.with(Perfil.this).load(currentPhotoUrl).into(ivProfilePhoto);
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(Perfil.this, "Error al cargar: " + error, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void updateProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String dni = etDni.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || ageStr.isEmpty() || dni.isEmpty()) {
            Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);

        if (photoFile != null) {
            authManager.uploadProfilePhoto(userId, photoFile, new SupabaseAuthManager.DataCallback() {
                @Override
                public void onSuccess(String newPhotoUrl) {
                    updateData(newPhotoUrl, firstName, lastName, age, dni);
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(Perfil.this, "Error al subir foto: " + error, Toast.LENGTH_LONG).show());
                }
            });
        } else {
            updateData(currentPhotoUrl, firstName, lastName, age, dni);
        }
    }

    private void updateData(String photoUrl, String firstName, String lastName, int age, String dni) {
        JsonObject data = new JsonObject();
        data.addProperty("first_name", firstName);
        data.addProperty("last_name", lastName);
        data.addProperty("age", age);
        data.addProperty("dni", dni);
        data.addProperty("profile_photo_url", photoUrl);

        authManager.updateProfile(userId, data, new SupabaseAuthManager.DataCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> Toast.makeText(Perfil.this, "Perfil actualizado!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(Perfil.this, "Error al actualizar: " + error, Toast.LENGTH_LONG).show());
            }
        });
    }

    // Helper para Uri a File (corregido)
    private File uriToFile(Uri uri) {
        if (uri == null) return null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "profile_photo.jpg");
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            return file;
        } catch (Exception e) {
            Toast.makeText(this, "Error al procesar foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}