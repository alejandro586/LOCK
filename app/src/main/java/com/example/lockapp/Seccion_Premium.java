package com.example.lockapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class Seccion_Premium extends AppCompatActivity {

    private SharedPreferences prefs;
    private MaterialCardView cardAnnual, cardMonthly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seccion_premium);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        SwitchMaterial switchTrial = findViewById(R.id.switch_trial);
        TextView tvTrialLabel = findViewById(R.id.tv_trial_label);
        MaterialButton btnActivate = findViewById(R.id.btn_activate_premium);
        MaterialButton btnViewNovedades = findViewById(R.id.btn_view_novedades);

        cardAnnual = findViewById(R.id.card_annual);
        cardMonthly = findViewById(R.id.card_monthly);

        // Lógica de selección de planes
        cardAnnual.setOnClickListener(v -> selectPlan(true));
        cardMonthly.setOnClickListener(v -> selectPlan(false));

        switchTrial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvTrialLabel.setText("Prueba gratuita activada");
                btnActivate.setText("Empezar prueba gratuita de 7 días");
            } else {
                tvTrialLabel.setText("¿No te decides? Haz la prueba gratuita.");
                btnActivate.setText("Continuar");
            }
        });

        btnActivate.setOnClickListener(v -> {
            prefs.edit().putBoolean("premium_enabled", true).apply();
            
            String message = switchTrial.isChecked() ? "¡Prueba de 7 días activada!" : "¡Modo premium activado con éxito!";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Seccion_Premium.this, Pantalla_Principal_con_Mapa.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnViewNovedades.setOnClickListener(v -> {
            Intent intent = new Intent(Seccion_Premium.this, PremiumFeaturesActivity.class);
            startActivity(intent);
        });
    }

    private void selectPlan(boolean isAnnual) {
        if (isAnnual) {
            cardAnnual.setStrokeColor(ContextCompat.getColor(this, R.color.primary_turquoise));
            cardAnnual.setStrokeWidth(dpToPx(2));
            cardMonthly.setStrokeColor(ContextCompat.getColor(this, R.color.surface_grey));
            cardMonthly.setStrokeWidth(dpToPx(1));
        } else {
            cardMonthly.setStrokeColor(ContextCompat.getColor(this, R.color.primary_turquoise));
            cardMonthly.setStrokeWidth(dpToPx(2));
            cardAnnual.setStrokeColor(ContextCompat.getColor(this, R.color.surface_grey));
            cardAnnual.setStrokeWidth(dpToPx(1));
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}