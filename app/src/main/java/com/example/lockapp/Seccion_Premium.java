package com.example.lockapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Seccion_Premium extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seccion_premium);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup RecyclerView para benefits (inspirado en Citizen's premium list)
        RecyclerView benefitsRecycler = findViewById(R.id.benefits_recycler);
        benefitsRecycler.setLayoutManager(new LinearLayoutManager(this));

        List<String> benefits = new ArrayList<>();
        benefits.add("Alertas prioritarias con PNP");
        benefits.add("Lockdown avanzado con IA");
        benefits.add("Rutas seguras sin ads");
        // Adapter simple (crea un custom Adapter con cards/icons)
        // benefitsRecycler.setAdapter(new BenefitsAdapter(benefits));  // Implementa tu adapter

        findViewById(R.id.subscribe).setOnClickListener(v -> {
            // Lógica de suscripción con BillingClient
        });
    }
}