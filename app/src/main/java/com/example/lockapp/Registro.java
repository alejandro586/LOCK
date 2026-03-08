package com.example.lockapp;

import android.content.Context;
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
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Registro extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etAge, etDni, etEmail, etPassword;
    private com.google.android.material.imageview.ShapeableImageView ivProfilePhoto;
    private MaterialButton btnRegister;
    private ImageButton btnBack, btnAgePlus, btnAgeMinus;
    private SupabaseAuthManager authManager;
    private Uri photoUri;
    private File photoFile;

    private final ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    photoUri = result.getData().getData();
                    Glide.with(this).load(photoUri).into(ivProfilePhoto);
                    photoFile = uriToFile(photoUri); // Usa helper corregido
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        authManager = new SupabaseAuthManager(this);

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
        btnRegister = findViewById(R.id.btn_register);

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

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || ageStr.isEmpty() || dni.isEmpty() || email.isEmpty() || password.isEmpty() || photoFile == null) {
            Toast.makeText(this, "Completa todos los campos y selecciona una foto", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);

        authManager.signUpWithEmail(email, password, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String userId, String token) {
                authManager.uploadProfilePhoto(userId, photoFile, new SupabaseAuthManager.DataCallback() {
                    @Override
                    public void onSuccess(String photoUrl) {
                        JsonObject data = new JsonObject();
                        data.addProperty("user_id", userId);
                        data.addProperty("first_name", firstName);
                        data.addProperty("last_name", lastName);
                        data.addProperty("age", age);
                        data.addProperty("dni", dni);
                        data.addProperty("profile_photo_url", photoUrl);

                        authManager.insertData("profiles", data, new SupabaseAuthManager.DataCallback() {
                            @Override
                            public void onSuccess(String response) {
                                runOnUiThread(() -> {
                                    Toast.makeText(Registro.this, "Registro exitoso!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Registro.this, Pantalla_Principal_con_Mapa.class));
                                    finish();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> Toast.makeText(Registro.this, "Error al insertar: " + error, Toast.LENGTH_LONG).show());
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(Registro.this, "Error al subir foto: " + error, Toast.LENGTH_LONG).show());
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(Registro.this, "Error al registrar: " + error, Toast.LENGTH_LONG).show());
            }
        });
    }

    // Helper para Uri a File (corregido para Android moderno)
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