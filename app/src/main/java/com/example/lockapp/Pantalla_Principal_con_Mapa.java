package com.example.lockapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

public class Pantalla_Principal_con_Mapa extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_principal_con_mapa);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup mapa (inspirado en Reach's heatmaps)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // OnClick para report button (abre encuesta o reporte)
        findViewById(R.id.report_button).setOnClickListener(v -> {
            // StartActivity(Encuesta_Post_Notificacion.class) o lógica de reporte
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Ejemplo heatmap (agrega datos reales de reportes validados, colores inspirados en Lima Segura: rojo high-risk)
        List<LatLng> list = new ArrayList<>();
        list.add(new LatLng(-12.0464, -77.0428)); // Ejemplo Lima centro
        // Añade más puntos basados en reportes

        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .data(list)
                .gradient(HeatmapTileProvider.DEFAULT_GRADIENT) // Personaliza: rojo-turquesa
                .build();
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
    }
}