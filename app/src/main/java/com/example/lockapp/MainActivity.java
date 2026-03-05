package com.example.lockapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

        // Instanciamos el manager de autenticación (pasamos context)
        final SupabaseAuthManager authManager = new SupabaseAuthManager(this);

        // Prueba automática de conexión al abrir la app (opcional)
        authManager.testConnection();

        // Botón "Ingresar" → login real con email/password
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clic en Ingresar");

                // Credenciales de prueba (cámbialas o usa EditText más adelante)
                String email = "test@example.com";      // ← usa un usuario real de tu Supabase
                String password = "Password123";        // ← contraseña real

                authManager.loginWithEmail(email, password, new SupabaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String accessToken) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Login exitoso! Token guardado", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Token guardado: " + accessToken);

                                // Saltamos al mapa después del login
                                startActivity(new Intent(MainActivity.this, Pantalla_Principal_con_Mapa.class));
                                finish(); // Cerramos esta pantalla
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Error en login: " + error, Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Error login: " + error);
                            }
                        });
                    }
                });
            }
        });

        // Botón "Registrarse" → por ahora solo mensaje (próximo paso: pantalla registro)
        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clic en Registrarse");
                Toast.makeText(MainActivity.this, "Registro pendiente de implementar", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón "Continuar como anónimo"
        binding.anonymousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clic en Anónimo");
                startActivity(new Intent(MainActivity.this, Pantalla_Principal_con_Mapa.class));
            }
        });
    }
}