package com.example.lockapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class Seccion_Premium extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seccion_premium);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Botón para ver novedades/mejoras detalladas
        MaterialButton btnViewNovedades = findViewById(R.id.btn_view_novedades);
        btnViewNovedades.setOnClickListener(v -> {
            startActivity(new Intent(Seccion_Premium.this, PremiumFeaturesActivity.class));
        });

        MaterialButton btnActivate = findViewById(R.id.btn_activate_premium);
        btnActivate.setOnClickListener(v -> {
            btnActivate.animate().scaleX(1.08f).scaleY(1.08f).setDuration(180)
                    .withEndAction(() -> btnActivate.animate().scaleX(1f).scaleY(1f).setDuration(180).start())
                    .start();

            prefs.edit().putBoolean("premium_enabled", true).apply();

            Toast.makeText(this, "¡Premium activado! Disfruta sin límites", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Seccion_Premium.this, Pantalla_Principal_con_Mapa.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}