package com.example.lockapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lockapp.data.SupabaseAuthManager;
import com.example.lockapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupabaseAuthManager authManager = new SupabaseAuthManager(this);

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

        // REGISTRO: Redirige a la nueva vista vacía para el equipo
        binding.registerButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Registro.class));
        });

        binding.anonymousButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, Pantalla_Principal_con_Mapa.class)));
    }
}