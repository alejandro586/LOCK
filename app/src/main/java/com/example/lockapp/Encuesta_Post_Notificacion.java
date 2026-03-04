package com.example.lockapp;

import android.os.Bundle;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

public class Encuesta_Post_Notificacion extends AppCompatActivity {

    private Button submitButton;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_encuesta_post_notificacion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        submitButton = findViewById(R.id.submit_survey);
        radioGroup = findViewById(R.id.radio_group);

        // Listener para enable button y animar card seleccionada (inspirado en Cránealo's validación)
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            submitButton.setEnabled(true);
            MaterialCardView selectedCard = findViewById(group.getCheckedRadioButtonId()).getParent() instanceof MaterialCardView ? (MaterialCardView) findViewById(group.getCheckedRadioButtonId()).getParent() : null;
            if (selectedCard != null) {
                selectedCard.setStrokeWidth(3);  // Bold stroke on checked
                scaleAnimation(selectedCard);
            }
            // Reset other cards stroke
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof MaterialCardView && child.getId() != checkedId) {
                    ((MaterialCardView) child).setStrokeWidth(0);
                }
            }
        });

        submitButton.setOnClickListener(v -> {
            // Enviar encuesta, validar datos (robo/zona/falso) y actualizar mapa
        });
    }

    private void scaleAnimation(View view) {
        ScaleAnimation scale = new ScaleAnimation(1f, 1.05f, 1f, 1.05f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(300);
        scale.setRepeatCount(1);
        scale.setRepeatMode(Animation.REVERSE);
        view.startAnimation(scale);
    }
}