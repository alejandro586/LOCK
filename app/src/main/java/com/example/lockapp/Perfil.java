package com.example.lockapp;

import android.os.Bundle;
import android.widget.ImageButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;

public class Perfil extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        TextInputEditText etAge = findViewById(R.id.et_age);
        ImageButton btnPlus = findViewById(R.id.btn_age_plus);
        ImageButton btnMinus = findViewById(R.id.btn_age_minus);

        btnPlus.setOnClickListener(v -> {
            String currentAgeStr = etAge.getText().toString();
            try {
                int currentAge = currentAgeStr.isEmpty() ? 0 : Integer.parseInt(currentAgeStr);
                etAge.setText(String.valueOf(currentAge + 1));
            } catch (NumberFormatException e) {
                etAge.setText("1");
            }
        });

        btnMinus.setOnClickListener(v -> {
            String currentAgeStr = etAge.getText().toString();
            try {
                int currentAge = currentAgeStr.isEmpty() ? 0 : Integer.parseInt(currentAgeStr);
                if (currentAge > 0) {
                    etAge.setText(String.valueOf(currentAge - 1));
                }
            } catch (NumberFormatException e) {
                etAge.setText("0");
            }
        });
    }
}