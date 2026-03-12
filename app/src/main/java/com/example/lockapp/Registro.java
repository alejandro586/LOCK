package com.example.lockapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lockapp.data.SupabaseAuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Registro extends AppCompatActivity {

    private static final String TAG = "Registro";

    private TextInputEditText etFirstName, etLastName, etAge, etDni, etEmail, etPassword;
    private ShapeableImageView ivProfilePhoto;
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
                    photoFile = uriToFile(photoUri);
                    Log.d(TAG, "Foto seleccionada: " + (photoFile != null ? photoFile.getPath() : "null"));
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
        btnRegister = findViewById(R.id.btn_register);  // Asegúrate que el ID en XML sea btn_register

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
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String nombre = etFirstName.getText().toString().trim();
        String telefono = etDni.getText().toString().trim();  // Usamos telefono para DNI o número

        if (email.isEmpty() || password.isEmpty() || nombre.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        authManager.signUpWithEmail(email, password, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String userId, String accessToken) {
                runOnUiThread(() -> Toast.makeText(Registro.this, "Usuario creado. Guardando datos...", Toast.LENGTH_SHORT).show());

                if (photoFile != null) {
                    authManager.uploadProfilePhoto(userId, photoFile, new SupabaseAuthManager.DataCallback() {
                        @Override
                        public void onSuccess(String photoUrl) {
                            saveProfileData(userId, photoUrl, nombre, telefono);
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> Toast.makeText(Registro.this, "Foto no subida: " + error, Toast.LENGTH_LONG).show());
                            saveProfileData(userId, null, nombre, telefono);  // Guarda sin foto
                        }
                    });
                } else {
                    saveProfileData(userId, null, nombre, telefono);
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(Registro.this, "Error al registrar: " + error, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void saveProfileData(String userId, String photoUrl, String nombre, String telefono) {
        JsonObject data = new JsonObject();
        data.addProperty("user_id", userId);
        data.addProperty("nombre", nombre);
        data.addProperty("telefono", telefono);
        if (photoUrl != null) {
            data.addProperty("foto_url", photoUrl);
        }

        authManager.insertData("profiles", data, new SupabaseAuthManager.DataCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    Toast.makeText(Registro.this, "¡Registro completado con éxito!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Registro.this, Pantalla_Principal_con_Mapa.class));
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(Registro.this, "Error al guardar perfil: " + error, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Registro.this, Pantalla_Principal_con_Mapa.class));
                    finish();
                });
            }
        });
    }

    private File uriToFile(Uri uri) {
        if (uri == null) return null;
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "profile_" + System.currentTimeMillis() + ".jpg");
            OutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            output.close();
            input.close();
            return file;
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir URI a File", e);
            return null;
        }
    }
}