package com.example.lockapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.lockapp.data.SupabaseAuthManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
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

    private GoogleMap mMap;
    private DrawerLayout drawerLayout;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences prefs;
    private SupabaseAuthManager authManager;

    private final List<Circle> redCircles   = new ArrayList<>();
    private final List<Circle> yellowCircles = new ArrayList<>();
    private final List<Circle> safeCircles  = new ArrayList<>();

    private LatLng lastKnownLocation = new LatLng(-12.046374, -77.042793); // Lima por defecto

    private static final int REQUEST_CODE_SURVEY = 1001;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado.\nAlgunas funciones estarán limitadas.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal_con_mapa);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        authManager = new SupabaseAuthManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        checkAndRequestLocationPermission();
    }

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            updateMyLocationOnMap();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "No se pudo obtener ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateMyLocationOnMap() {
        if (mMap == null) return;
        mMap.addMarker(new MarkerOptions().position(lastKnownLocation).title("Mi ubicación actual"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15f));
    }

    private void setupChips() {
        Chip chipRed   = findViewById(R.id.chip_red_zones);
        Chip chipYellow = findViewById(R.id.chip_yellow_zones);
        Chip chipSafe   = findViewById(R.id.chip_safe_zones);

        chipRed.setOnCheckedChangeListener((buttonView, isChecked) -> toggleZones(redCircles, isChecked,
                new LatLng(-12.050, -77.040), Color.argb(80, 255, 99, 71), Color.RED));

        chipYellow.setOnCheckedChangeListener((buttonView, isChecked) -> toggleZones(yellowCircles, isChecked,
                new LatLng(-12.060, -77.030), Color.argb(80, 255, 193, 7), Color.YELLOW));

        chipSafe.setOnCheckedChangeListener((buttonView, isChecked) -> toggleZones(safeCircles, isChecked,
                new LatLng(-12.040, -77.020), Color.argb(80, 0, 191, 165), Color.CYAN));
    }

    // Método genérico para evitar duplicados y repetir código
    private void toggleZones(List<Circle> circlesList, boolean show, LatLng center, int fillColor, int strokeColor) {
        // Siempre limpiar primero → evita duplicados al togglear varias veces
        for (Circle c : circlesList) c.remove();
        circlesList.clear();

        if (show && mMap != null) {
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(600)
                    .fillColor(fillColor)
                    .strokeColor(strokeColor)
                    .strokeWidth(3f));
            circlesList.add(circle);

            // Opcional: centrar en la zona al activar
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 14f));
        }
    }

    private void setupReportButton() {
        ExtendedFloatingActionButton reportButton = findViewById(R.id.report_button);
        reportButton.setOnClickListener(v ->
                startActivityForResult(new Intent(this, Encuesta_Post_Notificacion.class), REQUEST_CODE_SURVEY));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SURVEY && resultCode == RESULT_OK && data != null) {
            String reportType = data.getStringExtra("REPORT_TYPE");
            if ("ROBO_CONFIRMADO".equals(reportType)) {
                addConfirmedRobberyZone();
            }
            sendReportToSupabase(reportType);
        }
    }

    private void addConfirmedRobberyZone() {
        if (mMap == null) return;
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(lastKnownLocation)
                .radius(600)
                .fillColor(Color.argb(140, 255, 0, 0))
                .strokeColor(Color.RED)
                .strokeWidth(3f));
        redCircles.add(circle); // ← se añade a la lista roja para que se limpie al toggle off
        Toast.makeText(this, "Zona de robo marcada en el mapa", Toast.LENGTH_SHORT).show();
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
                        "Reporte enviado correctamente", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(Pantalla_Principal_con_Mapa.this,
                        "Error al enviar reporte: " + error, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void setupShareLocationButton() {
        FloatingActionButton shareButton = findViewById(R.id.share_location_button);
        shareButton.setOnClickListener(v -> {
            boolean isPremium = prefs.getBoolean("premium_enabled", false);
            int shareCount = prefs.getInt("daily_share_count", 0);
            long lastShareDay = prefs.getLong("last_share_day", 0);

            Calendar cal = Calendar.getInstance();
            long currentDay = cal.getTimeInMillis() / 86400000;

            if (currentDay > lastShareDay) {
                prefs.edit().putInt("daily_share_count", 0).putLong("last_share_day", currentDay).apply();
                shareCount = 0;
            }

            if (!isPremium && shareCount >= 3) {
                Toast.makeText(this, "Límite diario alcanzado (3/3). ¡Suscríbete para ilimitado!", Toast.LENGTH_LONG).show();
                return;
            }

            String uri = "geo:" + lastKnownLocation.latitude + "," + lastKnownLocation.longitude +
                    "?q=" + lastKnownLocation.latitude + "," + lastKnownLocation.longitude;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));

            if (!isPremium) {
                prefs.edit().putInt("daily_share_count", ++shareCount).apply();
            }
        });
    }

    private void setupNavigation(NavigationView navigationView, ImageButton btnMenu) {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.RIGHT));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_lockdown) {  // asumo que tienes este id en drawer_menu.xml
                startActivity(new Intent(this, Configuracion_con_Bloqueo_Total.class));
            }
            // Agrega más items según tu menú
            drawerLayout.closeDrawer(Gravity.RIGHT);
            return true;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateMyLocationOnMap();

        // Opcional: habilitar botón "Mi ubicación" nativo de Google Maps
        // if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        //     mMap.setMyLocationEnabled(true);
        // }
    }
}