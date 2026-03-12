package com.example.lockapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lockapp.databinding.ActivityPinBinding;

public class PinActivity extends AppCompatActivity {

    private ActivityPinBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Bloquear el menú de apagado (Power Menu) y barra de estado
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        binding = ActivityPinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnUnlock.setOnClickListener(v -> {
            String pinIngresado = binding.pinEditText.getText().toString();
            
            SharedPreferences prefs = getSharedPreferences("seguridad", MODE_PRIVATE);
            String pinGuardado = prefs.getString("pin", "1234"); // "1234" por defecto para pruebas

            if (pinIngresado.equals(pinGuardado)) {
                Toast.makeText(this, "Acceso permitido", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la pantalla de bloqueo
            } else {
                Toast.makeText(this, "PIN incorrecto", Toast.LENGTH_SHORT).show();
                binding.pinEditText.setText("");
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Deshabilitar el botón atrás para que no puedan salir
        // super.onBackPressed(); 
    }
}