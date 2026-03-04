package com.example.lockapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;

public class Pantalla_Principal_con_Mapa extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_SURVEY = 1001;
    private GoogleMap mMap;
    private DrawerLayout drawerLayout;
    private final List<Circle> redCircles = new ArrayList<>();
    private final List<Circle> yellowCircles = new ArrayList<>();
    private final List<Circle> safeCircles = new ArrayList<>();
    private LatLng lastKnownLocation = new LatLng(-12.046374, -77.042793); // Lima por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal_con_mapa);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ImageButton btnMenu = findViewById(R.id.btn_menu);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupChips();
        setupReportButton();
        setupNavigation(navigationView, btnMenu);
    }

    private void setupNavigation(NavigationView navigationView, ImageButton btnMenu) {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.RIGHT));

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_lockdown) {
                Intent intent = new Intent(Pantalla_Principal_con_Mapa.this, Configuracion_con_Bloqueo_Total.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawer(Gravity.RIGHT);
            return true;
        });
    }

    private void setupChips() {
        Chip chipRed = findViewById(R.id.chip_red_zones);
        Chip chipYellow = findViewById(R.id.chip_yellow_zones);
        Chip chipSafe = findViewById(R.id.chip_safe_zones);

        chipRed.setOnClickListener(v -> toggleRedZones(chipRed.isChecked()));
        chipYellow.setOnClickListener(v -> toggleYellowZones(chipYellow.isChecked()));
        chipSafe.setOnClickListener(v -> toggleSafeZones(chipSafe.isChecked()));
    }

    private void setupReportButton() {
        ExtendedFloatingActionButton reportButton = findViewById(R.id.report_button);
        reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_Principal_con_Mapa.this, Encuesta_Post_Notificacion.class);
            startActivityForResult(intent, REQUEST_CODE_SURVEY);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SURVEY && resultCode == RESULT_OK && data != null) {
            String reportType = data.getStringExtra("REPORT_TYPE");
            if ("ROBO_CONFIRMADO".equals(reportType)) {
                addConfirmedRobberyZone();
                Toast.makeText(this, "Se agregó el robo exactamente donde estaba el individuo", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addConfirmedRobberyZone() {
        if (mMap == null) return;
        redCircles.add(addCircle(lastKnownLocation.latitude, lastKnownLocation.longitude, 
                Color.argb(120, 255, 0, 0), Color.RED));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(lastKnownLocation).title("Tu ubicación"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 13));
    }

    private void toggleRedZones(boolean show) {
        if (mMap == null) return;
        if (show) {
            redCircles.add(addCircle(-12.050, -77.040, Color.argb(70, 255, 77, 77), Color.RED));
            redCircles.add(addCircle(-12.042, -77.055, Color.argb(70, 255, 77, 77), Color.RED));
        } else {
            for (Circle c : redCircles) c.remove();
            redCircles.clear();
        }
    }

    private void toggleYellowZones(boolean show) {
        if (mMap == null) return;
        if (show) {
            yellowCircles.add(addCircle(-12.060, -77.030, Color.argb(70, 255, 211, 105), Color.YELLOW));
            yellowCircles.add(addCircle(-12.030, -77.065, Color.argb(70, 255, 211, 105), Color.YELLOW));
        } else {
            for (Circle c : yellowCircles) c.remove();
            yellowCircles.clear();
        }
    }

    private void toggleSafeZones(boolean show) {
        if (mMap == null) return;
        if (show) {
            safeCircles.add(addCircle(-12.040, -77.020, Color.argb(70, 0, 173, 181), Color.CYAN));
            safeCircles.add(addCircle(-12.070, -77.050, Color.argb(70, 0, 173, 181), Color.CYAN));
        } else {
            for (Circle c : safeCircles) c.remove();
            safeCircles.clear();
        }
    }

    private Circle addCircle(double lat, double lng, int fillColor, int strokeColor) {
        return mMap.addCircle(new CircleOptions()
                .center(new LatLng(lat, lng))
                .radius(500)
                .fillColor(fillColor)
                .strokeColor(strokeColor)
                .strokeWidth(2));
    }
}