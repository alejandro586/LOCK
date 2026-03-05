package com.example.lockapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lockapp.data.SupabaseAuthManager;
import com.example.lockapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupabaseAuthManager authManager = new SupabaseAuthManager(this);
        authManager.testConnection();

        // ─── LOGIN ───────────────────────────────────────
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.email.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.loginWithEmail(email, password, new SupabaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String token) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, Pantalla_Principal_con_Mapa.class));
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Error al ingresar: " + error, Toast.LENGTH_LONG).show());
                }
            });
        });

        // ─── REGISTRO (ahora funcional) ──────────────────
        binding.registerButton.setOnClickListener(v -> {
            String email = binding.email.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa correo y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Contraseña mínima 6 caracteres", Toast.LENGTH_LONG).show();
                return;
            }

            authManager.signUpWithEmail(email, password, new SupabaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String token) {
                    runOnUiThread(() -> {
                        if (token != null) {
                            // Signup auto-login (confirm email desactivado)
                            Toast.makeText(MainActivity.this, "¡Cuenta creada! Bienvenido", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, Pantalla_Principal_con_Mapa.class));
                            finish();
                        } else {
                            // Confirm email activado
                            Toast.makeText(MainActivity.this,
                                    "Cuenta creada. Revisa tu correo para confirmar", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        String msg = "Error al registrarte: " + error;
                        if (error.contains("duplicate key") || error.contains("already registered")) {
                            msg = "Este correo ya está registrado. Inicia sesión.";
                        } else if (error.contains("weak password")) {
                            msg = "Contraseña muy débil. Usa letras, números y mínimo 6 caracteres.";
                        }
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        // ─── ANÓNIMO ─────────────────────────────────────
        binding.anonymousButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, Pantalla_Principal_con_Mapa.class)));
    }
}