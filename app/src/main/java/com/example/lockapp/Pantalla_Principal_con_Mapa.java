package com.example.lockapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.lockapp.data.SupabaseAuthManager;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Pantalla_Principal_con_Mapa extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_SURVEY = 1001;
    private GoogleMap mMap;
    private DrawerLayout drawerLayout;
    private SupabaseAuthManager authManager;
    private final List<Circle> redCircles = new ArrayList<>();
    private final List<Circle> yellowCircles = new ArrayList<>();
    private final List<Circle> safeCircles = new ArrayList<>();
    private LatLng lastKnownLocation = new LatLng(-12.046374, -77.042793); // Lima por defecto
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal_con_mapa);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        authManager = new SupabaseAuthManager(this);

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
        setupShareLocationButton();
        setupNavigation(navigationView, btnMenu);
    }

    private void setupReportButton() {
        ExtendedFloatingActionButton reportButton = findViewById(R.id.report_button);
        reportButton.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, Encuesta_Post_Notificacion.class), REQUEST_CODE_SURVEY);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SURVEY && resultCode == RESULT_OK && data != null) {
            String reportType = data.getStringExtra("REPORT_TYPE");
            
            // 1. Mostrar visualmente en el mapa local
            if ("ROBO_CONFIRMADO".equals(reportType)) {
                addConfirmedRobberyZone();
            }

            // 2. ENVIAR A SUPABASE
            sendReportToSupabase(reportType);
        }
    }

    private void sendReportToSupabase(String type) {
        JsonObject reportData = new JsonObject();
        reportData.addProperty("report_type", type);
        reportData.addProperty("latitude", lastKnownLocation.latitude);
        reportData.addProperty("longitude", lastKnownLocation.longitude);
        reportData.addProperty("created_at", java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));

        authManager.insertData("reports", reportData, new SupabaseAuthManager.DataCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> Toast.makeText(Pantalla_Principal_con_Mapa.this, 
                    "Reporte guardado en la nube correctamente", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(Pantalla_Principal_con_Mapa.this, 
                    "Error al sincronizar reporte: " + error, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void addConfirmedRobberyZone() {
        if (mMap == null) return;
        redCircles.add(addCircle(lastKnownLocation.latitude, lastKnownLocation.longitude,
                Color.argb(140, 255, 0, 0), Color.RED));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
        Toast.makeText(this, "Zona de robo añadida localmente", Toast.LENGTH_SHORT).show();
    }

    // ... (resto de métodos setupChips, setupShareLocationButton, setupNavigation, etc. se mantienen igual)
    private void setupShareLocationButton() {
        FloatingActionButton shareButton = findViewById(R.id.share_location_button);
        shareButton.setOnClickListener(v -> shareCurrentLocation());
    }

    private void shareCurrentLocation() {
        boolean isPremium = prefs.getBoolean("premium_enabled", false);
        int shareCount = prefs.getInt("daily_share_count", 0);
        long lastShareDay = prefs.getLong("last_share_day", 0);
        Calendar cal = Calendar.getInstance();
        long currentDay = cal.getTimeInMillis() / (86400000);
        if (currentDay > lastShareDay) {
            prefs.edit().putInt("daily_share_count", 0).putLong("last_share_day", currentDay).apply();
            shareCount = 0;
        }
        if (!isPremium && shareCount >= 3) {
            Toast.makeText(this, "Límite diario alcanzado (3/3)", Toast.LENGTH_LONG).show();
            return;
        }
        String uri = "geo:" + lastKnownLocation.latitude + "," + lastKnownLocation.longitude + "?q=" + lastKnownLocation.latitude + "," + lastKnownLocation.longitude;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        if (!isPremium) prefs.edit().putInt("daily_share_count", ++shareCount).apply();
    }

    private void setupNavigation(NavigationView navigationView, ImageButton btnMenu) {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.RIGHT));
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_lockdown) {
                startActivity(new Intent(this, Configuracion_con_Bloqueo_Total.class));
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(lastKnownLocation).title("Tu ubicación actual"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 14));
    }

    private void toggleRedZones(boolean show) {
        if (mMap == null) return;
        if (show) {
            redCircles.add(addCircle(-12.050, -77.040, Color.argb(80, 255, 99, 71), Color.RED));
        } else {
            for (Circle c : redCircles) c.remove();
            redCircles.clear();
        }
    }

    private void toggleYellowZones(boolean show) {
        if (mMap == null) return;
        if (show) {
            yellowCircles.add(addCircle(-12.060, -77.030, Color.argb(80, 255, 193, 7), Color.YELLOW));
        } else {
            for (Circle c : yellowCircles) c.remove();
            yellowCircles.clear();
        }
    }

    private void toggleSafeZones(boolean show) {
        if (mMap == null) return;
        if (show) {
            safeCircles.add(addCircle(-12.040, -77.020, Color.argb(80, 0, 191, 165), Color.CYAN));
        } else {
            for (Circle c : safeCircles) c.remove();
            safeCircles.clear();
        }
    }

    private Circle addCircle(double lat, double lng, int fillColor, int strokeColor) {
        return mMap.addCircle(new CircleOptions().center(new LatLng(lat, lng)).radius(600).fillColor(fillColor).strokeColor(strokeColor).strokeWidth(3f));
    }
}