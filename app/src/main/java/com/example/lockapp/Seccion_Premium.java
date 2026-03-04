package com.example.lockapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class Seccion_Premium extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seccion_premium);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        MaterialButton btnActivate = findViewById(R.id.btn_activate_premium);
        btnActivate.setOnClickListener(v -> {
            // Mostrar notificación
            Toast.makeText(this, "modo premium activado con exito", Toast.LENGTH_LONG).show();

            // Regresar a la pantalla principal con el mapa
            Intent intent = new Intent(Seccion_Premium.this, Pantalla_Principal_con_Mapa.class);
            // Flags para limpiar el stack de actividades y no crear múltiples instancias del mapa
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            
            finish();
        });
    }
}