package com.example.lockapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class Configuracion_con_Bloqueo_Total extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_con_bloqueo_total);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        SwitchMaterial lockdownSwitch = findViewById(R.id.lockdown_switch);
        SwitchMaterial simSwitch = findViewById(R.id.sim_detection_switch);
        SwitchMaterial sirenSwitch = findViewById(R.id.siren_switch);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        lockdownSwitch.setChecked(prefs.getBoolean("lockdown_enabled", false));
        simSwitch.setChecked(prefs.getBoolean("sim_detection_enabled", false));
        sirenSwitch.setChecked(prefs.getBoolean("siren_enabled", false));

        lockdownSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("lockdown_enabled", isChecked).apply();
            Toast.makeText(this, "Bloqueo Total: " + (isChecked ? "Activado" : "Desactivado"), Toast.LENGTH_SHORT).show();
        });

        simSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sim_detection_enabled", isChecked).apply();
            Toast.makeText(this, "Detección de SIM: " + (isChecked ? "Activado" : "Desactivado"), Toast.LENGTH_SHORT).show();
        });

        sirenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("siren_enabled", isChecked).apply();
            Toast.makeText(this, "Sirena Anti-Robo: " + (isChecked ? "Activado" : "Desactivado"), Toast.LENGTH_SHORT).show();
        });

        MaterialButton premiumButton = findViewById(R.id.premium_button);
        premiumButton.setOnClickListener(v -> {
            Intent intent = new Intent(Configuracion_con_Bloqueo_Total.this, Seccion_Premium.class);
            startActivity(intent);
        });
    }
}