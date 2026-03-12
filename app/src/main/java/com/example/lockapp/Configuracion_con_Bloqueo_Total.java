package com.example.lockapp;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class Configuracion_con_Bloqueo_Total extends AppCompatActivity {

    private static final int REQUEST_CODE_ENABLE_ADMIN = 101;
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 102;
    
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_con_bloqueo_total);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, AdminReceiver.class);
        prefs = getSharedPreferences("seguridad", MODE_PRIVATE);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        SwitchMaterial lockdownSwitch = findViewById(R.id.lockdown_switch);
        SwitchMaterial simSwitch = findViewById(R.id.sim_detection_switch);
        SwitchMaterial sirenSwitch = findViewById(R.id.siren_switch);

        lockdownSwitch.setChecked(prefs.getBoolean("lockdown_enabled", false));
        simSwitch.setChecked(prefs.getBoolean("sim_detection_enabled", false));
        sirenSwitch.setChecked(prefs.getBoolean("siren_enabled", false));

        lockdownSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkPermissionsAndStart();
            } else {
                stopLockService();
                prefs.edit().putBoolean("lockdown_enabled", false).apply();
            }
        });

        simSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sim_detection_enabled", isChecked).apply();
            Toast.makeText(this, "Detección de SIM: " + (isChecked ? "Activada" : "Desactivada"), Toast.LENGTH_SHORT).show();
        });

        sirenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("siren_enabled", isChecked).apply();
            Toast.makeText(this, "Sirena Anti-Robo: " + (isChecked ? "Activada" : "Desactivada"), Toast.LENGTH_SHORT).show();
        });

        MaterialButton premiumButton = findViewById(R.id.premium_button);
        premiumButton.setOnClickListener(v -> {
            Intent intent = new Intent(Configuracion_con_Bloqueo_Total.this, Seccion_Premium.class);
            startActivity(intent);
        });
    }

    private void checkPermissionsAndStart() {
        // 1. Verificar Permiso de Administrador
        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Necesario para proteger el menú de apagado y el dispositivo.");
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
            return;
        }

        // 2. Verificar Permiso de Superposición (Overlay) para bloquear el menú de apagado
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Conceda el permiso para bloquear el menú de apagado", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
            return;
        }

        startLockService();
        prefs.edit().putBoolean("lockdown_enabled", true).apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN || requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            // Re-verificar tras volver de ajustes
            checkPermissionsAndStart();
        }
    }

    private void startLockService() {
        Intent serviceIntent = new Intent(this, LockService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Toast.makeText(this, "Protección de Bloqueo Total Activada", Toast.LENGTH_SHORT).show();
    }

    private void stopLockService() {
        stopService(new Intent(this, LockService.class));
        Toast.makeText(this, "Protección Desactivada", Toast.LENGTH_SHORT).show();
    }
}