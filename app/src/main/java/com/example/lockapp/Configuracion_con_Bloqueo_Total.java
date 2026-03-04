package com.example.lockapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class Configuracion_con_Bloqueo_Total extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_con_bloqueo_total);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            finish(); 
        });

        MaterialButton premiumButton = findViewById(R.id.premium_button);
        premiumButton.setOnClickListener(v -> {
            Intent intent = new Intent(Configuracion_con_Bloqueo_Total.this, Seccion_Premium.class);
            startActivity(intent);
        });
    }
}