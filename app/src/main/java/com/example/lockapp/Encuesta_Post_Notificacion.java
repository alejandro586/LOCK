package com.example.lockapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class Encuesta_Post_Notificacion extends AppCompatActivity {

    private MaterialCardView selectedOption = null;
    private int selectedOptionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuesta_post_notificacion);

        ImageButton btnBack = findViewById(R.id.btn_back);
        MaterialCardView cardRobo = findViewById(R.id.card_robo_confirmado);
        MaterialCardView cardZona = findViewById(R.id.card_zona_peligrosa);
        MaterialCardView cardFalso = findViewById(R.id.card_falso_positivo);
        MaterialButton btnConfirmar = findViewById(R.id.btn_confirm_survey);

        btnBack.setOnClickListener(v -> finish());

        cardRobo.setOnClickListener(v -> selectOption(cardRobo, R.id.card_robo_confirmado));
        cardZona.setOnClickListener(v -> selectOption(cardZona, R.id.card_zona_peligrosa));
        cardFalso.setOnClickListener(v -> selectOption(cardFalso, R.id.card_falso_positivo));

        btnConfirmar.setOnClickListener(v -> {
            if (selectedOptionId == -1) {
                Toast.makeText(this, "Por favor selecciona una opción", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            if (selectedOptionId == R.id.card_robo_confirmado) {
                resultIntent.putExtra("REPORT_TYPE", "ROBO_CONFIRMADO");
            } else if (selectedOptionId == R.id.card_zona_peligrosa) {
                resultIntent.putExtra("REPORT_TYPE", "ZONA_PELIGROSA");
            } else {
                resultIntent.putExtra("REPORT_TYPE", "FALSO_POSITIVO");
            }
            
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void selectOption(MaterialCardView card, int id) {
        // Deseleccionar el anterior
        if (selectedOption != null) {
            selectedOption.setStrokeWidth(0);
            selectedOption.setCardBackgroundColor(ContextCompat.getColor(this, R.color.surface_grey));
        }

        // Seleccionar el nuevo
        selectedOption = card;
        selectedOptionId = id;
        selectedOption.setStrokeColor(Color.parseColor("#00BFA6"));
        selectedOption.setStrokeWidth(4);
        selectedOption.setCardBackgroundColor(Color.parseColor("#454D5A"));
    }
}