package com.example.lockapp;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Configuracion_con_Bloqueo_Total extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracion_con_bloqueo_total);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Switch lockdownSwitch = findViewById(R.id.lockdown_switch);
        lockdownSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Lógica para activar lockdown (usa DevicePolicyManager)
            if (isChecked) {
                buttonView.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
                // Activar servicio de detección (GPS/sensores)
            }
        });

        findViewById(R.id.premium_button).setOnClickListener(v -> {
            // StartActivity(Seccion_Premium.class)
        });
    }
}